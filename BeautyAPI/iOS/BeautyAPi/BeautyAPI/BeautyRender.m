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

@property (nonatomic, strong) NSMutableArray *bytesNodes;

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
- (FUManager *)fuManager {
    if (_fuManager == nil) {
        _fuManager = [[FUManager alloc] init];
    }
    return _fuManager;
}
#endif

- (NSMutableArray *)bytesNodes {
    if (_bytesNodes == nil) {
        _bytesNodes = [[NSMutableArray alloc] initWithArray:@[@"/beauty_IOS_lite", @"/reshape_lite", @"/beauty_4Items"]];
    }
    return _bytesNodes;
}

#if __has_include(BytesMoudle)
- (BEFrameProcessor *)frameProcessor {
    if (_frameProcessor == nil) {
        EAGLContext *context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
        [EAGLContext setCurrentContext:context];
        _frameProcessor = [[BEFrameProcessor alloc]initWithContext:context resourceDelegate:nil];
        _frameProcessor.processorResult = BECVPixelBuffer;
        [_frameProcessor setEffectOn:YES];
        [_frameProcessor updateComposerNodes:self.bytesNodes];
    }
    return _frameProcessor;
}
- (BEImageUtils *)imageUtils {
    if (_imageUtils == nil) {
        _imageUtils = [[BEImageUtils alloc] init];
    }
    return _imageUtils;
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
#elif __has_include(BytesMoudle)
    if (isSelected) {
        if (![self.bytesNodes containsObject:@"/style_makeup/qise"]) {
            [self.bytesNodes addObject:@"/style_makeup/qise"];
            [self.frameProcessor updateComposerNodes:self.bytesNodes];
        }
        [self.frameProcessor updateComposerNodeIntensity:@"/style_makeup/qise" key:@"Makeup_ALL" intensity:0.6];
    } else {
        if ([self.bytesNodes containsObject:@"/style_makeup/qise"]) {
            [self.bytesNodes removeObject:@"/style_makeup/qise"];
            [self.frameProcessor updateComposerNodes:self.bytesNodes];
        }
        [self.frameProcessor updateComposerNodeIntensity:@"/style_makeup/qise" key:@"Makeup_ALL" intensity:0];
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
        NSString *path = [bundle pathForResource:[NSString stringWithFormat:@"贴纸/%@", @"fu_zh_fenshu"] ofType:@"bundle"];
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
#elif __has_include(BytesMoudle)
    if (isSelected) {
        [self.frameProcessor setStickerPath:@"stickers_huanlongshu"];
    } else {
        [self.frameProcessor setStickerPath:@""];
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
#elif __has_include(BytesMoudle)
    [self.frameProcessor updateComposerNodeIntensity:@"/beauty_IOS_lite" key:@"whiten" intensity:0.5];
    [self.frameProcessor updateComposerNodeIntensity:@"/beauty_IOS_lite" key:@"smooth" intensity:0.3];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Overall" intensity:0.15];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Zoom_Cheekbone" intensity:0.3];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Eye" intensity:0.15];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Nose" intensity:0.15];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Chin" intensity:0.46];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Zoom_Jawbone" intensity:0.46];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_Forehead" intensity:0.4];
    [self.frameProcessor updateComposerNodeIntensity:@"/reshape_lite" key:@"Internal_Deform_ZoomMouth" intensity:0.16];
    [self.frameProcessor updateComposerNodeIntensity:@"/beauty_4Items" key:@"BEF_BEAUTY_WHITEN_TEETH" intensity:0.2];
#endif
}

- (CVPixelBufferRef)onCapture:(CVPixelBufferRef)pixelBuffer {
#if __has_include(Sensetime)
    if (self.isSuccessLicense) {
        return [self.videoProcessing videoProcessHandler:pixelBuffer];
    }
    return nil;
#elif __has_include(FURenderMoudle)
    return [self.fuManager processFrame:pixelBuffer];
#elif __has_include(BytesMoudle)
    BEPixelBufferInfo *pixelBufferInfo = [self.imageUtils getCVPixelBufferInfo:pixelBuffer];
    if (pixelBufferInfo.format != BE_BGRA) {
        pixelBuffer = [self.imageUtils transforCVPixelBufferToCVPixelBuffer:pixelBuffer outputFormat:BE_BGRA];
    }
    CVPixelBufferRef px = [self.frameProcessor process: pixelBuffer
                                               timeStamp: [NSDate date].timeIntervalSince1970].pixelBuffer;
    return px;
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
#elif __has_include(BytesMoudle)
    [self.frameProcessor updateComposerNodeIntensity:@"/beauty_IOS_lite" key:@"whiten" intensity:0];
    [self.frameProcessor updateComposerNodeIntensity:@"/beauty_IOS_lite" key:@"smooth" intensity:0];
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
    _fuManager = nil;
#elif __has_include(BytesMoudle)
    _frameProcessor = nil;
    _imageUtils = nil;
#endif
}

@end
