//
//  LoadUtil.m
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/30.
//

#import "LoadUtil.h"
#import "BeautyAPi-Swift.h"

@implementation LoadUtil

+(void)load {
    NSMutableArray *tempArray = [NSMutableArray array];
#if __has_include("st_mobile_common.h")
    [tempArray addObject:@"sensetime"];
#endif
#if __has_include(<FURenderKit/FURenderKit.h>)
    [tempArray addObject:@"fu"];
#endif
#if __has_include(<effect-sdk/bef_effect_ai_api.h>)
    [tempArray addObject:@"bytes"];
#endif
#if __has_include(<CosmosBeautyKit/CosmosBeautySDK.h>)
    [tempArray addObject:@"cosmos"];
#endif
    [Configs setBeautyTypes:tempArray.copy];
}

@end
