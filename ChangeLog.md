# Beauty API Change Log
# 美颜场景化 API 更新日志

## 1.0.12
### Breaking changes / 不兼容变更
1. Android: Remove the FaceUnity reset() method added in 1.0.11, and remove channel switching from the demo / Android：移除 1.0.11 新增的相芯 reset() 方法，同时移除 Demo 中的切换频道功能
2. Android: Raise minSdk to 23 (Android 6.0) for the demo and all beauty modules / Android：Demo 及各美颜模块的 minSdk 提升至 23（Android 6.0）

### Updates / 更新
1. Android: Adapt to RTC SDK 4.6.4 / Android：适配 RTC SDK 4.6.4 版本

### Fixes / 修复
1. Android: SenseTime, FaceUnity and ByteDance call setLocalRenderMode on the main thread, and cancel pending updates on release / Android：商汤、相芯、字节火山在主线程调用 setLocalRenderMode，release 时取消未执行的更新
2. Android: SenseTime, FaceUnity and ByteDance keep the current local mirror mode when setupLocalVideo is called after the first frame / Android：商汤、相芯、字节火山在首帧之后调用 setupLocalVideo 时保留当前本地镜像模式
3. Android: ByteDance keeps the render mode passed to setupLocalVideo when the local mirror mode changes / Android：字节火山本地镜像变化时保留 setupLocalVideo 传入的渲染模式
4. Android: SenseTime, FaceUnity and ByteDance run wrapTextureBuffer on the beauty GL thread / Android：商汤、相芯、字节火山在美颜 GL 线程中执行 wrapTextureBuffer
5. Android: FaceUnity and ByteDance release only removes BeautyAPI's own pending tasks and keeps the RTC SDK's tasks on the beauty GL thread; for all three vendors, tasks queued by runOnProcessThread are skipped after release / Android：相芯、字节火山 release 时只移除 BeautyAPI 自身的待执行任务，保留 RTC SDK 在美颜 GL 线程上的任务；三家美颜 release 后都不再执行 runOnProcessThread 投递的任务
6. Android Demo: The FaceUnity page releases BeautyAPI before leaveChannel / stopPreview on exit / Android Demo：相芯页面退出时先 release BeautyAPI，再 leaveChannel / stopPreview
7. Android: ByteDance now mirrors the local preview for MIRROR_LOCAL_ONLY / MIRROR_REMOTE_ONLY on the front camera, and no longer drops the first frames at startup with the default mirror config / Android：字节火山前置摄像头配置为 MIRROR_LOCAL_ONLY / MIRROR_REMOTE_ONLY 时本地预览正确镜像，默认镜像配置下开播不再丢弃开头几帧
8. Android: SenseTime and ByteDance start the first_beauty_frame report when the beauty GL thread is created, the same as FaceUnity, so it is reported with every mirror config / Android：商汤、字节火山与相芯一致，在创建美颜 GL 线程时开始 first_beauty_frame 统计，任何镜像配置下都会上报

## 1.0.11
1. Android:  Adapt to RTC version 4.6.0. / Android: 适配 RTC 4.6.0
2. Android:  Add "reset" method to FaceUnity beauty. / Android: 相芯美颜增加 reset 方法
3. Android:  Remove cosmic beauty and Maven upload. / Android: 移除宇宙美颜，移除 maven 上传

## 1.0.10
1. Android:  Add gl fence. / Android: 增加 gl fence

## 1.0.9.1
1. Android: Fix crash issue when writing logs / Android: 修复写日志导致崩溃问题

## 1.0.9
1. iOS: Upgrade and adapt to FaceUnity beauty SDK 8.11.1 / iOS：升级适配相芯美颜 8.11.1 版本
2. Internationalization adaptation of code repository / 代码仓库国际化适配

## 1.0.8
1. Android: Upgrade and adapt to FaceUnity beauty SDK 8.11.0 / Android：升级适配相芯美颜 8.11.0 版本

## 1.0.7
1. Add APIReporter logging module / 添加 APIReporter 日志上报模块
2. Optimize handling logic when certificate expires / 优化证书过期时的处理逻辑

## 1.0.6.2
1. Android: Fix the issue of I420 beauty getting stuck when switching in FaceUnity direct texture processing mode / Android：修复相芯直接纹理处理模式下切换 I420 美颜卡住的问题
2. Android: Improve beauty settings / Android：完善美颜设置

## 1.0.6.1
1. Android: Fix the issue of beauty getting stuck when turning beauty on/off in FaceUnity texture asynchronous processing mode / Android: 修复相芯纹理异步处理模式打开关闭美颜时美颜卡住的问题

## 1.0.6
1. Android: Add EGLBase.lock to prevent ANR issues when exiting on some models / Android：美颜处理时添加 EGLBase.lock 锁，防止在部分机型退出时出现 anr 的问题
2. Android: Add a switch for FaceUnity beauty to choose between real-time priority or smooth priority / Android：相芯美颜添加一个开关，支持选择实时优先还是流畅优先
3. Android: Fix frame rollback issues on some mid-range devices with FaceUnity texture + async processing / Android：修复相芯纹理+异步处理在部分中端机上出现回帧的问题
4. Android: Change beauty library to dynamic download / Android：将美颜库改成动态下载
5. Remove setBeautyPreset call at demo level / demo 层去掉 setBeautyPreset 调用

## 1.0.5
1. Add cosmic beauty effect / 添加宇宙美颜
2. Android: Optimize demo beauty resource loading / Android：优化 demo 美颜资源加载

## 1.0.4.1
1. Android: Fix Video Frame Observer not being released / Android：修复 Video Frame Observer 没有释放的问题
2. Android: Add runOnProcessThread API for operations in beauty processing thread, such as beauty effect settings / Android：添加 runOnProcessThread api 用于在美颜处理线程里做一些操作，如美颜效果设置等
3. Android: Fix SenseTime beauty flashing black when turning on/off / Android：修复商汤在开关美颜时会黑一下的问题

## 1.0.4
1. Adapt to ByteDance beauty SDK 4.6.0 / 适配火山美颜 4.6.0 版本
2. Adapt to FaceUnity beauty SDK 8.7.0 / 适配相芯美颜 8.7.0 版本
3. Android: Fix screen auto-rotation rendering issues / Android：修复屏幕自动旋转渲染问题
4. Fix other online issues / 修复线上其他问题

## 1.0.3
1. Add beauty parameter setting popup / 添加美颜参数设置弹窗
2. Add RTC event tracking / 添加 RTC 打点上报

## 1.0.2
1. Add necessary logs, remove meaningless duplicate logs / 添加必要日志，去掉无意义重复的日志
2. Adapt to SenseTime 9.x version / 适配商汤 9.x 版本
3. Adapt to RTC 4.2.2 version / 适配 RTC 4.2.2 版本
4. Add low/medium/high device adaptation for FaceUnity beauty using FaceUnity's device judgment algorithm / 给相芯美颜加上中高低机型适配，判断机型用相芯提供的判断算法
5. Add mirror mode configuration / 添加镜像模式配置
