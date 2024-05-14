/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.beautyapi.demo.module.faceunity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.faceunity.core.faceunity.FURenderKit
import io.agora.base.VideoFrame
import io.agora.beautyapi.demo.BuildConfig
import io.agora.beautyapi.demo.R
import io.agora.beautyapi.demo.databinding.BeautyActivityBinding
import io.agora.beautyapi.demo.utils.ReflectUtils
import io.agora.beautyapi.demo.widget.BottomAlertDialog
import io.agora.beautyapi.demo.widget.SettingsDialog
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

class FaceUnityActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName

    companion object {
        private const val EXTRA_CHANNEL_NAME = "ChannelName"
        private const val EXTRA_RESOLUTION = "Resolution"
        private const val EXTRA_FRAME_RATE = "FrameRate"
        private const val EXTRA_CAPTURE_MODE = "CaptureMode"
        private const val EXTRA_PROCESS_MODE = "ProcessMode"
        private const val EXTRA_ENABLE_FENCE = "EnableFence"

        fun launch(
            context: Context,
            channelName: String,
            resolution: String,
            frameRate: String,
            captureMode: String,
            processMode: String,
            enableFence: Boolean
        ) {
            Intent(context, FaceUnityActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                putExtra(EXTRA_RESOLUTION, resolution)
                putExtra(EXTRA_FRAME_RATE, frameRate)
                putExtra(EXTRA_CAPTURE_MODE, captureMode)
                putExtra(EXTRA_PROCESS_MODE, processMode)
                putExtra(EXTRA_ENABLE_FENCE, enableFence)
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
    private var beautyEnable = true
    private val mSettingDialog by lazy {
        SettingsDialog(this).apply {
            setBeautyEnable(beautyEnable)
            setOnBeautyChangeListener { enable ->
                beautyEnable = enable
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
            setTextureAsyncChecked(false)
            setOnTextureAsyncChangeListener { enable ->
                if (enable) {
                    mFaceUnityApi.setParameters("enableTextureAsync", "true")
                } else {
                    mFaceUnityApi.setParameters("enableTextureAsync", "false")
                }
            }
            setResolutionSelect(intent.getStringExtra(EXTRA_RESOLUTION) ?: "")
            setOnResolutionChangeListener {resolution ->
                mVideoEncoderConfiguration.dimensions = ReflectUtils.getStaticFiledValue(
                    VideoEncoderConfiguration::class.java, resolution
                )
                mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
            }
            setFrameRateSelect(intent.getStringExtra(EXTRA_FRAME_RATE) ?: "")
            setOnFrameRateChangeListener { frameRate ->
                mVideoEncoderConfiguration.frameRate = ReflectUtils.getStaticFiledValue(
                    VideoEncoderConfiguration::class.java, frameRate
                ) ?: 15
                mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
            }
        }
    }
    private var cameraConfig = CameraConfig()
    private val isCustomCaptureMode by lazy {
        intent.getStringExtra(EXTRA_CAPTURE_MODE) == getString(R.string.beauty_capture_custom)
    }

    private val mBeautyDialog by lazy {
        BottomAlertDialog(this@FaceUnityActivity).apply {
            val view = FaceUnityControllerView(this@FaceUnityActivity)
            view.beautyOpenClickListener = View.OnClickListener {
                beautyEnable = !beautyEnable
                mFaceUnityApi.enable(beautyEnable)
            }
            setContentView(view)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true

        if (!FaceUnityBeautySDK.isAuthSuccess()) {
            AlertDialog.Builder(this).apply {
                setTitle("Auth Failed")
                setMessage("Please check your license file")
                setCancelable(false)
                setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }


        initRtcEngine()
        initBeautyApi()
        initView()
    }

    private fun initRtcEngine() {
        // Config RtcEngine
        mRtcEngine.addHandler(mRtcHandler)
        mRtcEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration)
        mRtcEngine.setParameters("{\"che.video.observer_texture.copy_enable\": ${intent.getBooleanExtra(EXTRA_ENABLE_FENCE, false)}}")
        mRtcEngine.setParameters("{\"che.video.enable_gl_fence\": ${intent.getBooleanExtra(EXTRA_ENABLE_FENCE, false)}}")
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
    }

    private fun initView() {
        mBinding.ivCamera.setOnClickListener {
            mRtcEngine.switchCamera()
        }
        mBinding.ivSetting.setOnClickListener {
            mSettingDialog.show()
        }
        mBinding.ivBeauty.setOnClickListener {
            mBeautyDialog.show()
        }
        mBinding.ivMirror.setOnClickListener {
            val isFront = mFaceUnityApi.isFrontCamera()
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
            mFaceUnityApi.updateCameraConfig(cameraConfig)
        }
    }

    private fun initBeautyApi() {
        FaceUnityBeautySDK.beautyConfig.reset()
        mFaceUnityApi.initialize(
            Config(
                applicationContext,
                mRtcEngine,
                FURenderKit.getInstance(),
                captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
                cameraConfig = this.cameraConfig,
                statsEnable = true,
                eventCallback = object : IEventCallback {
                    override fun onBeautyStats(stats: BeautyStats) {
                        Log.d(TAG, "BeautyStats stats = $stats")
                    }
                }
            )
        )
        FaceUnityBeautySDK.setBeautyAPI(mFaceUnityApi)
        when (intent.getStringExtra(EXTRA_PROCESS_MODE)) {
            getString(R.string.beauty_process_auto) -> mFaceUnityApi.setParameters(
                "beauty_mode",
                "0"
            )

            getString(R.string.beauty_process_texture) -> mFaceUnityApi.setParameters(
                "beauty_mode",
                "3"
            )

            getString(R.string.beauty_process_i420) -> mFaceUnityApi.setParameters(
                "beauty_mode",
                "2"
            )
        }
        if (isCustomCaptureMode) {
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

                override fun getVideoFrameProcessMode() =
                    IVideoFrameObserver.PROCESS_MODE_READ_WRITE

                override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

                override fun getRotationApplied() = false

                override fun getMirrorApplied() = mFaceUnityApi.getMirrorApplied()

                override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER
            })
        }
        mFaceUnityApi.enable(beautyEnable)

        // render local video
        mFaceUnityApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_HIDDEN)
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
        FaceUnityBeautySDK.setBeautyAPI(null)
        mFaceUnityApi.release()
        RtcEngine.destroy()
    }

}