# Beauty API Change Log
# 美颜场景化 API 更新日志

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
