//
//  BeautyRender.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#define Sensetime "st_mobile_common.h"
#define FURenderMoudle <FURenderKit/FURenderKit.h>

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

#if __has_include("VideoProcessingManager.h")
#import "VideoProcessingManager.h"
#endif

#if __has_include("FUManager.h")
#import "FUManager.h"
#import <FURenderKit/FURenderKit.h>
#endif


NS_ASSUME_NONNULL_BEGIN

@interface BeautyRender : NSObject

#if __has_include(Sensetime)
@property (nonatomic, strong) VideoProcessingManager *videoProcessing;
#endif

#if __has_include(FURenderMoudle)
@property (nonatomic, strong) FUManager *videoProcessing;
#endif

- (void)setBeautyPreset;

- (CVPixelBufferRef)onCapture: (CVPixelBufferRef)pixelBuffer;

- (void)setMakeup: (BOOL)isSelected;
- (void)setSticker: (BOOL)isSelected;

- (void)reset;

- (void)destory;

@end

NS_ASSUME_NONNULL_END
