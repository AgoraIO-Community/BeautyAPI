# 相芯美颜场景化API

[English](README.md) | 中文

## 前提条件
- 项目使用Kotlin插件
- 项目里已经集成了Agora RTC SDK
- 联系相芯客户拿到美颜资源以及证书

## 快速集成
1. (可选)配置相芯
- 配置依赖库
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
- 配置美颜资源/证书

| 美颜资源                 | 项目路径                            |
|----------------------|---------------------------------|
| 美妆资源(如naicha.bundle) | assets/beauty_faceunity/makeup  |
| 贴纸资源(如fashi.bundle)  | assets/beauty_faceunity/sticker |
| 证书authpack.java      | src                             |

2. 复制以下场景化接口及实现到项目里

src/main/java/io/agora/beauty/faceunity/beautyapi
├── FaceUnityBeautyAPI.kt
├── FaceUnityBeautyAPIImpl
└── utils

3. 初始化
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

4. 美颜开关(默认关)
```kotlin
mFaceUnityApi.enable(true)
```

5. 本地渲染
```kotlin
mFaceUnityApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. 设置推荐美颜参数
```kotlin
mFaceUnityApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：关闭推荐美颜参数
```

7. 销毁美颜
```kotlin
mRtcEngine.leaveChannel()
// 必须在leaveChannel后销毁
mFaceUnityApi.release()
FURenderer.getInstance().release()
mFuRenderKit.release()
```

## 自定义采集模式
美颜场景API除了能够内部直接使用RTC 祼数据接口进行美颜处理，也支持由外部传入视频帧进行处理，实现步骤如下：

1. 初始化时配置captureMode为CaptureMode.Custom
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
2. 将外部数据帧通过onFrame接口传入，处理成功会替换VideoFrame的buffer数据，即videoFrame参数既为输入也为输出
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

## 联系我们

- 如果你遇到了困难，可以先参阅 [常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考 [官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考 [官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看 [社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果需要售后技术支持, 你可以在 [Agora Dashboard](https://dashboard.agora.io) 提交工单
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO-Community/BeautyAPI/issues)

## 代码许可

The MIT License (MIT)