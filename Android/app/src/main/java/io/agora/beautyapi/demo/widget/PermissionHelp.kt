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

package io.agora.beautyapi.demo.widget

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionHelp(val activity: ComponentActivity) {

    private var granted: (() -> Unit)? = null
    private var unGranted: (() -> Unit)? = null
    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val granted = granted
            val unGranted = unGranted
            this.granted = null
            this.unGranted = null

            if (isGranted) {
                granted?.invoke()
            } else {
                unGranted?.invoke()
            }
        }
    private val appSettingLauncher =
        activity.registerForActivityResult(object : ActivityResultContract<String, Boolean>() {
            private var input: String? = null

            override fun createIntent(context: Context, input: String): Intent {
                this.input = input
                return Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:" + context.packageName)
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                return ContextCompat.checkSelfPermission(
                    activity,
                    input ?: ""
                ) == PackageManager.PERMISSION_GRANTED
            }

        }) { isGranted ->
            val granted = granted
            val unGranted = unGranted
            this.granted = null
            this.unGranted = null

            if (isGranted) {
                granted?.invoke()
            } else {
                unGranted?.invoke()
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private val managerAllFileLauncher =
        activity.registerForActivityResult(object : ActivityResultContract<String, Boolean>() {
            override fun createIntent(context: Context, input: String): Intent {
                return Intent().apply {
                    action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                return Environment.isExternalStorageManager()
            }
        }) { isGranted ->
            val granted = granted
            val unGranted = unGranted
            this.granted = null
            this.unGranted = null

            if (isGranted) {
                granted?.invoke()
            } else {
                unGranted?.invoke()
            }
        }


    /**
     * Check camera and microphone permissions
     *
     * @param force: If true, will navigate to the system application permission settings page if the permissions are disabled
     */
    fun checkCameraAndMicPerms(granted: () -> Unit, unGranted: () -> Unit, force: Boolean = false) {
        checkCameraPerm({
            checkMicPerm(granted, unGranted, force)
        }, unGranted, force)
    }

    /**
     * Check microphone permission
     *
     * @param force: If true, will navigate to the system application permission settings page if the permission is disabled
     */
    fun checkMicPerm(granted: () -> Unit, unGranted: () -> Unit, force: Boolean = false) {
        checkPermission(Manifest.permission.RECORD_AUDIO, granted, force, unGranted)
    }

    /**
     * Check camera permission
     *
     * @param force: If true, will navigate to the system application permission settings page if the permission is disabled
     */
    fun checkCameraPerm(granted: () -> Unit, unGranted: () -> Unit, force: Boolean = false) {
        checkPermission(Manifest.permission.CAMERA, granted, force, unGranted)
    }

    /**
     * Check external storage permission
     *
     * @param force: If true, will navigate to the system application permission settings page if the permission is disabled
     */
    fun checkStoragePerm(granted: () -> Unit, unGranted: () -> Unit, force: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                granted.invoke()
            } else {
                launchManagerFile(granted, unGranted)
            }
        } else {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, {
                checkPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    granted,
                    force,
                    unGranted
                )
            }, force, unGranted)
        }
    }

    private fun checkPermission(perm: String, granted: () -> Unit, force: Boolean, unGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                granted.invoke()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.shouldShowRequestPermissionRationale(perm) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                // showInContextUI(...)
                if (force) {
                    launchAppSetting(perm, granted, unGranted)
                } else {
                    unGranted.invoke()
                }
            }

            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                launchPermissionRequest(perm, granted, unGranted)
            }
        }
    }

    private fun launchPermissionRequest(perm: String, granted: () -> Unit, unGranted: () -> Unit) {
        this.granted = granted
        this.unGranted = unGranted
        requestPermissionLauncher.launch(perm)
    }

    private fun launchAppSetting(perm: String, granted: () -> Unit, unGranted: () -> Unit) {
        this.granted = granted
        this.unGranted = unGranted
        appSettingLauncher.launch(perm)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun launchManagerFile(granted: () -> Unit, unGranted: () -> Unit) {
        this.granted = granted
        this.unGranted = unGranted
        managerAllFileLauncher.launch("")
    }
}