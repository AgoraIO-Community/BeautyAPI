//
//  BeautyAPI.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import "BeautyAPI.h"
#import "APIReporter.h"

static NSString *const beautyAPIVersion = @"1.0.7";

@implementation BeautyStats
@end

@implementation CameraConfig
@end

@implementation BeautyConfig
@end

@interface BeautyAPI ()

@property (nonatomic, strong) BeautyConfig *config;
@property (nonatomic, assign) CFTimeInterval preTime;
@property (nonatomic, strong) NSMutableArray *statsArray;
@property (nonatomic, assign) AgoraVideoRenderMode renderMode;
@property (nonatomic, strong) APIReporter *reporter;
@property (nonatomic, assign) BOOL isFirstFrame;

@end

#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
@interface BeautyAPI ()<AgoraVideoFrameDelegate>
@end
#endif

@implementation BeautyAPI

- (instancetype)init {
    if (self == [super init]) {
        _isFrontCamera = YES;
    }
    return self;
}

- (NSMutableArray *)statsArray {
    if (_statsArray == nil) {
        _statsArray = [NSMutableArray new];
    }
    return _statsArray;
}

- (APIReporter *)reporter {
    if (_reporter == nil) {
        _reporter = [[APIReporter alloc] initWithType:(APITypeBeauty) 
                                              version:beautyAPIVersion
                                               engine:self.config.rtcEngine];
    }
    return _reporter;
}

- (int)initialize:(BeautyConfig *)config {
    if (config.cameraConfig == nil) {
        CameraConfig *cameraConfig = [[CameraConfig alloc] init];
        cameraConfig.frontMirror = MirrorMode_LOCAL_REMOTE;
        cameraConfig.backMirror = MirrorMode_NONE;
        config.cameraConfig = cameraConfig;
    }
    [LogUtil log:[NSString stringWithFormat:@"RTC Version == %@", [AgoraRtcEngineKit getSdkVersion]]];
    [LogUtil log:[NSString stringWithFormat:@"BeautyAPI Version == %@", [self getVersion]]];
    self.config = config;
    if (self.config.statsDuration <= 0) {
        self.config.statsDuration = 1;
    }
    if (config == nil) {
        [LogUtil log:@"Lack of configuration information" level:(LogLevelError)];
        return -1;
    }
    if (config.beautyRender == nil) {
        [LogUtil log:@"beautyRender is Empty" level:(LogLevelError)];
        return -1;
    }
    [LogUtil log:[NSString stringWithFormat:@"beautyRender == %@", config.beautyRender.description]];
    [self.reporter startDurationEventWithName:@"initialize-release"];
    self.beautyRender = config.beautyRender;
    if (config.captureMode == CaptureModeAgora) {
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
        [LogUtil log:@"captureMode == Agora"];
        [config.rtcEngine setVideoFrameDelegate:self];
        NSDictionary *dict = @{
            @"rtcVersion": [AgoraRtcEngineKit getSdkVersion],
            @"beautyRender": config.beautyRender.description,
            @"captureMode": @(config.captureMode),
            @"cameraConfig": @{
                @"frontMirror": @(config.cameraConfig.frontMirror),
                @"backMirror": @(config.cameraConfig.backMirror)
            }
        };
        [self rtcReportWithEvent:@"initialize" label:dict];
        [self setupMirror];
#else
        [LogUtil log:@"Rtc has not been imported" level:(LogLevelError)];
        return -1;
#endif
    } else {
        [LogUtil log:@"captureMode == Custom"];
    }
    [self setupMirror];
    return 0;
}

- (int)switchCamera {
    _isFrontCamera = !_isFrontCamera;
    NSDictionary *dict = @{ @"cameraPosition": @(_isFrontCamera) };
    [self rtcReportWithEvent:@"cameraPosition" label:dict];
    int res = [self.config.rtcEngine switchCamera];
    [self setupMirror];
    return res;
}

- (AgoraVideoMirrorMode)setupMirror {
    AgoraVideoMirrorMode mode = AgoraVideoMirrorModeDisabled;
    if (self.isFrontCamera) {
        if (self.config.cameraConfig.frontMirror == MirrorMode_LOCAL_ONLY || self.config.cameraConfig.frontMirror == MirrorMode_REMOTE_ONLY) {
            mode = AgoraVideoMirrorModeEnabled;
        }
    } else {
        if (self.config.cameraConfig.backMirror ==  MirrorMode_REMOTE_ONLY || self.config.cameraConfig.backMirror == MirrorMode_LOCAL_ONLY) {
            mode = AgoraVideoMirrorModeEnabled;
        }
    }
    [self.config.rtcEngine setLocalRenderMode:self.renderMode mirror:mode];
    [LogUtil log:[NSString stringWithFormat:@"AgoraVideoMirrorMode == %ld isFrontCamera == %d", mode, self.isFrontCamera]];
    return mode;
}

- (int)updateCameraConfig:(CameraConfig *)cameraConfig {
    self.config.cameraConfig = cameraConfig;
    [self setupMirror];
    NSDictionary *dict = @{
        @"cameraConfig": @{
            @"frontMirror": @(cameraConfig.frontMirror),
            @"backMirror": @(cameraConfig.backMirror)
        }
    };
    [self rtcReportWithEvent:@"updateCameraConfig" label:dict];
    return 0;
}

- (int)enable:(BOOL)enable {
    _isEnable = enable;
    NSDictionary *dict = @{ @"enable": @(enable) };
    [self rtcReportWithEvent:@"enable" label:dict];
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

#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (int)setupLocalVideo:(UIView *)view renderMode:(AgoraVideoRenderMode)renderMode {
    self.renderMode = renderMode;
    AgoraRtcVideoCanvas *localCanvas = [[AgoraRtcVideoCanvas alloc] init];
    localCanvas.mirrorMode = [self setupMirror];
    localCanvas.view = view;
    localCanvas.renderMode = renderMode;
    localCanvas.uid = 0;
    NSDictionary *dict = @{ @"renderMode": @(renderMode) };
    [self rtcReportWithEvent:@"setupLocalVideo" label:dict];
    [LogUtil log:@"setupLocalVideoCanvas"];
    return [self.config.rtcEngine setupLocalVideo:localCanvas];
}
#endif

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

- (int)destroy {
    if (self.config == nil) {
        return -1;
    }
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
    [self.config.rtcEngine setVideoFrameDelegate:nil];
#endif
//    [self.config.beautyRender destroy];
    self.config = nil;
    [LogUtil log:@"destroy"];
    [self.reporter endDurationEventWithName:@"initialize-release" ext:@{}];
    return 0;
}

- (void)rtcReportWithEvent: (NSString *)event label: (NSDictionary *)label {
    if (self.config.rtcEngine == nil) {
        [LogUtil log:@"Rtc cannot be empty" level:(LogLevelError)];
        return;
    }
    [self.reporter reportFuncEventWithName:event value:label ext:@{}];
}

- (NSString *)getVersion {
    return beautyAPIVersion;
}

#pragma mark - VideoFrameDelegate
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)videoFrame {
    return [self onCaptureVideoFrame:videoFrame sourceType:(AgoraVideoSourceTypeCamera)];
}
- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)videoFrame sourceType:(AgoraVideoSourceType)sourceType {
    if (!self.isEnable) { return YES; }
    CFTimeInterval startTime = CACurrentMediaTime();
    if (!self.isFirstFrame) {
        [self.reporter startDurationEventWithName:@"first_beauty_frame"];
    }
    CVPixelBufferRef pixelBuffer = [self.config.beautyRender onCapture:videoFrame.pixelBuffer];
    if (!self.isFirstFrame) {
        [self.reporter endDurationEventWithName:@"first_beauty_frame" ext:@{
            @"width": @(CVPixelBufferGetWidth(pixelBuffer)),
            @"height": @(CVPixelBufferGetHeight(pixelBuffer)),
            @"camera_facing": _isFrontCamera ? @"front" : @"back",
            @"buffer_type": @"pixelbuffer"
        }];
        self.isFirstFrame = YES;
    }
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
            [LogUtil log:[NSString stringWithFormat:@"minCostMs == %f", stats.minCostMs] level:(LogLevelInfo)];
            [LogUtil log:[NSString stringWithFormat:@"maxCostMs == %f", stats.maxCostMs] level:(LogLevelInfo)];
            [LogUtil log:[NSString stringWithFormat:@"averageCostMs == %f", stats.averageCostMs] level:(LogLevelInfo)];
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
    if (self.isFrontCamera) {
        return self.config.cameraConfig.frontMirror == MirrorMode_REMOTE_ONLY || self.config.cameraConfig.frontMirror == MirrorMode_LOCAL_REMOTE;
    }
    return self.config.cameraConfig.backMirror == MirrorMode_REMOTE_ONLY || self.config.cameraConfig.backMirror == MirrorMode_LOCAL_REMOTE;
}

- (BOOL)getRotationApplied {
    return NO;
}

- (AgoraVideoFramePosition)getObservedFramePosition {
    return AgoraVideoFramePositionPostCapture;
}
#endif

@end

@implementation LogUtil
+ (NSString *)getCurrentTime {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"yyyy-MM-dd HH:mm:ss";
    return [formatter stringFromDate:[NSDate date]];
}

+ (NSString *)getLogPrefixForLevel:(LogLevel)level {
    switch (level) {
        case LogLevelInfo:
            return @"[INFO]";
        case LogLevelError:
            return @"[ERROR]";
        case LogLevelDebug:
            return @"[DEBUG]";
        default:
            return @"";
    }
}

+ (void)log:(NSString *)message {
    [self log:message level:(LogLevelDebug)];
}

+ (void)log:(NSString *)message level:(LogLevel)level {
    
    NSString *logString = [NSString stringWithFormat:@"%@ %@ %@\n",
                           [self getCurrentTime],
                           [self getLogPrefixForLevel:level],
                           message];
    NSString *logFile = [NSString stringWithFormat:@"%@/agora_beautyapi.log", NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject];
    [self checkLogFileSizeWithPath: logFile];
    NSFileHandle *fileHandle = [NSFileHandle fileHandleForWritingAtPath:logFile];
    if (fileHandle) {
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
            [fileHandle seekToEndOfFile];
            [fileHandle writeData:[logString dataUsingEncoding:NSUTF8StringEncoding]];
            [fileHandle closeFile];
        });
    } else {
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
            [logString writeToFile:logFile atomically:YES encoding:NSUTF8StringEncoding error:nil];
        });
    }
}

+ (void)checkLogFileSizeWithPath: (NSString *)filePath {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSDictionary *fileAttributes = [fileManager attributesOfItemAtPath:filePath error:&error];
    if (fileAttributes) {
        NSNumber *fileSizeNumber = [fileAttributes objectForKey:NSFileSize];
        long long fileSize = [fileSizeNumber longLongValue];
        if (fileSize > 1024 * 1024 * 2) { 
            [fileManager removeItemAtPath:filePath error:&error];
        }
    }
}

@end
