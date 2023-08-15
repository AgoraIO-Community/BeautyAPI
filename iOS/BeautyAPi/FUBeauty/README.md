# FaceUnity BeautyAPI

_English | [中文](README.zh.md)

## Prerequisites
- Agora RTC SDK has been integrated in the project
- Contact FaceUnity customer service to get beauty resources and license

## Quick Start
FaceUnity Configuration

1.Config Beauty Resources

| FaceUnity Beauty Resources          | Location                        |
|-------------------------------------|---------------------------------|
| FaceUnity/Lib/Resources | iOS/FULib         |
| authpack.h      | iOS/BeautyAPi/FUBeauty/authpack.h  |

2.Config Dependencies
```podfile
	pod 'fuLib', :path => 'fu.podspec'
```

3.Copy the following BeautyAPI interface and implementation into the project
```
BeautyAPI
    ├── BeautyAPI.{h,m}
    └── Render/FURender
```

4.Initialization

```swift
private lazy var beautyAPI = BeautyAPI()
private lazy var fuRender = FUBeautyRender()

let config = BeautyConfig()
config.rtcEngine = rtcEngine
config.captureMode = .agora
config.beautyRender = fuRender
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


5.Beauty On/Off (default off)

```swift
beautyAPI.enable(true)
```

6.Local Rendering

```
beautyAPI.setupLocalVideo(localView, renderMode: .hidden)
```

7.Set Recommended Beauty Parameters

```swift
beautyAPI.setBeautyPreset(.default)
// BeautyPreset.CUSTOM：Implement your own beauty parameters
```

8.Destroy BeautyAPI

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
config.beautyRender = fuRender
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