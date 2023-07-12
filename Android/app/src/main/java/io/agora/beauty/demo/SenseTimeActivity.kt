package io.agora.beauty.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.sensetime.stmobile.model.STMobileMakeupType
import io.agora.base.VideoFrame
import io.agora.beauty.demo.databinding.BeautyActivityBinding
import io.agora.beauty.demo.utils.ReflectUtils
import io.agora.beauty.sensetime.beautyapi.BeautyPreset
import io.agora.beauty.sensetime.beautyapi.BeautyStats
import io.agora.beauty.sensetime.beautyapi.CaptureMode
import io.agora.beauty.sensetime.beautyapi.Config
import io.agora.beauty.sensetime.beautyapi.ErrorCode
import io.agora.beauty.sensetime.beautyapi.IEventCallback
import io.agora.beauty.sensetime.beautyapi.createSenseTimeBeautyAPI
import io.agora.beauty.sensetime.beautyapi.utils.STRenderKit
import io.agora.beauty.sensetime.beautyapi.utils.utils.FileUtils
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
import java.io.File

class SenseTimeActivity : ComponentActivity() {
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
            Intent(context, SenseTimeActivity::class.java).apply {
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
                    val renderView = SurfaceView(this@SenseTimeActivity)
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
    private val mSTRenderKit by lazy {
        STRenderKit(
            this,
            "beauty_sensetime"
        )
    }
    private val mSenseTimeApi by lazy {
        createSenseTimeBeautyAPI()
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
                mSenseTimeApi.enable(enable)
            }
            setOnColorEnhanceChangeListener {enable ->
                val options = ColorEnhanceOptions()
                options.strengthLevel = 0.5f
                options.skinProtectLevel = 0.5f
                mRtcEngine.setColorEnhanceOptions(enable, options)
            }
            setOnI420ChangeListener { enable ->
                if(enable){
                    mSenseTimeApi.setParameters(
                        "beauty_mode",
                        "2"
                    )
                }else{
                    mSenseTimeApi.setParameters(
                        "beauty_mode",
                        "0"
                    )
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true

        val isCustomCaptureMode =
            intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)

        mSenseTimeApi.initialize(
            Config(
                mRtcEngine,
                mSTRenderKit,
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                statsEnable = true,
                eventCallback = object: IEventCallback{
                    override fun onBeautyStats(stats: BeautyStats) {
                        Log.d(TAG, "BeautyStats stats = $stats")
                    }
                }
            )
        )

        when (intent.getStringExtra(EXTRA_PROCESS_MODE)) {
            getString(R.string.beauty_process_auto) -> mSenseTimeApi.setParameters(
                "beauty_mode",
                "0"
            )

            getString(R.string.beauty_process_texture) -> mSenseTimeApi.setParameters(
                "beauty_mode",
                "1"
            )

            getString(R.string.beauty_process_i420) -> mSenseTimeApi.setParameters(
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
                    when(mSenseTimeApi.onFrame(videoFrame!!)){
                        ErrorCode.ERROR_OK.value -> {
                            shouldMirror = false
                            return true
                        }
                        ErrorCode.ERROR_FRAME_SKIPPED.value -> {
                            shouldMirror = false
                            return false
                        }
                        else -> {
                            val mirror = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
                            if(shouldMirror != mirror){
                                shouldMirror = mirror
                                return false
                            }
                            return true
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
            mSenseTimeApi.enable(true)
        }

        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.enableVideo()


        // render local video
        mSenseTimeApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)


        // join channel
        mRtcEngine.joinChannel(null, mChannelName, 0, ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            publishCameraTrack = true
            publishMicrophoneTrack = false
            autoSubscribeAudio = false
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
            mSenseTimeApi.setBeautyPreset(if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM)
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable
            if (enable) {
                setMakeUpItem(
                    STMobileMakeupType.ST_MAKEUP_TYPE_LIP,
                    "makeup_lip" + File.separator + "12自然.zip",
                    1f
                )
            } else {
                setMakeUpItem(STMobileMakeupType.ST_MAKEUP_TYPE_LIP, null, 0f)
            }
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            setStickerItem("sticker_face_shape" + File.separator + "ShangBanLe.zip", enable)
        }
    }

    override fun onResume() {
        super.onResume()
        mSTRenderKit.enableSensor(true)
    }

    override fun onPause() {
        super.onPause()
        mSTRenderKit.enableSensor(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        mSenseTimeApi.release()
        mSTRenderKit.release()
        RtcEngine.destroy()
    }

    private fun setMakeUpItem(type: Int, typePath: String?, strength: Float) {
        if (typePath != null) {
            val split = typePath.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val className = split[0]
            val fileName = split[1]
            val _path = FileUtils.getFilePath(this, mSTRenderKit.getResourcePath(className) + File.separator + fileName)
            FileUtils.copyFileIfNeed(this, fileName, mSTRenderKit.getResourcePath(className))
            mSTRenderKit.setMakeupForType(type, _path)
            mSTRenderKit.setMakeupStrength(type, strength)
        } else {
            mSTRenderKit.removeMakeupByType(type)
        }
    }

    private fun setStickerItem(path: String, attach: Boolean) {
        val split =
            path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val className = split[0]
        val fileName = split[1]
        val _path = FileUtils.getFilePath(this, mSTRenderKit.getResourcePath(className) + File.separator + fileName)
        FileUtils.copyFileIfNeed(this, fileName, mSTRenderKit.getResourcePath(className))
        if (!attach) {
            mSTRenderKit.removeSticker(_path)
        } else {
            mSTRenderKit.changeSticker(_path)
        }
    }

    private fun setFilterItem(filterPath: String, strength: Float) {
        val split =
            filterPath.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val className = split[0]
        val fileName = split[1]
        val filterName = split[1].split("_".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[2].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0]
        val path = FileUtils.getFilePath(this, mSTRenderKit.getResourcePath(className) + File.separator + fileName)
        FileUtils.copyFileIfNeed(this, fileName, mSTRenderKit.getResourcePath(className))
        mSTRenderKit.setFilterStyle(className, filterName, path)
        mSTRenderKit.setFilterStrength(strength)
    }


}