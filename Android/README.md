# BeautyAPI Demo

_English | [中文](README.zh.md)_

> This document mainly introduces how to quickly run through the beauty scene API sample code.
> 
> **Demo Effect:**
>
> <img src="imgs/app_page_launch_en.png" width="300" />
---

## 1. Prerequisites

- Android 5.0 (SDK API Level 21) Above
- Android Studio 3.5+, Using Java 11
- Android 5.0 and above mobile devices

---

## 2. Run Project

- Get Agora App ID -------- [get-started-with-agora](https://docs.agora.io/en/video-calling/get-started/manage-agora-account?platform=Android#get-the-app-id)
   - Go to [Console](https://console.agora.io)
   
   - Click 'Create a project'
    
      <img src="imgs/console_create_project_en.png" width="1080" />
   
   - Select Debug Mode and Create (This example only supports Debug Mode)
   
      <img src="imgs/console_init_project_en.png" width="360" />
   
   - Copy App ID
      
      <img src="imgs/console_copy_appid_en.png" width="1080" />

- Create local.properties file in Android root direction, and fill in the Agora App ID to the file:

   ```xml
   AGORA_APP_ID=#YOUR APP ID#
   ```

- **Contact the beauty manufacturer to obtain the corresponding beauty certificate and resources, and make the following configurations** (If the beautification certificate and resources are not configured, the corresponding manufacturer's beautification will display a black screen.)
  
<h3 id="1">FaceUnity (Optional)</h3>

- Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle).
       
- Put the FaceUnity beauty resources into the corresponding path.

   | FaceUnity Beauty Resources          | Location                                                                  |
   |-------------------------------------|---------------------------------------------------------------------------|
   | makeup resource (e.g. naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup                               |
   | sticker resource (e.g. fashi.bundle) | app/src/main/assets/beauty_faceunity/sticker                              |
   | authpack.java                       | app/src/main/java/io/agora/beautyapi/demo/module/faceunity/authpack.java  |
  


**Run**
- Use AndroidStudio to open the `Android` project and click Run.

## 3. Quick integration

- Beauty API can be integrated into your project separately.
                                                                  
## FaceUnity

### Configure beauty
- Ensure you have contacted FaceUnity technical support to obtain the latest Beauty SDK, resources, and certificates.
- Configure beauty certificates and resources. -- [Configure](#1)
- Integrate the Agora Beauty Scene API into your project. Add the files from the directory [faceunity](/Android/lib_faceunity/src/main/java/io/agora/beautyapi/faceunity/) to your project, including the following:
  
   * [utils](/Android/lib_faceunity/src/main/java/io/agora/beautyapi/faceunity/utils/)
   * [FaceUnityBeautyAPI.kt](/Android/lib_faceunity/src/main/java/io/agora/beautyapi/faceunity/FaceUnityBeautyAPI.kt)
   * [FaceUnityBeautyAPIImpl.kt](/Android/lib_faceunity/src/main/java/io/agora/beautyapi/faceunity/FaceUnityBeautyAPIImpl.kt)

**Note:**

To facilitate future code upgrades, please avoid modifying the names and paths of these files you have added.

### Implementing Beauty Effects

#### 1. Initialize the RtcEngine
```kotlin
private val mRtcEngine by lazy {
    RtcEngine.create(RtcEngineConfig().apply {
        mContext = applicationContext
        // Enter the APP ID of your Agora project obtained from the console.
        mAppId = BuildConfig.AGORA_APP_ID
        mEventHandler = object : IRtcEngineEventHandler() {}
    })
}
```

#### 2. Initialize the beauty SDK

- Initialize the FURenderKit object by calling the `registerFURender` method from the FaceUnity Beauty SDK with the following parameters:
	- `context`: Android Context
	- `getAuth()`: A ByteArray returned by the `getAuth` method containing the authentication field. This authentication field is linked to the local `authpack.java` certificate file and requires successful validation to use the FaceUnity Beauty SDK.
	- `object`: Event callback

- After successful initialization of the Beauty SDK, load AI props on a new thread by calling `loadAIProcessor`.
```kotlin
object FaceUnityBeautySDK {
    private val TAG = "FaceUnityBeautySDK"

    private val fuAIKit = FUAIKit.getInstance()
    val fuRenderKit = FURenderKit.getInstance()

    // AI props
    private val BUNDLE_AI_FACE = "model" + File.separator + "ai_face_processor.bundle"
    private val BUNDLE_AI_HUMAN = "model" + File.separator + "ai_human_processor.bundle"

    private val workerThread = Executors.newSingleThreadExecutor()

    fun initBeauty(context: Context) {
        // Set beauty SDK logging
        FURenderManager.setKitDebug(FULogger.LogLevel.TRACE)
        FURenderManager.setCoreDebug(FULogger.LogLevel.ERROR)
        
        // Initialize beauty SDK
        // Need to pass beauty SDK authentication field and set beauty SDK event listener
        FURenderManager.registerFURender(context, getAuth(), object : OperateCallback {
            override fun onSuccess(code: Int, msg: String) {
                Log.i(TAG, "FURenderManager onSuccess -- code=$code, msg=$msg")
                if (code == OPERATE_SUCCESS_AUTH) {
                    faceunity.fuSetUseTexAsync(1)
                    // If beauty SDK initialization is successful, load AI props in new thread
                    workerThread.submit {
                        fuAIKit.loadAIProcessor(BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR)
                        fuAIKit.loadAIProcessor(BUNDLE_AI_HUMAN, FUAITypeEnum.FUAITYPE_HUMAN_PROCESSOR)
                    }
                }
            }

            override fun onFail(errCode: Int, errMsg: String) {
                Log.e(TAG, "FURenderManager onFail -- code=$errCode, msg=$errMsg")
            }
        })
    }

    // Get beauty SDK authentication field
    private fun getAuth(): ByteArray {
        val authpack = Class.forName("io.agora.beautyapi.demo.authpack")
        val aMethod = authpack.getDeclaredMethod("A")
        aMethod.isAccessible = true
        val authValue = aMethod.invoke(null) as? ByteArray
        return authValue ?: ByteArray(0)
    }
}
```

#### 3.Initialize the Beauty API.

- Call **createFaceUnityBeautyAPI** to create a Beauty API object. The Beauty API object is encapsulated based on FuRenderKit objects.
```kotlin
private val mFaceUnityApi by lazy {
    createFaceUnityBeautyAPI()
}

mFaceUnityApi.initialize(
    Config(
        // Android context
        context = mContext,
        
        // Agora RTC engine
        rtcEngine = mRtcEngine,
        
        // Beauty SDK handler
        fuRenderKit = FURenderKit.getInstance(),
        
        // Agora: Use the internal raw data interface of Agora for processing.
        // Custom: you need to call the [io.agora.rtc2.video.IVideoFrameObserver] interface yourself 
        // to pass the raw video frame to the BeautyAPI for processing.
        captureMode = if(isCustomCaptureMode) CaptureMode.Custom else CaptureMode.Agora,

        // Stats interval duration
        statsDuration = 1000,
        
        // Enable stats or not
        statsEnable = true,
        
        // Camera mirror configuration
        cameraConfig = CameraConfig(),

        // Event callback
        eventCallback = object : IEventCallback {
            override fun onBeautyStats(stats: BeautyStats) {
                Log.d(TAG, "BeautyStats stats = $stats")
            }
        }
    )
)

```

#### 4. Enable Beauty Mode

- Call the enable method of the Beauty API and set the parameter to true to activate beauty mode.
```kotlin
mFaceUnityApi.enable(true)
```

#### 5. Start Video Capture

- Developers can use the Agora module for video capture or customize the video capture process. This section explains how to start video capture in both scenarios.

**Using the Agora module for video capture**
```kotlin
// Enable video module
mRtcEngine.enableVideo()

// Set up the local view
mFaceUnityApi.setupLocalVideo(
    mBinding.localVideoView,
    Constants.RENDER_MODE_FIT
)
```

**Custom Video Capture Mode**
```kotlin
// Enable video module
mRtcEngine.enableVideo()
// Register the raw video data observer
// When custom video capture is enabled, that is, when CaptureMode is Custom, 
// you need to register the raw video observer
mRtcEngine.registerVideoFrameObserver(object : IVideoFrameObserver {

    override fun onCaptureVideoFrame(
        sourceType: Int,
        videoFrame: VideoFrame?
    ) = when (mFaceUnityApi.onFrame(videoFrame!!)) {
       // When the processing result is SKIPPED, it means frame dropping, 
        // i.e., the externally captured video data is not passed to the Agora RTC SDK
        // For other processing results, the externally captured video data is passed to the Agora RTC SDK
        ErrorCode.ERROR_FRAME_SKIPPED.value -> false
        else -> true
    }

    // Set whether to mirror the original video data
    override fun getMirrorApplied() = mFaceUnityApi.getMirrorApplied()

    // Set the observation point to the video data during local capture
    override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

    // Override other callback functions in the video observer
    ...
})
```

#### 6.Join a Channel

```kotlin
mRtcEngine.joinChannel(
    null, 
    mChannelName, 
    0, 
    ChannelMediaOptions().apply {
        // Set channel profile as live broadcasting
        channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        
        // Set user role as broadcaster, who can publish and subscribe to streams in the channel
        clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        
        // Set whether to publish camera video stream (applies when using Agora's video capture)
        publishCameraTrack = true
        
        // Set whether to publish custom video stream (applies when using custom video capture)
        publishCustomVideoTrack = false
        
        // Set whether to publish microphone audio stream
        publishMicrophoneTrack = false
        
        // Set whether to automatically subscribe to other users' audio streams when joining the channel
        autoSubscribeAudio = false
        
        // Set whether to automatically subscribe to other users' video streams when joining the channel
        autoSubscribeVideo = true
    }
)
```

#### 7. Update Camera Configuration(Optional)

```kotlin
val isFront = mSenseTimeApi.isFrontCamera()

// Check if front camera is being used
if (isFront) {
    cameraConfig = CameraConfig(
        // Configure front camera through frontMirror
        frontMirror = when (cameraConfig.frontMirror) {
            MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
            MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
            MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
            MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
        },
        backMirror = cameraConfig.backMirror
    )
} else {
    cameraConfig = CameraConfig(
        frontMirror = cameraConfig.frontMirror,
        // Configure rear camera through backMirror
        backMirror = when (cameraConfig.backMirror) {
            MirrorMode.MIRROR_NONE -> MirrorMode.MIRROR_LOCAL_REMOTE
            MirrorMode.MIRROR_LOCAL_REMOTE -> MirrorMode.MIRROR_LOCAL_ONLY
            MirrorMode.MIRROR_LOCAL_ONLY -> MirrorMode.MIRROR_REMOTE_ONLY
            MirrorMode.MIRROR_REMOTE_ONLY -> MirrorMode.MIRROR_NONE
        }
    )
}

// Update camera configuration
mFaceUnityApi.updateCameraConfig(cameraConfig)
```

#### 8. Leaving the Channel

```kotlin
mRtcEngine.leaveChannel()
```

#### 9. Destroying Resources

- Call release on the Beauty API to destroy the Beauty API.

```kotlin
mFaceUnityApi.release()
```

- Call release on the Beauty SDK to destroy STHandlers.

```kotlin
FURenderKit.getInstance().release()
```

- Call destroy on RtcEngine to destroy RtcEngine.

```kotlin
RtcEngine.destroy()
```

## Feedback

If you have any problems or suggestions regarding the sample projects, feel free to file an issue.

## Related Resources

- Check our [FAQ](https://docs.agora.io/en/faq) to see if your issue has been recorded.
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials.
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use cases.
- Repositories managed by developer communities can be found at [Agora Community](https://github.com/AgoraIO-Community).
- If you encounter problems during integration, feel free to ask questions in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io).

## 5. License

The sample projects are under the [MIT license](../LICENSE).

