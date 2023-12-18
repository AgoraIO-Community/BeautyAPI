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

package io.agora.beautyapi.demo.module.cosmos

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cosmos.beauty.CosmosBeautySDK
import com.cosmos.beauty.module.IMMRenderModuleManager
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.beauty.MakeupType
import com.cosmos.beauty.module.beauty.SimpleBeautyType
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beauty.module.makeup.IMakeupBeautyModule
import com.cosmos.beauty.module.sticker.IStickerModule
import io.agora.beautyapi.cosmos.CosmosBeautyAPI
import io.agora.beautyapi.demo.utils.FileUtils
import io.agora.beautyapi.demo.utils.ZipUtils
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

object CosmosBeautyWrapSDK {

    private const val TAG = "CosmosBeautySDK"
    private const val LICENSE = ""

    private val workerThread = Executors.newSingleThreadExecutor()
    private var context: Application? = null
    private var storagePath = ""
    private var assetsPath = ""
    private var initLoader : Future<*>? = null

    @Volatile
    private var authSuccess = false
    // 滤镜
    private var lookupModule: ILookupModule? = null
    // 美妆
    private var makeupModule: IMakeupBeautyModule? = null
    // 美颜
    private var beautyModule: IBeautyModule? = null
    // 贴纸
    private var stickerModule: IStickerModule? = null
    // 资源根路径
    private var cosmosPath = ""

    private var beautyAPI: CosmosBeautyAPI? = null

    // 美颜渲染句柄
    var renderModuleManager: IMMRenderModuleManager? = null
        get() {
            if(field == null){
                initRenderManager()
            }
            return field
        }

    // 美颜配置
    var beautyConfig = BeautyConfig()

    fun initBeautySDK(context: Context) {
        CosmosBeautyWrapSDK.context = context.applicationContext as Application?
        storagePath = context.getExternalFilesDir("")?.absolutePath ?: return
        assetsPath = "beauty_cosmos"
        initLoader = workerThread.submit {
            // copy cosmos.zip
            val cosmosZipPath = "$storagePath/beauty_cosmos/cosmos.zip"
            FileUtils.copyAssets(context, "$assetsPath/cosmos.zip", cosmosZipPath)

            // copy model-all.zip
            val modelAllZipPath = "$storagePath/beauty_cosmos/model-all.zip"
            FileUtils.copyAssets(context, "$assetsPath/model-all.zip", modelAllZipPath)

            // unzip cosmos.zip
            cosmosPath = "$storagePath/beauty_cosmos"
            ZipUtils.unzip(cosmosZipPath, cosmosPath)
            cosmosPath = "$cosmosPath/cosmos"

            // unzip model-all.zip
            val modelAllPath = "$storagePath/beauty_cosmos"
            ZipUtils.unzip(modelAllZipPath, modelAllPath)

            val letch = CountDownLatch(1)
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
                letch.countDown()
            }
            letch.await()
        }
    }

    private fun initRenderManager() {
        initLoader?.get()
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
    }

    internal fun setBeautyAPI(beautyAPI: CosmosBeautyAPI?) {
        this.beautyAPI = beautyAPI
    }

    private fun runOnBeautyThread(run: () -> Unit) {
        beautyAPI?.runOnProcessThread(run) ?: run.invoke()
    }

    fun reset() {
        renderModuleManager?.release()
        renderModuleManager = null
        beautyModule = null
        makeupModule = null
        lookupModule = null
        stickerModule = null
    }


    class BeautyConfig {

        // 磨皮
        var smooth = 0.65f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.SKIN_SMOOTH,
                        value
                    )
                }
            }

        // 美白
        var whiten = 0.65f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.SKIN_WHITENING,
                        value
                    )
                }
            }

        // 瘦脸
        var thinFace = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.FACE_WIDTH,
                        value
                    )
                }
            }

        // 大眼
        var enlargeEye = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.BIG_EYE,
                        value
                    )
                }
            }

        // 红润
        var redden = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.RUDDY,
                        value
                    )
                }
            }


        // 瘦颧骨
        var shrinkCheekbone = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.CHEEKBONE_WIDTH,
                        value
                    )
                }
            }

        // 下颌骨
        var shrinkJawbone = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.JAW_WIDTH,
                        value
                    )
                }
            }

        // 美牙
        var whiteTeeth = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.TEETH_WHITE,
                        value
                    )
                }
            }

        // 额头
        var hairlineHeight = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.FOREHEAD,
                        value
                    )
                }
            }

        // 瘦鼻
        var narrowNose = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.NOSE_SIZE,
                        value
                    )
                }
            }

        // 嘴形
        var mouthSize = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.MOUTH_SIZE,
                        value
                    )
                }
            }

        // 下巴
        var chinLength = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.CHIN_LENGTH,
                        value
                    )
                }
            }

        // 亮眼
        var brightEye = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.EYE_BRIGHT,
                        value
                    )
                }
            }


        // 祛法令纹
        var nasolabialFolds = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.NASOLABIAL_FOLDS,
                        value
                    )
                }
            }

        // 锐化
        var sharpen = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beautyModule?.setValue(
                        SimpleBeautyType.SHARPEN,
                        value
                    )
                }
            }

        // 贴纸
        var sticker: String? = null
            set(value) {
                if(field == value){
                    return
                }
                field = value
                runOnBeautyThread {
                    stickerModule?.removeModule()
                    stickerModule?.addMaskModel(
                        File("${cosmosPath}/$sticker")
                    ) { }
                }
            }

        // 美妆
        var makeUp: MakeUpItem? = null
            set(value) {
                if (field?.path != value?.path) {
                    makeupModule?.clear()
                    if (value?.path != null) {
                        makeupModule?.addMakeup("$cosmosPath/${value.path}")
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

        internal fun reset() {
            smooth = 0.65f
            whiten = 0.65f
            thinFace = 0.3f
            enlargeEye = 0.0f
            redden = 0.0f
            shrinkCheekbone = 0.3f
            shrinkJawbone = 0.0f
            whiteTeeth = 0.0f
            hairlineHeight = 0.0f
            narrowNose = 0.0f
            mouthSize = 0.0f
            chinLength = 0.0f
            brightEye = 0.0f
            nasolabialFolds = 0.0f

            makeUp = null
            sticker = null
        }
    }

    data class MakeUpItem(
        val path: String,
        val strength: Float
    )
}