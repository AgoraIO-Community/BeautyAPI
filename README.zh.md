# 美颜场景化API

[English](README.md) | 中文

### 概述

为降低开发者的集成美颜的难度，声网提供了美颜场景化 API。美颜场景化 API 使用**声网RTC祼数据接口**对**不同厂商的美颜处理**进行封装，力求提供最佳的美颜体验，将美颜耗时尽可能降到最低。你只需要调用简单几个场景化 API 即可实现流畅的美颜效果。

本仓库包含以下美颜场景API：

| 美颜   | 平台       | 语言           | 路径                                                         | RTC SDK 版本 | Beauty SDK 版本 |
|------|----------|--------------|------------------------------------------------------------|------------|---------------|
| 商汤   | Android  | Java/Kotlin  | [Android/lib_sensetime](Android/lib_sensetime) | 4.2.6.5    | 9.3.1         |
| 相芯   | Android  | Java/Kotlin  | [Android/lib_faceunity](Android/lib_faceunity) | 4.2.6.5    | 8.7.0         |
| 字节火山 | Android  | Java/Kotlin  | [Android/lib_bytedance](Android/lib_bytedance) | 4.2.6.5    | 4.6.0         |
| 宇宙   | Android  | Java/Kotlin  | [Android/lib_cosmos](Android/lib_cosmos)    | 4.2.6.5    | 3.7.0         |
| 商汤   | iOS      | Swift/OC     | [iOS/BeautyAPi/SenseBeauty](iOS/BeautyAPi/SenseBeauty) | 4.2.6.5    | 9.3.1         |
| 相芯   | iOS      | Swift/OC     | [iOS/BeautyAPi/FUBeauty](iOS/BeautyAPi/FUBeauty) | 4.2.6.5    | 8.7.0         |
| 字节火山 | iOS      | Swift/OC     | [iOS/BeautyAPi/ByteBeauty](iOS/BeautyAPi/ByteBeauty) | 4.2.6.5    | 4.5.1         |
| 宇宙   | iOS      | Swift/OC     | [iOS/BeautyAPi/CosmosBeauty](iOS/BeautyAPi/CosmosBeauty) | 4.2.6.5    | 3.7.1         |

### 快速开始

| 平台      | Demo                   |
|---------|------------------------|
| Android | [Android/app](Android/README.zh.md) |
| iOS     | [iOS/BeautyAPi](iOS/README.zh.md) |

### 集成遇到困难，该如何联系声网获取协助

> 方案1：如果您已经在使用声网服务或者在对接中，可以直接联系对接的销售或服务
> 
> 方案2：发送邮件给 [support@agora.io](mailto:support@agora.io) 咨询
> 
> 方案3：扫码加入我们的微信交流群提问
> 
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---

### 许可证

示例项目遵守 [MIT 许可证](LICENSE)。
