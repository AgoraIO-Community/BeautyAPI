package io.agora.beautyapi.demo

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bytedance.labcv.effectsdk.RenderManager
import io.agora.base.VideoFrame
import io.agora.beautyapi.bytedance.BeautyPreset
import io.agora.beautyapi.bytedance.CameraConfig
import io.agora.beautyapi.bytedance.CaptureMode
import io.agora.beautyapi.bytedance.Config
import io.agora.beautyapi.bytedance.ErrorCode
import io.agora.beautyapi.bytedance.EventCallback
import io.agora.beautyapi.bytedance.MirrorMode
import io.agora.beautyapi.bytedance.createByteDanceBeautyAPI
import io.agora.beautyapi.demo.databinding.BeautyActivityBinding
import io.agora.beautyapi.demo.utils.FileUtils
import io.agora.beautyapi.demo.utils.ReflectUtils
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
import java.util.concurrent.Executors

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
            setOnI420ChangeListener {enable ->
                if(enable){
                    mByteDanceApi.setParameters(
                        "beauty_mode",
                        "2"
                    )
                }else{
                    mByteDanceApi.setParameters(
                        "beauty_mode",
                        "0"
                    )
                }
            }
        }
    }
    private var cameraConfig = CameraConfig()
    private val renderManager = ByteDanceBeautySDK.renderManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val isCustomCaptureMode =
            intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)

        mByteDanceApi.initialize(
            Config(
                applicationContext,
                mRtcEngine,
                renderManager,
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                statsEnable = true,
                cameraConfig = cameraConfig,
                eventCallback = EventCallback(
                    onBeautyStats = {stats ->
                        Log.d(TAG, "BeautyStats stats = $stats")
                    },
                    onEffectInitialized = {
                        ByteDanceBeautySDK.initEffect(applicationContext)
                        Log.d(TAG, "onEffectInitialized")
                    },
                    onEffectDestroyed = {
                        ByteDanceBeautySDK.unInitEffect()
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

                override fun onCaptureVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ) = when (mByteDanceApi.onFrame(videoFrame!!)) {
                    ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                    else -> true
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

                override fun getMirrorApplied() = mByteDanceApi.getMirrorApplied()

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
            mByteDanceApi.setBeautyPreset(
                if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM,
                ByteDanceBeautySDK.beautyNodePath,
                ByteDanceBeautySDK.beauty4ItemsNodePath,
                ByteDanceBeautySDK.reSharpNodePath
            )
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable

            val tianmeiNode = ByteDanceBeautySDK.makeupTianmeiNodePath
            renderManager.appendComposerNodes(arrayOf(tianmeiNode))
            renderManager.loadResourceWithTimeout(-1)
            renderManager.updateComposerNodes(
                tianmeiNode,
                "Filter_ALL",
                if (enable) 0.5f else 0f
            )
            renderManager.updateComposerNodes(
                tianmeiNode,
                "Makeup_ALL",
                if (enable) 0.5f else 0f
            )
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            if(enable){
                renderManager.setSticker("${ByteDanceBeautySDK.stickerPath}/wochaotian")
            }else{
                renderManager.setSticker(null)
            }
        }
        mBinding.ivMirror.setOnClickListener {
            val isFront = mByteDanceApi.isFrontCamera()
            if(isFront){
                cameraConfig = CameraConfig(
                    frontMirror = when (cameraConfig.frontMirror) {
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                    },
                    backMirror = cameraConfig.backMirror
                )
                Toast.makeText(this, "frontMirror=${cameraConfig.frontMirror}", Toast.LENGTH_SHORT).show()
            } else {
                cameraConfig =CameraConfig(
                    frontMirror = cameraConfig.frontMirror,
                    backMirror = when (cameraConfig.backMirror) {
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                    }
                )
                Toast.makeText(this, "backMirror=${cameraConfig.backMirror}", Toast.LENGTH_SHORT).show()
            }
            mByteDanceApi.updateCameraConfig(cameraConfig)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        mByteDanceApi.release()
        RtcEngine.destroy()
    }


}

object ByteDanceBeautySDK {
    private val TAG = "ByteDanceBeautySDK"

    private val LICENSE_NAME = "agora_test_20220805_20230815_io.agora.entfull_4.2.3.licbag"
    private val workerThread = Executors.newSingleThreadExecutor()

    val renderManager = RenderManager()
    var licensePath = ""
    var modelsPath = ""
    var beautyNodePath = ""
    var beauty4ItemsNodePath = ""
    var reSharpNodePath = ""
    var makeupTianmeiNodePath = ""
    var stickerPath = ""



    fun initBeautySDK(context: Context){
        val storagePath = context.getExternalFilesDir("")?.absolutePath ?: return
        val assetsPath = "beauty_bytedance"

        workerThread.execute {
            // copy license
            licensePath = "$storagePath/beauty_bytedance/LicenseBag.bundle/$LICENSE_NAME"
            FileUtils.copyAssets(context, "$assetsPath/LicenseBag.bundle/$LICENSE_NAME", licensePath)

            // copy models
            modelsPath = "$storagePath/beauty_bytedance/ModelResource.bundle"
            FileUtils.copyAssets(context, "$assetsPath/ModelResource.bundle", modelsPath)

            // copy beauty node
            beautyNodePath = "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/beauty_Android_lite"
            FileUtils.copyAssets(context, "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/beauty_Android_lite", beautyNodePath)

            // copy beauty 4items node
            beauty4ItemsNodePath = "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/beauty_4Items"
            FileUtils.copyAssets(context, "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/beauty_4Items", beauty4ItemsNodePath)

            // copy resharp node
            reSharpNodePath = "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/reshape_lite"
            FileUtils.copyAssets(context, "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/reshape_lite", reSharpNodePath)

            // copy makeup node
            makeupTianmeiNodePath = "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/style_makeup/tianmei"
            FileUtils.copyAssets(context, "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/style_makeup/tianmei", makeupTianmeiNodePath)

            // copy stickers
            stickerPath = "$storagePath/beauty_bytedance/StickerResource.bundle/stickers"
            FileUtils.copyAssets(context, "$assetsPath/StickerResource.bundle/stickers", stickerPath)
        }
    }

    // GL Thread
    fun initEffect(context: Context){
        val ret = renderManager.init(
            context,
            modelsPath, licensePath, false, false, 0
        )
        if(!checkResult("RenderManager init ", ret)){
            return
        }
        renderManager.useBuiltinSensor(true)
        renderManager.set3Buffer(false)
        renderManager.appendComposerNodes(arrayOf(beautyNodePath, beauty4ItemsNodePath, reSharpNodePath))
        renderManager.loadResourceWithTimeout(-1)
    }

    // GL Thread
    fun unInitEffect(){
        renderManager.release()
    }

    private fun checkResult(msg: String, ret: Int): Boolean {
        if (ret != 0 && ret != -11 && ret != 1) {
            val log = "$msg error: $ret"
            Log.e(TAG, log)
            return false
        }
        return true
    }
}