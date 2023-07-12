# FaceUnity BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- The project has apply the Kotlin plugin
- Agora RTC SDK has been integrated in the project
- Contact FaceUnity customer service to get beauty resources and license

## Quick Start
1. (Optional)FaceUnity Configuration
- Config Dependencies
```groovy
allprojects {
    repositories {
        maven {
            url 'http://maven.faceunity.com/repository/maven-public/'
            allowInsecureProtocol = true
        }
    }
}

dependencies {
    implementation 'com.faceunity:core:8.3.0'
    implementation 'com.faceunity:model:8.3.0'
}
```
- Config Beauty Resources

| FaceUnity Beauty Resources          | Location                        |
|-------------------------------------|---------------------------------|
| makeup resource(e.g. naicha.bundle) | assets/beauty_faceunity/makeup  |
| sticker resource(e.g. fashi.bundle) | assets/beauty_faceunity/sticker |
| authpack.java                       | src                             |


2. Copy the following BeautyAPI interface and implementation into the project
   src/main/java/io/agora/beauty/faceunity/beautyapi
   ├── FaceUnityBeautyAPI.kt
   ├── FaceUnityBeautyAPIImpl
   └── utils

3. Initialization
```kotlin
private val mFaceUnityApi by lazy {
    createFaceUnityBeautyAPI()
}
private val mFuRenderKit by lazy {
    FURenderer.getInstance().setup(this, authpack.A())
    FURenderKit.getInstance()
}

mFaceUnityApi.initialize(
    Config(
        mRtcEngine,
        mFuRenderKit,
        captureMode = CaptureMode.Agora,
        statsEnable = true,
        eventCallback = object: IEventCallback{
            override fun onBeautyStats(stats: BeautyStats) {
                Log.d(TAG, "BeautyStats stats = $stats")
            }
        }
    ))
```

4. Beauty On/Off (default off)
```kotlin
mFaceUnityApi.enable(true)
```

5. Local Rendering
```kotlin
mFaceUnityApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. Set Recommended Beauty Parameters
```kotlin
mFaceUnityApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：Close Recommended Beauty
```

7. Destroy BeautyAPI
```kotlin
mRtcEngine.leaveChannel()
// Must release beauty api after leaveChannel
mFaceUnityApi.release()
FURenderer.getInstance().release()
mFuRenderKit.release()
```

## Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

1. Initialize BeautyAPI with CaptureMode.Custom
```kotlin
mFaceUnityApi.initialize(
    Config(
        mRtcEngine,
        mFuRenderKit,
        captureMode = CaptureMode.Custom,
        statsEnable = true,
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
) : Boolean{
    when(mFaceUnityApi.onFrame(videoFrame!!)){
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