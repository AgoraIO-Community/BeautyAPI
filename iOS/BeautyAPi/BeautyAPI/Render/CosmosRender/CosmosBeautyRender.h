//
//  CosmosBeautyRender.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/22.
//

#define CosmosMoudle <CosmosBeautyKit/CosmosBeautySDK.h>

#import <Foundation/Foundation.h>

#import "BeautyAPI.h"
#import "CEBeautyRender.h"

NS_ASSUME_NONNULL_BEGIN

@interface CosmosBeautyRender : NSObject<BeautyRenderDelegate>

@property (nonatomic, strong) CEBeautyRender *render;

- (void)setBeautyFactor:(float)value forKey:(NSString *)key;

- (void)addMakeupPath:(NSString *)path;

- (void)addStickerPath:(NSString *)path;

@end

NS_ASSUME_NONNULL_END
