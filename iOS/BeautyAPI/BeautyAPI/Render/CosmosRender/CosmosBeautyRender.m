//
//  CosmosBeautyRender.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/22.
//

#import "CosmosBeautyRender.h"

@interface CosmosBeautyRender ()
@property (nonatomic, copy) NSString *makeup;

@end

@implementation CosmosBeautyRender

- (CEBeautyRender *)render {
    if (_render == nil) {
        _render = [[CEBeautyRender alloc] init];
#if __has_include(CosmosMoudle)
        _render.inputType = CBRenderInputTypeStream;
#endif
    }
    return _render;
}

- (void)destroy { 
    [self reset];
    _render = nil;
}

- (void)reset {
#if __has_include(CosmosMoudle)
    [self.render clearLookup];
    [self.render clearMakeup];
    [self.render clearSticker];
    [self.render setAutoBeautyWithType:(CBBeautyAutoTypeNormal)];
#endif
}

- (void)setBeautyPreset {
#if __has_include(CosmosMoudle)
    [self.render setAutoBeautyWithType:(CBBeautyAutoTypeLovely)];
#endif
}

- (void)setBeautyFactor:(float)value forKey:(NSString *)key {
#if __has_include(CosmosMoudle)
    [self.render setBeautyFactor:value forKey:key];
#endif
}

- (void)addMakeupPath:(NSString *)path key:(NSString *)key value:(CGFloat)value {
#if __has_include(CosmosMoudle)
    if (path.length <= 0) {
        [self.render clearMakeup];
        self.makeup = nil;
        return;
    }
    if (![path isEqualToString:self.makeup]) {
        NSString *rootPath = [NSBundle.mainBundle pathForResource:@"makeup" ofType:@"bundle"];
        [self.render addMakeupPath:[rootPath stringByAppendingPathComponent:path]];
        self.makeup = path;
    }
    [self.render setBeautyFactor:value forKey:key];
#endif
}

- (void)addStickerPath:(NSString *)path {
#if __has_include(CosmosMoudle)
    if (path.length <= 0) {
        [self.render clearSticker];
        return;
    }
    NSString *rootPath = [NSBundle.mainBundle pathForResource:@"Resources" ofType:@"bundle"];
    [self.render setMaskModelPath:[rootPath stringByAppendingPathComponent:path]];
#endif
}

#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (AgoraVideoFormat)getVideoFormatPreference {
    return AgoraVideoFormatCVPixelBGRA;
}
#endif

- (nonnull CVPixelBufferRef)onCapture:(nonnull CVPixelBufferRef)pixelBuffer { 
#if __has_include(CosmosMoudle)
    NSError *error;
    CVPixelBufferRef renderedBuffer = [self.render renderPixelBuffer:pixelBuffer error:&error];
    if (renderedBuffer && !error) {
        return renderedBuffer;
    }
#endif
    return pixelBuffer;
}

@end
