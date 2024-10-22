//
//  FUManager.m
//  FULiveDemo
//
//

#import "FUManager.h"
#import "authpack.h"
#import "BundleUtil.h"
#if __has_include(<FURenderKit/FURenderKit.h>)
#import <FURenderKit/FURenderKit.h>
#endif

static FUManager *shareManager = NULL;

@interface FUManager ()

#if __has_include(<FURenderKit/FURenderKit.h>)
/// Current stickers
@property (nonatomic, strong) FUSticker *currentSticker;
#endif

@end

@implementation FUManager

+ (FUManager *)shareManager
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        shareManager = [[FUManager alloc] init];
    });
    
    return shareManager;
}

- (instancetype)init
{
    if (self = [super init]) {
#if __has_include(<FURenderKit/FURenderKit.h>)
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
            NSString *controllerPath = [[NSBundle mainBundle] pathForResource:@"controller_cpp" ofType:@"bundle"];
            NSString *controllerConfigPath = [[NSBundle mainBundle] pathForResource:@"controller_config" ofType:@"bundle"];
            FUSetupConfig *setupConfig = [[FUSetupConfig alloc] init];
            setupConfig.authPack = FUAuthPackMake(g_auth_package, sizeof(g_auth_package));
            setupConfig.controllerPath = controllerPath;
            setupConfig.controllerConfigPath = controllerConfigPath;
            
            // Initialize FURenderKit
            [FURenderKit setupWithSetupConfig:setupConfig];
            
            [FURenderKit setLogLevel:FU_LOG_LEVEL_ERROR];
            
            // Load the face AI model
            NSString *faceAIPath = [[NSBundle mainBundle] pathForResource:@"ai_face_processor" ofType:@"bundle"];
            [FUAIKit loadAIModeWithAIType:FUAITYPE_FACEPROCESSOR dataPath:faceAIPath];
            
            // Load the body AI model
            NSString *bodyAIPath = [[NSBundle mainBundle] pathForResource:@"ai_human_processor" ofType:@"bundle"];
            [FUAIKit loadAIModeWithAIType:FUAITYPE_HUMAN_PROCESSOR dataPath:bodyAIPath];
            
            NSString *path = [[NSBundle mainBundle] pathForResource:@"tongue" ofType:@"bundle"];
            [FUAIKit loadTongueMode:path];
            
            /* Set the flexibility of the mouth by default= 0*/ //
            float flexible = 0.5;
            [FUAIKit setFaceTrackParam:@"mouth_expression_more_flexible" value:flexible];
            
            // Set the quality of the face algorithm
            [FUAIKit shareKit].faceProcessorFaceLandmarkQuality = [FURenderKit devicePerformanceLevel] == FUDevicePerformanceLevelHigh ? FUFaceProcessorFaceLandmarkQualityHigh : FUFaceProcessorFaceLandmarkQualityMedium;
            
            // Set up small face detection
            [FUAIKit shareKit].faceProcessorDetectSmallFace = [FURenderKit devicePerformanceLevel] == FUDevicePerformanceLevelHigh;
        });
        
        [FUAIKit shareKit].maxTrackFaces = 4;
#endif
    }
    return self;
}

- (void)destoryItems {
#if __has_include(<FURenderKit/FURenderKit.h>)
    [FURenderKit shareRenderKit].beauty = nil;
    [FURenderKit shareRenderKit].bodyBeauty = nil;
    [FURenderKit shareRenderKit].makeup = nil;
    [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    self.currentSticker = nil;
#endif
}

- (void)setBuauty: (BOOL)isSelected {
#if __has_include(<FURenderKit/FURenderKit.h>)
    if (isSelected) {
        NSString *beautyPath = [[NSBundle mainBundle] pathForResource:@"face_beautification" ofType:@"bundle"];
        FUBeauty *beauty = [[FUBeauty alloc] initWithPath:beautyPath name:@"FUBeauty"];
        // Default uniform skin grinding
        beauty.heavyBlur = 0;
        beauty.blurType = 3;
        [FURenderKit shareRenderKit].beauty = beauty;
    } else {
        [FURenderKit shareRenderKit].beauty = nil;
    }
#endif
}
- (void)setMakeup: (BOOL)isSelected {
#if __has_include(<FURenderKit/FURenderKit.h>)
    if (isSelected) {
        NSString *beautyPath = [[NSBundle mainBundle] pathForResource:@"face_makeup" ofType:@"bundle"];
        FUMakeup *makeup = [[FUMakeup alloc] initWithPath:beautyPath name:@"face_makeup"];
        makeup.isMakeupOn = YES;
        [FURenderKit setLogLevel:FU_LOG_LEVEL_DEBUG];
        
        [FURenderKit shareRenderKit].makeup = makeup;
        [FURenderKit shareRenderKit].makeup.enable = isSelected;
        
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
        NSString *makeupPath = [bundle pathForResource:@"美妆/ziyun" ofType:@"bundle"];
        FUItem *makeupItem = [[FUItem alloc] initWithPath:makeupPath name:@"ziyun"];
        [makeup updateMakeupPackage:makeupItem needCleanSubItem:NO];
        makeup.intensity = 0.9;
    } else {
        [FURenderKit shareRenderKit].makeup.enable = NO;
        [FURenderKit shareRenderKit].makeup = nil;
    }
#endif
}
- (void)setSticker: (BOOL)isSelected {
#if __has_include(<FURenderKit/FURenderKit.h>)
    if (isSelected) {
        [self setStickerPath:@"DaisyPig"];
    } else {
        [[FURenderKit shareRenderKit].stickerContainer removeAllSticks];
    }
#endif
}
- (void)setFilter: (BOOL)isSelected {
#if __has_include(<FURenderKit/FURenderKit.h>)
    if (isSelected) {
        NSString *beautyPath = [[NSBundle mainBundle] pathForResource:@"face_beautification" ofType:@"bundle"];
        FUBeauty *beauty = [[FUBeauty alloc] initWithPath:beautyPath name:@"FUBeauty"];
        beauty.filterName = FUFilterMiTao1;
        beauty.filterLevel = 0.8;
        [FURenderKit shareRenderKit].beauty = beauty;
    } else {
        [FURenderKit shareRenderKit].beauty = nil;
    }
#endif
}


- (void)setStickerPath: (NSString *)stickerName {
    NSBundle *bundle = [BundleUtil bundleWithBundleName:@"FURenderKit" podName:@"fuLib"];
    NSString *path = [bundle pathForResource:[NSString stringWithFormat:@"贴纸/%@", stickerName] ofType:@"bundle"];
    if (!path) {
        NSLog(@"FaceUnity：Can't find the sticker path");
        return;
    }
#if __has_include(<FURenderKit/FURenderKit.h>)
    FUSticker *sticker = [[FUSticker alloc] initWithPath:path name:@"sticker"];
    if (self.currentSticker) {
        [[FURenderKit shareRenderKit].stickerContainer replaceSticker:self.currentSticker withSticker:sticker completion:nil];
    } else {
        [[FURenderKit shareRenderKit].stickerContainer addSticker:sticker completion:nil];
    }
    self.currentSticker = sticker;
#endif
}

- (void)updateBeautyBlurEffect {
#if __has_include(<FURenderKit/FURenderKit.h>)
    if (![FURenderKit shareRenderKit].beauty || ![FURenderKit shareRenderKit].beauty.enable) {
        return;
    }
    if ([FURenderKit devicePerformanceLevel] == FUDevicePerformanceLevelHigh) {
        // Set different skin polishing effects according to the confidence of the face
        CGFloat score = [FUAIKit fuFaceProcessorGetConfidenceScore:0];
        if (score > 0.95) {
            [FURenderKit shareRenderKit].beauty.blurType = 3;
            [FURenderKit shareRenderKit].beauty.blurUseMask = YES;
        } else {
            [FURenderKit shareRenderKit].beauty.blurType = 2;
            [FURenderKit shareRenderKit].beauty.blurUseMask = NO;
        }
    } else {
        // Set the fine abrasive effect
        [FURenderKit shareRenderKit].beauty.blurType = 2;
        [FURenderKit shareRenderKit].beauty.blurUseMask = NO;
    }
#endif
}


#pragma mark - VideoFilterDelegate

- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame {
    [self updateBeautyBlurEffect];
    if ([self.delegate respondsToSelector:@selector(faceUnityManagerCheckAI)]) {
        [self.delegate faceUnityManagerCheckAI];
    }
#if __has_include(<FURenderKit/FURenderKit.h>)
    if ([FURenderKit shareRenderKit].beauty == nil) {
        return frame;
    }
    FURenderInput *input = [[FURenderInput alloc] init];
    input.pixelBuffer = frame;
    //The face inside the default picture is always facing up, and there is no need to modify the attribute when rotating the screen.。
    input.renderConfig.imageOrientation = FUImageOrientationUP;
    //Turn on gravity sensing, and the internal will automatically calculate the correct direction and set fuSetDefaultRotationMode without external setting.
    input.renderConfig.gravityEnable = YES;
    //If the picture captured by the source camera must be set, otherwise it will lead to abnormal internal detection.
    input.renderConfig.isFromFrontCamera = YES;
    //This attribute refers to whether the system camera has been mirrored: in general, the frames from the front camera have been mirrored, so it needs to be set by default. If the mirror image is not set in the camera attributes, there is no need to change the attribute.
    input.renderConfig.isFromMirroredCamera = YES;
    FURenderOutput *output = [[FURenderKit shareRenderKit] renderWithInput:input];
    return output.pixelBuffer;
#else
    return frame;
#endif
}

@end
