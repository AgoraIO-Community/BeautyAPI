package io.agora.beautyapi.demo

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.faceunity.core.callback.OperateCallback
import com.faceunity.core.entity.FUBundleData
import com.faceunity.core.enumeration.FUAITypeEnum
import com.faceunity.core.faceunity.FUAIKit
import com.faceunity.core.faceunity.FURenderConfig.OPERATE_SUCCESS_AUTH
import com.faceunity.core.faceunity.FURenderKit
import com.faceunity.core.faceunity.FURenderManager
import com.faceunity.core.model.facebeauty.FaceBeauty
import com.faceunity.core.model.makeup.SimpleMakeup
import com.faceunity.core.model.prop.Prop
import com.faceunity.core.model.prop.sticker.Sticker
import com.faceunity.core.utils.FULogger
import com.faceunity.wrapper.faceunity
import io.agora.base.VideoFrame
import io.agora.beautyapi.demo.databinding.BeautyActivityBinding
import io.agora.beautyapi.demo.utils.ReflectUtils
import io.agora.beautyapi.demo.widget.BeautyDialog
import io.agora.beautyapi.faceunity.BeautyPreset
import io.agora.beautyapi.faceunity.BeautyStats
import io.agora.beautyapi.faceunity.CameraConfig
import io.agora.beautyapi.faceunity.CaptureMode
import io.agora.beautyapi.faceunity.Config
import io.agora.beautyapi.faceunity.ErrorCode
import io.agora.beautyapi.faceunity.IEventCallback
import io.agora.beautyapi.faceunity.MirrorMode
import io.agora.beautyapi.faceunity.createFaceUnityBeautyAPI
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

class FaceUnityActivity : ComponentActivity() {
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
            Intent(context, FaceUnityActivity::class.java).apply {
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
    private val mFaceUnityApi by lazy {
        createFaceUnityBeautyAPI()
    }
    private val mVideoEncoderConfiguration by lazy {
        VideoEncoderConfiguration(
            ReflectUtils.getStaticFiledValue(
                VideoEncoderConfiguration::class.java,
                intent.getStringExtra(EXTRA_RESOLUTION)
            ),
            ReflectUtils.getStaticFiledValue(
                FRAME_RATE::class.java,
                intent.getStringExtra(EXTRA_FRAME_RATE)
            ),
            0,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        )
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
                    val renderView = SurfaceView(this@FaceUnityActivity)
                    renderView.setZOrderMediaOverlay(true)
                    renderView.setZOrderOnTop(true)
                    mBinding.remoteVideoView.addView(renderView)
                    mRtcEngine.setupRemoteVideo(
                        VideoCanvas(
                            renderView,
                            Constants.RENDER_MODE_HIDDEN,
                            uid
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
    private val beautyEnableDefault = true
    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnableDefault)
            setOnBeautyChangeListener { enable ->
                mFaceUnityApi.enable(enable)
            }
            setOnColorEnhanceChangeListener {enable ->
                val options = ColorEnhanceOptions()
                options.strengthLevel = 0.5f
                options.skinProtectLevel = 0.5f
                mRtcEngine.setColorEnhanceOptions(enable, options)
            }
            setOnI420ChangeListener { enable ->
                if(enable){
                    mFaceUnityApi.setParameters("beauty_mode", "2")
                }else{
                    mFaceUnityApi.setParameters("beauty_mode", "0")
                }
            }
        }
    }
    private var cameraConfig = CameraConfig()
    private val fuRenderKit = FaceUnityBeautySDK.fuRenderKit
    private val isCustomCaptureMode by lazy {
        intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)
    }


    private val mBeautyDialog by lazy {
        BeautyDialog(this).apply {
            isEnable = beautyEnableDefault
            onEnableChanged = { enable ->
                mFaceUnityApi.enable(enable)
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
                            FaceUnityBeautySDK.setBeauty(
                                smooth = 0.0,
                                whiten = 0.0,
                                thinFace = 0.0,
                                enlargeEye = 0.0,
                                redden = 0.0,
                                shrinkCheekbone = 0.0,
                                shrinkJawbone = 0.0,
                                whiteTeeth = 0.0,
                                hairlineHeight = 0.0,
                                narrowNose = 0.0,
                                mouthSize = 0.0,
                                chinLength = 0.0,
                                brightEye = 0.0,
                                darkCircles = 0.0,
                                nasolabialFolds = 0.0,
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_smooth,
                            R.mipmap.ic_beauty_face_mopi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(smooth = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_whiten,
                            R.mipmap.ic_beauty_face_meibai,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(whiten = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_redden,
                            R.mipmap.ic_beauty_face_redden,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(redden = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_overall,
                            R.mipmap.ic_beauty_face_shoulian,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(thinFace = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_cheekbone,
                            R.mipmap.ic_beauty_face_shouquangu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(shrinkCheekbone = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_eye,
                            R.mipmap.ic_beauty_face_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(enlargeEye = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_nose,
                            R.mipmap.ic_beauty_face_shoubi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(narrowNose = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_chin,
                            R.mipmap.ic_beauty_face_xiaba,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(chinLength = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_jawbone,
                            R.mipmap.ic_beauty_face_xiahegu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(shrinkJawbone = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_forehead,
                            R.mipmap.ic_beauty_face_etou,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(hairlineHeight = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_mouth,
                            R.mipmap.ic_beauty_face_zuixing,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(mouthSize = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_teeth,
                            R.mipmap.ic_beauty_face_meiya,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(whiteTeeth = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_bright_eye,
                            R.mipmap.ic_beauty_face_bright_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(brightEye = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_dark_circles,
                            R.mipmap.ic_beauty_face_remove_dark_circles,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(darkCircles = value.toDouble())
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_nasolabial_folds,
                            R.mipmap.ic_beauty_face_remove_nasolabial_folds,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            FaceUnityBeautySDK.setBeauty(nasolabialFolds = value.toDouble())
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
                            fuRenderKit.makeup = null
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_effect_tianmei,
                            R.mipmap.ic_beauty_effect_tianmei,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true

                            val makeup =
                                SimpleMakeup(FUBundleData("graphics" + File.separator + "face_makeup.bundle"))
                            makeup.setCombinedConfig(FUBundleData("beauty_faceunity/makeup/naicha.bundle"))
                            makeup.makeupIntensity = value.toDouble()
                            fuRenderKit.makeup = makeup
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_effect_yuanqi,
                            R.mipmap.ic_beauty_effect_yuanqi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true

                            val makeup =
                                SimpleMakeup(FUBundleData("graphics" + File.separator + "face_makeup.bundle"))
                            makeup.setCombinedConfig(FUBundleData("beauty_faceunity/makeup/dousha.bundle"))
                            makeup.makeupIntensity = value.toDouble()
                            fuRenderKit.makeup = makeup
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
                            fuRenderKit.propContainer.removeAllProp()
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_huahua,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            dialog.isTopSliderVisible = false

                            fuRenderKit.propContainer.removeAllProp()
                            val prop: Prop = Sticker(FUBundleData("beauty_faceunity/sticker/fu_zh_fenshu.bundle"))
                            fuRenderKit.propContainer.addProp(prop)
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_wochaotian,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            dialog.isTopSliderVisible = false

                            fuRenderKit.propContainer.removeAllProp()
                            val prop: Prop = Sticker(FUBundleData("beauty_faceunity/sticker/fashi.bundle"))
                            fuRenderKit.propContainer.addProp(prop)
                        },
                    )
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true


        mFaceUnityApi.initialize(
            Config(
                applicationContext,
                mRtcEngine,
                fuRenderKit,
                captureMode = if(isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                cameraConfig = this.cameraConfig,
                statsEnable = true,
                eventCallback = object: IEventCallback{
                    override fun onBeautyStats(stats: BeautyStats) {
                        Log.d(TAG, "BeautyStats stats = $stats")
                    }
                }
            )
        )
        when (intent.getStringExtra(EXTRA_PROCESS_MODE)) {
            getString(R.string.beauty_process_auto) -> mFaceUnityApi.setParameters("beauty_mode", "0")
            getString(R.string.beauty_process_texture) -> mFaceUnityApi.setParameters("beauty_mode", "3")
            getString(R.string.beauty_process_i420) -> mFaceUnityApi.setParameters("beauty_mode", "2")
        }
        if(isCustomCaptureMode){
            mRtcEngine.registerVideoFrameObserver(object : IVideoFrameObserver {

                override fun onCaptureVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ) = when (mFaceUnityApi.onFrame(videoFrame!!)) {
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

                override fun getMirrorApplied() = mFaceUnityApi.getMirrorApplied()

                override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER
            })
        }
        if (beautyEnableDefault) {
            mFaceUnityApi.enable(true)
        }
        mFaceUnityApi.setBeautyPreset(BeautyPreset.DEFAULT)
        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.enableVideo()


        // render local video
        mFaceUnityApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_HIDDEN)


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
            mFaceUnityApi.setBeautyPreset(if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM)
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable
            if (enable) {
                val makeup =
                    SimpleMakeup(FUBundleData("graphics" + File.separator + "face_makeup.bundle"))
                makeup.setCombinedConfig(FUBundleData("beauty_faceunity/makeup/naicha.bundle"))
                makeup.makeupIntensity = 1.0
                fuRenderKit.makeup = makeup
            } else {
                fuRenderKit.makeup = null
            }
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
            if (enable) {
                val prop: Prop = Sticker(FUBundleData("beauty_faceunity/sticker/fu_zh_fenshu.bundle"))
                fuRenderKit.propContainer.replaceProp(null, prop)
            } else {
                fuRenderKit.propContainer.removeAllProp()
            }
        }
        mBinding.ivMirror.setOnClickListener {
            val isFront = mFaceUnityApi.isFrontCamera()
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
            mFaceUnityApi.updateCameraConfig(cameraConfig)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            (mBinding.remoteVideoView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                it.dimensionRatio = "9:16"
                mBinding.remoteVideoView.layoutParams = it
            }
        } else {
            (mBinding.remoteVideoView.layoutParams as? ConstraintLayout.LayoutParams)?.let {
                it.dimensionRatio = "16:9"
                mBinding.remoteVideoView.layoutParams = it
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        mRtcEngine.stopPreview()
        if (isCustomCaptureMode) {
            mRtcEngine.registerVideoFrameObserver(null)
        }
        mFaceUnityApi.release()
        RtcEngine.destroy()
    }

}

object FaceUnityBeautySDK {
    private val TAG = "FaceUnityBeautySDK"

    private val fuAIKit = FUAIKit.getInstance()
    val fuRenderKit = FURenderKit.getInstance()

    /* AI道具*/
    private val BUNDLE_AI_FACE = "model" + File.separator + "ai_face_processor.bundle"
    private val BUNDLE_AI_HUMAN = "model" + File.separator + "ai_human_processor.bundle"

    private val workerThread = Executors.newSingleThreadExecutor()

    fun initBeauty(context: Context){
        FURenderManager.setKitDebug(FULogger.LogLevel.TRACE)
        FURenderManager.setCoreDebug(FULogger.LogLevel.ERROR)
        FURenderManager.registerFURender(context, getAuth(), object : OperateCallback {
            override fun onSuccess(code: Int, msg: String) {
                Log.i(TAG, "FURenderManager onSuccess -- code=$code, msg=$msg")
                if (code == OPERATE_SUCCESS_AUTH) {
                    faceunity.fuSetUseTexAsync(1)
                    workerThread.submit {
                        fuAIKit.loadAIProcessor(BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR)
                        fuAIKit.loadAIProcessor(BUNDLE_AI_HUMAN, FUAITypeEnum.FUAITYPE_HUMAN_PROCESSOR)
                    }
                }
            }

            override fun onFail(errCode: Int, errMsg: String) {
                Log.e(TAG, "FURenderManager onFail -- code=$errCode, msg=$errMsg")
            }
        })
    }

    private fun getAuth(): ByteArray{
        val authpack = Class.forName("io.agora.beautyapi.demo.authpack")
        val aMethod = authpack.getDeclaredMethod("A")
        aMethod.isAccessible = true
        val authValue = aMethod.invoke(null) as? ByteArray
        return authValue ?: ByteArray(0)
    }

    fun setBeauty(
        smooth: Double? = null,
        whiten: Double? = null,
        thinFace: Double? = null,
        enlargeEye: Double? = null,
        redden: Double? = null,
        shrinkCheekbone: Double? = null,
        shrinkJawbone: Double? = null,
        whiteTeeth: Double? = null,
        hairlineHeight: Double? = null,
        narrowNose: Double? = null,
        mouthSize: Double? = null,
        chinLength: Double? = null,
        brightEye: Double? = null,
        darkCircles: Double? = null,
        nasolabialFolds: Double? = null,
    ){
        if(fuRenderKit.faceBeauty == null){
            fuRenderKit.faceBeauty = FaceBeauty(FUBundleData("graphics" + File.separator + "face_beautification.bundle"))
        }
        // 磨皮
        smooth?.let { fuRenderKit.faceBeauty?.blurIntensity = it * 6 }

        // 美白
        whiten?.let { fuRenderKit.faceBeauty?.colorIntensity = it * 2 }

        // 瘦脸
        thinFace?.let { fuRenderKit.faceBeauty?.cheekThinningIntensity = it }

        // 大眼
        enlargeEye?.let { fuRenderKit.faceBeauty?.eyeEnlargingIntensity = it }

        // 红润
        redden?.let { fuRenderKit.faceBeauty?.redIntensity = it * 2 }

        // 瘦颧骨
        shrinkCheekbone?.let { fuRenderKit.faceBeauty?.cheekBonesIntensity = it }

        // 下颌骨
        shrinkJawbone?.let { fuRenderKit.faceBeauty?.lowerJawIntensity = it }

        // 美牙
        whiteTeeth?.let {fuRenderKit.faceBeauty?.toothIntensity = it}

        // 额头
        hairlineHeight?.let { fuRenderKit.faceBeauty?.forHeadIntensity = it }

        // 瘦鼻
        narrowNose?.let {fuRenderKit.faceBeauty?.noseIntensity = it }

        // 嘴形
        mouthSize?.let { fuRenderKit.faceBeauty?.mouthIntensity = it }

        // 下巴
        chinLength?.let {fuRenderKit.faceBeauty?.chinIntensity = it}

        // 亮眼
        brightEye?.let {fuRenderKit.faceBeauty?.eyeBrightIntensity = it}

        // 祛黑眼圈
        darkCircles?.let { fuRenderKit.faceBeauty?.removePouchIntensity = it }

        // 祛法令纹
        nasolabialFolds?.let {fuRenderKit.faceBeauty?.removeLawPatternIntensity = it}

    }
}