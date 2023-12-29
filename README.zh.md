# 美颜场景化API

[English](README.md) | 中文

## 简介

为降低开发者的集成美颜的难度，声网提供了美颜场景化 API。美颜场景化 API 使用**声网RTC祼数据接口**对**不同厂商的美颜处理**进行封装，力求提供最佳的美颜体验，将美颜耗时尽可能降到最低。你只需要调用简单几个场景化 API 即可实现流畅的美颜效果。

本仓库包含以下美颜场景API：

| 美颜   | 平台       | 语言           | 路径                                                         | RTC SDK 版本 | Beauty SDK 版本 |
|------|----------|--------------|------------------------------------------------------------|------------|---------------|
| 商汤   | Android  | Java/Kotlin  | [/Android/lib_sensetime](/Android/lib_sensetime)           | 4.2.6   | 9.3.1         |
| 相芯   | Android  | Java/Kotlin  | [/Android/lib_faceunity](/Android/lib_faceunity)           | 4.2.6   | 8.7.0         |
| 字节火山 | Android  | Java/Kotlin  | [/Android/lib_bytedance](/Android/lib_bytedance)           | 4.2.6   | 4.6.0         |
| 宇宙   | Android  | Java/Kotlin  | [/Android/lib_cosmos](/Android/lib_cosmos)                 | 4.2.6   | 3.7.0         |
| 商汤   | iOS      | Swift/OC     | [/iOS/BeautyAPi/SenseBeauty](/iOS/BeautyAPi/SenseBeauty)   | 4.2.6   | 9.3.1         |
| 相芯   | iOS      | Swift/OC     | [/iOS/BeautyAPi/FUBeauty](/iOS/BeautyAPi/FUBeauty)         | 4.2.6  | 8.7.0         |
| 字节火山 | iOS      | Swift/OC     | [/iOS/BeautyAPi/ByteBeauty](/iOS/BeautyAPi/ByteBeauty)     | 4.2.6   | 4.5.1         |
| 宇宙   | iOS      | Swift/OC     | [/iOS/BeautyAPi/CosmosBeauty](/iOS/BeautyAPi/CosmosBeauty) | 4.2.6   | 3.7.1         |

你可以参考下面的Demo，以集成美颜场景化API：

| 平台      | Demo                   | 备注 |
|---------|------------------------|-------|
| Android | [Android/app](Android) |       |
| iOS     | [iOS/BeautyAPi](iOS)   |       |

## 联系我们

- 如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
- 发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
- 扫码加入我们的微信交流群提问

![](https://download.agora.io/demo/release/SDHY_QA.jpg)

## 相关资源

- 你可以先参阅 [常见问题](https://doc.shengwang.cn/faq/list)
- 如果你想了解更多官方示例，可以参考 [官方 SDK 示例](https://github.com/AgoraIO)
- 如果你想了解声网 SDK 在复杂场景下的应用，可以参考 [官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看 [社区](https://github.com/AgoraIO-Community)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果需要售后技术支持, 你可以在 [声网支持](https://ticket.shengwang.cn/form) 上提交工单

## 许可证

示例项目遵守 [MIT 许可证](LICENSE)。
