//
//  BeautyConfig.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "BeautyRender.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ProcessMode) {
    processModeAgora = 0,
    processModeCustom = 1
};

@interface BeautyConfig : NSObject
// 由外部传入的rtc对象，不可为空
@property(nonatomic, weak)AgoraRtcEngineKit *rtcEngine;
// 由外部传入的美颜SDK接口对象(不同厂家不一样)，不可为空
@property(nonatomic, strong)BeautyRender *beautyRender;
// 是否由内部自动注册祼数据回调处理
@property(nonatomic, assign)ProcessMode processMode;
@end

NS_ASSUME_NONNULL_END
