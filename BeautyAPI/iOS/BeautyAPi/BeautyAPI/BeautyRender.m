//
//  BeautyRender.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import "BeautyRender.h"
#import "BundleUtil.h"

@interface BeautyRender ()
#if __has_include(Sensetime)
@property (nonatomic, assign) BOOL isSuccessLicense;
@property (nonatomic, strong) NSTimer *timer;
///贴纸id
@property (nonatomic, assign) int stickerId;
@property (nonatomic, assign) int filterId;
#endif
#if __has_include(FURenderMoudle)
/// 当前的贴纸
@property (nonatomic, strong) FUSticker *currentSticker;
#endif

@end

@implementation BeautyRender

#if __has_include(Sensetime)
- (VideoProcessingManager *)videoProcessing {
    if (_videoProcessing == nil) {
        _videoProcessing = [VideoProcessingManager new];
    }
    return _videoProcessing;
}
#endif

#if __has_include(FURenderMoudle)
- (FUManager *)videoProcessing {
    if (_videoProcessing == nil) {
        _videoProcessing = [[FUManager alloc] init];
    }
    return _videoProcessing;
}
#endif

- (instancetype)init {
    if (self == [super init]) {
#if __has_include(Sensetime)
        [self checkSensetimeLicense];
#endif
    }
    return self;
}

- (NSDictionary *)sensetimeDefault {
    NSDictionary *params = @{
        @"103": @(0.55),
        @"101": @(0.2),
        @"201": @(0.2),
        @"318": @(0),
        @"202": @(0.3),
        @"306": @(0),
        @"303": @(0),
        @"320": @(0),
        @"304": @(0),
        @"309": @(0),
        @"317": @(0),
    };
    return params;
}

- (void)setMakeup: (BOOL)isSelected {
#if __has_include(Sensetime)
    if (isSelected) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"qise.zip" ofType:nil];
        __weak BeautyRender *weakself = self;
        [self.videoProcessing.effectsProcess addStickerWithPath:path callBack:^(st_result_t state, int sticker, uint64_t action) {
            [weakself.videoProcessing.effectsProcess setPackageId:sticker groupType:EFFECT_BEAUTY_GROUP_MAKEUP strength:0.5];
            weakself.stickerId = sticker;
        }];
    } else {
        [self.videoProcessing.effectsProcess removeSticker:self.stickerId];
        self.stickerId = 0;
    }
#elif __has_include(FURenderMoudle)
    if (isSelected) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *makeupPath = [bundle pathForResource:@"graphics/face_makeup" ofType:@"bundle"];
        FUMakeup *makeup = [[FUMakeup alloc] initWithPath:makeupPath name:@"face_makeup"];
        NSString *path = [bundle pathForResource:@"美妆/ziyun" ofType:@"bundle"];
        FUItem *makupItem = [[FUItem alloc] initWithPath:path name:@"ziyun"];
        makeup.isMakeupOn = YES;
        [FURenderKit shareRenderKit].makeup = makeup;
        [FURenderKit shareRenderKit].makeup.enable = YES;
        [makeup updateMakeupPackage:makupItem needCleanSubItem:NO];
        makeup.intensity = 0.7;
    } else {
        [FURenderKit shareRenderKit].makeup.enable = NO;
        [FURenderKit shareRenderKit].makeup = nil;
    }
#endif
}
- (void)setSticker: (BOOL)isSelected {
#if __has_include(Sensetime)
    if (isSelected) {
        NSString *path = [[NSBundle mainBundle] pathForResource:@"lianxingface.zip" ofType:nil];
        [self.videoProcessing.effectsProcess setStickerWithPath:path callBack:^(st_result_t state, int stickerId, uint64_t action) {
                 
        }];
    } else {
        [self.videoProcessing cleareStickers];
    }
#elif __has_include(FURenderMoudle)
    if (isSelected) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *path = [bundle pathForResource:[NSString stringWithFormat:@"贴纸/%@", @"fashi"] ofType:@"bundle"];
        FUSticker *sticker = [[FUSticker alloc] initWithPath:path name:@"sticker"];
        if (self.currentSticker) {
            [[FURenderKit shareRenderKit].stickerContainer replaceSticker:self.currentSticker withSticker:sticker completion:nil];
        } else {
            [[FURenderKit shareRenderKit].stickerContainer addSticker:sticker completion:nil];
        }
        self.currentSticker = sticker;
    } else {
        [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    }
#endif
}

#if __has_include(Sensetime)
- (void)checkSensetimeLicense {
    NSString *licensePath = [[NSBundle mainBundle] pathForResource:@"SENSEME" ofType:@"lic"];
    self.isSuccessLicense = [EffectsProcess authorizeWithLicensePath:licensePath];
    __weak BeautyRender *weakSelf = self;
    self.timer = [NSTimer timerWithTimeInterval:1 repeats:YES block:^(NSTimer * _Nonnull timer) {
        weakSelf.isSuccessLicense = weakSelf.videoProcessing.effectsProcess.isAuthrized;
        if (weakSelf.isSuccessLicense) {
            [weakSelf.timer invalidate];
            weakSelf.timer = nil;
        }
    }];
    [[NSRunLoop mainRunLoop]addTimer:self.timer forMode:NSRunLoopCommonModes];
}
#endif

- (void)setBeautyPreset {
#if __has_include(Sensetime)
    for (NSString *key in [self sensetimeDefault].allKeys) {
        int type = key.intValue;
        float value = [[self sensetimeDefault][key]floatValue];
        [self.videoProcessing setEffectType:type value: value];
    }
#elif __has_include(FURenderMoudle)
    NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
    NSString *faceAIPath = [bundle pathForResource:@"graphics/face_beautification" ofType:@"bundle"];
    FUBeauty *beauty = [[FUBeauty alloc] initWithPath:faceAIPath name:@"FUBeauty"];
    [FURenderKit shareRenderKit].beauty = beauty;
#endif
}

- (CVPixelBufferRef)onCapture:(CVPixelBufferRef)pixelBuffer {
#if __has_include(Sensetime)
    if (self.isSuccessLicense) {
        return [self.videoProcessing videoProcessHandler:pixelBuffer];
    }
    return nil;
#elif __has_include(FURenderMoudle)
    return [self.videoProcessing processFrame:pixelBuffer];
#endif
    return nil;
}

- (void)reset {
#if __has_include(Sensetime)
    for (NSString *key in [self sensetimeDefault].allKeys) {
        int type = key.intValue;
        [self.videoProcessing setEffectType:type value: 0];
    }
#elif __has_include(FURenderMoudle)
    [FURenderKit shareRenderKit].beauty = nil;
#endif
}

- (void)destory {
#if __has_include(Sensetime)
    [self reset];
    _videoProcessing = nil;
#elif __has_include(FURenderMoudle)
    [FURenderKit shareRenderKit].beauty = nil;
    [FURenderKit shareRenderKit].makeup = nil;
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    [FURenderKit destroy];
    _videoProcessing = nil;
#endif
}

@end
