# Cosmos BeautyAPI

_English | [中文](README.zh.md)

> This document mainly introduces how to quickly integrate the Cosmos Beauty Scenario API.
---

## 1. Prerequisites

- The project has apply the Kotlin plugin
- Agora RTC SDK has been integrated in the project
- Contact ByteDance customer service to get Cosmos's beauty SDK, beauty resources and license
---

## 2. Quick Integration
- Unzip the Cosmos SDK and configure the following aar libraries, resource files, and certificates to the corresponding directory of the project
  > 
  > | Cosmos SDK                                   | Location                                         |
  > |----------------------------------------------|--------------------------------------------------|
  > | sample/app/src/main/assets/model-all.zip     | app/src/main/assets/beauty_cosmos/model-all.zip  |
  > | sample/app/src/main/assets/cosmos.zip        | app/src/main/assets/beauty_cosmos/cosmos.zip     |
  > | sample/app/libs/beautysdk-3.7.0-20230301.aar | libs                                             |

- Copy the following BeautyAPI interface and implementation into the project
  > Please keep the package name so that we can upgrade the code.
  > ```xml
  > src/main/java/io/agora/beautyapi/cosmos
  >     ├── CosmosBeautyAPI.kt
  >     ├── CosmosBeautyAPIImpl.kt
  >     └── utils
  > ```

- Initialization
  > Before initialization, you need to copy the resources required by the ByteDanceBeauty SDK to the sdcard, and create a renderModuleManager instance in advance and pass it to CosmosBeautyAPI.
  > 
  > ```kotlin
  > private val mCosmosApi by lazy {
  >   createCosmosBeautyAPI()
  > }
  > mCosmosApi.initialize(
  >   Config(
  >     application,
  >     mRtcEngine,
  >     CosmosBeautyWrapSDK.renderModuleManager!!,
  >     captureMode = CaptureMode.Agora,
  >     statsEnable = true,
  >     eventCallback = EventCallback(
  >       onBeautyStats = { stats ->
  >         Log.d(TAG, "onBeautyStats >> $stats")
  >       }
  >     )
  >   )
  > )
  > ```

- Beauty On/Off (default off)
  > ```kotlin
  > mCosmosApi.enable(true)
  > ```

- Local Rendering
  > ```kotlin
  > mCosmosApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
  > ```

- Set Recommended Beauty Parameters
  > ```kotlin
  > mCosmosApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：Close Recommended Beauty
  > ```

- Update Camera Config
  > ```kotlin
  > val cameraConfig = CameraConfig(
  >     frontMirror = MirrorMode.MIRROR_LOCAL_REMOTE,
  >     backMirror = MirrorMode.MIRROR_NONE
  > )
  > mCosmosApi.updateCameraConfig(cameraConfig)
  > ```

- Destroy BeautyAPI
  > The calling time must be after leaveChannel/stopPreview/registerVideoFrameObserver(null) and before RtcEngine.destroy!
  > 
  > ```kotlin
  > mRtcEngine.leaveChannel()
  > mRtcEngine.stopPreview()
  > if (isCustomCaptureMode) {
  >   // If you use custom capture mode and have registered video frame observer, register video frame observer to null here!
  >   mRtcEngine.registerVideoFrameObserver(null)
  > }
  > mCosmosApi.release()
  > RtcEngine.destroy()
  > ```
---

## 3. Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

- Initialize BeautyAPI with CaptureMode.Custom
  > ```kotlin
  > mCosmosApi.initialize(
  >   Config(
  >     application,
  >     mRtcEngine,
  >     CosmosBeautyWrapSDK.renderModuleManager!!,
  >     captureMode = CaptureMode.Custom,
  >     statsEnable = true,
  >     eventCallback = EventCallback(
  >       onBeautyStats = { stats ->
  >         Log.d(TAG, "onBeautyStats >> $stats")
  >       }
  >     )
  >   )
  > )
  > ```

- Pass external video frame to BeautyAPI by onFrame interface.
  > ```kotlin
  > override fun onCaptureVideoFrame(
  >   sourceType: Int,
  >   videoFrame: VideoFrame?
  > ) = when (mCosmosApi.onFrame(videoFrame!!)) {
  >   ErrorCode.ERROR_FRAME_SKIPPED.value -> false
  >   else -> true
  > }
  > 
  > override fun getMirrorApplied() = mCosmosApi.getMirrorApplied()
  > ```
---

## 4. Contact us

> Plan 1: If you are already using Shengwang services or are in the process of docking, you can directly contact the docked sales or service.
>
> Plan 2: Send an email to [support@agora.io](mailto:support@agora.io) for consultation
>
> Plan 3: Scan the QR code to join our WeChat communication group to ask questions
>
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---

## 5. License

The sample projects are under the MIT license.