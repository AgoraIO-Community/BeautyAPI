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

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport
import io.agora.beautyapi.demo.module.bytedance.ByteDanceBeautySDK
import io.agora.beautyapi.demo.module.cosmos.CosmosBeautyWrapSDK
import io.agora.beautyapi.demo.module.faceunity.FaceUnityBeautySDK
import io.agora.beautyapi.demo.module.sensetime.SenseTimeBeautySDK

class MApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        SenseTimeBeautySDK.initBeautySDK(this)
        FaceUnityBeautySDK.initBeauty(this)
        ByteDanceBeautySDK.initBeautySDK(this)
        CosmosBeautyWrapSDK.initBeautySDK(this)

        CrashReport.initCrashReport(this, BuildConfig.BUGLY_APP_ID, true, CrashReport.UserStrategy(this).apply {
            isEnableCatchAnrTrace = true
            isEnableANRCrashMonitor = true
            isEnableRecordAnrMainStack = true
            isEnableUserInfo = true
            isEnableNativeCrashMonitor = true
        })
    }

}