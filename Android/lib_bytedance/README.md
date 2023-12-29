# ByteDance BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- The project has apply the Kotlin plugin
- Agora RTC SDK has been integrated in the project
- Contact ByteDance customer service to get ByteDance's beauty SDK, beauty resources and license

## Quick Start
1. (Optional)Unzip the ByteDance SDK and configure the following aar libraries, resource files, and certificates to the corresponding directory of the project

| ByteDance SDK                                | Location                |
|----------------------------------------------|-------------------------|
| resource/LicenseBag.bundle                   | assets/beauty_bytedance |
| resource/ModelResource.bundle                | assets/beauty_bytedance |
| resource/ComposeMakeup.bundle                | assets/beauty_bytedance |
| resource/StickerResource.bundle              | assets/beauty_bytedance |
| resource/StickerResource.bundle              | assets/beauty_bytedance |
| byted_effect_andr/libs/effectAAR-release.aar | libs                    |

2. Copy the following BeautyAPI interface and implementation into the project
> Please keep the package name so that we can upgrade the code.
```xml
src/main/java/io/agora/beautyapi/bytedance
   ├── ByteDanceBeautyAPI.kt
   ├── ByteDanceBeautyAPIImpl.kt
   └── utils
```

3. Initialization

> Before initialization, you need to copy the resources required by the ByteDanceBeauty SDK to the sdcard, and create a RenderManager instance in advance and pass it to ByteDanceBeautyAPI.
> For the initialization and destruction of renderManager, it needs to be called in the GL thread. Here, ByteDanceBeautyAPI provides two callbacks, onEffectInitialized and onEffectDestroyed.

```kotlin
private val mByteDanceApi by lazy {
  createByteDanceBeautyAPI()
}
mByteDanceApi.initialize(
  Config(
    applicationContext,
    mRtcEngine,
    renderManager,
    captureMode = if (isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,
    statsEnable = true,
    cameraConfig = CameraConfig(),
    eventCallback = EventCallback(
      onBeautyStats = {stats ->
        Log.d(TAG, "BeautyStats stats = $stats")
      },
      onEffectInitialized = {
        // Callback in the GL thread, used to initialize the Bytebeauty SDK
        ByteDanceBeautySDK.initEffect(applicationContext)
        Log.d(TAG, "onEffectInitialized")
      },
      onEffectDestroyed = {
        // Callback in the GL thread, used to destroy the Bytebeauty SDK
        ByteDanceBeautySDK.unInitEffect()
        Log.d(TAG, "onEffectInitialized")
      }
    )
  )
)
```

4. Beauty On/Off (default off)
```kotlin
mByteDanceApi.enable(true)
```

5. Local Rendering
```kotlin
mByteDanceApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. Set Recommended Beauty Parameters
```kotlin
mByteDanceApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：Close Recommended Beauty
```

7. Update Camera Config
```kotlin
val cameraConfig = CameraConfig(
    frontMirror = MirrorMode.MIRROR_LOCAL_REMOTE,
    backMirror = MirrorMode.MIRROR_NONE
)
mByteDanceApi.updateCameraConfig(cameraConfig)
```

8. Destroy BeautyAPI

> The calling time must be after leaveChannel/stopPreview/registerVideoFrameObserver(null) and before RtcEngine.destroy!

```kotlin
mRtcEngine.leaveChannel()
mRtcEngine.stopPreview()
if (isCustomCaptureMode) {
  // If you use custom capture mode and have registered video frame observer, register video frame observer to null here!
  mRtcEngine.registerVideoFrameObserver(null)
}
mByteDanceApi.release()
RtcEngine.destroy()
```

## Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

1. Initialize BeautyAPI with CaptureMode.Custom
```kotlin
mByteDanceApi.initialize(
    Config(
        mRtcEngine,
        mEffectManager,
        captureMode = CaptureMode.Custom,
        statsEnable = BuildConfig.BUILD,
        cameraConfig = CameraConfig(),
        eventCallback = EventCallback(
            onBeautyStats = {stats ->
                Log.d(TAG, "BeautyStats stats = $stats")
            },
            onEffectInitialized = {
                Log.d(TAG, "onEffectInitialized")
            },
            onEffectDestroyed = {
                Log.d(TAG, "onEffectInitialized")
            }
        )
    ))
```
2. Pass external video frame to BeautyAPI by onFrame interface.
```kotlin
override fun onCaptureVideoFrame(
  sourceType: Int,
  videoFrame: VideoFrame?
) = when (mByteDanceApi.onFrame(videoFrame!!)) {
  ErrorCode.ERROR_FRAME_SKIPPED.value -> false
  else -> true
}

override fun getMirrorApplied() = mByteDanceApi.getMirrorApplied()
```

## Contact us

- If you are already using Shengwang services or are in the process of docking, you can directly contact the docked sales or service.
- Send an email to [support@agora.io](mailto:support@agora.io) for consultation
- Scan the QR code to join our WeChat communication group to ask questions

![](https://download.agora.io/demo/release/SDHY_QA.jpg)

## License

The sample projects are under the MIT license.