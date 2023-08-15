# BeautyAPI Demo

_English | [中文](README.zh.md)

## Prerequisites

- Android 5.0（SDK API Level 21）Above
- Android Studio 3.5+, Using java 11
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
| Android/models                                                       | app/src/main/assets/beauty_sensetime/models              |
| Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape | app/src/main/assets/beauty_sensetime/sticker_face_shape  |
| Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly      | app/src/main/assets/beauty_sensetime/style_lightly       |
| Android/smaple/SenseMeEffects/app/src/main/assets/makeup_lip         | app/src/main/assets/beauty_sensetime/makeup_lip          |
| SenseME.lic                                                          | app/src/main/assets/beauty_sensetime/license/SenseME.lic |

2. FaceUnity
   Put the FaceUnity beauty resources into the corresponding path

| FaceUnity Beauty Resources          | Location                                                |
|-------------------------------------|---------------------------------------------------------|
| makeup resource(e.g. naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup             |
| sticker resource(e.g. fashi.bundle) | app/src/main/assets/beauty_faceunity/sticker            |
| authpack.java                       | app/src/main/java/io/agora/beautyapi/demo/authpack.java |

3. ByteDance
   Unzip the ByteDance beauty resource and copy the following files/directories to the corresponding path

| ByteDance Beauty Resources      | Location                             |
|---------------------------------|--------------------------------------|
| resource/LicenseBag.bundle      | app/src/main/assets/beauty_bytedance |
| resource/ModelResource.bundle   | app/src/main/assets/beauty_bytedance |
| resource/ComposeMakeup.bundle   | app/src/main/assets/beauty_bytedance |
| resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |
| resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |

Modify the LICENSE_NAME in the app/src/main/java/io/agora/beautyapi/demo/ByteDanceActivity.kt file to the name of the applied certificate file.

### Agora AppID Configuration

> PS：This demo does not support AppId with certificate

1. Create a developer account at [agora.io](https://www.agora.io). Once you finish the signup process, you will be redirected to the Dashboard.

2. Navigate in the Dashboard tree on the left to Projects > Project List.

3. Save the App Id from the Dashboard for later use.

4. Edit the local.properties，if it does not exist, create one and then configure it whth
```xml
AGORA_APP_ID=#YOUR APP ID#
```

### Run Project

1. Edit the app/build.gradle file, change applicationId to your application id using to apply to beauty SDK.
2. Run Project


## Integrate Into Project

Each beauty api can be integrated into your project separately, see the below integration documentation for details

| Beauty     | Documentation                      |
|------------|------------------------------------|
| SenseTime  | [README](lib_sensetime/README.md)  |
| FaceUnity  | [README](lib_faceunity/README.md)  |
| ByteDance  | [README](lib_bytedance/README.md)  |

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
