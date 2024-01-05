# Cosmos BeautyAPI

_English | [中文](README.zh.md)

> This document mainly introduces how to quickly integrate the Cosmos Beauty Scenario API.

---

## 1. Prerequisites
- Agora RTC SDK has been integrated in the project
- Contact Cosmos customer service to get Cosmos beauty SDK, beauty resources and license

---

## 2. Quick Integration
- Unzip the Cosmos SDK and configure the following framework libraries, resource files, and certificates to the corresponding directory of the project

	>| Cosmos SDK(Must)                                | Location                |
	>|----------------------------------------------|-------------------------|
	>| iOS-Release-xxx/libs/CosmosEffect-xxx                       | iOS/CosmosEffect           |
	>| iOS/CosmosEffect/cv.bundle    | iOS/CosmosEffect/Frameworks/Resources/cv.bundle          |

- If you need to add stickers and other resources, they must be placed in the Resource directory

	>| Cosmos Resource(Option)                                 | Location                           |
	>|-----------------------------------------------|---------------------------------|
	>| iOS-Release-xxx/sample/BeautyKit/BeuautyUI/makeup.bundle(<span style="color:red;">makeup</span>)                      | iOS/CosmosEffect/Frameworks/Resources/makeup.bundle          |
	>| iOS-Release-xxx/sample/BeautyKit/Resources.bundle(<span style="color:red;">sticker</span>)   | iOS/CosmosEffect/Frameworks/Resources/Resources.bundle         


- Configuration dependency library
	>```podfile
	>	pod 'CosmosEffect', :path => './CosmosEffect'
	>```

- Configuration license and Bundle Identifier

	>- BeautyAPI
	>  - CosmosBeauty
	>    - CEBeautyRender.m
	    
	>- BeautyAPI
	>  - Signing&Capabilities
	>    - Bundle Identifier

- Copy the following BeautyAPI interface and implementation into the project

	>```
	>BeautyAPI
	>    ├── BeautyAPI.{h,m}
	>    └── Render/CosmosRender
	>```

- Initialization

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


- Beauty On/Off (default off)

	>```swift
	>beautyAPI.enable(true)
	>```

- Local Rendering

	>```
	>beautyAPI.setupLocalVideo(localView, renderMode: .hidden)
	>```

- Set Recommended Beauty Parameters

	>```swift
	>beautyAPI.setBeautyPreset(.default)
	>// BeautyPreset.CUSTOM：Implement your own beauty parameters
	>```

- Destroy BeautyAPI

	>```swift
	>rtcEngine.leaveChannel()
	>beautyAPI.destroy()
	>AgoraRtcEngineKit.destroy()
	>```
---

## 3. Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

- Initialize BeautyAPI with CaptureMode.Custom

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

- Pass external video frame to BeautyAPI by onFrame interface.

	>```swift
	>beautyAPI.onFrame(pixelBuffer) { pixelBuffer in
	>    videoFrame.pixelBuffer = pixelBuffer
	>}
	>```
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