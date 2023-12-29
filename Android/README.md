# BeautyAPI Demo

_English | [中文](README.zh.md)

# Introduction

This document mainly introduces how to quickly run through the beauty scene API sample code.

## Prerequisites

- Android 5.0（SDK API Level 21）Above
- Android Studio 3.5+, Using java 11
- Android 5.0 and above mobile devices.
- (Optional) Contact SenseTime customer service to get SenseTime's beauty SDK, beauty resources and license
- (Optional) Contact FaceUnity customer service to get beauty resources and license
- (Optional) Contact ByteDance customer service to get ByteDance's beauty SDK, beauty resources and license
- (Optional) Contact Cosmos customer service to get ByteDance's beauty SDK, beauty resources and license

## Quick Start
### Agora AppID Configuration

> PS：This demo does not support AppId with certificate

1. Create a developer account at [agora.io](https://www.agora.io/en/). Once you finish the signup process, you will be redirected to the Dashboard.

2. Navigate in the Dashboard tree on the left to Projects > Project List.

3. Save the App Id from the Dashboard for later use.

4. Edit the local.properties in android root direction，if it does not exist, create one and then configure it whth

```xml
AGORA_APP_ID=#YOUR APP ID#
```

### Beauty SDK Configuration

> PS：**You can only configure one kind of beauty**. You can also enter the beauty without configuration, but the corresponding beauty will display a black screen.

1. SenseTime
   - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
   - Unzip the SenseTime Beauty SDK and copy the following files/directories to the corresponding path

| SenseTime Beauty SDK                                                 | Location                                                 |
|----------------------------------------------------------------------|----------------------------------------------------------|
| Android/models                                                       | app/src/main/assets/beauty_sensetime/models              |
| Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape | app/src/main/assets/beauty_sensetime/sticker_face_shape  |
| Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly      | app/src/main/assets/beauty_sensetime/style_lightly       |
| Android/smaple/SenseMeEffects/app/src/main/assets/makeup_lip         | app/src/main/assets/beauty_sensetime/makeup_lip          |
| SenseME.lic                                                          | app/src/main/assets/beauty_sensetime/license/SenseME.lic |

2. FaceUnity
   - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
   - Put the FaceUnity beauty resources into the corresponding path

| FaceUnity Beauty Resources          | Location                                                                  |
|-------------------------------------|---------------------------------------------------------------------------|
| makeup resource(e.g. naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup                               |
| sticker resource(e.g. fashi.bundle) | app/src/main/assets/beauty_faceunity/sticker                              |
| authpack.java                       | app/src/main/java/io/agora/beautyapi/demo/module/faceunity/authpack.java  |

3. ByteDance
   - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
   - Modify the LICENSE_NAME in the [ByteDanceBeautySDK.kt](app/src/main/java/io/agora/beautyapi/demo/module/bytedance/ByteDanceBeautySDK.kt file to the name of the applied certificate file).
   - Unzip the ByteDance beauty resource and copy the following files/directories to the corresponding path

| ByteDance Beauty Resources      | Location                             |
|---------------------------------|--------------------------------------|
| resource/LicenseBag.bundle      | app/src/main/assets/beauty_bytedance |
| resource/ModelResource.bundle   | app/src/main/assets/beauty_bytedance |
| resource/ComposeMakeup.bundle   | app/src/main/assets/beauty_bytedance |
| resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |
| resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |

4. Cosmos
   - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](
   - Modify the LICENSE in the [CosmosBeautyWrapSDK.kt](app/src/main/java/io/agora/beautyapi/demo/module/cosmos/CosmosBeautyWrapSDK.kt) file to the applied license code.
   - Unzip the Cosmos beauty resource and copy the following files/directories to the corresponding path

| ByteDance Beauty Resources                | Location                                         |
|-------------------------------------------|--------------------------------------------------|
| sample/app/src/main/assets/model-all.zip  | app/src/main/assets/beauty_cosmos/model-all.zip  |
| sample/app/src/main/assets/cosmos.zip     | app/src/main/assets/beauty_cosmos/cosmos.zip     |



### Run Project

1. Edit the app/build.gradle file, change applicationId to your application id using to apply to beauty SDK.
2. Run Project


## Integrate Into Project

Each beauty api can be integrated into your project separately, see the below integration documentation for details

| Beauty    | Documentation                     |
|-----------|-----------------------------------|
| SenseTime | [README](lib_sensetime/README.md) |
| FaceUnity | [README](lib_faceunity/README.md) |
| ByteDance | [README](lib_bytedance/README.md) |
| Cosmos    | [README](lib_cosmos/README.md)    |

- ## Contact us
  
  - If you are already using Shengwang services or are in the process of docking, you can directly contact the docked sales or service.
  - Send an email to [support@agora.io](mailto:support@agora.io) for consultation
  - Scan the QR code to join our WeChat communication group to ask questions
  
  ![](https://download.agora.io/demo/release/SDHY_QA.jpg)

## License

The sample projects are under the MIT license.
