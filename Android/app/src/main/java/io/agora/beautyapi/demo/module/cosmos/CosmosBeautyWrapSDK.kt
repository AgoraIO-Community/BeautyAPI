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
    // Filter
    private var lookupModule: ILookupModule? = null
    // Makeup
    private var makeupModule: IMakeupBeautyModule? = null
    // Beauty
    private var beautyModule: IBeautyModule? = null
    // Sticker
    private var stickerModule: IStickerModule? = null
    // Root path for resources
    private var cosmosPath = ""

    private var beautyAPI: CosmosBeautyAPI? = null

    // Beauty rendering handle
    var renderModuleManager: IMMRenderModuleManager? = null
        get() {
            if(field == null){
                initRenderManager()
            }
            return field
        }

    // Beauty configuration
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
                    Log.d(TAG, "Authorization successful")
                    authSuccess = true
                } else {
                    Log.e(TAG, "Authorization failed ${result.msg}")
                }
                letch.countDown()
            }
            letch.await()
        }
    }

    fun isAuthSuccess(): Boolean {
        return authSuccess
    }

    private fun initRenderManager() {
        initLoader?.get()
        renderModuleManager = CosmosBeautySDK.createRenderModuleManager()
        renderModuleManager?.prepare(true)
        initModules()
        beautyConfig.resume()
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
        beautyConfig.resume()
    }

    private fun runOnBeautyThread(run: () -> Unit) {
        beautyAPI?.runOnProcessThread(run) ?: run.invoke()
    }

    fun reset() {
        renderModuleManager?.release()
        renderModuleManager = null
        beautyConfig.reset()
        beautyModule = null
        makeupModule = null
        lookupModule = null
        stickerModule = null
    }


    class BeautyConfig {

        // Smooth skin
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

        // Whitening
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

        // Slim face
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

        // Enlarged eyes
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

        // Reddening
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


        // Slim cheekbones
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

        // Jawbone
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

        // White teeth
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

        // Hairline height
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

        // Slim nose
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

        // Mouth shape
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

        // Chin length
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

        // Bright eyes
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


        // Nasolabial folds removal
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

        // Sharpening
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

        // Sticker
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

        // Makeup
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

        internal fun resume(){
            smooth = smooth
            whiten = whiten
            thinFace = thinFace
            enlargeEye = enlargeEye
            redden = redden
            shrinkCheekbone = shrinkCheekbone
            shrinkJawbone = shrinkJawbone
            whiteTeeth = whiteTeeth
            hairlineHeight = hairlineHeight
            narrowNose = narrowNose
            mouthSize = mouthSize
            chinLength = chinLength
            brightEye = brightEye
            nasolabialFolds = nasolabialFolds

            makeUp = makeUp
            sticker = sticker
        }
    }

    data class MakeUpItem(
        val path: String,
        val strength: Float
    )
}