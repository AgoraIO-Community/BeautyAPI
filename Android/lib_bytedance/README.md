# 字节/火山美颜场景化API

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
   src/main/java/io/agora/beauty/bytedance/beautyapi
   ├── ByteDanceBeautyAPI.kt
   ├── ByteDanceBeautyAPIImpl.kt
   └── utils

3. Initialization
```kotlin
private val mByteDanceApi by lazy {
    createByteDanceBeautyAPI()
}
private val mEffectManager by lazy {
    val resourceHelper =
        AssetsResourcesHelper(this, "beauty_bytedance")
    EffectManager(
        this,
        resourceHelper,
        resourceHelper.licensePath
    )
}

mByteDanceApi.initialize(
    Config(
        mRtcEngine,
        mEffectManager,
        captureMode = CaptureMode.Agora,
        statsEnable = BuildConfig.BUILD,
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

7. Destroy BeautyAPI
```kotlin
mRtcEngine.leaveChannel()
// Must release beauty api after leaveChannel
mByteDanceApi.release()
mEffectManager.destroy()
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
) : Boolean {
    when(mByteDanceApi.onFrame(videoFrame!!)){
        ErrorCode.ERROR_OK.value -> {
            shouldMirror = false
            return true
        }
        ErrorCode.ERROR_FRAME_SKIPPED.value ->{
            shouldMirror = false
            return false
        }
        else -> {
            val mirror = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
            if(shouldMirror != mirror){
                shouldMirror = mirror
                return false
            }
            return true
        }
    }
}
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