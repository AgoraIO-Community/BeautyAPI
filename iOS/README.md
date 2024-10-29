# BeautyAPI Demo

_English | [中文](README.zh.md)

> This document mainly introduces how to quickly run through the beauty scene API sample code.
> 
> **Demo Effect:**
>
> <img src="imgs/app_page_launch_en.png" width="300" />
---

## 1. Prerequisites

- iOS 11 And Above
- Xcode 13 And Above

---

## 2. Run Project

- Get Agora App

	> 1. Create a developer account at [agora.io](https://www.agora.io). Once you finish the signup process, you will be redirected to the Dashboard.
	>
	> 2. Navigate in the Dashboard tree on the left to Projects > Project List.
	>
	> 3. Save the App Id from the Dashboard for later use.

- Edit the KeyCenter.swift, configure it with

	```
	static let AppId: String = <#YOUR AppId#>
	```
 
- Open the pod 'fuLib' comment in Podfile.
   
- Create FULib folder under iOS root directory

- Add Resources folder such as "Animoji", "makeup", "sticker" under the iOS/FULib directory

- Replace license in the BeautyAPI/FUBeauty/authpack.h, configure it with
    ```
    static char g_auth_package[]=<#YOUR g_auth_package Value#>;
    ```
    
- Edit the `BeautyAPi Project`, change `Bundle Identifier` to your application id using to apply to beauty SDK.

- Execute pod install

- Run Project
	
---

## Integrate Into Project

> Each beauty api can be integrated into your project separately, see the below integration documentation for details
> 
> | Beauty    | Documentation                                                |
> | --------- | ------------------------------------------------------------ |
> | SenseTime | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/sensetime/integrate) |
> | FaceUnity | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/faceunity/integrate) |
> | ByteDance | [Official website document](https://doc.shengwang.cn/doc/showroom/ios/advanced-features/beauty/bytedance/integrate) |
> | Cosmos    | [Cosmos](./BeautyAPi/CosmosBeauty/README.md)                                                      |

---

## License

The sample projects are under the [MIT license](../LICENSE).
