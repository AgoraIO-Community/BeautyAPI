//
//  APIReporter.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2024/4/17.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

typedef NS_ENUM(NSInteger, APIType) {
    APITypeKTV = 1,  //KTV/Karaoke
    APITypeCall = 2,  //Call
    APITypeBeauty = 3,  //Beauty
    APITypeVideoLoader = 4, //Quick Start/Switch
    APITypePK = 5, //Team Battle
    APITypeVitualSpace = 6,
    APITypeScreenSpace = 7, //Screen Sharing
    APITypeAudioScenario = 8  //Audio Scenario
};

typedef NS_ENUM(NSInteger, APIEventType) {
    APIEventTypeAPI = 0, //API Event
    APIEventTypeCost,    //Cost Event
    APIEventTypeCustom   //Custom Event
};

typedef NS_ENUM(NSInteger, APICostEvent) {
    APICostEventChannelUsage = 0, //Channel Usage Time
    APICostEventFirstFrameActual,  //Actual First Frame Time
    APICostEventFirstFramePerceived //Perceived First Frame Time
};

NS_ASSUME_NONNULL_BEGIN

@interface APIReporter : NSObject

- (instancetype)initWithType:(APIType)type version:(NSString *)version engine:(AgoraRtcEngineKit *)engine;
- (void)reportFuncEventWithName:(NSString *)name value:(NSDictionary<NSString *, id> *)value ext:(NSDictionary<NSString *, id> *)ext;
- (void)startDurationEventWithName:(NSString *)name;
- (void)endDurationEventWithName:(NSString *)name ext:(NSDictionary<NSString *, id> *)ext;
- (void)reportCostEventWithName:(APICostEvent)name cost:(NSInteger)cost ext:(NSDictionary<NSString *, id> *)ext;
- (void)reportCustomEventWithName:(NSString *)name ext:(NSDictionary<NSString *, id> *)ext;
- (void)writeLogWithContent:(NSString *)content level:(AgoraLogLevel)level;
- (void)cleanCache;

@end

NS_ASSUME_NONNULL_END
