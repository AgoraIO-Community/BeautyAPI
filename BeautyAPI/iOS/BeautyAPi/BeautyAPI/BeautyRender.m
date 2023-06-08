//
//  BeautyRender.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import "BeautyRender.h"

@interface BeautyRender ()
#if __has_include(Sensetime)
@property (nonatomic, assign) BOOL isSuccessLicense;
@property (nonatomic, strong) NSTimer *timer;
///贴纸id
@property (nonatomic, assign) int stickerId;
@property (nonatomic, assign) int filterId;
#endif

@end

@implementation BeautyRender

- (instancetype)init {
    if (self == [super init]) {
#if __has_include(Sensetime)
        [self checkSensetimeLicense];
#endif
    }
    return self;
}

#if __has_include(Sensetime)
- (VideoProcessingManager *)videoProcessing {
    if (_videoProcessing == nil) {
        _videoProcessing = [VideoProcessingManager new];
    }
    return _videoProcessing;
}
#endif

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
#endif
}
- (void)setFilter: (BOOL)isSelected {
#if __has_include(Sensetime)
    if (isSelected) {
        NSString *path =  [[NSBundle mainBundle] pathForResource:@"qise.zip" ofType:nil];
        __weak BeautyRender *weakself = self;
        [self.videoProcessing.effectsProcess addStickerWithPath:path callBack:^(st_result_t state, int sticker, uint64_t action) {
            [weakself.videoProcessing.effectsProcess setPackageId:sticker groupType:EFFECT_BEAUTY_GROUP_FILTER strength:0.5];
            weakself.filterId = sticker;
        }];
    } else {
        [self.videoProcessing.effectsProcess removeSticker:self.filterId];
        self.filterId = 0;
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

- (void)setOptimizedDefault {
#if __has_include(Sensetime)
    for (NSString *key in [self sensetimeDefault].allKeys) {
        int type = key.intValue;
        float value = [[self sensetimeDefault][key]floatValue];
        [self.videoProcessing setEffectType:type value: value];
    }
#endif
}

- (CVPixelBufferRef)onCapture:(CVPixelBufferRef)pixelBuffer {
#if __has_include(Sensetime)
    if (self.isSuccessLicense) {
        return [self.videoProcessing videoProcessHandler:pixelBuffer];
    }
    return nil;
#endif
    return nil;
}

- (void)reset {
#if __has_include(Sensetime)
    for (NSString *key in [self sensetimeDefault].allKeys) {
        int type = key.intValue;
        [self.videoProcessing setEffectType:type value: 0];
    }
#endif
}

- (void)destory {
#if __has_include(Sensetime)
    [self reset];
    _videoProcessing = nil;
#endif
}

@end
