# Cosmos BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- The project has apply the Kotlin plugin
- Agora RTC SDK has been integrated in the project
- Contact ByteDance customer service to get Cosmos's beauty SDK, beauty resources and license

## Quick Start
1. (Optional)Unzip the Cosmos SDK and configure the following aar libraries, resource files, and certificates to the corresponding directory of the project

| Cosmos SDK                                   | Location                                         |
|----------------------------------------------|--------------------------------------------------|
| sample/app/src/main/assets/model-all.zip     | app/src/main/assets/beauty_cosmos/model-all.zip  |
| sample/app/src/main/assets/cosmos.zip        | app/src/main/assets/beauty_cosmos/cosmos.zip     |
| sample/app/libs/beautysdk-3.7.0-20230301.aar | libs                                             |

2. Copy the following BeautyAPI interface and implementation into the project
> Please keep the package name so that we can upgrade the code.
```xml
src/main/java/io/agora/beautyapi/cosmos
    ├── CosmosBeautyAPI.kt
    ├── CosmosBeautyAPIImpl.kt
    └── utils
```

3. Initialization

> Before initialization, you need to copy the resources required by the ByteDanceBeauty SDK to the sdcard, and create a renderModuleManager instance in advance and pass it to CosmosBeautyAPI.

```kotlin
private val mCosmosApi by lazy {
  createCosmosBeautyAPI()
}
mCosmosApi.initialize(
  Config(
    application,
    mRtcEngine,
    CosmosBeautyWrapSDK.renderModuleManager!!,
    captureMode = CaptureMode.Agora,
    statsEnable = true,
    eventCallback = EventCallback(
      onBeautyStats = { stats ->
        Log.d(TAG, "onBeautyStats >> $stats")
      }
    )
  )
)
```

4. Beauty On/Off (default off)
```kotlin
mCosmosApi.enable(true)
```

5. Local Rendering
```kotlin
mCosmosApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. Set Recommended Beauty Parameters
```kotlin
mCosmosApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：Close Recommended Beauty
```

7. Update Camera Config
```kotlin
val cameraConfig = CameraConfig(
    frontMirror = MirrorMode.MIRROR_LOCAL_REMOTE,
    backMirror = MirrorMode.MIRROR_NONE
)
mCosmosApi.updateCameraConfig(cameraConfig)
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
mCosmosApi.release()
RtcEngine.destroy()
```

## Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

1. Initialize BeautyAPI with CaptureMode.Custom
```kotlin
mCosmosApi.initialize(
  Config(
    application,
    mRtcEngine,
    CosmosBeautyWrapSDK.renderModuleManager!!,
    captureMode = CaptureMode.Custom,
    statsEnable = true,
    eventCallback = EventCallback(
      onBeautyStats = { stats ->
        Log.d(TAG, "onBeautyStats >> $stats")
      }
    )
  )
```
2. Pass external video frame to BeautyAPI by onFrame interface.
```kotlin
override fun onCaptureVideoFrame(
  sourceType: Int,
  videoFrame: VideoFrame?
) = when (mCosmosApi.onFrame(videoFrame!!)) {
  ErrorCode.ERROR_FRAME_SKIPPED.value -> false
  else -> true
}

override fun getMirrorApplied() = mCosmosApi.getMirrorApplied()
```

## Feedback

If you have any problems or suggestions regarding the sample projects, feel free to file an issue.

## Related resources

- Check our [FAQ](https://docs.agora.io/en/faq) to see if your issue has been recorded.
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials.
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use
  case.
- Repositories managed by developer communities can be found
  at [Agora Community](https://github.com/AgoraIO-Community).
- If you encounter problems during integration, feel free to ask questions
  in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io).

## License

The sample projects are under the MIT license.