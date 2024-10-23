//
//  CEBeautyRender.m
//  CosmosEffectSample
//
//  Created by cosmos783 on 2019/12/19.
//  Copyright © 2019 cosmos783_fish@sina.cn. All rights reserved.
//

#import "CEBeautyRender.h"

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
@interface CEBeautyRender () <CBRenderModuleManagerDelegate>
{
    BOOL _isAuthSuccess;
}

@property (nonatomic, strong) CBRenderModuleManager *render;
@property (nonatomic, strong) CBRenderFilterBeautyMakeupModule *beautyDescriptor;

@property (nonatomic, strong) CBRenderFilterLookupModule *lookupDescriptor;

@property (nonatomic, strong) CBRenderFilterStickerModule *stickerDescriptor;

@end
#endif

@implementation CEBeautyRender

- (instancetype)init {
    self = [super init];
    if (self) {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
//#Error Replace the authorization information to ensure that the authorization information and Bundle ID correspond
        CBRenderError ret1 = [CosmosBeautySDK initSDKWith:@"<#Input License#>"];
        
        CBRenderError ret2 = [CosmosBeautySDK setupCVModelPath:[[NSBundle mainBundle]pathForResource:@"cv" ofType:@"bundle"]];
        _isAuthSuccess = ret1 == CBRenderErrorNone && ret2 == CBRenderErrorNone;
        
        NSLog(@"Initialization of beauty path = %@",[NSBundle bundleForClass:[CBRenderModuleManager class]].bundlePath);

        CBRenderModuleManager *render = [[CBRenderModuleManager alloc] init];
        render.devicePosition = AVCaptureDevicePositionBack;
        render.inputType = CBRenderInputTypeStream;
        render.delegate = self;
        self.render = render;
        
        _beautyDescriptor = [[CBRenderFilterBeautyMakeupModule alloc] init];
        [render registerModule:_beautyDescriptor];

        _lookupDescriptor = [[CBRenderFilterLookupModule alloc] init];
        [render registerModule:_lookupDescriptor];
        
        _stickerDescriptor = [[CBRenderFilterStickerModule alloc] init];
        [render registerModule:_stickerDescriptor];
#endif
    }
    return self;
}

- (BOOL)isAuthSuccess {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    return _isAuthSuccess;
#else
    return NO;
#endif
}

- (void)addBeauty {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    _beautyDescriptor = [[CBRenderFilterBeautyMakeupModule alloc] init];
    [_render registerModule:_beautyDescriptor];
#endif
}

- (void)removeBeauty {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    [_render unregisterModule:_beautyDescriptor];
    _beautyDescriptor = nil;
#endif
}

- (void)addLookup {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    _lookupDescriptor = [[CBRenderFilterLookupModule alloc] init];
    [_render registerModule:_lookupDescriptor];
#endif
}

- (void)removeLookup {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    [_render unregisterModule:_lookupDescriptor];
    _lookupDescriptor = nil;
#endif
}
- (void)setMakeupLipsType:(NSUInteger)type{
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    [_beautyDescriptor setMakeupLipsType:type];
#endif
}

- (void)addSticker {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    _stickerDescriptor = [[CBRenderFilterStickerModule alloc] init];
    [_render registerModule:_stickerDescriptor];
#endif
}

- (void)removeSticker {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    [_render unregisterModule:_stickerDescriptor];
    _stickerDescriptor = nil;
#endif
}

- (CVPixelBufferRef _Nullable)renderPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                          error:(NSError * __autoreleasing _Nullable *)error {
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    return [self.render renderFrame:pixelBuffer error:error];
#else
    return pixelBuffer;
#endif
}


#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (CVPixelBufferRef _Nullable)renderPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                        context:(MTIContext*)context
                                          error:(NSError * __autoreleasing _Nullable *)error {
    CVPixelBufferRef renderedPixelBuffer = NULL;
    renderedPixelBuffer =  [self.render renderFrame:pixelBuffer context:context error:error];
    return renderedPixelBuffer;
}
#endif

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (MTIImage *)renderToImageWithPixelBuffer:(CVPixelBufferRef)pixelBuffer
                                   context:(nonnull MTIContext *)context
                                     error:(NSError *__autoreleasing  _Nullable * _Nullable)error
{
    return [self.render renderFrameToImage:pixelBuffer context:context error:error];
}
#endif

#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
- (void)setInputType:(CBRenderInputType)inputType {
    self.render.inputType = inputType;
}

- (CBRenderInputType)inputType {
    return self.render.inputType;
}

- (void)setCameraRotate:(CBRenderModuleCameraRotate)cameraRotate {
    self.render.cameraRotate = cameraRotate;
}

- (CBRenderModuleCameraRotate)cameraRotate {
    return self.render.cameraRotate;
}

- (void)setDevicePosition:(AVCaptureDevicePosition)devicePosition {
    self.render.devicePosition = devicePosition;
}

- (AVCaptureDevicePosition)devicePosition {
    return self.render.devicePosition;
}

- (void)setBeautyFactor:(float)value forKey:(CBBeautyFilterKey)key {
    [self.beautyDescriptor setBeautyFactor:value forKey:key];
    
}
- (void)setAutoBeautyWithType:(CBBeautyAutoType)type{
    [self.beautyDescriptor adjustAutoBeautyWithType:type];
}

- (void)setBeautyWhiteVersion:(NSInteger)version{
    [self.beautyDescriptor setBeautyWhiteVersion:(CBBeautyWhittenFilterVersion)version];
}
- (void)setBeautyreddenVersion:(NSInteger)version{
    [self.beautyDescriptor setBeautyRaddenVersion:(CBBeautyReddenFilterVersion)version];
}

- (void)setLookupPath:(NSString *)lookupPath {
    [self.lookupDescriptor setLookupResourcePath:lookupPath];
    [self.lookupDescriptor setIntensity:1.0];
}

- (void)setLookupIntensity:(CGFloat)intensity {
    [self.lookupDescriptor setIntensity:intensity];
}

- (void)clearLookup {
    [self.lookupDescriptor clear];
}

- (void)setMaskModelPath:(NSString *)path {
    [self.stickerDescriptor setMaskModelPath:path];
}

- (void)clearSticker {
    [self.stickerDescriptor clear];
}

- (void)clearMakeup {
    [self.beautyDescriptor clearMakeup];
}

- (void)addMakeupPath:(NSString *)path {
    [self.beautyDescriptor addMakeupWithResourceURL:path];
}

- (void)removeMakeupLayerWithType:(CBBeautyFilterKey)type {
    [self.beautyDescriptor removeMakeupLayerWithType:type];
}

- (void)renderModuleManager:(CBRenderModuleManager *)manager faceFeatureCount:(NSInteger)faceFeatures{
//    NSLog(@"face count %d",faceFeatures);
}
#endif

@end

