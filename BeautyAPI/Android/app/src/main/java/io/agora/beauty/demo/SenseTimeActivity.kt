package io.agora.beauty.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.sensetime.effects.STRenderKit
import com.sensetime.effects.utils.FileUtils
import com.sensetime.stmobile.model.STMobileMakeupType
import com.sensetime.stmobile.params.STEffectBeautyType
import io.agora.beauty.demo.databinding.SensetimeActivityBinding
import io.agora.beauty.demo.utils.ReflectUtils
import io.agora.beauty.sensetime.Config
import io.agora.beauty.sensetime.createSenseTimeBeautyAPI
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.CameraCapturerConfiguration
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

        fun launch(context: Context, channelName: String, resolution: String, frameRate: String) {
            Intent(context, SenseTimeActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_RESOLUTION, resolution)
                putExtra(EXTRA_FRAME_RATE, frameRate)
                context.startActivity(this)
            }
        }
    }

    private val mBinding by lazy {
        SensetimeActivityBinding.inflate(LayoutInflater.from(this))
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
        })
    }
    private val mSTRenderKit by lazy {
        STRenderKit(this)
    }
    private val mSenseTimeApi by lazy {
        createSenseTimeBeautyAPI()
    }
    private val mVideoCaptureConfig by lazy {
        val resolution =
            ReflectUtils.getStaticFiledValue<VideoEncoderConfiguration.VideoDimensions>(
                VideoEncoderConfiguration::class.java,
                intent.getStringExtra(EXTRA_RESOLUTION)
            )
        val fps = ReflectUtils.getStaticFiledValue<FRAME_RATE>(
            FRAME_RATE::class.java,
            intent.getStringExtra(EXTRA_FRAME_RATE)
        )
        CameraCapturerConfiguration(
            CameraCapturerConfiguration.CaptureFormat(
                resolution?.width ?: 0, resolution?.height ?: 0, fps?.value ?: 0
            )
        )
    }
    private val beautyEnable = true
    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnable)
            setOnBeautyChangeListener { enable ->
                mSenseTimeApi.enable(enable)
                mSenseTimeApi.setOptimizedDefault()
            }
            setFrameRateSelect(intent.getStringExtra(EXTRA_FRAME_RATE).toString())
            setOnFrameRateChangeListener { frameRate ->
                mVideoCaptureConfig.captureFormat.fps =
                    ReflectUtils.getStaticFiledValue<FRAME_RATE>(
                        FRAME_RATE::class.java,
                        frameRate
                    )?.value ?: 0
                mRtcEngine.setCameraCapturerConfiguration(mVideoCaptureConfig)
            }
            setResolutionSelect(intent.getStringExtra(EXTRA_RESOLUTION).toString())
            setOnResolutionChangeListener { resolution ->
                val res =
                    ReflectUtils.getStaticFiledValue<VideoEncoderConfiguration.VideoDimensions>(
                        VideoEncoderConfiguration::class.java,
                        resolution
                    )
                mVideoCaptureConfig.captureFormat.width = res?.width ?: 0
                mVideoCaptureConfig.captureFormat.height = res?.height ?: 0
                mRtcEngine.setCameraCapturerConfiguration(mVideoCaptureConfig)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mSenseTimeApi.initialize(Config(mRtcEngine, mSTRenderKit))
        if (beautyEnable) {
            mSenseTimeApi.enable(true)
            mSenseTimeApi.setOptimizedDefault()
        }

        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(
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
        )
        mRtcEngine.enableVideo()

        // render local video
        mRtcEngine.setupLocalVideo(VideoCanvas(mBinding.localVideoView).apply {
            mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
        })

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
            if (enable) {
                mSTRenderKit.setBeautyMode(
                    STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
                    STEffectBeautyType.WHITENING1_MODE
                )
                mSTRenderKit.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, 100f)
                mSTRenderKit.setBeautyStrength(
                    STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE,
                    1.0f
                )
            } else {
                mSTRenderKit.setBeautyMode(
                    STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
                    STEffectBeautyType.WHITENING1_MODE
                )
                mSTRenderKit.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, 0f)
                mSTRenderKit.setBeautyStrength(
                    STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE,
                    0f
                )
            }
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
        mBinding.ctvFilter.setOnClickListener {
            val enable = !mBinding.ctvFilter.isChecked
            mBinding.ctvFilter.isChecked = enable
            if (enable) {
                setFilterItem(
                    "filter_portrait" + File.separator + "filter_style_babypink.model",
                    1f
                )
            } else {
                setFilterItem(
                    "filter_portrait" + File.separator + "filter_style_babypink.model",
                    0f
                )
            }
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
        mSenseTimeApi.release()
        mRtcEngine.leaveChannel()
        mSTRenderKit.release()
        RtcEngine.destroy()
    }

    private fun setMakeUpItem(type: Int, typePath: String?, strength: Float) {
        if (typePath != null) {
            val split = typePath.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val className = split[0]
            val fileName = split[1]
            val _path = FileUtils.getFilePath(this, className + File.separator + fileName)
            FileUtils.copyFileIfNeed(this, fileName, className)
            mSTRenderKit.setMakeupForType(type, _path)
            mSTRenderKit.setMakeupStrength(type, strength)
        } else {
            mSTRenderKit.removeMakeupByType(type)
        }
    }

    private fun setStickerItem(path: String, attach: Boolean) {
        val split = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val className = split[0]
        val fileName = split[1]
        val _path = FileUtils.getFilePath(this, className + File.separator + fileName)
        FileUtils.copyFileIfNeed(this, fileName, className)
        if (!attach) {
            mSTRenderKit.removeSticker(_path)
        } else {
            mSTRenderKit.changeSticker(_path)
        }
    }

    private fun setFilterItem(filterPath: String, strength: Float) {
        val split = filterPath.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val className = split[0]
        val fileName = split[1]
        val filterName = split[1].split("_".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[2].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0]
        val path = FileUtils.getFilePath(this, className + File.separator + fileName)
        FileUtils.copyFileIfNeed(this, fileName, className)
        mSTRenderKit.setFilterStyle(className, filterName, path)
        mSTRenderKit.setFilterStrength(strength)
    }


}