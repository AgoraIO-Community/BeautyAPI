//
//  EffectsAnimal.h
//  SenseMeEffects
//
//  Created by sunjian on 2021/7/16.
//  Copyright © 2021 SoftSugar. All rights reserved.
//

#import <Foundation/Foundation.h>
#if __has_include("st_mobile_effect.h")
#import "st_mobile_common.h"
#import "st_mobile_animal.h"
#endif
#import "EffectMacro.h"

NS_ASSUME_NONNULL_BEGIN

@interface EffectsAnimal : NSObject
- (instancetype)initWithType:(EffectsType)type;

#if __has_include("st_mobile_effect.h")
/// Animal detection function
/// @param pixelBuffer image data per frame
/// @param rotate the rotation direction of the mobile phone
/// @param detectResult test results
- (st_result_t)detectAnimalWithPixelbuffer:(CVPixelBufferRef)pixelBuffer
                                    rotate:(st_rotate_type)rotate
                                    config:(st_mobile_animal_type)config
                             detectResult:(st_mobile_animal_result_t *)detectResult;

/// Animal detection function
/// @param buffer Image data per frame
/// @param rotate the rotation direction of the mobile phone
/// @param pixelFormat Video Data Format (YUV/RGBA/BGRA...)
/// @param width Image width
/// @param height Image height
/// @param stride The stride of the image
/// @param detectResult test results
- (st_result_t)detectAnimalWithBuffer:(unsigned char *)buffer
                               rotate:(st_rotate_type)rotate
                          pixelFormat:(st_pixel_format)pixelFormat
                                width:(int)width
                               height:(int)height
                               stride:(int)stride
                               config:(st_mobile_animal_type)config
                         detectResult:(st_mobile_animal_result_t *)detectResult;

-(st_result_t)resetAnimalFaceTracker;
#endif

@end

NS_ASSUME_NONNULL_END
