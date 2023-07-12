# 字节/火山美颜场景化API

[English](README.md) | 中文

## 前提条件
- 项目使用Kotlin插件
- 项目里已经集成了Agora RTC SDK
- 联系字节客服拿到字节/火山的美颜SDK及其资源文件

## 快速集成
1. (可选)解压字节/火山SDK并将以下aar库、资源文件、证书配置到项目对应目录下

| 字节/火山SDK文件/目录                                 | 项目目录                            |
|-----------------------------------------------|---------------------------------|
| resource/LicenseBag.bundle                    | assets/beauty_bytedance         |
| resource/ModelResource.bundle                 | assets/beauty_bytedance         |
| resource/ComposeMakeup.bundle                 | assets/beauty_bytedance         |
| resource/StickerResource.bundle               | assets/beauty_bytedance         |
| resource/StickerResource.bundle               | assets/beauty_bytedance         |
| byted_effect_andr/libs/effectAAR-release.aar  | libs                            |

2. 复制以下场景化接口及实现到项目里
> 请保留原有包名目录，以便于代码升级
```xml
src/main/java/io/agora/beautyapi/bytedance
   ├── ByteDanceBeautyAPI.kt
   ├── ByteDanceBeautyAPIImpl.kt
   └── utils
```

3. 初始化
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

4. 美颜开关(默认关)
```kotlin
mByteDanceApi.enable(true)
```

5. 本地渲染
```kotlin
mByteDanceApi.setupLocalVideo(mBinding.localVideoView, Constants.RENDER_MODE_FIT)
```

6. 设置推荐美颜参数
```kotlin
mByteDanceApi.setBeautyPreset(BeautyPreset.DEFAULT) // BeautyPreset.CUSTOM：关闭推荐美颜参数
```

7. 销毁美颜
```kotlin
mRtcEngine.leaveChannel()
// 必须在leaveChannel后销毁
mByteDanceApi.release()
mEffectManager.destroy()
```

## 自定义采集模式
美颜场景API除了能够内部直接使用RTC 祼数据接口进行美颜处理，也支持由外部传入视频帧进行处理，实现步骤如下：

1. 初始化时配置captureMode为CaptureMode.Custom
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
2. 将外部数据帧通过onFrame接口传入，处理成功会替换VideoFrame的buffer数据，即videoFrame参数既为输入也为输出
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