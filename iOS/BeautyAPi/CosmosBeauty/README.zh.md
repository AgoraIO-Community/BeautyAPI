# 宇宙美颜场景化API

[English](README.md) | 中文

> 本文档主要介绍如何快速集成宇宙美颜场景化API。
---

## 1. 环境准备
- 项目里已经集成了Agora RTC SDK
- 联系宇宙客服拿到宇宙美颜SDK及其资源文件

---

## 2. 快速集成
- 解压宇宙SDK并将以下framework库、资源文件、证书配置到项目对应目录下

  	> | 宇宙SDK文件/目录(必须)                                | 项目目录                            |
	> |-----------------------------------------------|---------------------------------|
	> | iOS-Release-xxx/libs/CosmosEffect-xxx                       | iOS/CosmosEffect           |
	> | iOS/CosmosEffect/cv.bundle    | iOS/CosmosEffect/Frameworks/Resources/cv.bundle          |


- 如需添加贴纸及其它资源,都需放在Resource目录下

	> | 宇宙资源目录(可选)                                 | 项目目录                            |
	> |-----------------------------------------------|---------------------------------|
	> | iOS-Release-xxx/sample/BeautyKit/BeuautyUI/makeup.bundle(<span style="color:red;">风格妆</span>)                      | iOS/CosmosEffect/Frameworks/Resources/makeup.bundle          |
	> | iOS-Release-xxx/sample/BeautyKit/Resources.bundle(<span style="color:red;">贴纸</span>)   | iOS/CosmosEffect/Frameworks/Resources/Resources.bundle         

- 配置依赖库

	>```podfile
	>	pod 'CosmosEffect', :path => './CosmosEffect'
	>```

- 配置license和Bundle Identifier

	 > - BeautyAPI
	   	 - CosmosBeauty
	    	- CEBeautyRender.m
	 >
	 > - BeautyAPI
	  	 - Signing&Capabilities
	    	- Bundle Identifier
    

- 复制以下场景化接口及实现到项目里

	>```
	>BeautyAPI
	>  ├── BeautyAPI.{h,m}
	>  └── Render/CosmosRender
	>```

- 初始化

	>```swift
	>private lazy var beautyAPI = BeautyAPI()
	>private lazy var cosmosRender = CosmosBeautyRender()
	>
	>let config = BeautyConfig()
	>config.rtcEngine = rtcEngine
	>config.captureMode = .agora
	>config.beautyRender = cosmosRender
	>config.statsEnable = false
	>config.statsDuration = 1
	>config.eventCallback = { stats in
	>    print("min == \(stats.minCostMs)")
	>    print("max == \(stats.maxCostMs)")
	>    print("averageCostMs == \(stats.averageCostMs)")
	>}
	>let result = beautyAPI.initialize(config)
	>if result != 0 {
	>    print("initialize error == \(result)")
	>}
	>```

- 美颜开关(默认关)

	>```swift
	>beautyAPI.enable(true)
	>```

- 本地渲染

	>```swift
	>beautyAPI.setupLocalVideo(localView, renderMode: .hidden)
	>rtcEngine.startPreview()
	>```

- 设置推荐美颜参数
	>```swift
	>beautyAPI.setBeautyPreset(.default) // BeautyPreset.CUSTOM：自己实现美颜参数
	>```

- 销毁美颜
	>```swift
	>rtcEngine.leaveChannel()
	>beautyAPI.destroy()
	>AgoraRtcEngineKit.destroy()
	>```
---

## 3. 自定义采集模式
美颜场景API除了能够内部直接使用RTC 祼数据接口进行美颜处理，也支持由外部传入视频帧进行处理，实现步骤如下：

- 初始化时配置captureMode为CaptureMode.Custom

	>```swift
	>let config = BeautyConfig()
	>config.rtcEngine = rtcEngine
	>config.captureMode = .custom
	>config.beautyRender = cosmosRender
	>config.statsEnable = false
	>config.statsDuration = 1
	>config.eventCallback = { stats in
	>    print("min == \(stats.minCostMs)")
	>    print("max == \(stats.maxCostMs)")
	>    print("averageCostMs == \(stats.averageCostMs)")
	>}
	>let result = beautyAPI.initialize(config)
	>if result != 0 {
	>    print("initialize error == \(result)")
	>}
	>```
	
- 将外部数据帧通过onFrame接口传入，处理成功会替换VideoFrame的buffer数据，即videoFrame参数既为输入也为输出

	>```swift
	>beautyAPI.onFrame(pixelBuffer) { pixelBuffer in
	>    videoFrame.pixelBuffer = pixelBuffer
	>}
	>```
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
