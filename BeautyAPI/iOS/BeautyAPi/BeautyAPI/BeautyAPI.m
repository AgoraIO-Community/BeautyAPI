//
//  BeautyAPI.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import "BeautyAPI.h"
#import <AgoraRtcKit/AgoraRtcKit.h>
#import "BeautyRender.h"

@interface BeautyAPI ()<AgoraVideoFrameDelegate>

@property (nonatomic, strong) BeautyConfig *config;

@end

@implementation BeautyAPI

- (int)initialize:(BeautyConfig *)config {
    self.config = config;
    self.isFrontCamera = YES;
    if (config == nil) {
        return -1;
    }
    if (config.beautyRender == nil) {
        return -1;
    }
    if (config.processMode == processModeAgora) {
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
    if (self.config.processMode == processModeAgora) {
        return -1;
    }
    if (pixelBuffer == nil) {
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

- (int)setOptimizedDefault:(BOOL)enable {
    if (self.config.beautyRender == nil) {
        return -1;
    }
    if (enable) {
        [self.config.beautyRender setOptimizedDefault];
    } else {
        [self.config.beautyRender reset];
    }
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
    CVPixelBufferRef pixelBuffer = [self.config.beautyRender onCapture:videoFrame.pixelBuffer];
    videoFrame.pixelBuffer = pixelBuffer;
    return YES;
}

- (AgoraVideoFormat)getVideoFormatPreference{
#if __has_include(Sensetime)
    return AgoraVideoFormatCVPixelNV12;
#endif
    return AgoraVideoFormatCVPixelBGRA;
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

@end
