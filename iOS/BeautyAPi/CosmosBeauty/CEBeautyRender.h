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

// 如果是相机，需要传入前置/后置位置, 该参数仅在相机模式下设置
@property (nonatomic, assign) AVCaptureDevicePosition devicePosition;

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
// 目前摄像头相对于人脸的旋转角度, 该参数仅在相机模式下设置
@property (nonatomic, assign) CBRenderModuleCameraRotate cameraRotate;

// 图像数据形式, 默认CERenderInputTypeStream。 相机或视频CERenderInputTypeStream，静态图片CERenderInputTypeStatic
@property (nonatomic, assign) CBRenderInputType inputType;


// 设置美颜参数
- (void)setBeautyFactor:(float)value forKey:(CBBeautyFilterKey)key;
#endif

- (BOOL)isAuthSuccess;

- (void)setBeautyWhiteVersion:(NSInteger)version;
- (void)setBeautyreddenVersion:(NSInteger)version;

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (void)setAutoBeautyWithType:(CBBeautyAutoType)type;
#endif
- (void)setMakeupLipsType:(NSUInteger)type;
// 设置lookup素材路径
- (void)setLookupPath:(NSString *)lookupPath;
// 设置lookup滤镜浓度
- (void)setLookupIntensity:(CGFloat)intensity;
// 清除滤镜效果
- (void)clearLookup;

// 设置贴纸资源路径
- (void)setMaskModelPath:(NSString *)path;
- (void)clearSticker;

// 美妆效果
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
