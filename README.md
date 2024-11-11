# BeautyAPI

_English | [中文](README.zh.md)_

### Overview

In order to reduce the difficulty for developers to integrate beauty, Agora provides a beauty scene-based API. The beauty scene API uses the **Agora RTC raw data interface** to encapsulate the beauty processing of different manufacturers, striving to provide the best beauty experience and minimize the beauty time as much as possible. You only need to call a few simple scene-based APIs to achieve smooth beautification effects.

This repository contains the following beauty scene APIs:

| Beauty    | Platform | Language    | Location                                                 | RTC SDK Version | Beauty SDK Version |
| --------- | -------- | ----------- | -------------------------------------------------------- |-----------------| ------------------ |
| SenseTime | Android  | Java/Kotlin | [Android/lib_sensetime](Android/lib_sensetime)           | 4.2.6.5         | 9.3.1              |
| FaceUnity | Android  | Java/Kotlin | [Android/lib_faceunity](Android/lib_faceunity)           | 4.2.6.5         | 8.11.0              |
| ByteDance | Android  | Java/Kotlin | [Android/lib_bytedance](Android/lib_bytedance)           | 4.2.6.5         | 4.6.0              |
| Cosmos    | Android  | Java/Kotlin | [Android/lib_cosmos](Android/lib_cosmos)                 | 4.2.6.5         | 3.7.0              |
| SenseTime | iOS      | Swift/OC    | [iOS/BeautyAPi/SenseBeauty](iOS/BeautyAPi/SenseBeaufy)   | 4.2.6.5         | 9.3.1              |
| FaceUnity | iOS      | Swift/OC    | [iOS/BeautyAPi/FUBeauty](iOS/BeautyAPi/FUBeauty)         | 4.2.6.5         | 8.7.0              |
| ByteDance | iOS      | Swift/OC    | [iOS/BeautyAPi/ByteBeauty](iOS/BeautyAPi/ByteBeaufy)     | 4.2.6.5         | 4.5.1              |
| Cosmos    | iOS      | Swift/OC    | [iOS/BeautyAPi/CosmosBeauty](iOS/BeautyAPi/CosmosBeauty) | 4.2.6.5         | 3.7.1              |

You can refer to following demo to lean how to use the beauty api:

| Platform  | Demo                   | Notes |
|-----------|------------------------|---------|
| Android   | [Android/app](Android) |         |
| iOS       | [iOS/BeautyAPi](iOS)   |         |

### Feedback

If you have any problems or suggestions regarding the sample projects, feel free to file an issue.

### Related resources
>

- Check our [FAQ](https://docs.agora.io/en/faq) to see if your issue has been recorded.
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials.
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use case.
- Repositories managed by developer communities can be found at [Agora Community](https://github.com/AgoraIO-Community).
- If you encounter problems during integration, feel free to ask questions in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io).

### License

The sample projects are under the [MIT license](LICENSE).

