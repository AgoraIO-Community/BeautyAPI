package io.agora.beautyapi.demo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.cosmos.beauty.CosmosBeautySDK
import com.cosmos.beauty.module.IMMRenderModuleManager
import com.cosmos.beauty.module.beauty.AutoBeautyType
import com.cosmos.beauty.module.beauty.IBeautyBodyModule
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.MakeupType
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beauty.module.makeup.IMakeupBeautyModule
import com.cosmos.beauty.module.sticker.IStickerModule
import io.agora.base.VideoFrame
import io.agora.beautyapi.cosmos.BeautyPreset
import io.agora.beautyapi.cosmos.CameraConfig
import io.agora.beautyapi.cosmos.CaptureMode
import io.agora.beautyapi.cosmos.Config
import io.agora.beautyapi.cosmos.ErrorCode
import io.agora.beautyapi.cosmos.EventCallback
import io.agora.beautyapi.cosmos.MirrorMode
import io.agora.beautyapi.cosmos.createCosmosBeautyAPI
import io.agora.beautyapi.demo.databinding.BeautyActivityBinding
import io.agora.beautyapi.demo.utils.FileUtils
import io.agora.beautyapi.demo.utils.ReflectUtils
import io.agora.beautyapi.demo.utils.ZipUtils
import io.agora.beautyapi.demo.widget.BeautyDialog
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


class CosmosActivity : ComponentActivity() {
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
            Intent(context, CosmosActivity::class.java).apply {
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

    private val mCosmosApi by lazy {
        createCosmosBeautyAPI()
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
                    val renderView = SurfaceView(this@CosmosActivity)
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
            ), 0, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        )
    }

    private val isCustomCaptureMode by lazy {
        intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)
    }

    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnableDefault)
            setOnBeautyChangeListener { enable ->
                mCosmosApi.enable(enable)
            }
            setOnColorEnhanceChangeListener { enable ->
                val options = ColorEnhanceOptions()
                options.strengthLevel = 0.5f
                options.skinProtectLevel = 0.5f
                mRtcEngine.setColorEnhanceOptions(enable, options)
            }
            setOnI420ChangeListener { enable ->
                if (enable) {
                    mCosmosApi.setParameters(
                        "beauty_mode",
                        "2"
                    )
                } else {
                    mCosmosApi.setParameters(
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
                mCosmosApi.enable(enable)
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
                            CosmosBeautyWrapSDK.beautyModule?.setAutoBeauty(AutoBeautyType.AUTOBEAUTY_NULL)
                            dialog.isTopLayoutVisible = false
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_smooth,
                            R.mipmap.ic_beauty_face_mopi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.SKIN_SMOOTH,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_whiten,
                            R.mipmap.ic_beauty_face_meibai,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.SKIN_WHITENING,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_redden,
                            R.mipmap.ic_beauty_face_redden,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.RUDDY,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_overall,
                            R.mipmap.ic_beauty_face_shoulian,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.THIN_FACE,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_cheekbone,
                            R.mipmap.ic_beauty_face_shouquangu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.LIP_THICKNESS,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_eye,
                            R.mipmap.ic_beauty_face_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.BIG_EYE,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_nose,
                            R.mipmap.ic_beauty_face_shoubi,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.NOSE_SIZE,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_chin,
                            R.mipmap.ic_beauty_face_xiaba,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.CHIN_LENGTH,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_jawbone,
                            R.mipmap.ic_beauty_face_xiahegu,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.CHEEKBONE_WIDTH,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_forehead,
                            R.mipmap.ic_beauty_face_etou,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.FOREHEAD,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_mouth,
                            R.mipmap.ic_beauty_face_zuixing,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.MOUTH_SIZE,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_teeth,
                            R.mipmap.ic_beauty_face_meiya,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.TEETH_WHITE,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_bright_eye,
                            R.mipmap.ic_beauty_face_bright_eye,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.EYE_BRIGHT,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_dark_circles,
                            R.mipmap.ic_beauty_face_remove_dark_circles,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.SKIN_SMOOTHING_EYES,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_beauty_remove_nasolabial_folds,
                            R.mipmap.ic_beauty_face_remove_nasolabial_folds,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.NASOLABIAL_FOLDS,
                                value
                            )
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_adjust_sharpen,
                            R.mipmap.ic_beauty_adjust_sharp,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.beautyModule?.setValue(
                                SimpleBeautyType.SHARPEN,
                                value
                            )
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
                            CosmosBeautyWrapSDK.makeUpItem = null
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_effect_tianmei,
                            R.mipmap.ic_beauty_effect_tianmei,
                            0f
                        ) { dialog, value ->
                            dialog.isTopLayoutVisible = true
                            CosmosBeautyWrapSDK.makeUpItem = CosmosBeautyWrapSDK.MakeUpItem(
                                "${CosmosBeautyWrapSDK.cosmosPath}/makeup_style/heitonghua",
                                value
                            )
                        }
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
                            CosmosBeautyWrapSDK.stickerModule?.removeModule()
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_huahua,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            dialog.isTopSliderVisible = false
                            CosmosBeautyWrapSDK.stickerModule?.removeModule()
                            CosmosBeautyWrapSDK.stickerModule?.addMaskModel(
                                File("${CosmosBeautyWrapSDK.cosmosPath}/sticker/weixiao")
                            ) { }
                        },
                        BeautyDialog.ItemInfo(
                            R.string.beauty_item_sticker_wochaotian,
                            R.mipmap.ic_beauty_filter_naiyou,
                            0f
                        ) { dialog, _ ->
                            dialog.isTopLayoutVisible = true
                            dialog.isTopSliderVisible = false
                            CosmosBeautyWrapSDK.stickerModule?.removeModule()
                            CosmosBeautyWrapSDK.stickerModule?.addMaskModel(
                                File("${CosmosBeautyWrapSDK.cosmosPath}/sticker/xiha")
                            ) { }
                        },
                    )
                )
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.keepScreenOn = true

        // Step 1: initialize cosmos beauty api with renderModuleManager, witch created by CosmosBeautySDK.
        mCosmosApi.initialize(
            Config(
                application,
                mRtcEngine,
                CosmosBeautyWrapSDK.renderModuleManager!!,
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                statsEnable = true,
                eventCallback = EventCallback(
                    onBeautyStats = { stats ->
                        Log.d(TAG, "onBeautyStats >> $stats")
                    }
                )
            )
        )
        if (beautyEnableDefault) {
            mCosmosApi.enable(true)
        }
        mCosmosApi.setBeautyPreset(BeautyPreset.DEFAULT)
        // render local video
        mCosmosApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_HIDDEN)

        when (intent.getStringExtra(EXTRA_PROCESS_MODE)) {
            getString(R.string.beauty_process_auto) -> mCosmosApi.setParameters(
                "beauty_mode",
                "0"
            )

            getString(R.string.beauty_process_texture) -> mCosmosApi.setParameters(
                "beauty_mode",
                "1"
            )

            getString(R.string.beauty_process_i420) -> mCosmosApi.setParameters(
                "beauty_mode",
                "2"
            )
        }

        if (isCustomCaptureMode) {
            mRtcEngine.registerVideoFrameObserver(object : IVideoFrameObserver {

                override fun onCaptureVideoFrame(
                    sourceType: Int,
                    videoFrame: VideoFrame?
                ): Boolean {
                    return when (mCosmosApi.onFrame(videoFrame!!)) {
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

                override fun getVideoFrameProcessMode() =
                    IVideoFrameObserver.PROCESS_MODE_READ_WRITE

                override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

                override fun getRotationApplied() = false

                override fun getMirrorApplied() = mCosmosApi.getMirrorApplied()

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
            mCosmosApi.setBeautyPreset(if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM)
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable
            if (enable) {

            } else {

            }
        }
        mBinding.ctvSticker.setOnClickListener {
            val enable = !mBinding.ctvSticker.isChecked
            mBinding.ctvSticker.isChecked = enable
        }
        mBinding.ivMirror.setOnClickListener {
            val isFront = mCosmosApi.isFrontCamera()
            if (isFront) {
                cameraConfig = CameraConfig(
                    frontMirror = when (cameraConfig.frontMirror) {
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                    },
                    backMirror = cameraConfig.backMirror
                )
                Toast.makeText(this, "frontMirror=${cameraConfig.frontMirror}", Toast.LENGTH_SHORT)
                    .show()
            } else {
                cameraConfig = CameraConfig(
                    frontMirror = cameraConfig.frontMirror,
                    backMirror = when (cameraConfig.backMirror) {
                        MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
                        MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
                        MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
                        MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
                    }
                )
                Toast.makeText(this, "backMirror=${cameraConfig.backMirror}", Toast.LENGTH_SHORT)
                    .show()
            }
            mCosmosApi.updateCameraConfig(cameraConfig)
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
        mCosmosApi.release()
        CosmosBeautyWrapSDK.reset()
        RtcEngine.destroy()
    }

}

object CosmosBeautyWrapSDK {
    private const val TAG = "CosmosBeautySDK"
    private const val LICENSE =
        "UHlDYW5rdzZrSWVkTUlHZE1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTEFEQ0Jma1Nod0tCZ1FDOTZhZjhRalBTRERhcHpscFgzK1pXbzRDd1pUV1N6CnNERkZwb0doZkpLV0V0cnNNdEFnbk1tOFhsbGhJVTlpaGVxMEdEbG02RVM1SkdTSVEreUZlb3dRRHB4em1zMzZsSTJ5YlNjcjZKb1ZkcjFITGNkQ20KQmV4aEp2ZDNtdXNMNmphTDYwZzhhL0tjLzhjSjltbTYwclUwdEtyNXp4dWZCYlhlMTZCNGs4M3RVNHhRaEkySXVzWGd1RDRsM1EwZTNMYWJKR1pBVGRqcGwyZ2dHTW9WNnpxUy9IRVlBcW1BaUJveW5rTENBV1EKUTdnQS9nZ3hKR1FwWVZsSmdRSUJBdzRqNE9WZldnSVJHOFRVWXNNeGNHSFNOVzhmMEEyV2puVHNIcEI3M3lLY0lOZm11VzJHREl6REpzSHgvWkd1b1lTQXY1VHNHanhvdz0="

    private val workerThread = Executors.newSingleThreadExecutor()
    private var context: Application? = null
    private var storagePath = ""
    private var assetsPath = ""

    @Volatile
    private var authSuccess = false
    // 滤镜
    var lookupModule: ILookupModule? = null
    // 美妆
    var makeupModule: IMakeupBeautyModule? = null
    // 美颜
    var beautyModule: IBeautyModule? = null
    // 贴纸
    var stickerModule: IStickerModule? = null
    // 体形
    var beautyBodyModule: IBeautyBodyModule? = null
    // 资源根路径
    var cosmosPath = ""

    // 美颜渲染句柄
    var renderModuleManager: IMMRenderModuleManager? = null
        get() {
            if(field == null){
                initRenderManager()
            }
            return field
        }


    var makeUpItem: MakeUpItem? = null
        set(value) {
            if (field?.path != value?.path) {
                makeupModule?.clear()
                if (value?.path != null) {
                    makeupModule?.addMakeup(value.path)
                }
            }
            field = value
            if (value != null) {
                makeupModule?.setValue(MakeupType.MAKEUP_BLUSH, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_EYEBOW, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_EYESHADOW, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_LIP, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_FACIAL, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_PUPIL, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_STYLE, value.strength)
                makeupModule?.setValue(MakeupType.MAKEUP_LUT, value.strength)
            }
        }


    fun initBeautySDK(context: Context) {
        this.context = context.applicationContext as Application?
        this.storagePath = context.getExternalFilesDir("")?.absolutePath ?: return
        this.assetsPath = "beauty_cosmos"
        workerThread.execute {
            // copy cosmos.zip
            val cosmosZipPath = "${storagePath}/beauty_cosmos/cosmos.zip"
            FileUtils.copyAssets(context, "$assetsPath/cosmos.zip", cosmosZipPath)

            // copy model-all.zip
            val modelAllZipPath = "${storagePath}/beauty_cosmos/model-all.zip"
            FileUtils.copyAssets(context, "$assetsPath/model-all.zip", modelAllZipPath)

            // unzip cosmos.zip
            cosmosPath = "${storagePath}/beauty_cosmos"
            ZipUtils.unzip(cosmosZipPath, cosmosPath)
            cosmosPath = "$cosmosPath/cosmos"

            // unzip model-all.zip
            val modelAllPath = "${storagePath}/beauty_cosmos"
            ZipUtils.unzip(modelAllZipPath, modelAllPath)

            Handler(Looper.getMainLooper()).post {
                val result = CosmosBeautySDK.init(
                    context, LICENSE, modelAllPath
                )
                if (result.isSucceed) {
                    Log.d(TAG, "授权成功")
                    authSuccess = true
                } else {
                    Log.e(TAG, "授权失败 ${result.msg}")
                }
            }
        }
    }

    private fun initRenderManager() {
        renderModuleManager = CosmosBeautySDK.createRenderModuleManager()
        renderModuleManager?.prepare(true)
        initModules()
    }

    private fun initModules(){
        beautyModule = CosmosBeautySDK.createBeautyModule()
        renderModuleManager?.registerModule(beautyModule!!)

        makeupModule = CosmosBeautySDK.createMakeupBeautyModule()
        renderModuleManager?.registerModule(makeupModule!!)

        lookupModule = CosmosBeautySDK.createLoopupModule()
        renderModuleManager?.registerModule(lookupModule!!)

        stickerModule = CosmosBeautySDK.createStickerModule()
        renderModuleManager?.registerModule(stickerModule!!)

        beautyBodyModule = CosmosBeautySDK.createBeautyBodyModule()
        renderModuleManager?.registerModule(beautyBodyModule!!)
    }

    fun reset() {
        renderModuleManager?.release()
        renderModuleManager = null
    }


    data class MakeUpItem(
        val path: String,
        val strength: Float
    )



}