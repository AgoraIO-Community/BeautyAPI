//
//  CEBeautyRender.h
//  CosmosEffectSample
//
//  Created by cosmos783 on 2019/12/19.
//  Copyright © 2019 cosmos783_fish@sina.cn. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
#import <CosmosBeautyKit/CosmosBeautySDK.h>
#endif

NS_ASSUME_NONNULL_BEGIN

@interface CEBeautyRender : NSObject

- (void)addBeauty;
- (void)removeBeauty;

- (void)addLookup;
- (void)removeLookup;

- (void)addSticker;
- (void)removeSticker;

// If it is a camera, it needs to be passed to the front/rear position, and this parameter is only set in camera mode.
@property (nonatomic, assign) AVCaptureDevicePosition devicePosition;

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
// At present, the rotation angle of the camera relative to the face, this parameter is only set in camera mode.
@property (nonatomic, assign) CBRenderModuleCameraRotate cameraRotate;

// Image data form, default CERenderInputTypeStream. Camera or video CERenderInputTypeStream, static picture CERenderInputTypeStatic
@property (nonatomic, assign) CBRenderInputType inputType;


// Set beauty parameters
- (void)setBeautyFactor:(float)value forKey:(CBBeautyFilterKey)key;
#endif

- (BOOL)isAuthSuccess;

- (void)setBeautyWhiteVersion:(NSInteger)version;
- (void)setBeautyreddenVersion:(NSInteger)version;

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (void)setAutoBeautyWithType:(CBBeautyAutoType)type;
#endif
- (void)setMakeupLipsType:(NSUInteger)type;
// Set the lookup material path
- (void)setLookupPath:(NSString *)lookupPath;
// Set the concentration of lookup filter
- (void)setLookupIntensity:(CGFloat)intensity;
// Clear the filter effect
- (void)clearLookup;

// Set the sticker resource path
- (void)setMaskModelPath:(NSString *)path;
- (void)clearSticker;

// Beauty effect
- (void)addMakeupPath:(NSString *)path;
- (void)clearMakeup;
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (void)removeMakeupLayerWithType:(CBBeautyFilterKey)type;
#endif

- (CVPixelBufferRef _Nullable)renderPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                          error:(NSError * __autoreleasing _Nullable *)error;


#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (CVPixelBufferRef _Nullable)renderPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                        context:(MTIContext*)context
                                          error:(NSError * __autoreleasing _Nullable *)error;

- (MTIImage * _Nullable)renderToImageWithPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                             context:(MTIContext*)context
                                               error:(NSError * __autoreleasing _Nullable *)error;

#endif

@end

NS_ASSUME_NONNULL_END
