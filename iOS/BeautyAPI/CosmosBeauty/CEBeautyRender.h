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

// For camera mode, need to set front/back camera position. This parameter is only used in camera mode
@property (nonatomic, assign) AVCaptureDevicePosition devicePosition;

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
// Current camera rotation angle relative to face. This parameter is only used in camera mode
@property (nonatomic, assign) CBRenderModuleCameraRotate cameraRotate;

// Image data format, default is CERenderInputTypeStream. Camera or video use CERenderInputTypeStream, static image use CERenderInputTypeStatic
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
// Set lookup material path
- (void)setLookupPath:(NSString *)lookupPath;
// Set lookup filter intensity
- (void)setLookupIntensity:(CGFloat)intensity;
// Clear filter effect
- (void)clearLookup;

// Set sticker resource path
- (void)setMaskModelPath:(NSString *)path;
- (void)clearSticker;

// Makeup effects
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
