//
//  FUBeautyRender.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/6/30.
//

#import "FUBeautyRender.h"
#import "BundleUtil.h"

@interface FUBeautyRender ()

#if __has_include(FURenderMoudle)
/// 当前的贴纸
@property (nonatomic, strong) FUSticker *currentSticker;
#endif

@end

@implementation FUBeautyRender

- (instancetype)init {
    if (self == [super init]) {
#if __has_include("FUManager.h")
        self.fuManager = [[FUManager alloc] init];
#endif
    }
    return self;
}

- (void)destroy {
#if __has_include(FURenderMoudle)
    [FURenderKit shareRenderKit].beauty = nil;
    [FURenderKit shareRenderKit].makeup = nil;
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    dispatch_queue_t referQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_LOW, 0);
    dispatch_async(referQueue, ^{
        [FURenderKit destroy];
    });
    _fuManager = nil;
#endif
}

- (nonnull CVPixelBufferRef)onCapture:(nonnull CVPixelBufferRef)pixelBuffer {
#if __has_include(FURenderMoudle)
    return [self.fuManager processFrame:pixelBuffer];
#endif
    return pixelBuffer;
}
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (AgoraVideoFormat)getVideoFormatPreference {
    return AgoraVideoFormatCVPixelNV12;
}
#endif

- (void)setBeautyWithPath:(NSString *)path key:(NSString *)key value:(float)value {
#if __has_include(FURenderMoudle)
    FUBeauty *beauty = [FURenderKit shareRenderKit].beauty;
    if (beauty == nil) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *faceAIPath = [bundle pathForResource:[NSString stringWithFormat:@"graphics/%@", path] ofType:@"bundle"];
        beauty = [[FUBeauty alloc] initWithPath:faceAIPath name:@"FUBeauty"];
        beauty.heavyBlur = 0;
    }
    if ([key isEqualToString:@"blurLevel"]) {
        beauty.blurLevel = value * 6.0;
    } else if ([key isEqualToString:@"whiten"]) {
        beauty.colorLevel = value;
    } else if ([key isEqualToString:@"thin"]) {
        beauty.cheekThinning = value;
    } else if ([key isEqualToString:@"cheekNarrow"]) {
        beauty.cheekNarrow = value;
    } else if ([key isEqualToString:@"cheekSmall"]) {
        beauty.cheekSmall = value;
    } else if ([key isEqualToString:@"cheek"]) {
        beauty.intensityCheekbones = value;
    } else if ([key isEqualToString:@"chin"]) {
        beauty.intensityChin = value;
    } else if ([key isEqualToString:@"forehead"]) {
        beauty.intensityForehead = value;
    } else if ([key isEqualToString:@"enlarge"]) {
        beauty.eyeEnlarging = value;
    } else if ([key isEqualToString:@"eyeBright"]) {
        beauty.eyeBright = value;
    } else if ([key isEqualToString:@"eyeCircle"]) {
        beauty.intensityEyeCircle = value;
    } else if ([key isEqualToString:@"eyeSpace"]) {
        beauty.intensityEyeSpace = value;
    } else if ([key isEqualToString:@"eyeLid"]) {
        beauty.intensityEyeLid = value;
    } else if ([key isEqualToString:@"pouchStrength"]) {
        beauty.removePouchStrength = value;
    } else if ([key isEqualToString:@"browHeight"]) {
        beauty.intensityBrowHeight = value;
    } else if ([key isEqualToString:@"browThick"]) {
        beauty.intensityBrowThick = value;
    } else if ([key isEqualToString:@"nose"]) {
        beauty.intensityNose = value;
    } else if ([key isEqualToString:@"wrinkles"]) {
        beauty.removeNasolabialFoldsStrength = value;
    } else if ([key isEqualToString:@"philtrum"]) {
        beauty.intensityPhiltrum = value;
    } else if ([key isEqualToString:@"longNose"]) {
        beauty.intensityLongNose = value;
    } else if ([key isEqualToString:@"lowerJaw"]) {
        beauty.intensityLowerJaw = value;
    } else if ([key isEqualToString:@"mouth"]) {
        beauty.intensityMouth = value;
    } else if ([key isEqualToString:@"lipThick"]) {
        beauty.intensityLipThick = value;
    } else if ([key isEqualToString:@"toothWhiten"]) {
        beauty.toothWhiten = value;
    }
    [FURenderKit shareRenderKit].beauty = beauty;
#endif
}

- (void)setStyleWithPath:(NSString *)path key:(NSString *)key value:(float)value {
#if __has_include(FURenderMoudle)
    NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
    NSString *makeupPath = [bundle pathForResource:path ofType:@"bundle"];
    FUMakeup *makeup = [[FUMakeup alloc] initWithPath:makeupPath name:@"face_makeup"];
    NSString *stylePath = [bundle pathForResource:[NSString stringWithFormat:@"makeup/%@", key] ofType:@"bundle"];
    FUItem *makupItem = [[FUItem alloc] initWithPath:stylePath name:key];
    makeup.isMakeupOn = YES;
    [FURenderKit shareRenderKit].makeup = makeup;
    [FURenderKit shareRenderKit].makeup.enable = YES;
    [makeup updateMakeupPackage:makupItem needCleanSubItem:NO];
    makeup.intensity = 0.7;
#endif
}

- (void)setStickerWithPath:(NSString *)path {
    NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
    NSString *stickerPath = [bundle pathForResource:[NSString stringWithFormat:@"sticker/%@", path] ofType:@"bundle"];
    FUSticker *sticker = [[FUSticker alloc] initWithPath:stickerPath name:@"sticker"];
#if __has_include(FURenderMoudle)
    if (self.currentSticker) {
        [[FURenderKit shareRenderKit].stickerContainer replaceSticker:self.currentSticker withSticker:sticker completion:nil];
    } else {
        [[FURenderKit shareRenderKit].stickerContainer addSticker:sticker completion:nil];
    }
    self.currentSticker = sticker;
#endif
}

- (void)reset {
#if __has_include(FURenderMoudle)
    [FURenderKit shareRenderKit].beauty = nil;
#endif
}

- (void)resetStyle {
#if __has_include(FURenderMoudle)
    [FURenderKit shareRenderKit].makeup.enable = NO;
    [FURenderKit shareRenderKit].makeup = nil;
#endif
}

- (void)resetSticker {
#if __has_include(FURenderMoudle)
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    self.currentSticker = nil;
#endif
}

- (void)setBeautyPreset {
#if __has_include(FURenderMoudle)
    NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
    NSString *faceAIPath = [bundle pathForResource:@"graphics/face_beautification" ofType:@"bundle"];
    FUBeauty *beauty = [[FUBeauty alloc] initWithPath:faceAIPath name:@"FUBeauty"];
    [FURenderKit shareRenderKit].beauty = beauty;
#endif
}

- (void)setMakeup:(BOOL)isSelected {
#if __has_include(FURenderMoudle)
    if (isSelected) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *makeupPath = [bundle pathForResource:@"graphics/face_makeup" ofType:@"bundle"];
        FUMakeup *makeup = [[FUMakeup alloc] initWithPath:makeupPath name:@"face_makeup"];
        NSString *path = [bundle pathForResource:@"makeup/ziyun" ofType:@"bundle"];
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

- (void)setSticker:(BOOL)isSelected {
#if __has_include(FURenderMoudle)
    if (isSelected) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *path = [bundle pathForResource:[NSString stringWithFormat:@"sticker/%@", @"fu_zh_fenshu"] ofType:@"bundle"];
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

@end
