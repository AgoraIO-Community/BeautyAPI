# SenseTime BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- The project has apply the Kotlin plugin
- Agora RTC SDK has been integrated in the project
- Contact SenseTime customer service to get SenseTime's beauty SDK, beauty resources and license

## Quick Start
1. (Optional)Unzip the SenseTime SDK and configure the following aar libraries, resource files, and certificates to the corresponding directory of the project

| SenseTime SDK                                                           | Location                        |
|-------------------------------------------------------------------------|---------------------------------|
| Android/models                                                          | assets/beauty_sensetime         |
| Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape    | assets/beauty_sensetime         |
| Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly         | assets/beauty_sensetime         |
| Android/aar/STMobileJNI-release.aar                                     | libs                            |
| Android/smaple/SenseMeEffects/app/libs/SenseArSourceManager-release.aar | libs                            |
| Android/smaple/SenseMeEffects/app/libs/HardwareBuffer-release.aar       | libs                            |
| SenseME.lic                                                             | assets/beauty_sensetime/license |

2. Copy the following BeautyAPI interface and implementation into the project
   
src/main/java/io/agora/beauty/sensetime/beautyapi
   ├── SenseTimeBeautyAPI.kt
   ├── SenseTimeBeautyAPIImpl.kt
   └── utils

3. Initialization
```kotlin
private val mSTRenderKit by lazy {
    STRenderKit(this, "beauty_sensetime")
}
private val mSenseTimeApi by lazy {
    createSenseTimeBeautyAPI()
}

mSenseTimeApi.initialize(
    Config(
        mRtcEngine,
        mSTRenderKit,
        captureMode = CaptureMode.Agora,
        statsEnable = BuildConfig.DEBUG,
        eventCallback = object: IEventCallback{
            override fun onBeautyStats(stats: BeautyStats) {
                Log.d(TAG, "BeautyStats stats = $stats")
            }
        }
    ))
```

4. Beauty On/Off (default off)
```kotlin
mSenseTimeApi.enable(true)
```

5. Local Rendering
```kotlin
mSenseTimeApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. Set Recommended Beauty Parameters
```kotlin
mSenseTimeApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：Close Recommended Beauty
```

7. Destroy BeautyAPI
```kotlin
mRtcEngine.leaveChannel()
// Must release beauty api after leaveChannel
mSenseTimeApi.release()
mSTRenderKit.release()
```

## Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

1. Initialize BeautyAPI with CaptureMode.Custom
```kotlin
mSenseTimeApi.initialize(
    Config(
        mRtcEngine,
        mSTRenderKit,
        captureMode = CaptureMode.Custom,
        statsEnable = BuildConfig.DEBUG,
        eventCallback = object: IEventCallback{
            override fun onBeautyStats(stats: BeautyStats) {
                Log.d(TAG, "BeautyStats stats = $stats")
            }
        }
    ))
```
2. Pass external video frame to BeautyAPI by onFrame interface.
```kotlin
override fun onCaptureVideoFrame(
    sourceType: Int,
    videoFrame: VideoFrame?
) : Boolean {
    when(mSenseTimeApi.onFrame(videoFrame!!)){
        ErrorCode.ERROR_OK.value -> {
            shouldMirror = false
            return true
        }
        ErrorCode.ERROR_FRAME_SKIPPED.value -> {
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