# BeautyAPI Demo

_English | [中文](README.zh.md)

> This document mainly introduces how to quickly run through the beauty scene API sample code.
> 
> **Demo Effect:**
>
> <img src="imgs/app_page_launch.png" width="300" />
---

## 1. Prerequisites

- iOS 11 And Above
- Xcode 13 And Above
- Contact SenseTime customer service to get SenseTime's beauty SDK, beauty resources and license
- Contact FaceUnity customer service to get beauty resources and license
- Contact ByteDance customer service to get ByteDance's beauty SDK, beauty resources and license

---

## 2. Agora AppID Configuration

- Get Agora App

> 1. Create a developer account at [agora.io](https://www.agora.io). Once you finish the signup process, you will be redirected to the Dashboard.

> 2. Navigate in the Dashboard tree on the left to Projects > Project List.

> 3. Save the App Id from the Dashboard for later use.

- Edit the KeyCenter.swift, configure it whth

```
static let AppId: String = #YOUR APP ID#
```
---

## 3. Integrate Into Project

> Each beauty api can be integrated into your project separately, see the below integration documentation for details
> 
> | Beauty    | Documentation                                                |
> | --------- | ------------------------------------------------------------ |
> | SenseTime | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/sensetime/integrate) |
> | FaceUnity | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/faceunity/integrate) |
> | ByteDance | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/bytedance/integrate) |
> | Cosmos    | None yet                                                     |

---

## 4. Run Project

1. Edit the `BeautyAPi Project`, change `Bundle Identifier` to your application id using to apply to beauty SDK.
2. Run Project

---

### Contact us

> Plan 1: If you are already using Shengwang services or are in the process of docking, you can directly contact the docked sales or service.
>
> Plan 2: Send an email to [support@agora.io](mailto:support@agora.io) for consultation
>
> Plan 3: Scan the QR code to join our WeChat communication group to ask questions
>
> <img src="https://download.agora.io/demo/release/SDHY_QA.jpg" width="360" height="360">
---

## License

The sample projects are under the [MIT license](../LICENSE).
