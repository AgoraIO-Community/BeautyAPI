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

package io.agora.beautyapi.bytedance

import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLES30
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants
import com.effectsar.labcv.effectsdk.RenderManager
import io.agora.base.TextureBufferHelper
import io.agora.base.VideoFrame
import io.agora.base.VideoFrame.I420Buffer
import io.agora.base.VideoFrame.TextureBuffer
import io.agora.base.internal.video.RendererCommon
import io.agora.base.internal.video.YuvHelper
import io.agora.beautyapi.bytedance.utils.APIReporter
import io.agora.beautyapi.bytedance.utils.APIType
import io.agora.beautyapi.bytedance.utils.AgoraImageHelper
import io.agora.beautyapi.bytedance.utils.ImageUtil
import io.agora.beautyapi.bytedance.utils.LogUtils
import io.agora.beautyapi.bytedance.utils.StatsHelper
import io.agora.rtc2.Constants
import io.agora.rtc2.gl.EglBaseProvider
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import java.nio.ByteBuffer
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class ByteDanceBeautyAPIImpl : ByteDanceBeautyAPI, IVideoFrameObserver {
    private val TAG = "ByteDanceBeautyAPIImpl"

    /**
     * Beauty mode
     * 美颜模式
     * 0: Automatically switch based on buffer type,
     *    根据缓冲类型自动切换，
     * 1: Fixed use of OES texture,
     *    固定使用 OES 纹理，
     * 2: Fixed use of i420,
     *    固定使用 I420 格式，
     */
    private var beautyMode = 0


    private var textureBufferHelper: TextureBufferHelper? = null
    private var imageUtils: ImageUtil? = null
    private var agoraImageHelper: AgoraImageHelper? = null
    private var nv21ByteBuffer: ByteBuffer? = null
    private var config: Config? = null
    private var enable: Boolean = false
    private var isReleased: Boolean = false
    private var captureMirror = true
    private var renderMirror = true
    private var statsHelper: StatsHelper? = null
    private var skipFrame = 0
    private val workerThreadExecutor = Executors.newSingleThreadExecutor()
    private var currBeautyProcessType = BeautyProcessType.UNKNOWN
    private var isFrontCamera = true
    private var cameraConfig = CameraConfig()
    private var localVideoRenderMode = Constants.RENDER_MODE_HIDDEN
    private val pendingProcessRunList = Collections.synchronizedList(mutableListOf<()->Unit>())
    private var frameWidth = 0
    private var frameHeight = 0
    private val apiReporter by lazy {
        APIReporter(APIType.BEAUTY, VERSION, config!!.rtcEngine)
    }

    private enum class BeautyProcessType{
        UNKNOWN, TEXTURE_OES, TEXTURE_2D, I420
    }

    /**
     * Initializes the API.
     * 初始化 API。
     *
     * @param config Configuration parameters
     *               配置参数
     * @return [ErrorCode] corresponding to the result of initialization
     *                     对应初始化结果的错误代码
     */
    override fun initialize(config: Config): Int {
        if (this.config != null) {
            LogUtils.e(TAG, "initialize >> The beauty api has been initialized!")
            return ErrorCode.ERROR_HAS_INITIALIZED.value
        }
        this.config = config
        this.cameraConfig = config.cameraConfig
        if (config.captureMode == CaptureMode.Agora) {
            config.rtcEngine.registerVideoFrameObserver(this)
        }
        statsHelper = StatsHelper(config.statsDuration) {
            this.config?.eventCallback?.onBeautyStats?.invoke(it)
        }
        LogUtils.i(TAG, "initialize >> config = $config")
        LogUtils.i(TAG, "initialize >> beauty api version=$VERSION, beauty sdk version=${RenderManager.getSDKVersion()}")
        apiReporter.reportFuncEvent(
            "initialize",
            mapOf(
                "captureMode" to config.captureMode,
                "statsDuration" to config.statsDuration,
                "statsEnable" to config.statsEnable,
                "cameraConfig" to config.cameraConfig,
            ),
            emptyMap()
        )
        apiReporter.startDurationEvent("initialize-release")
        return ErrorCode.ERROR_OK.value
    }

    /**
     * Enable/Disable beauty effects.
     * 启用/禁用美颜效果。
     *
     * @param enable true: Enable; false: Disable
     *               true: 启用；false: 禁用
     * @return [ErrorCode] corresponding to the result of the operation
     *                     对应操作结果的错误代码
     */
    override fun enable(enable: Boolean): Int {
        LogUtils.i(TAG, "enable >> enable = $enable")
        if (config == null) {
            LogUtils.e(TAG, "enable >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "enable >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (config?.captureMode == CaptureMode.Custom) {
            skipFrame = 2
            LogUtils.i(TAG, "enable >> skipFrame = $skipFrame")
        }
        this.enable = enable
        apiReporter.reportFuncEvent(
            "enable",
            mapOf("enable" to enable),
            emptyMap()
        )
        return ErrorCode.ERROR_OK.value
    }

    /**
     * Sets up local video rendering, with internal handling of mirror mode.
     * 设置本地视频渲染，内部处理镜像模式。
     *
     * @param view SurfaceView or TextureView for rendering the video
     *             用于渲染视频的 SurfaceView 或 TextureView
     * @param renderMode Scaling mode for rendering (e.g., Constants.RENDER_MODE_HIDDEN)
     *                   渲染的缩放模式（例如，Constants.RENDER_MODE_HIDDEN）
     * @return [ErrorCode] corresponding to the result of the operation
     *         对应操作结果的错误代码
     */
    override fun setupLocalVideo(view: View, renderMode: Int): Int {
        val rtcEngine = config?.rtcEngine
        if(rtcEngine == null){
            LogUtils.e(TAG, "setupLocalVideo >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        LogUtils.i(TAG, "setupLocalVideo >> view=$view, renderMode=$renderMode")
        apiReporter.reportFuncEvent(
            "setupLocalVideo",
            mapOf("view" to view, "renderMode" to renderMode),
            emptyMap()
        )
        if (view is TextureView || view is SurfaceView) {
            val canvas = VideoCanvas(view, renderMode, 0)
            canvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
            rtcEngine.setupLocalVideo(canvas)
            return ErrorCode.ERROR_OK.value
        }
        return ErrorCode.ERROR_VIEW_TYPE_ERROR.value
    }

    /**
     * When ProcessMode == [CaptureMode.Custom], external input of raw video frames is required.
     * 当处理模式为 [CaptureMode.Custom] 时，需要外部输入原始视频帧。
     *
     * @param videoFrame The raw video frame
     *                   原始视频帧
     * @return [ErrorCode] corresponding to the result of the operation
     *                     对应操作结果的错误代码
     */
    override fun onFrame(videoFrame: VideoFrame): Int {
        val conf = config
        if (conf == null) {
            LogUtils.e(TAG, "onFrame >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "onFrame >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (conf.captureMode != CaptureMode.Custom) {
            LogUtils.e(TAG, "onFrame >> The capture mode is not Custom!")
            return ErrorCode.ERROR_PROCESS_NOT_CUSTOM.value
        }
        if (processBeauty(videoFrame)) {
            return ErrorCode.ERROR_OK.value
        }
        LogUtils.i(TAG, "onFrame >> Skip Frame.")
        return ErrorCode.ERROR_FRAME_SKIPPED.value
    }

    /**
     * Sets the best default beauty parameters provided by Agora.
     * 设置 Agora 提供的最佳默认美颜参数。
     *
     * @param preset The beauty preset, defaulting to [BeautyPreset.DEFAULT]
     *               美颜预设，默认为 [BeautyPreset.DEFAULT]
     * @return [ErrorCode] corresponding to the result of the operation
     *                     对应操作结果的错误代码
     */
    override fun setBeautyPreset(
        preset: BeautyPreset,
        beautyNodePath: String,
        beauty4ItemNodePath: String,
        reSharpNodePath: String
    ): Int {
        val conf = config
        if(conf == null){
            LogUtils.e(TAG, "setBeautyPreset >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "setBeautyPreset >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        val initialized = textureBufferHelper != null
        if(!initialized){
            runOnProcessThread {
                setBeautyPreset(preset, beautyNodePath, beauty4ItemNodePath, reSharpNodePath)
            }
            return ErrorCode.ERROR_OK.value
        }

        LogUtils.i(TAG, "setBeautyPreset >> preset = $preset")
        apiReporter.reportFuncEvent(
            "setBeautyPreset",
            mapOf(
                "preset" to preset,
                "beautyNodePath" to beautyNodePath,
                "beauty4ItemNodePath" to beauty4ItemNodePath,
                "reSharpNodePath" to reSharpNodePath
            ),
            emptyMap())

        runOnProcessThread {
            val renderManager =
                config?.renderManager ?: return@runOnProcessThread

            val enable = preset == BeautyPreset.DEFAULT
            // Smooth skin
            renderManager.updateComposerNodes(
                beautyNodePath,
                "smooth",
                if (enable) 0.3f else 0f
            )
            // Whitening
            renderManager.updateComposerNodes(
                beautyNodePath,
                "whiten",
                if (enable) 0.5f else 0f
            )
            // Slim face
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Overall",
                if (enable) 0.15f else 0f
            )
            // Slim cheekbones
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Zoom_Cheekbone",
                if (enable) 0.3f else 0f
            )
            // Jawbone
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Zoom_Jawbone",
                if (enable) 0.46f else 0f
            )
            // Enlarged eyes
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Eye",
                if (enable) 0.15f else 0f
            )
            // White teeth
            renderManager.updateComposerNodes(
                beauty4ItemNodePath,
                "BEF_BEAUTY_WHITEN_TEETH",
                if (enable) 0.2f else 0f
            )
            // Hairline height
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Forehead",
                if (enable) 0.4f else 0f
            )
            // Slim nose
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Nose",
                if (enable) 0.15f else 0f
            )
            // Mouth shape
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_ZoomMouth",
                if (enable) 0.16f else 0f
            )
            // Chin length
            renderManager.updateComposerNodes(
                reSharpNodePath,
                "Internal_Deform_Chin",
                if (enable) 0.46f else 0f
            )
        }
        return ErrorCode.ERROR_OK.value
    }

    /**
     * Private parameter configuration for internal API calls, primarily for testing.
     * 内部 API 调用的私有参数配置，主要用于测试。
     *
     * @param key The parameter key.
     *            参数键。
     * @param value The parameter value.
     *              参数值。
     */
    override fun setParameters(key: String, value: String) {
        apiReporter.reportFuncEvent("setParameters", mapOf("key" to key, "value" to value), emptyMap())
        when (key) {
            "beauty_mode" -> beautyMode = value.toInt()
        }
    }

    /**
     * Executes an operation within the processing thread.
     * 在处理线程中执行操作。
     *
     * @param run The operation to execute.
     *            要执行的操作。
     */
    override fun runOnProcessThread(run: () -> Unit) {
        if (config == null) {
            LogUtils.e(TAG, "runOnProcessThread >> The beauty api has not been initialized!")
            return
        }
        if (isReleased) {
            LogUtils.e(TAG, "runOnProcessThread >> The beauty api has been released!")
            return
        }
        if (textureBufferHelper?.handler?.looper?.thread == Thread.currentThread()) {
            run.invoke()
        } else if (textureBufferHelper != null) {
            textureBufferHelper?.handler?.post(run)
        } else {
            pendingProcessRunList.add(run)
        }
    }

    /**
     * Updates the camera configuration.
     * 设置 Agora 提供的最佳默认美颜参数。
     *
     * @param config New camera configuration to apply
     *               新的相机配置已应用
     * @return [ErrorCode] corresponding to the result of the operation
     *                     对应操作结果的错误代码
     */
    override fun updateCameraConfig(config: CameraConfig): Int {
        LogUtils.i(TAG, "updateCameraConfig >> oldCameraConfig=$cameraConfig, newCameraConfig=$config")
        cameraConfig = CameraConfig(config.frontMirror, config.backMirror)
        apiReporter.reportFuncEvent(
            "updateCameraConfig",
            mapOf("config" to config),
            emptyMap()
        )

        return ErrorCode.ERROR_OK.value
    }

    /**
     * Checks if the current camera is the front camera.
     * 检查当前摄像头是否为前置摄像头。
     *
     * Note: This returns an accurate value only during beauty processing; otherwise, it will always return true.
     * 注意：此值仅在美颜处理期间返回准确值；否则，它将始终返回 true。
     *
     * @return true if the current camera is the front camera, false otherwise
     *         如果当前摄像头是前置摄像头，则返回 true，否则返回 false
     */
    override fun isFrontCamera() = isFrontCamera

    /**
     * Releases resources. Once released, this instance can no longer be used.
     * 释放资源。一旦释放，该实例将无法再使用。
     *
     * @return Refer to ErrorCode
     *         参考 ErrorCode
     */
    override fun release(): Int {
        val conf = config
        if(conf == null){
            LogUtils.e(TAG, "release >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "setBeautyPreset >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (conf.captureMode == CaptureMode.Agora) {
            conf.rtcEngine.registerVideoFrameObserver(null)
        }
        LogUtils.i(TAG, "release")
        apiReporter.reportFuncEvent("release", emptyMap(), emptyMap())
        apiReporter.endDurationEvent("initialize-release", emptyMap())
        isReleased = true
        workerThreadExecutor.shutdown()
        textureBufferHelper?.let {
            textureBufferHelper = null
            it.handler.removeCallbacksAndMessages(null)
            it.invoke {
                imageUtils?.release()
                agoraImageHelper?.release()
                imageUtils = null
                agoraImageHelper = null
                config?.eventCallback?.onEffectDestroyed?.invoke()
                null
            }
            it.dispose()
        }
        statsHelper?.reset()
        statsHelper = null
        pendingProcessRunList.clear()
        return ErrorCode.ERROR_OK.value
    }

    /**
     * Processes the beauty effects on the given video frame.
     * 在给定的视频帧上处理美颜效果。
     *
     * @param videoFrame The video frame to process.
     *                   要处理的视频帧。
     * @return true if processing was successful, false otherwise.
     *         如果处理成功则返回 true，否则返回 false。
     */
    private fun processBeauty(videoFrame: VideoFrame): Boolean {
        if (isReleased) {
            LogUtils.e(TAG, "processBeauty >> The beauty api has been released!")
            return false
        }

        val cMirror =
            if (isFrontCamera) {
                when (cameraConfig.frontMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> true
                    MirrorMode.MIRROR_LOCAL_ONLY -> false
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            } else {
                when (cameraConfig.backMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> true
                    MirrorMode.MIRROR_LOCAL_ONLY -> false
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            }
        val rMirror =
            if (isFrontCamera) {
                when (cameraConfig.frontMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> false
                    MirrorMode.MIRROR_LOCAL_ONLY -> true
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            } else {
                when (cameraConfig.backMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> false
                    MirrorMode.MIRROR_LOCAL_ONLY -> true
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            }
        if (captureMirror != cMirror || renderMirror != rMirror) {
            LogUtils.w(TAG, "processBeauty >> enable=$enable, captureMirror=$captureMirror->$cMirror, renderMirror=$renderMirror->$rMirror")
            captureMirror = cMirror
            if(renderMirror != rMirror){
                renderMirror = rMirror
                config?.rtcEngine?.setLocalRenderMode(
                    localVideoRenderMode,
                    if(renderMirror) Constants.VIDEO_MIRROR_MODE_ENABLED else Constants.VIDEO_MIRROR_MODE_DISABLED
                )
            }
            textureBufferHelper?.invoke {
                skipFrame = 2
                imageUtils?.release()
            }
            apiReporter.startDurationEvent("first_beauty_frame")
            return false
        }

        val oldIsFrontCamera = isFrontCamera
        isFrontCamera = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
        if(oldIsFrontCamera != isFrontCamera){
            LogUtils.w(TAG, "processBeauty >> oldIsFrontCamera=$oldIsFrontCamera, isFrontCamera=$isFrontCamera")
            return false
        }

        val oldFrameWidth = frameWidth
        val oldFrameHeight = frameHeight
        frameWidth = videoFrame.rotatedWidth
        frameHeight = videoFrame.rotatedHeight
        if (oldFrameWidth > 0 || oldFrameHeight > 0) {
            if(oldFrameWidth != frameWidth || oldFrameHeight != frameHeight){
                skipFrame = 2
                return false
            }
        }

        if(!enable){
            return true
        }

        if (textureBufferHelper == null) {
            textureBufferHelper = TextureBufferHelper.create(
                "ByteDanceRender",
                EglBaseProvider.instance().rootEglBase.eglBaseContext
            )
            textureBufferHelper?.invoke {
                imageUtils = ImageUtil()
                agoraImageHelper = AgoraImageHelper()
                config?.eventCallback?.onEffectInitialized?.invoke()
                synchronized(pendingProcessRunList){
                    val iterator = pendingProcessRunList.iterator()
                    while (iterator.hasNext()){
                        iterator.next().invoke()
                        iterator.remove()
                    }
                }
            }
            LogUtils.i(TAG, "processBeauty >> create texture buffer, beautyMode=$beautyMode")
        }

        val startTime = System.currentTimeMillis()

        val processTexId = when (beautyMode) {
            1 -> processBeautySingleTexture(videoFrame)
            2 -> processBeautySingleBuffer(videoFrame)
            else -> processBeautyAuto(videoFrame)
        }
        if (config?.statsEnable == true) {
            val costTime = System.currentTimeMillis() - startTime
            statsHelper?.once(costTime)
        }

        if (processTexId < 0) {
            LogUtils.w(TAG, "processBeauty >> processTexId < 0")
            return false
        }

        if (skipFrame > 0) {
            skipFrame--
            return false
        }

        apiReporter.endDurationEvent("first_beauty_frame", emptyMap())

        val newFence = textureBufferHelper?.invoke {
            val texBuffer = videoFrame.buffer as? TextureBuffer ?: return@invoke 0L
            val fenceOpen = GLES30.glIsSync(texBuffer.fenceObject)
            if (fenceOpen) {
                val glFenceSync = GLES30.glFenceSync(GLES30.GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
                GLES20.glFlush()
                return@invoke glFenceSync
            }
            GLES20.glFinish()
            return@invoke 0L
        } ?: 0L

        val processBuffer: TextureBuffer = textureBufferHelper?.wrapTextureBuffer(
            videoFrame.rotatedWidth,
            videoFrame.rotatedHeight,
            TextureBuffer.Type.RGB,
            processTexId,
            newFence,
            Matrix().apply {
                preTranslate(0.5f, 0.5f)
                preScale(1.0f, -1.0f)
                preTranslate(-0.5f, -0.5f)
            }
        ) ?: return false
        videoFrame.replaceBuffer(processBuffer, 0, videoFrame.timestampNs)
        return true
    }

    /**
     * Automatically processes beauty effects based on the video frame.
     * 根据视频帧自动处理美颜效果。
     *
     * @param videoFrame The video frame to process.
     *                   要处理的视频帧。
     * @return The texture ID of the processed frame.
     *         处理后帧的纹理 ID。
     */
    private fun processBeautyAuto(videoFrame: VideoFrame): Int {
        val buffer = videoFrame.buffer
        return if (buffer is TextureBuffer) {
            processBeautySingleTexture(videoFrame)
        } else {
            processBeautySingleBuffer(videoFrame)
        }
    }

    /**
     * Processes a single texture for beauty effects.
     * 处理单个纹理以应用美颜效果。
     *
     * @param videoFrame The video frame containing the texture to process.
     *                   包含要处理的纹理的视频帧。
     * @return The texture ID of the processed frame.
     *         处理后帧的纹理 ID。
     */
    private fun processBeautySingleTexture(videoFrame: VideoFrame): Int {
        val texBufferHelper = textureBufferHelper ?: return -1
        val imageUtils = imageUtils ?: return -1
        val agoraImageHelper = agoraImageHelper ?: return -1
        val buffer = videoFrame.buffer as? TextureBuffer ?: return -1
        val isFront = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera

        when(buffer.type){
            TextureBuffer.Type.OES -> {
                if(currBeautyProcessType != BeautyProcessType.TEXTURE_OES){
                    LogUtils.i(TAG, "processBeauty >> process source type change old=$currBeautyProcessType, new=${BeautyProcessType.TEXTURE_OES}")
                    currBeautyProcessType = BeautyProcessType.TEXTURE_OES
                    return -1
                }
            }
            else -> {
                if(currBeautyProcessType != BeautyProcessType.TEXTURE_2D){
                    LogUtils.i(TAG, "processBeauty >> process source type change old=$currBeautyProcessType, new=${BeautyProcessType.TEXTURE_2D}")
                    currBeautyProcessType = BeautyProcessType.TEXTURE_2D
                    return -1
                }
            }
        }

        return texBufferHelper.invoke(Callable {
            val renderManager = config?.renderManager ?: return@Callable -1
            var mirror = isFront
            if((isFrontCamera && !captureMirror) || (!isFrontCamera && captureMirror)){
                mirror = !mirror
            }

            val width = videoFrame.rotatedWidth
            val height = videoFrame.rotatedHeight

            val renderMatrix = Matrix()
            renderMatrix.preTranslate(0.5f, 0.5f)
            renderMatrix.preRotate(videoFrame.rotation.toFloat())
            renderMatrix.preScale(if (mirror) -1.0f else 1.0f, -1.0f)
            renderMatrix.preTranslate(-0.5f, -0.5f)
            val finalMatrix = Matrix(buffer.transformMatrix)
            finalMatrix.preConcat(renderMatrix)

            val transform =
                RendererCommon.convertMatrixFromAndroidGraphicsMatrix(finalMatrix)


            val dstTexture = imageUtils.prepareTexture(width, height)
            val srcTexture = agoraImageHelper.transformTexture(
                buffer.textureId,
                buffer.type,
                width,
                height,
                transform
            )
            renderManager.setCameraPostion(isFront)
            val success = renderManager.processTexture(
                srcTexture,
                dstTexture,
                width,
                height,
                EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0,
                videoFrame.timestampNs
            )
            if (!success) {
                return@Callable srcTexture
            }
            return@Callable dstTexture
        })
    }

    /**
     * Processes a single buffer for beauty effects.
     * 处理单个缓冲区以应用美颜效果。
     *
     * @param videoFrame The video frame containing the buffer to process.
     *                   包含要处理的缓冲区的视频帧。
     * @return The texture ID of the processed frame.
     *         处理后帧的纹理 ID。
     */
    private fun processBeautySingleBuffer(videoFrame: VideoFrame): Int {
        val texBufferHelper = textureBufferHelper ?: return -1
        val imageUtils = imageUtils ?: return -1
        val nv21Buffer = getNV21Buffer(videoFrame) ?: return -1
        val isFront = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera

        if (currBeautyProcessType != BeautyProcessType.I420) {
            LogUtils.i(TAG, "processBeauty >> process source type change old=$currBeautyProcessType, new=${BeautyProcessType.I420}")
            currBeautyProcessType = BeautyProcessType.I420
            return -1
        }

        return texBufferHelper.invoke(Callable {
            val renderManager = config?.renderManager ?: return@Callable -1

            val width = videoFrame.rotatedWidth
            val height = videoFrame.rotatedHeight

            val ySize = width * height
            val yBuffer = ByteBuffer.allocateDirect(ySize)
            yBuffer.put(nv21Buffer, 0, ySize)
            yBuffer.position(0)
            val vuBuffer = ByteBuffer.allocateDirect(ySize / 2)
            vuBuffer.put(nv21Buffer, ySize, ySize / 2)
            vuBuffer.position(0)

            var mirror = isFront
            if((isFrontCamera && !captureMirror) || (!isFrontCamera && captureMirror)){
                mirror = !mirror
            }
            val isScreenLandscape = videoFrame.rotation % 180 == 0
            val dstTexture = imageUtils.prepareTexture(width, height)
            val srcTexture = imageUtils.transferYUVToTexture(
                yBuffer,
                vuBuffer,
                if (isScreenLandscape) width else height,
                if (isScreenLandscape) height else width,
                ImageUtil.Transition().apply {
                    rotate(videoFrame.rotation.toFloat())
                    flip(
                        if (isScreenLandscape) mirror else false,
                        if (isScreenLandscape) false else mirror
                    )
                }
            )
            renderManager.setCameraPostion(isFront)
            val success = renderManager.processTexture(
                srcTexture,
                dstTexture,
                width,
                height,
                EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0,
                videoFrame.timestampNs
            )
            return@Callable if (success) {
                dstTexture
            } else {
                srcTexture
            }
        })
    }

    /**
     * Retrieves the NV21 buffer from the given video frame.
     * 从给定的视频帧中获取 NV21 缓冲区。
     *
     * @param videoFrame The video frame containing the buffer.
     *                   包含缓冲区的视频帧。
     * @return ByteArray The NV21 buffer as a byte array, or null if it cannot be retrieved.
     *                    NV21 缓冲区的字节数组，如果无法获取则返回 null。
     */
    private fun getNV21Buffer(videoFrame: VideoFrame): ByteArray? {
        val buffer = videoFrame.buffer
        val i420Buffer = buffer as? I420Buffer ?: buffer.toI420()
        val width = i420Buffer.width
        val height = i420Buffer.height
        val nv21Size = (width * height * 3.0f / 2.0f + 0.5f).toInt()
        if (nv21ByteBuffer == null || nv21ByteBuffer?.capacity() != nv21Size) {
            nv21ByteBuffer?.clear()
            nv21ByteBuffer = ByteBuffer.allocateDirect(nv21Size)
            return null
        }
        val nv21ByteArray = ByteArray(nv21Size)

        YuvHelper.I420ToNV12(
            i420Buffer.dataY, i420Buffer.strideY,
            i420Buffer.dataV, i420Buffer.strideV,
            i420Buffer.dataU, i420Buffer.strideU,
            nv21ByteBuffer, width, height
        )
        nv21ByteBuffer?.position(0)
        nv21ByteBuffer?.get(nv21ByteArray)
        if (buffer !is I420Buffer) {
            i420Buffer.release()
        }
        return nv21ByteArray
    }

    // IVideoFrameObserver implements

    /**
     * Callback when a video frame is captured.
     * 采集视频帧时回调。
     *
     * @param sourceType The source type of the video frame.
     *                   视频帧的源类型。
     * @param videoFrame The captured video frame.
     *                   采集的视频帧。
     * @return true if the frame was processed successfully, false otherwise.
     *         如果帧处理成功则返回 true，否则返回 false。
     */
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

    /**
     * Retrieves the current mirror status.
     * 获取当前镜像状态。
     *
     * @return true if mirroring is applied, false if it is not.
     *         如果应用了镜像，则返回 true；否则返回 false。
     */
    override fun getMirrorApplied() = captureMirror && !enable

    override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

}