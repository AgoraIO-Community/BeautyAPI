//
//  BeautyAPI.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import "BeautyAPI.h"
#import <AgoraRtcKit/AgoraRtcKit.h>

@interface BeautyAPI ()<AgoraVideoFrameDelegate>

@property (nonatomic, strong) BeautyConfig *config;
@property (nonatomic, assign) CFTimeInterval preTime;
@property (nonatomic, strong) NSMutableArray *statsArray;

@end

@implementation BeautyAPI

- (NSMutableArray *)statsArray {
    if (_statsArray == nil) {
        _statsArray = [NSMutableArray new];
    }
    return _statsArray;
}

- (int)initialize:(BeautyConfig *)config {
    self.config = config;
    self.isFrontCamera = YES;
    if (self.config.statsDuration <= 0) {
        self.config.statsDuration = 1;
    }
    if (config == nil) {
        return -1;
    }
    if (config.beautyRender == nil) {
        return -1;
    }
    self.beautyRender = config.beautyRender;
    if (config.captureMode == CaptureModeAgora) {
        [config.rtcEngine setVideoFrameDelegate:self];
    }
    return 0;
}


- (int)enable:(BOOL)enable {
    if (self.config.beautyRender == nil) {
        return -1;
    }
    _isEnable = enable;
    return 0;
}

- (int)onFrame:(CVPixelBufferRef)pixelBuffer callback:(void (^)(CVPixelBufferRef _Nonnull))callback {
    if (self.config.captureMode == CaptureModeAgora) {
        return -1;
    }
    if (pixelBuffer == nil) {
        return -1;
    }
    if (self.isEnable == NO && callback) {
        callback(pixelBuffer);
        return -1;
    }
    CVPixelBufferRef pb = [self.config.beautyRender onCapture:pixelBuffer];
    if (pb == nil) {
        return -1;
    }
    if (callback) {
        callback(pb);
    }
    return 0;
}

- (int)setupLocalVideo:(UIView *)view renderMode:(AgoraVideoRenderMode)renderMode {
    AgoraRtcVideoCanvas *localCanvas = [[AgoraRtcVideoCanvas alloc] init];
    localCanvas.mirrorMode = AgoraVideoMirrorModeDisabled;
    localCanvas.view = view;
    localCanvas.renderMode = renderMode;
    localCanvas.uid = 0;
    return [self.config.rtcEngine setupLocalVideo:localCanvas];
}

- (int)setBeautyPreset: (BeautyPresetMode)mode {
    if (self.config.beautyRender == nil) {
        return -1;
    }
    if (mode == BeautyPresetModeCustom) {
        return -1;
    }
    [self.config.beautyRender setBeautyPreset];
    return 0;
}

- (int)destory {
    if (self.config == nil) {
        return -1;
    }
    [self.config.rtcEngine setVideoFrameDelegate:nil];
    [self.config.beautyRender destory];
    self.config = nil;
    return 0;
}

#pragma mark - VideoFrameDelegate
- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)videoFrame sourceType:(AgoraVideoSourceType)sourceType {
    if (!self.isEnable) { return YES; }
    CFTimeInterval startTime = CACurrentMediaTime();
    CVPixelBufferRef pixelBuffer = [self.config.beautyRender onCapture:videoFrame.pixelBuffer];
    CFTimeInterval endTime = CACurrentMediaTime();
    if (self.config.statsEnable) {
        [self.statsArray addObject:@(endTime - startTime)];
    }
    videoFrame.pixelBuffer = pixelBuffer;
    if (self.config.eventCallback && self.preTime > 0 && self.config.statsEnable) {
        CFTimeInterval time = startTime - self.preTime;
        if (time > self.config.statsDuration && self.statsArray.count > 0) {
           NSArray *sortArray = [self.statsArray sortedArrayUsingComparator:^NSComparisonResult(NSNumber * _Nonnull obj1, NSNumber * _Nonnull obj2) {
                return obj1.doubleValue > obj2.doubleValue;
            }];
            double totalValue = 0;
            for (NSNumber *value in sortArray) {
                totalValue += value.doubleValue;
            }
            BeautyStats *stats = [[BeautyStats alloc] init];
            stats.minCostMs = [sortArray.firstObject doubleValue];
            stats.maxCostMs = [sortArray.lastObject doubleValue];
            stats.averageCostMs = totalValue / sortArray.count;
            self.config.eventCallback(stats);
            [self.statsArray removeAllObjects];
            self.preTime = startTime;
        }
    }
    if (self.preTime <= 0) {
        self.preTime = startTime;
    }
    return YES;
}

- (AgoraVideoFormat)getVideoFormatPreference{
    return [self.config.beautyRender getVideoFormatPreference];
}
- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode{
    return AgoraVideoFrameProcessModeReadWrite;
}

- (BOOL)getMirrorApplied{
    return self.isFrontCamera;
}

- (BOOL)getRotationApplied {
    return NO;
}

- (AgoraVideoFramePosition)getObservedFramePosition {
    return AgoraVideoFramePositionPostCapture;
}

@end
