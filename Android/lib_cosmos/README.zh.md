# 宇宙美颜场景化API

[English](README.md) | 中文

> 本文档主要介绍如何快速集成宇宙美颜场景化API。
---

## 1. 环境准备

- 项目使用Kotlin插件
- 项目里已经集成了Agora RTC SDK
- 联系字节客服拿到宇宙的美颜SDK及其资源文件
---

## 2. 快速集成
- 解压宇宙美颜SDK并将以下aar库、资源文件、证书配置到项目对应目录下
  > | 宇宙SDK文件/目录                                   | 项目目录                                            |
  > |----------------------------------------------|-------------------------------------------------|
  > | sample/app/src/main/assets/model-all.zip     | app/src/main/assets/beauty_cosmos/model-all.zip |
  > | sample/app/src/main/assets/cosmos.zip        | app/src/main/assets/beauty_cosmos/cosmos.zip    |
  > | sample/app/libs/beautysdk-3.7.0-20230301.aar | libs                                            |

- 复制以下场景化接口及实现到项目里
  > 请保留原有包名目录，以便于代码升级
  > 
  > ```xml
  > src/main/java/io/agora/beautyapi/cosmos
  >   ├── CosmosBeautyAPI.kt
  >   ├── CosmosBeautyAPIImpl.kt
  >   └── utils
  >```

- 初始化美颜
  > 初始化前需要先复制字节美颜SDK所需的资源model-all.zip、cosmos.zip到sdcard上，并解压到对应目录，然后进行授权认证并renderModuleManager实例传给ByteDanceBeautyAPI。
  >
  > ```kotlin
  > private val mCosmosApi by lazy {
  >     createCosmosBeautyAPI()
  > }
  > mCosmosApi.initialize(
  >     Config(
  >         application,
  >         mRtcEngine,
  >         CosmosBeautyWrapSDK.renderModuleManager!!,
  >         captureMode = CaptureMode.Agora,
  >         statsEnable = true,
  >         eventCallback = EventCallback(
  >             onBeautyStats = { stats ->
  >                 Log.d(TAG, "onBeautyStats >> $stats")
  >             }
  >         )
  >     )
  > )
  > ```

- 美颜开关(默认关)
  > ```kotlin
  > mCosmosApi.enable(true)
  > ```

- 渲染本地视图
  > ```kotlin
  > mCosmosApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
  > ```

- 设置推荐美颜参数
  > ```kotlin
  > mCosmosApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：关闭推荐美颜参数
  > ```

- 更新镜像配置
  > ```kotlin
  > val cameraConfig = CameraConfig(
  >     frontMirror = MirrorMode.MIRROR_LOCAL_REMOTE,
  >     backMirror = MirrorMode.MIRROR_NONE
  > )
  > mCosmosApi.updateCameraConfig(cameraConfig)
  > ```

- 销毁美颜
  > 调用时机必须在leaveChannel/stopPreview/registerVideoFrameObserver(null)之后，RtcEngine.destroy之前！
  > 
  > ```kotlin
  > mRtcEngine.leaveChannel()
  > mRtcEngine.stopPreview()
  > if (isCustomCaptureMode) {
  >     // 如果使用Custom采集模式并注册过裸数据回调，需要调用registerVideoFrameObserver将observer置空
  >     mRtcEngine.registerVideoFrameObserver(null)
  > }
  > mCosmosApi.release()
  > RtcEngine.destroy()
  > ```
---

## 3. 自定义采集模式
美颜场景API除了能够内部直接使用RTC 祼数据接口进行美颜处理，也支持由外部传入视频帧进行处理，实现步骤如下：

- 初始化时配置captureMode为CaptureMode.Custom
  > ```kotlin
  > mCosmosApi.initialize(
  >     Config(
  >         application,
  >         mRtcEngine,
  >         CosmosBeautyWrapSDK.renderModuleManager!!,
  >         captureMode = CaptureMode.Custom,
  >         statsEnable = true,
  >         eventCallback = EventCallback(
  >             onBeautyStats = { stats ->
  >                 Log.d(TAG, "onBeautyStats >> $stats")
  >             }
  >         )
  >     )
  > )
  > ```
- 将外部数据帧通过onFrame接口传入，处理成功会替换VideoFrame的buffer数据，即videoFrame参数既为输入也为输出
  > ```kotlin
  > override fun onCaptureVideoFrame(
  >     sourceType: Int,
  >     videoFrame: VideoFrame?
  > ) = when (mCosmosApi.onFrame(videoFrame!!)) {
  >     ErrorCode.ERROR_FRAME_SKIPPED.value -> false
  >     else -> true
  > }
  > 
  > override fun getMirrorApplied() = mCosmosApi.getMirrorApplied()
  > ```
---

## 4. 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
>
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
>
> 方案3：扫码加入我们的微信交流群提问
>
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---

## 5. 代码许可

The MIT License (MIT)