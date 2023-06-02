//
//  BeautyRender.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#define Sensetime "VideoProcessingManager.h"

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#if __has_include(Sensetime)
#import Sensetime
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BeautyRender : NSObject

#if __has_include(Sensetime)
@property (nonatomic, strong) VideoProcessingManager *videoProcessing;
#endif

- (void)setOptimizedDefault;

- (CVPixelBufferRef)onCapture: (CVPixelBufferRef)pixelBuffer;

- (void)reset;

- (void)destory;

@end

NS_ASSUME_NONNULL_END
