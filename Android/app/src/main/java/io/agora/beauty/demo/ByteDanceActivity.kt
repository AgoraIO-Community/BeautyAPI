package io.agora.beauty.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.bytedance.labcv.core.effect.EffectManager
import io.agora.base.VideoFrame
import io.agora.beauty.bytedance.BeautyPreset
import io.agora.beauty.bytedance.CaptureMode
import io.agora.beauty.bytedance.Config
import io.agora.beauty.bytedance.ErrorCode
import io.agora.beauty.bytedance.EventCallback
import io.agora.beauty.bytedance.createByteDanceBeautyAPI
import io.agora.beauty.bytedance.helpers.AssetsResourcesHelper
import io.agora.beauty.demo.databinding.BeautyActivityBinding
import io.agora.beauty.demo.utils.ReflectUtils
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.ColorEnhanceOptions
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE

class ByteDanceActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName

    companion object {
        private const val EXTRA_CHANNEL_NAME = "ChannelName"
        private const val EXTRA_RESOLUTION = "Resolution"
        private const val EXTRA_FRAME_RATE = "FrameRate"
        private const val EXTRA_CAPTURE_MODE = "CaptureMode"
        private const val EXTRA_PROCESS_MODE = "ProcessMode"

        fun launch(
            context: Context,
            channelName: String,
            resolution: String,
            frameRate: String,
            captureMode: String,
            processMode: String
        ) {
            Intent(context, ByteDanceActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_RESOLUTION, resolution)
                putExtra(EXTRA_FRAME_RATE, frameRate)
                putExtra(EXTRA_CAPTURE_MODE, captureMode)
                putExtra(EXTRA_PROCESS_MODE, processMode)
                context.startActivity(this)
            }
        }
    }

    private val mBinding by lazy {
        BeautyActivityBinding.inflate(LayoutInflater.from(this))
    }
    private val mChannelName by lazy {
        intent.getStringExtra(EXTRA_CHANNEL_NAME)
    }
    private val mRtcHandler = object : IRtcEngineEventHandler() {
        override fun onError(err: Int) {
            super.onError(err)
            Log.e(TAG, "Rtc error code=$err, msg=${RtcEngine.getErrorDescription(err)}")
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                if (mBinding.remoteVideoView.tag == null) {
                    mBinding.remoteVideoView.tag = uid
                    val renderView = SurfaceView(this@ByteDanceActivity)
                    mBinding.remoteVideoView.addView(renderView)
                    mRtcEngine.setupRemoteVideo(
                        VideoCanvas(
                            renderView, Constants.RENDER_MODE_HIDDEN, uid
                        )
                    )
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                if (mBinding.remoteVideoView.tag == uid) {
                    mBinding.remoteVideoView.tag = null
                    mBinding.remoteVideoView.removeAllViews()
                    mRtcEngine.setupRemoteVideo(
                        VideoCanvas(null, Constants.RENDER_MODE_HIDDEN, uid)
                    )
                }
            }
        }
    }
    private val mRtcEngine by lazy {
        RtcEngine.create(RtcEngineConfig().apply {
            mContext = applicationContext
            mAppId = BuildConfig.AGORA_APP_ID
            mEventHandler = object : IRtcEngineEventHandler() {}
        }).apply {
            enableExtension("agora_video_filters_clear_vision", "clear_vision", true)
        }
    }
    private val mEffectManager by lazy {
        val resourceHelper =
            AssetsResourcesHelper(this, "beauty_bytedance")
        EffectManager(this, resourceHelper, resourceHelper.licensePath)
    }
    private val mByteDanceApi by lazy {
        createByteDanceBeautyAPI()
    }
    private val mVideoEncoderConfiguration by lazy {
        VideoEncoderConfiguration(
            ReflectUtils.getStaticFiledValue(
                VideoEncoderConfiguration::class.java, intent.getStringExtra(EXTRA_RESOLUTION)
            ), ReflectUtils.getStaticFiledValue(
                FRAME_RATE::class.java, intent.getStringExtra(EXTRA_FRAME_RATE)
            ), 0, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
    }
    private val beautyEnableDefault = true
    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnableDefault)
            setOnBeautyChangeListener { enable ->
                mByteDanceApi.enable(enable)
            }
            setOnColorEnhanceChangeListener {enable ->
                val options = ColorEnhanceOptions()
                options.strengthLevel = 0.5f
                options.skinProtectLevel = 0.5f
                mRtcEngine.setColorEnhanceOptions(enable, options)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        val isCustomCaptureMode =
            intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)

        mByteDanceApi.initialize(
            Config(
                mRtcEngine,
                mEffectManager,
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                statsEnable = true,
                eventCallback = EventCallback(
                    onBeautyStats = {stats ->
                        Log.d(TAG, "BeautyStats stats = $stats")
                    },
                    onEffectInitialized = {
                        Log.d(TAG, "onEffectInitialized")
                    },
                    onEffectDestroyed = {
                        Log.d(TAG, "onEffectInitialized")
                    }
                )
            )
        )

        when (intent.getStringExtra(EXTRA_PROCESS_MODE)) {
            getString(R.string.beauty_process_auto) -> mByteDanceApi.setParameters(
                "beauty_mode",
                "0"
            )

            getString(R.string.beauty_process_texture) -> mByteDanceApi.setParameters(
                "beauty_mode",
                "1"
            )

            getString(R.string.beauty_process_i420) -> mByteDanceApi.setParameters(
                "beauty_mode",
                "2"
            )
        }

        if (isCustomCaptureMode) {
            mRtcEngine.registerVideoFrameObserver(object : IVideoFrameObserver {
                private var shouldMirror = true

                override fun onCaptureVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ) : Boolean {
                    shouldMirror = false
                    return when(mByteDanceApi.onFrame(videoFrame!!)){
                        ErrorCode.ERROR_OK.value -> true
                        ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> {
                            shouldMirror = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
                            true
                        }
                    }
                }

                override fun onPreEncodeVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ) = true

                override fun onMediaPlayerVideoFrame(
                    videoFrame: VideoFrame?,
                    mediaPlayerId: Int
                ) = true

                override fun onRenderVideoFrame(
                    channelId: String?,
                    uid: Int,
                    videoFrame: VideoFrame?
                ) = true

                override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

                override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

                override fun getRotationApplied() = false

                override fun getMirrorApplied() = shouldMirror

                override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER
            })
        }
        if (beautyEnableDefault) {
            mByteDanceApi.enable(true)
        }

        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.enableVideo()


        // render local video
        mByteDanceApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)


        // join channel
        mRtcEngine.joinChannel(null, mChannelName, 0, ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            publishCameraTrack = true
            publishMicrophoneTrack = true
            autoSubscribeAudio = true
            autoSubscribeVideo = true
        })


        // init view
        mBinding.ivCamera.setOnClickListener {
            mRtcEngine.switchCamera()
        }
        mBinding.ivSetting.setOnClickListener {
            mSettingDialog.show()
        }
        mBinding.ctvFaceBeauty.setOnClickListener {
            val enable = !mBinding.ctvFaceBeauty.isChecked
            mBinding.ctvFaceBeauty.isChecked = enable
            mByteDanceApi.setBeautyPreset(if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM)
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable
            mEffectManager.appendComposeNodes(arrayOf("style_makeup/tianmei"))
            mEffectManager.updateComposerNodeIntensity(
                "style_makeup/tianmei",
                "Filter_ALL",
                if (enable) 0.5f else 0f
            )
            mEffectManager.updateComposerNodeIntensity(
                "style_makeup/tianmei",
                "Makeup_ALL",
                if (enable) 0.5f else 0f
            )
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            if(enable){
                mEffectManager.setSticker("wochaotian")
            }else{
                mEffectManager.setSticker(null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mByteDanceApi.release()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }


}