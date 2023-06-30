//
//  BeautyRender.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#define Sensetime "st_mobile_common.h"
#define FURenderMoudle <FURenderKit/FURenderKit.h>
#define BytesMoudle "bef_effect_ai_api.h"

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

#if __has_include("VideoProcessingManager.h") && __has_include(Sensetime)
#import "VideoProcessingManager.h"
#endif

#if __has_include("FUManager.h") && __has_include(<FURenderKit/FURenderKit.h>)
#import "FUManager.h"
#import <FURenderKit/FURenderKit.h>
#endif

#if __has_include(BytesMoudle) && __has_include("BEImageUtils.h") && __has_include("BEFrameProcessor.h")
#import "BEImageUtils.h"
#import "BEFrameProcessor.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BeautyRender : NSObject

#if __has_include(Sensetime)
@property (nonatomic, strong) VideoProcessingManager *videoProcessing;
#endif

#if __has_include(FURenderMoudle)
@property (nonatomic, strong) FUManager *fuManager;
#endif

#if __has_include(BytesMoudle)
@property (nonatomic, strong) BEFrameProcessor *frameProcessor;
@property (nonatomic, strong) BEImageUtils *imageUtils;
#endif

- (void)setBeautyPreset;

- (CVPixelBufferRef)onCapture: (CVPixelBufferRef)pixelBuffer;

- (void)setMakeup: (BOOL)isSelected;
- (void)setSticker: (BOOL)isSelected;

- (void)reset;

- (void)destory;

@end

NS_ASSUME_NONNULL_END
