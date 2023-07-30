package io.agora.beautyapi.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.softsugar.stmobile.STCommonNative
import com.softsugar.stmobile.STMobileAuthentificationNative
import com.softsugar.stmobile.STMobileEffectNative
import com.softsugar.stmobile.STMobileEffectParams
import com.softsugar.stmobile.STMobileHumanActionNative
import com.softsugar.stmobile.params.STEffectBeautyType
import com.softsugar.stmobile.params.STHumanActionParamsType
import io.agora.base.VideoFrame
import io.agora.beautyapi.demo.databinding.BeautyActivityBinding
import io.agora.beautyapi.demo.utils.ReflectUtils
import io.agora.beautyapi.sensetime.BeautyPreset
import io.agora.beautyapi.sensetime.BeautyStats
import io.agora.beautyapi.sensetime.CaptureMode
import io.agora.beautyapi.sensetime.Config
import io.agora.beautyapi.sensetime.ErrorCode
import io.agora.beautyapi.sensetime.IEventCallback
import io.agora.beautyapi.sensetime.STHandlers
import io.agora.beautyapi.sensetime.createSenseTimeBeautyAPI
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
import java.util.concurrent.Executors


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
    private val workerExecutor = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true

        val isCustomCaptureMode =
            intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)

        workerExecutor.execute {
            initBeautySDK()
            mSenseTimeApi.initialize(
                Config(
                    mRtcEngine,
                    STHandlers(mobileEffectNative, humanActionNative),
                    captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                    statsEnable = true,
                    logPath = getExternalFilesDir(null)?.absolutePath ?: "",
                    eventCallback = object: IEventCallback{
                        override fun onBeautyStats(stats: BeautyStats) {
                            Log.d(TAG, "BeautyStats stats = $stats")
                        }
                    }
                )
            )
            if (beautyEnableDefault) {
                mSenseTimeApi.enable(true)
            }
            runOnUiThread {
                // render local video
                mSenseTimeApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
            }
        }


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


        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.enableVideo()

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
                    STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP,
                    "makeup_lip" + File.separator + "12自然.zip",
                    1f
                )
            } else {
                setMakeUpItem(STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP)
            }
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            setStickerItem("sticker_face_shape" + File.separator + "ShangBanLe.zip", enable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        workerExecutor.shutdown()
        mRtcEngine.leaveChannel()
        mSenseTimeApi.release()
        unInitBeautySDK()
        RtcEngine.destroy()
    }

    // SenseTime api
    private val resourcePath = "beauty_sensetime"
    private val mobileEffectNative = STMobileEffectNative()
    private val humanActionNative = STMobileHumanActionNative()
    private val humanActionCreateConfig = 0
    private val packageMap = mutableMapOf<String, Int>()

    val MODEL_106 = "models/M_SenseME_Face_Video_Template_p_3.9.0.3.model" // 106
    val MODEL_FACE_EXTRA = "models/M_SenseME_Face_Extra_Advanced_Template_p_2.0.0.model" // 282
    val MODEL_AVATAR_HELP = "models/M_SenseME_Avatar_Help_p_2.3.7.model" // avatar人脸驱动
    val MODEL_LIPS_PARSING = "models/M_SenseME_MouthOcclusion_p_1.3.0.1.model" // 嘴唇分割
    val MODEL_HAND = "models/M_SenseME_Hand_p_6.0.8.1.model" // 手势
    val MODEL_SEGMENT = "models/M_SenseME_Segment_Figure_p_4.14.1.1.model" // 前后背景分割
    val MODEL_SEGMENT_HAIR = "models/M_SenseME_Segment_Hair_p_4.4.0.model" // 头发分割
    val MODEL_FACE_OCCLUSION = "models/M_SenseME_FaceOcclusion_p_1.0.7.1.model" // 妆容遮挡
    val MODEL_SEGMENT_SKY = "models/M_SenseME_Segment_Sky_p_1.1.0.1.model" // 天空分割
    val MODEL_SEGMENT_SKIN = "models/M_SenseME_Segment_Skin_p_1.0.1.1.model" // 皮肤分割
    val MODEL_3DMESH = "models/M_SenseME_3DMesh_Face2396pt_280kpts_Ear_p_1.1.0v2.model" // 3DMesh
    val MODEL_HEAD_P_EAR = "models/M_SenseME_Ear_p_1.0.1.1.model" // 搭配 mesh 耳朵模型
    val MODEL_360HEAD_INSTANCE = "models/M_SenseME_3Dmesh_360Head2396pt_p_1.0.0.1.model" // 360度人头mesh
    val MODEL_FOOT = "models/M_SenseME_Foot_p_2.10.7.model" // 鞋子检测模型
    val MODEL_PANT = "models/M_SenseME_Segment_Trousers_p_1.1.10.model" // 裤腿的检测
    val MODEL_WRIST = "models/M_SenseME_Wrist_p_1.4.0.model" // 试表
    val MODEL_CLOTH = "models/M_SenseME_Segment_Clothes_p_1.0.2.2.model" // 衣服分割
    val MODEL_HEAD_INSTANCE = "models/M_SenseME_Segment_Head_Instance_p_1.1.0.1.model" // 实例分割版本
    val MODEL_HEAD_P_INSTANCE = "models/M_SenseME_Head_p_1.3.0.1.model" // 360度人头-头部模型
    val MODEL_NAIL = "models/M_SenseME_Nail_p_2.4.0.model" // 指甲检测

    private fun initBeautySDK(){
        checkBeautyLicense()
        initMobileEffect()
        initHumanAction()
    }

    private fun unInitBeautySDK(){
        mobileEffectNative.destroyInstance()
        humanActionNative.destroyInstance()
        packageMap.clear()
    }

    private fun checkBeautyLicense() {
        val license = io.agora.beautyapi.demo.utils.FileUtils.getAssetsString(
            this,
            "$resourcePath/license/SenseME.lic"
        )
        val activeCode = STMobileAuthentificationNative.generateActiveCodeFromBuffer(
            application,
            license,
            license.length
        )
        val success = activeCode.isNotEmpty()
        if (success) {
            Log.d(TAG, "SenseTime >> checkLicense successfully!")
        } else {
            Log.e(TAG, "SenseTime >> checkLicense failed!")
        }
    }

    private fun initMobileEffect(){
        val result =
            mobileEffectNative.createInstance(application, STMobileEffectNative.EFFECT_CONFIG_NONE)
        mobileEffectNative.setParam(STMobileEffectParams.EFFECT_PARAM_QUATERNION_SMOOTH_FRAME, 5f)
        Log.d(TAG, "SenseTime >> STMobileEffectNative create result : $result")
    }

    private fun initHumanAction(){
        val result = humanActionNative.createInstanceFromAssetFile(
            "$resourcePath/$MODEL_106",
            humanActionCreateConfig,
            assets
        )
        Log.d(TAG, "SenseTime >> STMobileHumanActionNative create result : $result")

        if(result != 0){
            return
        }

        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_HAND", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FACE_EXTRA", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT_HAIR", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_LIPS_PARSING", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FACE_OCCLUSION", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT_SKY", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_AVATAR_HELP", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FOOT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_PANT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_3DMESH", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_WRIST", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_CLOTH", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_HEAD_INSTANCE", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_360HEAD_INSTANCE", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_NAIL", assets)

        // 背景分割羽化程度[0,1](默认值0.35),0 完全不羽化,1羽化程度最高,在strenth较小时,羽化程度基本不变.值越大,前景与背景之间的过度边缘部分越宽.
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_BACKGROUND_BLUR_STRENGTH, 0.35f)
        // 设置face mesh结果输出坐标系,(0: 屏幕坐标系， 1：3d世界坐标系， 2:3d摄像机坐标系,是摄像头透视投影坐标系, 原点在摄像机 默认是0）
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_FACE_MESH_OUTPUT_FORMAT, 1.0f)
        // 设置mesh渲染模式
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_MESH_MODE, STCommonNative.MESH_CONFIG.toFloat())
        // 设置人头实例分割
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_INSTANCE, 1.0f)
    }

    private fun setMakeUpItem(type: Int, path: String = "", strength: Float = 1.0f) {
        if (path.isNotEmpty()) {
            mobileEffectNative.setBeautyFromAssetsFile(type, "$resourcePath/$path", assets)
            mobileEffectNative.setBeautyStrength(type, strength)
        } else {
            mobileEffectNative.setBeauty(type, null)
        }
    }

    private fun setStickerItem(path: String, attach: Boolean) {
        if(attach){
            packageMap[path] = mobileEffectNative.changePackageFromAssetsFile("$resourcePath/$path", assets)
        }else{
            packageMap.remove(path)?.let {
                mobileEffectNative.removeEffect(it)
            }
        }
    }

}