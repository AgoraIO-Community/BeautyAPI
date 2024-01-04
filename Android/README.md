# BeautyAPI Demo

_English | [中文](README.zh.md)

> This document mainly introduces how to quickly run through the beauty scene API sample code.
> 
> **Demo Effect:**
>
> <img src="imgs/app_page_launch.png" width="300" />
---

## 1. Prerequisites

- Android 5.0（SDK API Level 21）Above
- Android Studio 3.5+, Using java 11
- Android 5.0 and above mobile devices.

---

## 2. Run Project
- Get Agora App
   > - Create a developer account at [agora.io](https://www.agora.io/en/). Once you finish the signup process, you will be redirected to the Dashboard.
   >
   > - Navigate in the Dashboard tree on the left to Projects > Project List.
   > 
   > - Save the App Id from the Dashboard for later use.

- Create local.properties file in android root direction, and fill in the agora app id to the file:

```xml
AGORA_APP_ID=#YOUR APP ID#
```

- **Contact the beauty manufacturer to obtain the corresponding beauty certificate and resources, and make the following configurations**(If the beautification certificate and resources are not configured, the corresponding manufacturer’s beautification will display a black screen.)
   - **SenseTime(Optional)**
      > - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
      >
      > - Unzip the SenseTime Beauty SDK and copy the following files/directories to the corresponding path
      >
      > | SenseTime Beauty SDK                                                 | Location                                                 |
      > |----------------------------------------------------------------------|----------------------------------------------------------|
      > | Android/models                                                       | app/src/main/assets/beauty_sensetime/models              |
      > | Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape | app/src/main/assets/beauty_sensetime/sticker_face_shape  |
      > | Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly      | app/src/main/assets/beauty_sensetime/style_lightly       |
      > | Android/smaple/SenseMeEffects/app/src/main/assets/makeup_lip         | app/src/main/assets/beauty_sensetime/makeup_lip          |
      > | SenseME.lic                                                          | app/src/main/assets/beauty_sensetime/license/SenseME.lic |
   - **FaceUnity(Optional)**
      > - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
      > 
      > - Put the FaceUnity beauty resources into the corresponding path
      >
      > | FaceUnity Beauty Resources          | Location                                                                  |
      > |-------------------------------------|---------------------------------------------------------------------------|
      > | makeup resource(e.g. naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup                               |
      > | sticker resource(e.g. fashi.bundle) | app/src/main/assets/beauty_faceunity/sticker                              |
      > | authpack.java                       | app/src/main/java/io/agora/beautyapi/demo/module/faceunity/authpack.java  |
   - **ByteDance(Optional)**
      > - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
      >
      > - Modify the LICENSE_NAME in the [ByteDanceBeautySDK.kt](app/src/main/java/io/agora/beautyapi/demo/module/bytedance/ByteDanceBeautySDK.kt file to the name of the applied certificate file).
      >
      > - Unzip the ByteDance beauty resource and copy the following files/directories to the corresponding path
      >
      > | ByteDance Beauty Resources      | Location                             |
      > |---------------------------------|--------------------------------------|
      > | resource/LicenseBag.bundle      | app/src/main/assets/beauty_bytedance |
      > | resource/ModelResource.bundle   | app/src/main/assets/beauty_bytedance |
      > | resource/ComposeMakeup.bundle   | app/src/main/assets/beauty_bytedance |
      > | resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |
      > | resource/StickerResource.bundle | app/src/main/assets/beauty_bytedance |
   - **Cosmos(Optional)**
      > - Configure the package name applicationId corresponding to the certificate in [app/build.gradle](app/build.gradle)
      >
      > - Unzip the Cosmos beauty resource and copy the following files/directories to the corresponding path
      >
      > | ByteDance Beauty Resources                | Location                                         |
      > |-------------------------------------------|--------------------------------------------------|
      > | sample/app/src/main/assets/model-all.zip  | app/src/main/assets/beauty_cosmos/model-all.zip  |
      > | sample/app/src/main/assets/cosmos.zip     | app/src/main/assets/beauty_cosmos/cosmos.zip     |
- Run
   > - Use AndroidStudio to open the `Android` project and click Run.

---

## 3. Integrate Into Project

> Each beauty api can be integrated into your project separately, see the below integration documentation for details
> 
> | Beauty    | Documentation                                                                                                           |
> |-------------------------------------------------------------------------------------------------------------------------| ------------------------------------------------------------ |
> | SenseTime | [Official website document](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/sensetime/integrate) |
> | FaceUnity | [Official website document](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/faceunity/integrate) |
> | ByteDance | [Official website document](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/bytedance/integrate) |
> | Cosmos    | [Cosmos beauty](./lib_cosmos/README.zh.md)                                                                              |

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

### 5. License

The sample projects are under the [MIT license](../LICENSE).

