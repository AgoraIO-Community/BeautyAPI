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
import io.agora.beautyapi.demo.widget.BeautyDialog
import io.agora.beautyapi.sensetime.BeautyPreset
import io.agora.beautyapi.sensetime.BeautyStats
import io.agora.beautyapi.sensetime.CameraConfig
import io.agora.beautyapi.sensetime.CaptureMode
import io.agora.beautyapi.sensetime.Config
import io.agora.beautyapi.sensetime.ErrorCode
import io.agora.beautyapi.sensetime.IEventCallback
import io.agora.beautyapi.sensetime.MirrorMode
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

    private val beautyEnableDefault = true

    private var cameraConfig = CameraConfig()

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
                    renderView.setZOrderMediaOverlay(true)
                    renderView.setZOrderOnTop(true)
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

    private val mVideoEncoderConfiguration by lazy {
        VideoEncoderConfiguration(
            ReflectUtils.getStaticFiledValue(
                VideoEncoderConfiguration::class.java, intent.getStringExtra(EXTRA_RESOLUTION)
            ), ReflectUtils.getStaticFiledValue(
                FRAME_RATE::class.java, intent.getStringExtra(EXTRA_FRAME_RATE)
            ), 0, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
    }

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


    private val mBeautyDialog by lazy {
        BeautyDialog(this).apply {
            isEnable = beautyEnableDefault
            onEnableChanged = { enable ->
                mSenseTimeApi.enable(enable)
            }
            groupList = listOf(
                BeautyDialog.GroupInfo(
                    R.string.beauty_group_beauty,
                    0,
                    listOf(
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_none,
                            R.mipmap.ic_beauty_none,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = false
                            SenseTimeBeautySDK.setBeauty(
                                smooth = 0.0f,
                                whiten = 0.0f,
                                thinFace = 0.0f,
                                enlargeEye = 0.0f,
                                redden = 0.0f,
                                shrinkCheekbone = 0.0f,
                                shrinkJawbone = 0.0f,
                                whiteTeeth = 0.0f,
                                hairlineHeight = 0.0f,
                                narrowNose = 0.0f,
                                mouthSize = 0.0f,
                                chinLength = 0.0f,
                                brightEye = 0.0f,
                                darkCircles = 0.0f,
                                nasolabialFolds = 0.0f,
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_smooth,
                            R.mipmap.ic_beauty_face_mopi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(smooth = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_whiten,
                            R.mipmap.ic_beauty_face_meibai,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(whiten = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_redden,
                            R.mipmap.ic_beauty_face_redden,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(redden = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_overall,
                            R.mipmap.ic_beauty_face_shoulian,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(thinFace = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_cheekbone,
                            R.mipmap.ic_beauty_face_shouquangu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(shrinkCheekbone = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_eye,
                            R.mipmap.ic_beauty_face_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(enlargeEye = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_nose,
                            R.mipmap.ic_beauty_face_shoubi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(narrowNose = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_chin,
                            R.mipmap.ic_beauty_face_xiaba,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(chinLength = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_jawbone,
                            R.mipmap.ic_beauty_face_xiahegu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(shrinkJawbone = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_forehead,
                            R.mipmap.ic_beauty_face_etou,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(hairlineHeight = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_mouth,
                            R.mipmap.ic_beauty_face_zuixing,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(mouthSize = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_teeth,
                            R.mipmap.ic_beauty_face_meiya,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(whiteTeeth = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_bright_eye,
                            R.mipmap.ic_beauty_face_bright_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(brightEye = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_dark_circles,
                            R.mipmap.ic_beauty_face_remove_dark_circles,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(darkCircles = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_nasolabial_folds,
                            R.mipmap.ic_beauty_face_remove_nasolabial_folds,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(nasolabialFolds = value)
                        },
                    )
                ),
                BeautyDialog.GroupInfo(
                    R.string.beauty_group_adjust,
                    0,
                    listOf(
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_none,
                            R.mipmap.ic_beauty_none,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = false
                            SenseTimeBeautySDK.setBeauty(
                                saturation = 0.0f,
                                contrast = 0.0f,
                                sharpen = 0.0f,
                                clear = 0.0f,
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_adjust_contrast,
                            R.mipmap.ic_beauty_adjust_contrast,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(contrast = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_adjust_saturation,
                            R.mipmap.ic_beauty_adjust_saturation,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(saturation = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_adjust_sharpen,
                            R.mipmap.ic_beauty_adjust_sharp,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(sharpen = value)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_adjust_clarity,
                            R.mipmap.ic_beauty_adjust_clear,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setBeauty(clear = value)
                        },
                    )
                ),
                BeautyDialog.GroupInfo(
                    R.string.beauty_group_effect,
                    0,
                    listOf(
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_none,
                            R.mipmap.ic_beauty_none,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = false
                            SenseTimeBeautySDK.setMakeUpItem(this@SenseTimeActivity, STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_effect_tianmei,
                            R.mipmap.ic_beauty_effect_tianmei,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setMakeUpItem(
                                this@SenseTimeActivity,
                                STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                                "makeup_lip" + File.separator + "12自然.zip",
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_effect_yuanqi,
                            R.mipmap.ic_beauty_effect_yuanqi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setMakeUpItem(
                                this@SenseTimeActivity,
                                STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL,
                                "makeup_lip" + File.separator + "6自然.zip",
                                value
                            )
                        },
                    )
                ),
                BeautyDialog.GroupInfo(
                    R.string.beauty_group_sticker,
                    0,
                    listOf(
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_none,
                            R.mipmap.ic_beauty_none,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = false
                            SenseTimeBeautySDK.cleanSticker()
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_huahua,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setStickerItem(this@SenseTimeActivity, "sticker_face_shape" + File.separator + "ShangBanLe.zip", true)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_wochaotian,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            SenseTimeBeautySDK.setStickerItem(this@SenseTimeActivity, "sticker_face_shape" + File.separator + "chunjie.zip", true)
                        },
                    )
                )
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.keepScreenOn = true

        val isCustomCaptureMode =
            intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)

        SenseTimeBeautySDK.initMobileEffect(this)
        mSenseTimeApi.initialize(
            Config(
                application,
                mRtcEngine,
                STHandlers(SenseTimeBeautySDK.mobileEffectNative, SenseTimeBeautySDK.humanActionNative),
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                statsEnable = true,
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
        // render local video
        mSenseTimeApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)

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

                override fun onCaptureVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ) : Boolean {
                    return when(mSenseTimeApi.onFrame(videoFrame!!)){
                        ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> true
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

                override fun getMirrorApplied() = mSenseTimeApi.getMirrorApplied()

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
        mBinding.ivBeauty.setOnClickListener {
            mBeautyDialog.show()
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
                SenseTimeBeautySDK.setMakeUpItem(
                    this,
                    STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP,
                    "makeup_lip" + File.separator + "12自然.zip",
                    1f
                )
            } else {
                SenseTimeBeautySDK.setMakeUpItem(this, STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP)
            }
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            SenseTimeBeautySDK.setStickerItem(this, "sticker_face_shape" + File.separator + "ShangBanLe.zip", enable)
        }
        mBinding.ivMirror.setOnClickListener {
            val isFront = mSenseTimeApi.isFrontCamera()
            if(isFront){
                cameraConfig = CameraConfig(
                    frontMirror = when(cameraConfig.frontMirror){
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                    },
                    backMirror = cameraConfig.backMirror
                )
                Toast.makeText(this, "frontMirror=${cameraConfig.frontMirror}", Toast.LENGTH_SHORT).show()
            } else {
                cameraConfig = CameraConfig(
                    frontMirror = cameraConfig.frontMirror,
                    backMirror = when(cameraConfig.backMirror){
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                    }
                )
                Toast.makeText(this, "backMirror=${cameraConfig.backMirror}", Toast.LENGTH_SHORT).show()
            }
            mSenseTimeApi.updateCameraConfig(cameraConfig)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        mSenseTimeApi.release()
        SenseTimeBeautySDK.unInitMobileEffect()
        RtcEngine.destroy()
    }

}

object SenseTimeBeautySDK {
    private val TAG = "SenseTimeBeautySDK"

    private val resourcePath = "beauty_sensetime"
    private val humanActionCreateConfig = 0
    private val packageMap = mutableMapOf<String, Int>()

    private val MODEL_106 = "models/M_SenseME_Face_Video_Template_p_3.9.0.3.model" // 106
    private val MODEL_FACE_EXTRA = "models/M_SenseME_Face_Extra_Advanced_Template_p_2.0.0.model" // 282
    private val MODEL_AVATAR_HELP = "models/M_SenseME_Avatar_Help_p_2.3.7.model" // avatar人脸驱动
    private val MODEL_LIPS_PARSING = "models/M_SenseME_MouthOcclusion_p_1.3.0.1.model" // 嘴唇分割
    private val MODEL_HAND = "models/M_SenseME_Hand_p_6.0.8.1.model" // 手势
    private val MODEL_SEGMENT = "models/M_SenseME_Segment_Figure_p_4.14.1.1.model" // 前后背景分割
    private val MODEL_SEGMENT_HAIR = "models/M_SenseME_Segment_Hair_p_4.4.0.model" // 头发分割
    private val MODEL_FACE_OCCLUSION = "models/M_SenseME_FaceOcclusion_p_1.0.7.1.model" // 妆容遮挡
    private val MODEL_SEGMENT_SKY = "models/M_SenseME_Segment_Sky_p_1.1.0.1.model" // 天空分割
    private val MODEL_SEGMENT_SKIN = "models/M_SenseME_Segment_Skin_p_1.0.1.1.model" // 皮肤分割
    private val MODEL_3DMESH = "models/M_SenseME_3DMesh_Face2396pt_280kpts_Ear_p_1.1.0v2.model" // 3DMesh
    private val MODEL_HEAD_P_EAR = "models/M_SenseME_Ear_p_1.0.1.1.model" // 搭配 mesh 耳朵模型
    private val MODEL_360HEAD_INSTANCE = "models/M_SenseME_3Dmesh_360Head2396pt_p_1.0.0.1.model" // 360度人头mesh
    private val MODEL_FOOT = "models/M_SenseME_Foot_p_2.10.7.model" // 鞋子检测模型
    private val MODEL_PANT = "models/M_SenseME_Segment_Trousers_p_1.1.10.model" // 裤腿的检测
    private val MODEL_WRIST = "models/M_SenseME_Wrist_p_1.4.0.model" // 试表
    private val MODEL_CLOTH = "models/M_SenseME_Segment_Clothes_p_1.0.2.2.model" // 衣服分割
    private val MODEL_HEAD_INSTANCE = "models/M_SenseME_Segment_Head_Instance_p_1.1.0.1.model" // 实例分割版本
    private val MODEL_HEAD_P_INSTANCE = "models/M_SenseME_Head_p_1.3.0.1.model" // 360度人头-头部模型
    private val MODEL_NAIL = "models/M_SenseME_Nail_p_2.4.0.model" // 指甲检测

    private val workerThread = Executors.newSingleThreadExecutor()

    val mobileEffectNative = STMobileEffectNative()
    val humanActionNative = STMobileHumanActionNative()


    fun initBeautySDK(context: Context){
        workerThread.submit {
            checkLicense(context)
            initHumanAction(context)
        }
    }

    fun initMobileEffect(context: Context){
        val result =
            mobileEffectNative.createInstance(context, STMobileEffectNative.EFFECT_CONFIG_NONE)
        mobileEffectNative.setParam(STMobileEffectParams.EFFECT_PARAM_QUATERNION_SMOOTH_FRAME, 5f)
        Log.d(TAG, "SenseTime >> STMobileEffectNative create result : $result")
    }

    fun unInitMobileEffect(){
        mobileEffectNative.destroyInstance()
    }

    private fun checkLicense(context: Context) {
        val license = io.agora.beautyapi.demo.utils.FileUtils.getAssetsString(
            context,
            "$resourcePath/license/SenseME.lic"
        )
        val activeCode = STMobileAuthentificationNative.generateActiveCodeFromBuffer(
            context,
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

    private fun initHumanAction(context: Context){
        val assets = context.assets
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

    fun setMakeUpItem(context: Context, type: Int, path: String = "", strength: Float = 1.0f) {
        if (path.isNotEmpty()) {
            val assets = context.assets
            mobileEffectNative.setBeautyFromAssetsFile(type, "$resourcePath/$path", assets)
            mobileEffectNative.setBeautyStrength(type, strength)
        } else {
            mobileEffectNative.setBeauty(type, null)
        }
    }

    fun setStickerItem(context: Context, path: String, attach: Boolean) {
        if(attach){
            val assets = context.assets
            packageMap[path] = mobileEffectNative.changePackageFromAssetsFile("$resourcePath/$path", assets)
        }else{
            packageMap.remove(path)?.let {
                mobileEffectNative.removeEffect(it)
            }
        }
    }

    fun cleanSticker(){
        packageMap.values.forEach {
            mobileEffectNative.removeEffect(it)
        }
        packageMap.clear()
    }

    fun setBeauty(
        smooth: Float? = null,
        whiten: Float? = null,
        thinFace: Float? = null,
        enlargeEye: Float? = null,
        redden: Float? = null,
        shrinkCheekbone: Float? = null,
        shrinkJawbone: Float? = null,
        whiteTeeth: Float? = null,
        hairlineHeight: Float? = null,
        narrowNose: Float? = null,
        mouthSize: Float? = null,
        chinLength: Float? = null,
        brightEye: Float? = null,
        darkCircles: Float? = null,
        nasolabialFolds: Float? = null,

        saturation: Float? = null,
        contrast: Float? = null,
        sharpen: Float? = null,
        clear: Float? = null,
    ){
        val effectNative = mobileEffectNative
        // 锐化
        sharpen?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN, it) }

        // 清晰度
        clear?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_TONE_CLEAR, it) }

        // 磨皮
        smooth?.let {
            effectNative.setBeautyMode(
                STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
                STEffectBeautyType.SMOOTH2_MODE
            )
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
                it
            )
        }

        // 美白
        whiten?.let {
            effectNative.setBeautyMode(
                STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
                STEffectBeautyType.WHITENING3_MODE
            )
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
                it
            )
        }

        // 瘦脸
        thinFace?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE, it) }

        // 大眼
        enlargeEye?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, it) }

        // 红润
        redden?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, it) }

        // 瘦颧骨
        shrinkCheekbone?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE, it) }

        // 下颌骨
        shrinkJawbone?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE, it) }

        // 美牙
        whiteTeeth?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH,
                it
            )
        }

        // 额头
        hairlineHeight?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT, it) }

        // 瘦鼻
        narrowNose?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE,
                it
            )
        }

        // 嘴形
        mouthSize?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE, it) }

        // 下巴
        chinLength?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH,it
            )
        }

        // 亮眼
        brightEye?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE,
                it
            )
        }

        // 祛黑眼圈
        darkCircles?.let { effectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES, it) }

        // 祛法令纹
        nasolabialFolds?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS,
                it
            )
        }

        // 饱和度
        saturation?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION,
                it
            )
        }

        // 对比度
        contrast?.let {
            effectNative.setBeautyStrength(
                STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST,
                it
            )
        }

    }
}