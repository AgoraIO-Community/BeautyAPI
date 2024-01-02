# 美颜场景化API Demo

[English](README.md) | 中文

> 本文档主要介绍如何快速跑通美颜场景化API示例代码。
> 
> **Demo 效果：**
>
> <img src="imgs/app_page_launch.png" width="300" />
---


## 1. 环境准备

- 最低兼容 Android 5.0（SDK API Level 21）
- Android Studio 3.5及以上版本，使用Java 11
- Android 5.0 及以上的手机设备。

---

## 2. 运行示例
- 获取声网 App ID -------- [声网Agora - 文档中心 - 如何获取 App ID](https://docs.agora.io/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id)
  > - 进入[控制台](https://console.shengwang.cn/)
  > 
  > - 点击创建应用
      > 
      > <img src="imgs/console_create_project.jpeg" width="1080" />
  >
  > - 选择调试模式并创建（本示例仅支持调试模式）
      >
      > <img src="imgs/console_init_project.png" width="1080" />
  > 
  > - 得到 App ID
      >
      > <img src="imgs/console_copy_appid.png" width="1080" />

- 在`Android`目录下创建`local.properties`（如果存在则不需要重复创建），在`Android/local.properties`里填写需要的声网 App ID

```xml
AGORA_APP_ID=<=声网 App ID=>
```

- **联系美颜厂家获取对应的美颜证书和资源**（如果没有配置需要的美颜证书和资源无法进行以下配置，对应厂家美颜显示会黑屏）

- 配置美颜证书/资源
  - **商汤美颜（可选）**
    > - 在[app/build.gradle](app/build.gradle)里配置证书对应的包名applicationId
    >
    > - 解压商汤美颜SDK并复制以下文件/目录到对应路径下
    >
    > | 商汤SDK文件/目录                                                           | 项目路径                                                     |
    > |----------------------------------------------------------------------|----------------------------------------------------------|
    > | Android/models                                                       | app/src/main/assets/beauty_sensetime/models              |
    > | Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape | app/src/main/assets/beauty_sensetime/sticker_face_shape  |
    > | Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly      | app/src/main/assets/beauty_sensetime/style_lightly       |
    > | Android/smaple/SenseMeEffects/app/src/main/assets/makeup_lip         | app/src/main/assets/beauty_sensetime/makeup_lip          |
    > | SenseME.lic                                                          | app/src/main/assets/beauty_sensetime/license/SenseME.lic |
  - **相芯美颜（可选）**
    > - 在[app/build.gradle](app/build.gradle)里配置证书对应的包名applicationId
    >
    > - 将相芯美颜资源放入对应路径下
    >
    > | 美颜资源                 | 项目路径                                                                      |
    > |----------------------|---------------------------------------------------------------------------|
    > | 美妆资源(如naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup                               |
    > | 贴纸资源(如fashi.bundle)  | app/src/main/assets/beauty_faceunity/sticker                              |
    > | 证书authpack.java      | app/src/main/java/io/agora/beautyapi/demo/module/faceunity/authpack.java  |
  - **字节火山美颜（可选）**
    > - 在[app/build.gradle](app/build.gradle)里配置证书对应的包名applicationId
    >
    > - 修改[ByteDanceBeautySDK.kt](app/src/main/java/io/agora/beautyapi/demo/module/bytedance/ByteDanceBeautySDK.kt)文件里LICENSE_NAME为申请到的证书文件名
    > 
    > - 解压字节/火山美颜资源并复制以下文件/目录到对应路径下
    >
    > | 字节SDK文件/目录                                       | 项目路径                                  |
    > |--------------------------------------------------|---------------------------------------|
    > | resource/LicenseBag.bundle                       | app/src/main/assets/beauty_bytedance  |
    > | resource/ModelResource.bundle                    | app/src/main/assets/beauty_bytedance  |
    > | resource/ComposeMakeup.bundle                    | app/src/main/assets/beauty_bytedance  |
    > | resource/StickerResource.bundle                  | app/src/main/assets/beauty_bytedance  |
    > | resource/StickerResource.bundle                  | app/src/main/assets/beauty_bytedance  |
  - **宇宙美颜（可选）**
    > - 在[app/build.gradle](app/build.gradle)里配置证书对应的包名applicationId
    > 
    > - 修改[CosmosBeautyWrapSDK.kt](app/src/main/java/io/agora/beautyapi/demo/module/cosmos/CosmosBeautyWrapSDK.kt)文件里LICENSE为申请到的证书
    > 
    > - 获取宇宙美颜资源并复制以下文件到对应路径下
    > 
    > | 宇宙SDK文件/目录                                | 项目路径                                            |
    > |-------------------------------------------|-------------------------------------------------|
    > | sample/app/src/main/assets/model-all.zip  | app/src/main/assets/beauty_cosmos/model-all.zip |
    > | sample/app/src/main/assets/cosmos.zip     | app/src/main/assets/beauty_cosmos/cosmos.zip    |

- 运行项目
  > - 使用AndroidStudio打开`Android`项目，点击运行即可

---

### 集成到项目

> 每个美颜可以单独集成到自己的项目，详见对应的集成说明文档
> 
> | 美颜     | 集成说明                                                     |
> | -------- | ------------------------------------------------------------ |
> | 商汤     | [官网文档](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/sensetime/integrate) |
> | 相芯     | [官网文档](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/faceunity/integrate) |
> | 字节火山 | [官网文档](https://doc.shengwang.cn/doc/showroom/android/advanced-features/beauty/bytedance/integrate) |
> | 宇宙     | 暂无                                                         |
---

### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务；
>
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
>
> 方案3：扫码加入我们的微信交流群提问
>
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---

## 代码许可

The MIT [License (MIT)](../LICENSE)
