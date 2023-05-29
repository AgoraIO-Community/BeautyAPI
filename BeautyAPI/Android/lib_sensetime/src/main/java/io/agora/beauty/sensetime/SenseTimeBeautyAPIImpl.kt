package io.agora.beauty.sensetime

import android.graphics.Matrix
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.sensetime.stmobile.STCommonNative
import com.sensetime.stmobile.params.STEffectBeautyType
import io.agora.base.TextureBufferHelper
import io.agora.base.VideoFrame
import io.agora.base.internal.video.YuvHelper
import io.agora.rtc2.gl.EglBaseProvider
import io.agora.rtc2.video.IVideoFrameObserver
import java.nio.ByteBuffer
import java.util.concurrent.Callable

class SenseTimeBeautyAPIImpl : SenseTimeBeautyAPI, IVideoFrameObserver {

    private var textureBufferHelper: TextureBufferHelper? = null
    private var nv21ByteBuffer: ByteBuffer? = null
    private var config: Config? = null
    private var enable: Boolean = false
    private var isReleased: Boolean = false

    override fun initialize(config: Config): Int {
        if(this.config != null){
            return ErrorCode.ERROR_HAS_INITIALIZED.value
        }
        this.config = config
        if (!config.useCustom) {
            config.rtcEngine.registerVideoFrameObserver(this)
        }
        return ErrorCode.ERROR_OK.value
    }

    override fun enable(enable: Boolean): Int {
        if(config == null){
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if(isReleased){
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        this.enable = enable
        return ErrorCode.ERROR_OK.value
    }

    override fun onFrame(videoFrame: VideoFrame):Int {
        if(config == null){
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if(isReleased){
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if(config?.useCustom != true){
            return ErrorCode.ERROR_PROCESS_NOT_CUSTOM.value
        }
        if (!enable) {
            return ErrorCode.ERROR_PROCESS_DISABLE.value
        }
        processBeauty(videoFrame)
        return ErrorCode.ERROR_OK.value
    }

    override fun setOptimizedDefault():Int {
        if(config == null){
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if(isReleased){
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        val stRenderer = config?.stRenderKit ?: return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        // 锐化
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_TONE_SHARPEN,
            0.5f
        )
        // 清晰度
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_TONE_CLEAR,
            1.0f
        )
        // 磨皮
        stRenderer.setBeautyMode(
            STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
            STEffectBeautyType.SMOOTH2_MODE
        )
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH,
            0.55f
        )
        // 美白
        stRenderer.setBeautyMode(
            STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
            STEffectBeautyType.WHITENING3_MODE
        )
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN,
            0.2f
        )
        // 瘦脸
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE,
            0.4f
        )
        // 大眼
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE,
            0.3f
        )
        // 红润
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN,
            0.0f
        )
        // 瘦颧骨
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE,
            0.0f
        )
        // 下颌骨
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE,
            0.0f
        )
        // 美牙
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH,
            0.0f
        )
        // 额头
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT,
            0.0f
        )
        // 瘦鼻
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE,
            0.0f
        )
        // 嘴形
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE,
            0.0f
        )
        // 下巴
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH,
            0.0f
        )
        // 亮眼
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE,
            0.0f
        )
        // 祛黑眼圈
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES,
            0.0f
        )
        // 祛法令纹
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS,
            0.0f
        )
        // 饱和度
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION,
            0.0f
        )
        // 对比度
        stRenderer.setBeautyStrength(
            STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST,
            0.0f
        )
        return ErrorCode.ERROR_OK.value
    }

    override fun release():Int {
        if(config == null){
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if(isReleased){
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        isReleased = true
        textureBufferHelper?.let {
            textureBufferHelper = null
            it.invoke {
                config?.stRenderKit?.resetProcessor()
                null
            }
            it.dispose()
        }
        return ErrorCode.ERROR_OK.value
    }

    private fun processBeauty(videoFrame: VideoFrame): Boolean {
        if (!enable || isReleased) {
            return true
        }
        val buffer = videoFrame.buffer

        val width = buffer.width
        val height = buffer.height


        // Obtain nv21 pixel data
        val nv21Size = (width * height * 3.0f / 2.0f + 0.5f).toInt()
        if (nv21ByteBuffer == null || nv21ByteBuffer?.capacity() != nv21Size) {
            nv21ByteBuffer?.clear()
            nv21ByteBuffer = ByteBuffer.allocateDirect(nv21Size)
            return false
        }
        val nv21ByteArray = ByteArray(nv21Size)
        val i420Buffer = buffer.toI420()
        YuvHelper.I420ToNV12(
            i420Buffer.dataY, i420Buffer.strideY,
            i420Buffer.dataV, i420Buffer.strideV,
            i420Buffer.dataU, i420Buffer.strideU,
            nv21ByteBuffer, width, height
        )
        nv21ByteBuffer?.position(0)
        nv21ByteBuffer?.get(nv21ByteArray)
        i420Buffer.release()

        if (textureBufferHelper == null) {
            textureBufferHelper = TextureBufferHelper.create(
                "STRender",
                EglBaseProvider.instance().rootEglBase.eglBaseContext
            )
        }

        val processTexId: Int
        if (buffer is VideoFrame.TextureBuffer) {
            val textureFormat =
                if (buffer.type == VideoFrame.TextureBuffer.Type.OES) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D
            processTexId = textureBufferHelper?.invoke(Callable {
                return@Callable config?.stRenderKit?.preProcess(
                    width, height, videoFrame.rotation,
                    nv21ByteArray, STCommonNative.ST_PIX_FMT_NV21,
                    buffer.textureId,
                    textureFormat
                ) ?: -1
            }) ?: -1
        } else {
            processTexId = textureBufferHelper?.invoke(Callable {
                return@Callable config?.stRenderKit?.preProcess(
                    width, height, videoFrame.rotation,
                    nv21ByteArray, STCommonNative.ST_PIX_FMT_NV21,
                ) ?: -1
            }) ?: -1
        }
        if (processTexId < 0) {
            return false
        }

        val processBuffer: VideoFrame.TextureBuffer = textureBufferHelper?.wrapTextureBuffer(
            videoFrame.rotatedWidth, videoFrame.rotatedHeight, VideoFrame.TextureBuffer.Type.RGB, processTexId, Matrix()
        ) ?: return false
        videoFrame.replaceBuffer(processBuffer, 0, videoFrame.timestampNs)
        return true
    }

    // IVideoFrameObserver implements

    override fun onCaptureVideoFrame(sourceType: Int, videoFrame: VideoFrame?): Boolean {
        videoFrame ?: return false
        return processBeauty(videoFrame)
    }

    override fun onPreEncodeVideoFrame(sourceType: Int, videoFrame: VideoFrame?) = false

    override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int) = false

    override fun onRenderVideoFrame(
        channelId: String?,
        uid: Int,
        videoFrame: VideoFrame?
    ) = false

    override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

    override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

    override fun getRotationApplied() = false

    override fun getMirrorApplied() = false

    override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

}