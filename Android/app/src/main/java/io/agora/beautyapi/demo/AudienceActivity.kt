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

package io.agora.beautyapi.demo

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.beautyapi.demo.databinding.AudienceActivityBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class AudienceActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_CHANNEL_NAME = "ChannelName"

        fun launch(
            context: Context,
            channelName: String
        ) {
            Intent(context, AudienceActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_NAME, channelName)
                context.startActivity(this)
            }
        }
    }

    private val mBinding by lazy {
        AudienceActivityBinding.inflate(LayoutInflater.from(this))
    }
    private val mChannelName by lazy {
        intent.getStringExtra(EXTRA_CHANNEL_NAME)
    }
    private var mRtcEngine: RtcEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        window.decorView.keepScreenOn = true

        val config = RtcEngineConfig()
        config.mContext = applicationContext
        config.mAppId = BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                runOnUiThread {
                    if (mBinding.fullVideoView.tag == null) {
                        mBinding.fullVideoView.tag = uid
                        mBinding.fullVideoView.addView(SurfaceView(this@AudienceActivity).apply {
                            mRtcEngine?.setupRemoteVideo(
                                VideoCanvas(
                                    this,
                                    Constants.RENDER_MODE_FIT,
                                    uid
                                )
                            )
                        })
                    } else if (mBinding.remoteVideoView.tag == null) {
                        mBinding.remoteVideoView.tag = uid
                        mBinding.remoteVideoView.addView(SurfaceView(this@AudienceActivity).apply {
                            mRtcEngine?.setupRemoteVideo(
                                VideoCanvas(
                                    this,
                                    Constants.RENDER_MODE_FIT,
                                    uid
                                )
                            )
                        })
                    }
                }


            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                runOnUiThread {
                    if (mBinding.fullVideoView.tag == uid) {
                        mBinding.fullVideoView.tag = null
                        mBinding.fullVideoView.removeAllViews()
                        if (mBinding.remoteVideoView.childCount > 0) {
                            val remoteView = mBinding.remoteVideoView.getChildAt(0)
                            mBinding.remoteVideoView.removeAllViews()
                            mBinding.fullVideoView.addView(remoteView)
                            mBinding.fullVideoView.tag = mBinding.remoteVideoView.tag
                            mBinding.remoteVideoView.tag = null
                        }
                    } else if (mBinding.remoteVideoView.tag == uid) {
                        mBinding.remoteVideoView.tag = null
                        mBinding.remoteVideoView.removeAllViews()
                    }
                }
            }
        }
        val rtcEngine = RtcEngine.create(config)
        mRtcEngine = rtcEngine

        rtcEngine.enableVideo()

        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        options.autoSubscribeVideo = true
        options.autoSubscribeAudio = true
        rtcEngine.joinChannel("", mChannelName, 0, options)

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
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }


}