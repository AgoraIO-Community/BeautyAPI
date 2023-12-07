# ByteDance BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- Agora RTC SDK has been integrated in the project
- Contact ByteDance customer service to get ByteDance's beauty SDK, beauty resources and license

## Quick Start
1.Unzip the ByteDance SDK and configure the following framework libraries, resource files, and certificates to the corresponding directory of the project

| Cosmos SDK(Must)                                | Location                |
|----------------------------------------------|-------------------------|
| iOS-Release-xxx/libs/CosmosEffect-xxx                       | iOS/CosmosEffect           |
| iOS/CosmosEffect/cv.bundle    | iOS/CosmosEffect/Frameworks/Resources/cv.bundle          |

##### If you need to add stickers and other resources, they must be placed in the Resource directory
| Cosmos Resource(Option)                                 | Location                           |
|-----------------------------------------------|---------------------------------|
| iOS-Release-xxx/sample/BeautyKit/BeuautyUI/makeup.bundle(<span style="color:red;">makeup</span>)                      | iOS/CosmosEffect/Frameworks/Resources/makeup.bundle          |
| iOS-Release-xxx/sample/BeautyKit/Resources.bundle(<span style="color:red;">sticker</span>)   | iOS/CosmosEffect/Frameworks/Resources/Resources.bundle         


2.Configuration dependency library
```podfile
	pod 'CosmosEffect', :path => './CosmosEffect'
```

3.Configuration license and Bundle Identifier

- BeautyAPI
  - CosmosBeauty
    - CEBeautyRender.m
    
- BeautyAPI
  - Signing&Capabilities
    - Bundle Identifier

4.Copy the following BeautyAPI interface and implementation into the project

```
BeautyAPI
    ├── BeautyAPI.{h,m}
    └── Render/CosmosRender
```

5.Initialization

```swift
private lazy var beautyAPI = BeautyAPI()
private lazy var cosmosRender = CosmosBeautyRender()

let config = BeautyConfig()
config.rtcEngine = rtcEngine
config.captureMode = .agora
config.beautyRender = cosmosRender
config.statsEnable = false
config.statsDuration = 1
config.eventCallback = { stats in
    print("min == \(stats.minCostMs)")
    print("max == \(stats.maxCostMs)")
    print("averageCostMs == \(stats.averageCostMs)")
}
let result = beautyAPI.initialize(config)
if result != 0 {
    print("initialize error == \(result)")
}
```


6.Beauty On/Off (default off)

```swift
beautyAPI.enable(true)
```

7.Local Rendering

```
beautyAPI.setupLocalVideo(localView, renderMode: .hidden)
```

8.Set Recommended Beauty Parameters

```swift
beautyAPI.setBeautyPreset(.default)
// BeautyPreset.CUSTOM：Implement your own beauty parameters
```

9.Destroy BeautyAPI

```swift
rtcEngine.leaveChannel()
beautyAPI.destroy()
AgoraRtcEngineKit.destroy()
```

## Custom Capture Mode
The BeautyAPI also supports external video frames for processing. The implementation steps are as follows:

1.Initialize BeautyAPI with CaptureMode.Custom

```swift
let config = BeautyConfig()
config.rtcEngine = rtcEngine
config.captureMode = .custom
config.beautyRender = cosmosRender
config.statsEnable = false
config.statsDuration = 1
config.eventCallback = { stats in
    print("min == \(stats.minCostMs)")
    print("max == \(stats.maxCostMs)")
    print("averageCostMs == \(stats.averageCostMs)")
}
let result = beautyAPI.initialize(config)
if result != 0 {
    print("initialize error == \(result)")
}
```

2.Pass external video frame to BeautyAPI by onFrame interface.

```swift
beautyAPI.onFrame(pixelBuffer) { pixelBuffer in
    videoFrame.pixelBuffer = pixelBuffer
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