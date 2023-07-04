package com.sensetime.effects.processor

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Camera
import android.opengl.GLES20
import android.os.Build
import android.util.Log
import android.util.Size
import com.sensetime.effects.egl.EGLContextHelper
import com.sensetime.effects.egl.GLCopyHelper
import com.sensetime.effects.egl.GLFrameBuffer
import com.sensetime.effects.utils.Accelerometer
import com.sensetime.effects.utils.Accelerometer.CLOCKWISE_ANGLE
import com.sensetime.effects.utils.CostTimeUtils
import com.sensetime.effects.utils.GlUtil
import com.sensetime.effects.utils.LogUtils
import com.sensetime.effects.utils.QueueBlockThreadExecutor
import com.sensetime.hardwarebuffer.STMobileHardwareBufferNative
import com.sensetime.stmobile.STCommonNative
import com.sensetime.stmobile.STEffectInImage
import com.sensetime.stmobile.STMobileAnimalNative
import com.sensetime.stmobile.STMobileColorConvertNative
import com.sensetime.stmobile.STMobileEffectNative
import com.sensetime.stmobile.STMobileHumanActionNative
import com.sensetime.stmobile.model.STAnimalFace
import com.sensetime.stmobile.model.STAnimalFaceInfo
import com.sensetime.stmobile.model.STEffectCustomParam
import com.sensetime.stmobile.model.STEffectRenderInParam
import com.sensetime.stmobile.model.STEffectRenderOutParam
import com.sensetime.stmobile.model.STEffectTexture
import com.sensetime.stmobile.model.STHumanAction
import com.sensetime.stmobile.model.STImage
import com.sensetime.stmobile.model.STQuaternion
import com.sensetime.stmobile.params.STEffectParam
import com.sensetime.stmobile.params.STRotateType
import com.sensetime.stmobile.sticker_module_types.STCustomEvent
import java.util.concurrent.Callable
import kotlin.math.max

class BeautyProcessor(private val cacheCount: Int = 2) : IBeautyProcessor {
    private val TAG = this::class.java.simpleName
    private val cacheExecutor = QueueBlockThreadExecutor<Int>(cacheCount, 1)
    private val glExecutor = QueueBlockThreadExecutor<OutputInfo>(0, 1)

    private var eglContextHelper: EGLContextHelper? = null
    private var glCopyHelper = GLCopyHelper(cacheCount)
    private val cacheFrameBuffer = arrayOfNulls<GLFrameBuffer>(max(cacheCount, 1))
    private var mAnimalFaceInfo = arrayOfNulls<STAnimalFaceInfo>(max(cacheCount, 1))
    private val inputTextures = arrayOfNulls<Int>(max(cacheCount, 1))
    private val inputByteArrays = arrayOfNulls<ByteArray>(max(cacheCount, 1))
    private var inputGLFrameBuffer: GLFrameBuffer? = null
    private var outTextureIds = arrayOfNulls<Int>(max(cacheCount, 1))
    private var outFrameBuffer: GLFrameBuffer? = null

    private lateinit var mSTMobileEffectNative: STMobileEffectNative
    private lateinit var mSTHumanActionNative: STMobileHumanActionNative
    private var mSTMobileColorConvertNative: STMobileColorConvertNative? = null
    private var mStAnimalNative: STMobileAnimalNative? = null
    private var mSTMobileHardwareBufferNative: STMobileHardwareBufferNative? = null

    private var mAccelerometer: Accelerometer? = null
    private var mCustomEvent = 0
    private var mInputWidth = 0
    private var mInputHeight = 0

    @Volatile
    private var isReleased = false

    override fun initialize(
        effectNative: STMobileEffectNative,
        humanActionNative: STMobileHumanActionNative,
        animalNative: STMobileAnimalNative?,
    ) {
        this.mSTMobileEffectNative = effectNative
        this.mSTHumanActionNative = humanActionNative
        this.mStAnimalNative = animalNative
    }

    override fun release() {
        isReleased = true
        mAccelerometer?.stop()
        mAccelerometer = null
        glExecutor.execute(Callable {
            cacheFrameBuffer.forEachIndexed { index, buffer ->
                buffer?.release()
                cacheFrameBuffer[index] = null
            }
            outTextureIds.forEachIndexed { index, textureId ->
                textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                outTextureIds[index] = null
            }
            inputTextures.forEachIndexed { index, textureId ->
                textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                inputTextures[index] = null
            }
            if (mSTMobileColorConvertNative != null) {
                mSTMobileColorConvertNative?.destroyInstance()
                mSTMobileColorConvertNative = null
            }
            eglContextHelper?.let {
                it.release()
                eglContextHelper = null
            }
            glCopyHelper.release()
            inputGLFrameBuffer?.let {
                it.release()
                inputGLFrameBuffer = null
            }
            mSTMobileHardwareBufferNative?.release()
            mSTMobileHardwareBufferNative = null
            return@Callable null
        })
        glExecutor.shutdown()
        cacheExecutor.shutdown()
    }

    override fun enableSensor(context: Context, enable: Boolean) {
        if (enable) {
            if (mAccelerometer == null) {
                mAccelerometer = Accelerometer(context)
                mAccelerometer?.start()
            }
        } else {
            mAccelerometer?.stop()
            mAccelerometer = null
        }
    }

    override fun triggerScreenTap(isDouble: Boolean) {
        Log.d(
            TAG,
            "changeCustomEvent() called:" + mSTMobileEffectNative.customEventNeeded
        )
        mCustomEvent = mSTMobileEffectNative.customEventNeeded
        mCustomEvent = if (isDouble) {
            (mCustomEvent.toLong() and STCustomEvent.ST_CUSTOM_EVENT_SCREEN_TAP.inv()).toInt()
        } else {
            (mCustomEvent.toLong() and STCustomEvent.ST_CUSTOM_EVENT_SCREEN_DOUBLE_TAP.inv()).toInt()
        }
    }


    override fun process(input: InputInfo): OutputInfo? {
        if (isReleased) {
            return null
        }
        if (eglContextHelper == null) {
            val glContext = GlUtil.getCurrGLContext()
            glExecutor.execute(Callable {
                if (isReleased) {
                    return@Callable null
                }
                if (eglContextHelper == null) {
                    eglContextHelper = EGLContextHelper()
                        .apply {
                            initEGL(glContext)
                            eglMakeCurrent()
                        }
                }
                return@Callable null
            })
        }

        return if (input.bytes != null && input.textureId != null) {
            processDoubleInput(input)
        } else if (input.bytes != null) {
            processSingleBytesInput(input)
        } else if (input.textureId != null && Build.VERSION.SDK_INT >= 26) {
            processSingleTextureInput(input)
        } else {
            null
        }
    }

    @TargetApi(26)
    private fun processSingleTextureInput(input: InputInfo): OutputInfo? {
        if (isReleased) {
            return null
        }
        if (input.textureId == null) {
            return null
        }
        val current = cacheExecutor.current()
        glExecutor.execute(Callable {
            if (isReleased) {
                return@Callable null
            }
            if (mSTMobileHardwareBufferNative == null) {
                mInputWidth = input.width
                mInputHeight = input.height
                mSTMobileHardwareBufferNative = STMobileHardwareBufferNative().apply {
                    init(
                        mInputWidth,
                        mInputHeight,
                        STMobileHardwareBufferNative.HARDWARE_BUFFER_FORMAT_RGBA,
                        STMobileHardwareBufferNative.HARDWARE_BUFFER_USAGE_DOWNLOAD
                    )
                }
                inputByteArrays.forEachIndexed { index, _ ->
                    inputByteArrays[index] = ByteArray(mInputWidth * mInputHeight * 4)
                }
            } else if (mInputWidth != input.width || mInputHeight != input.height) {
                mSTMobileHardwareBufferNative?.release()
                mSTMobileHardwareBufferNative = null
                cacheExecutor.cleanAllTasks()
                outTextureIds.forEachIndexed { index, textureId ->
                    textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                    outTextureIds[index] = null
                }
                return@Callable null
            }
            if (inputGLFrameBuffer == null) {
                inputGLFrameBuffer = GLFrameBuffer(input.textureType)
            } else if (inputGLFrameBuffer?.textureType != input.textureType) {
                inputGLFrameBuffer?.release()
                inputGLFrameBuffer = null
                return@Callable null
            }
            inputGLFrameBuffer?.setSize(mInputWidth, mInputHeight)
            if (input.textureMatrix != null) {
                inputGLFrameBuffer?.setTexMatrix(input.textureMatrix)
                inputGLFrameBuffer?.setFlipV(true)
            }
            inputGLFrameBuffer?.process(input.textureId)

            glCopyHelper.copy2DTextureToOesTexture(
                inputGLFrameBuffer?.textureId ?: return@Callable null,
                mSTMobileHardwareBufferNative?.textureId ?: return@Callable null,
                mInputWidth,
                mInputHeight,
                current
            )

            mSTMobileHardwareBufferNative?.downloadRgbaImage(
                mInputWidth,
                mInputHeight,
                inputByteArrays[current]
            )
            return@Callable null
        })

        return processDoubleInput(
            InputInfo(
                inputByteArrays[current],
                STCommonNative.ST_PIX_FMT_RGBA8888,
                input.textureId,
                input.textureType,
                input.textureMatrix,
                1,
                input.width,
                input.height,
                input.isFrontCamera,
                input.cameraOrientation,
                input.timestamp,
            )
        )
    }

    private fun processSingleBytesInput(input: InputInfo): OutputInfo? {
        if (isReleased) {
            return null
        }
        if (input.bytes == null) {
            return null
        }
        val current = cacheExecutor.current()
        glExecutor.execute(Callable {
            if (isReleased) {
                return@Callable null
            }
            if (mSTMobileColorConvertNative == null) {
                mInputWidth = input.width
                mInputHeight = input.height
                mSTMobileColorConvertNative = STMobileColorConvertNative().apply {
                    createInstance()
                    setTextureSize(mInputWidth, mInputHeight)
                }
            } else if (mInputWidth != input.width || mInputHeight != input.height) {
                mSTMobileColorConvertNative?.destroyInstance()
                mSTMobileColorConvertNative = null
                inputTextures.forEachIndexed { index, textureId ->
                    textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                    inputTextures[index] = null
                }
                cacheExecutor.cleanAllTasks()
                outTextureIds.forEachIndexed { index, textureId ->
                    textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                    outTextureIds[index] = null
                }
                return@Callable null
            }
            var textureId = inputTextures[current]
            if (textureId == null) {
                val texIds = IntArray(1)
                GlUtil.initEffectTexture(input.width, input.height, texIds, GLES20.GL_TEXTURE_2D)
                textureId = texIds[0]
                inputTextures[current] = textureId
            }
            //上传nv21 buffer到纹理
            mSTMobileColorConvertNative?.nv21BufferToRgbaTexture(
                input.width,
                input.height,
                0,
                false,
                input.bytes,
                textureId
            )
            return@Callable null
        })
        return processDoubleInput(
            InputInfo(
                input.bytes,
                input.bytesType,
                inputTextures[current] ?: return null,
                GLES20.GL_TEXTURE_2D,
                input.textureMatrix,
                0,
                input.width,
                input.height,
                input.isFrontCamera,
                input.cameraOrientation,
                input.timestamp,
            )
        )
    }

    private fun processDoubleInput(input: InputInfo): OutputInfo? {
        if (input.bytes == null || input.textureId == null) {
            return null
        }
        val current = cacheExecutor.current()

        if (mInputWidth != input.width || mInputHeight != input.height) {
            mInputWidth = input.width
            mInputHeight = input.height
            cacheExecutor.cleanAllTasks()
            return glExecutor.execute(Callable {
                cacheExecutor
                outTextureIds.forEachIndexed { index, textureId ->
                    textureId?.let { GLES20.glDeleteTextures(1, intArrayOf(it), 0) }
                    outTextureIds[index] = null
                }
                return@Callable null
            })
        }

        val outSize = when (input.cameraOrientation) {
            90, 270 -> Size(input.height, input.width)
            else -> Size(input.width, input.height)
        }
        val renderOrientation = getCurrentOrientation()
        var execute: OutputInfo? = null
        LogUtils.i("processDoubleInput index=$current")
        cacheExecutor.execute(
            beforeSubmit = { executedIndex ->
                if (executedIndex != null && executedIndex >= 0) {
                    execute = glExecutor.execute(Callable {
                        if (isReleased) {
                            return@Callable null
                        }
                        val glFrameBuffer = cacheFrameBuffer[executedIndex] ?: return@Callable null
                        var outTextureId = outTextureIds[executedIndex]
                        if (outTextureId == null) {
                            val texId = IntArray(1)
                            GlUtil.initEffectTexture(
                                outSize.width,
                                outSize.height,
                                texId,
                                GLES20.GL_TEXTURE_2D
                            )
                            outTextureId = texId[0]
                            outTextureIds[executedIndex] = outTextureId
                        }
                        //输入纹理
                        val stEffectTexture =
                            STEffectTexture(
                                glFrameBuffer.textureId,
                                outSize.width,
                                outSize.height,
                                0
                            )
                        //输出纹理，需要在上层初始化
                        val stEffectTextureOut =
                            STEffectTexture(outTextureId, outSize.width, outSize.height, 0)

                        //用户自定义参数设置
                        val event: Int = mCustomEvent
                        val customParam: STEffectCustomParam
                        val sensorEvent = mAccelerometer?.sensorEvent
                        customParam =
                            if (sensorEvent?.values != null && sensorEvent.values.isNotEmpty()) {
                                STEffectCustomParam(
                                    STQuaternion(sensorEvent.values),
                                    input.isFrontCamera,
                                    event
                                )
                            } else {
                                STEffectCustomParam(
                                    STQuaternion(0f, 0f, 0f, 1f),
                                    input.isFrontCamera,
                                    event
                                )
                            }

                        //渲染接口输入参数
                        val sTEffectRenderInParam = STEffectRenderInParam(
                            mSTHumanActionNative.getNativeHumanActionResultCache(executedIndex),
                            mAnimalFaceInfo[executedIndex],
                            renderOrientation,
                            renderOrientation,
                            false,
                            customParam,
                            stEffectTexture,
                            STEffectInImage(
                                STImage(
                                    input.bytes,
                                    input.bytesType,
                                    input.width,
                                    input.height
                                ),
                                input.cameraOrientation,
                                input.isFrontCamera
                            )
                        )
                        //渲染接口输出参数
                        val stEffectRenderOutParam = STEffectRenderOutParam(
                            stEffectTextureOut,
                            null,
                            null
                        )
                        LogUtils.i("processDoubleInput index=$executedIndex render start")
                        val mStartRenderTime = System.currentTimeMillis()
                        mSTMobileEffectNative.setParam(
                            STEffectParam.EFFECT_PARAM_USE_INPUT_TIMESTAMP,
                            1.0f
                        )
                        mSTMobileEffectNative.render(
                            sTEffectRenderInParam,
                            stEffectRenderOutParam,
                            false
                        )
                        LogUtils.i(
                            TAG,
                            "render cost time total: %d",
                            System.currentTimeMillis() - mStartRenderTime
                        )
                        CostTimeUtils.printAverage(
                            "CostTimeUtils",
                            System.currentTimeMillis() - mStartRenderTime
                        )

                        if (event == mCustomEvent) {
                            mCustomEvent = 0
                        }

                        if (outFrameBuffer == null) {
                            outFrameBuffer = GLFrameBuffer(GLES20.GL_TEXTURE_2D)
                        }
                        outFrameBuffer?.setSize(outSize.width, outSize.height)
                        outFrameBuffer?.setFlipV(true)
                        outFrameBuffer?.process(stEffectRenderOutParam.texture?.id ?: 0)


                        LogUtils.i("processDoubleInput index=$executedIndex render end")
                        return@Callable OutputInfo(
                            textureId = outFrameBuffer?.textureId ?: 0,
                            width = outSize.width,
                            height = outSize.height,
                            timestamp = input.timestamp
                        )
                    })
                }
                glExecutor.execute(Callable {
                    var glFrameBuffer = cacheFrameBuffer[current]
                    if (glFrameBuffer == null) {
                        glFrameBuffer = GLFrameBuffer(input.textureType)
                        cacheFrameBuffer[current] = glFrameBuffer
                    } else if (glFrameBuffer.textureType != input.textureType) {
                        glFrameBuffer.release()
                        glFrameBuffer = GLFrameBuffer(input.textureType)
                        cacheFrameBuffer[current] = glFrameBuffer
                    }
                    glFrameBuffer.setSize(outSize.width, outSize.height)
                    glFrameBuffer.setRotation(input.cameraOrientation)
                    if (input.textureMatrix != null) {
                        glFrameBuffer.setTexMatrix(input.textureMatrix)
                        glFrameBuffer.setFlipH(!input.isFrontCamera)
                    } else {
                        glFrameBuffer.setFlipH(input.isFrontCamera)
                    }


                    glFrameBuffer.process(input.textureId)
                    LogUtils.i("processDoubleInput index=$current cache frame buffer")
                    return@Callable null
                })
            },
            task = Callable {
                if (isReleased) {
                    return@Callable -1
                }
                LogUtils.i("processDoubleInput index=$current nativeHumanActionDetect start")
                val orientation: Int =
                    getHumanActionOrientation(input.isFrontCamera, input.cameraOrientation)
                val deviceOrientation: Int =
                    mAccelerometer?.direction ?: CLOCKWISE_ANGLE.Deg90.value
                val startHumanAction = System.currentTimeMillis()
                //Log.e(TAG, "config: "+Long.toHexString(mDetectConfig) );
                val ret: Int = mSTHumanActionNative.nativeHumanActionDetectPtr(
                    input.bytes,
                    input.bytesType,
                    mSTMobileEffectNative.humanActionDetectConfig,
                    orientation,
                    input.width,
                    input.height
                )

                LogUtils.i(
                    TAG,
                    "human action cost time: %d, ret: %d",
                    System.currentTimeMillis() - startHumanAction, ret
                )
                //nv21数据为横向，相对于预览方向需要旋转处理，前置摄像头还需要镜像
                STHumanAction.nativeHumanActionRotateAndMirror(
                    mSTHumanActionNative,
                    mSTHumanActionNative.nativeHumanActionResultPtr,
                    outSize.width,
                    outSize.height,
                    if (input.isFrontCamera) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK,
                    input.cameraOrientation,
                    deviceOrientation
                )
                if (mStAnimalNative != null) {
                    animalDetect(
                        input.bytes,
                        input.bytesType,
                        getHumanActionOrientation(input.isFrontCamera, input.cameraOrientation),
                        input.height,
                        input.width,
                        current,
                        input.isFrontCamera,
                        input.cameraOrientation
                    )
                } else {
                    mAnimalFaceInfo[current] = STAnimalFaceInfo(null, 0)
                }
                LogUtils.i("processDoubleInput index=$current nativeHumanActionDetect end")
                mSTHumanActionNative.updateNativeHumanActionCache((current + input.diffBetweenBytesAndTexture) % cacheCount)
                if (isReleased) {
                    return@Callable -1
                }
                return@Callable current
            },
        )
        return execute
    }


    /**
     * 用于humanActionDetect接口。根据传感器方向计算出在不同设备朝向时，人脸在buffer中的朝向
     *
     * @return 人脸在buffer中的朝向
     */
    private fun getHumanActionOrientation(frontCamera: Boolean, cameraRotation: Int): Int {
        //获取重力传感器返回的方向
        var orientation: Int = mAccelerometer?.direction ?: CLOCKWISE_ANGLE.Deg90.value

        //在使用后置摄像头，且传感器方向为0或2时，后置摄像头与前置orientation相反
        if (!frontCamera && orientation == STRotateType.ST_CLOCKWISE_ROTATE_0) {
            orientation = STRotateType.ST_CLOCKWISE_ROTATE_180
        } else if (!frontCamera && orientation == STRotateType.ST_CLOCKWISE_ROTATE_180) {
            orientation = STRotateType.ST_CLOCKWISE_ROTATE_0
        }

        // 请注意前置摄像头与后置摄像头旋转定义不同 && 不同手机摄像头旋转定义不同
        if (cameraRotation == 270 && orientation and STRotateType.ST_CLOCKWISE_ROTATE_90 == STRotateType.ST_CLOCKWISE_ROTATE_90
            || cameraRotation == 90 && orientation and STRotateType.ST_CLOCKWISE_ROTATE_90 == STRotateType.ST_CLOCKWISE_ROTATE_0
        ) {
            orientation = orientation xor STRotateType.ST_CLOCKWISE_ROTATE_180
        }

        return orientation
    }

    private fun animalDetect(
        imageData: ByteArray?,
        format: Int,
        orientation: Int,
        width: Int,
        height: Int,
        index: Int,
        isFrontCamera: Boolean,
        cameraRotation: Int
    ) {
        val need = mStAnimalNative != null
        if (need) {
            val catDetectStartTime = System.currentTimeMillis()
            val animalDetectConfig = mSTMobileEffectNative.animalDetectConfig.toInt()
            Log.d(
                TAG,
                "test_animalDetect: $animalDetectConfig"
            )
            var animalFaces: Array<STAnimalFace?>? = mStAnimalNative?.animalDetect(
                imageData,
                format,
                orientation,
                animalDetectConfig,
                width,
                height
            )
            LogUtils.i(
                TAG,
                "animal detect cost time: %d",
                System.currentTimeMillis() - catDetectStartTime
            )
            if (!animalFaces.isNullOrEmpty()) {
                Log.d(
                    TAG,
                    "animalDetect: " + animalFaces.size
                )
                animalFaces = processAnimalFaceResult(
                    animalFaces,
                    width,
                    height,
                    isFrontCamera,
                    cameraRotation
                )
            }
            mAnimalFaceInfo[index] = STAnimalFaceInfo(animalFaces, animalFaces?.size ?: 0)
        } else {
            mAnimalFaceInfo[index] = STAnimalFaceInfo(null, 0)
        }
    }


    private fun processAnimalFaceResult(
        animalFaces: Array<STAnimalFace?>?,
        width: Int,
        height: Int,
        isFrontCamera: Boolean,
        cameraOrientation: Int
    ): Array<STAnimalFace?>? {
        var animalFacesRet = animalFaces ?: return null
        if (isFrontCamera && cameraOrientation == 90) {
            animalFacesRet = STMobileAnimalNative.animalRotate(
                height,
                width,
                STRotateType.ST_CLOCKWISE_ROTATE_90,
                animalFacesRet,
                animalFacesRet.size
            )
            animalFacesRet =
                STMobileAnimalNative.animalMirror(width, animalFacesRet, animalFacesRet.size)
        } else if (isFrontCamera && cameraOrientation == 270) {
            animalFacesRet = STMobileAnimalNative.animalRotate(
                height,
                width,
                STRotateType.ST_CLOCKWISE_ROTATE_270,
                animalFacesRet,
                animalFacesRet.size
            )
            animalFacesRet =
                STMobileAnimalNative.animalMirror(width, animalFacesRet, animalFacesRet.size)
        } else if (!isFrontCamera && cameraOrientation == 270) {
            animalFacesRet = STMobileAnimalNative.animalRotate(
                height,
                width,
                STRotateType.ST_CLOCKWISE_ROTATE_270,
                animalFacesRet,
                animalFacesRet.size
            )
        } else if (!isFrontCamera && cameraOrientation == 90) {
            animalFacesRet = STMobileAnimalNative.animalRotate(
                height,
                width,
                STRotateType.ST_CLOCKWISE_ROTATE_90,
                animalFacesRet,
                animalFacesRet.size
            )
        }
        return animalFacesRet
    }

    private fun getCurrentOrientation(): Int {
        val dir = mAccelerometer?.direction ?: CLOCKWISE_ANGLE.Deg90.value
        var orientation = dir - 1
        if (orientation < 0) {
            orientation = dir xor 3
        }
        return orientation
    }


}