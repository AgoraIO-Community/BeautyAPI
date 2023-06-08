package io.agora.beauty.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.faceunity.core.entity.FUBundleData
import com.faceunity.core.faceunity.FURenderKit
import com.faceunity.core.model.makeup.SimpleMakeup
import com.faceunity.core.model.prop.Prop
import com.faceunity.core.model.prop.sticker.Sticker
import com.faceunity.nama.FURenderer
import io.agora.beauty.demo.databinding.BeautyActivityBinding
import io.agora.beauty.demo.utils.ReflectUtils
import io.agora.beauty.faceunity.BeautyPreset
import io.agora.beauty.faceunity.BeautyStats
import io.agora.beauty.faceunity.Config
import io.agora.beauty.faceunity.IEventCallback
import io.agora.beauty.faceunity.createFaceUnityBeautyAPI
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE
import java.io.File

class FaceUnityActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName

    companion object {
        private const val EXTRA_CHANNEL_NAME = "ChannelName"
        private const val EXTRA_RESOLUTION = "Resolution"
        private const val EXTRA_FRAME_RATE = "FrameRate"

        fun launch(
            context: Context,
            channelName: String,
            resolution: String,
            frameRate: String
        ) {
            Intent(context, FaceUnityActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_RESOLUTION, resolution)
                putExtra(EXTRA_FRAME_RATE, frameRate)
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
                    val renderView = SurfaceView(this@FaceUnityActivity)
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
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
    }
    private val beautyEnableDefault = false
    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnableDefault)
            setOnBeautyChangeListener { enable ->
                mFaceUnityApi.enable(enable)
            }
        }
    }
    private val fuRenderKit by lazy {
        FURenderer.getInstance().setup(this)
        FURenderKit.getInstance()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mFaceUnityApi.initialize(Config(
            mRtcEngine,
            fuRenderKit,
            eventCallback = object : IEventCallback {
                override fun onBeautyStats(stats: BeautyStats) {
//                    Log.d(
//                        "SenseTime",
//                        "onBeautyStatus totalCostTime = ${stats.totalCostTimeMs}"
//                    )
                }
            }
        ))
        if (beautyEnableDefault) {
            mFaceUnityApi.enable(true)
        }

        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.enableVideo()


        // render local video
        mFaceUnityApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)


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
            mFaceUnityApi.setBeautyPreset(if (enable) BeautyPreset.DEFAULT else BeautyPreset.CUSTOM)
        }
        mBinding.ctvMarkup.setOnClickListener {
            val enable = !mBinding.ctvMarkup.isChecked
            mBinding.ctvMarkup.isChecked = enable
            if (enable) {
                val makeup =
                    SimpleMakeup(FUBundleData("graphics" + File.separator + "face_makeup.bundle"))
                makeup.setCombinedConfig(FUBundleData("makeup/naicha.bundle"))
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
                val prop: Prop = Sticker(FUBundleData("sticker/fashi.bundle"))
                fuRenderKit.propContainer.replaceProp(null, prop)
            } else {
                fuRenderKit.propContainer.removeAllProp()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mFaceUnityApi.release()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }



}