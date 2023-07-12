# 美颜场景化API Demo

[English](README.md) | 中文

## 前提条件

- 最低兼容 Android 5.0（SDK API Level 21）
- Android Studio 3.5及以上版本，使用Java 11
- 联系商汤客服拿到商汤的美颜SDK、美颜资源以及证书
- 联系字节火山客户拿到火山SDK、美颜资源以及证书
- 联系相芯客户拿到美颜资源以及证书

## 快速跑通
### 配置美颜SDK

> PS：没有配置项目能正常运行，但是对应美颜可能会黑屏

1. 商汤美颜
解压商汤美颜SDK并复制以下文件/目录到对应路径下

| 商汤SDK文件/目录                                                           | 项目路径                                                     |
|----------------------------------------------------------------------|----------------------------------------------------------|
| Android/models                                                       | app/src/main/assets/beauty_sensetime/models              |
| Android/smaple/SenseMeEffects/app/src/main/assets/sticker_face_shape | app/src/main/assets/beauty_sensetime/sticker_face_shape  |
| Android/smaple/SenseMeEffects/app/src/main/assets/style_lightly      | app/src/main/assets/beauty_sensetime/style_lightly       |
| Android/smaple/SenseMeEffects/app/src/main/assets/makeup_lip         | app/src/main/assets/beauty_sensetime/makeup_lip          |
| SenseME.lic                                                          | app/src/main/assets/beauty_sensetime/license/SenseME.lic |

2. 相芯美颜
将相芯美颜资源放入对应路径下

| 美颜资源                 | 项目路径                                                  |
|----------------------|-------------------------------------------------------|
| 美妆资源(如naicha.bundle) | app/src/main/assets/beauty_faceunity/makeup           |
| 贴纸资源(如fashi.bundle)  | app/src/main/assets/beauty_faceunity/sticker          |
| 证书authpack.java      | app/src/main/java/io/agora/beauty/demo/authpack.java  |

3. 字节/火山美颜
解压字节/火山美颜资源并复制以下文件/目录到对应路径下

| 字节SDK文件/目录                                       | 项目路径                                                  |
|--------------------------------------------------|-------------------------------------------------------|
| resource/LicenseBag.bundle                       | app/src/main/assets/beauty_bytedance           |
| resource/ModelResource.bundle                    | app/src/main/assets/beauty_bytedance           |
| resource/ComposeMakeup.bundle                    | app/src/main/assets/beauty_bytedance           |
| resource/StickerResource.bundle                  | app/src/main/assets/beauty_bytedance           |
| resource/StickerResource.bundle                  | app/src/main/assets/beauty_bytedance           |

### 配置声网AppID

> PS：这个demo不支持带证书的AppId

1. 在[agora.io](https://www.shengwang.cn/)创建一个开发者账号

2. 前往后台页面，点击左部导航栏的 项目 > 项目列表 菜单

3. 复制后台的 App Id 并备注，稍后启动应用时会用到它

4. 编辑local.properties，如果不存在则创建一个，并配置上
```xml
AGORA_APP_ID=#YOUR APP ID#
```

### 运行项目

1. 编辑app/build.gradle， 修改applicationId包名成申请美颜时所用包名
2. 运行项目


## 集成到项目

每个美颜可以单独集成到自己的项目，详见对应的集成说明文档

| 美颜  | 集成说明                                         |
|-----|----------------------------------------------|
| 商汤  | [lib_sensetime](lib_sensetime/README.zh.md)  |
| 相芯  | [lib_sensetime](lib_faceunity/README.zh.md)  |
| 字节  | [lib_sensetime](lib_bytedance/README.zh.md)  |

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
