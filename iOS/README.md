# BeautyAPI Demo

_English | [中文](README.zh.md)

## Prerequisites

- iOS 11 And Above
- Xcode 13 And Above
- Contact SenseTime customer service to get SenseTime's beauty SDK, beauty resources and license
- Contact FaceUnity customer service to get beauty resources and license
- Contact ByteDance customer service to get ByteDance's beauty SDK, beauty resources and license

## Quick Start
### Beauty SDK Configuration

> PS：The project can run without config beauty sdk, but the preview may be black.

1. SenseTime
   Unzip the SenseTime Beauty SDK and copy the following files/directories to the corresponding path

| SenseTime Beauty SDK                                                 | Location                                                 |
|----------------------------------------------------------------------|----------------------------------------------------------|
| SenseAR_Effects__*/models | iOS/SenseLib/st\_mobile\_sdk/models  |
| SenseAR_Effects__*/include | iOS/SenseLib/st\_mobile\_sdk/include  |
| SenseAR_Effects__*/libs/ios\_os-universal | iOS/SenseLib/st\_mobile\_sdk/ios\_os-universal  |
|SenseAR_Effects__*/license/SENSEME.lic                                                          | iOS/SenseLib/SENSEME.lic |

2. FaceUnity
   Put the FaceUnity beauty resources into the corresponding path

| FaceUnity Beauty          | Location                                             |
|-------------------------------------|------------------------------------------------------|
| FaceUnity/Lib/Resources | iOS/FULib         |
| authpack.h      | iOS/BeautyAPi/FUBeauty/authpack.h  |

3. ByteDance
   Unzip the ByteDance beauty resource and copy the following files/directories to the corresponding path

| ByteDance Beauty Resources      | Location                             |
|---------------------------------|--------------------------------------|
| BytedEffects/app/Resource                       | iOS/ByteEffectLib/Resource           |
| byted_effect_ios_static/iossample\_static/libeffect-sdk.a                    | iOS/ByteEffectLib/ibeffect-sdk.a           |
| byted_effect_ios_static/iossample\_static/include/BytedEffectSDK                    | iOS/ByteEffectLib/BytedEffectSDK           |

### Agora AppID Configuration

> PS：This demo does not support AppId with certificate

1. Create a developer account at [agora.io](https://www.agora.io). Once you finish the signup process, you will be redirected to the Dashboard.

2. Navigate in the Dashboard tree on the left to Projects > Project List.

3. Save the App Id from the Dashboard for later use.

4. Edit the KeyCenter.swift, configure it whth
```
AppId=#YOUR APP ID#
```

### Run Project

1. Edit the BeautyAPi Project, change Bundle Identifier to your application id using to apply to beauty SDK.
2. Run Project


## Integrate Into Project

Each beauty api can be integrated into your project separately, see the below integration documentation for details

| Beauty     | Documentation                             |
|------------|-------------------------------------------|
| SenseTime  | [SenseBeauty](BeautyAPi/SenseBeaufy/README.md)   |
| FaceUnity  | [fuBeauty](BeautyAPi/FUBeauty/README.md)  |
| ByteDance  | [byteBeauty](BeautyAPi/ByteBeaufy/README.md)   |

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
