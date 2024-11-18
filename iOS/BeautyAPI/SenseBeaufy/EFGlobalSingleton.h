//
//  EFGlobalSingleton.h
//  SenseMeEffects
//
//  Created by 马浩萌 on 2021/11/30.
//  Copyright © 2021 SoftSugar. All rights reserved.
//

#import <Foundation/Foundation.h>

static NSString * const EFGlobalSingletonMaleKey = @"EFGlobalSingletonMaleKey";

@interface EFGlobalSingleton : NSObject

@property (nonatomic, assign) int efTouchTriggerAction; // Save screen touch trigger event
@property (nonatomic, assign) BOOL efHasSegmentCapability; // Flag indicating whether skin segmentation capability exists
@property (nonatomic, assign) BOOL isMale; // Flag indicating current user gender (different default parameters)
@property (nonatomic, assign) BOOL needDelay; // Whether tryon shoes/watch needs to enable future frames
@property (nonatomic, assign) BOOL isTryonShoes; // Whether it is tryon shoes (barefoot prompt)
@property (nonatomic, assign) BOOL isPortraitOnly; // Whether it is gan image (landscape only)

+(instancetype)sharedInstance;
-(instancetype)init NS_UNAVAILABLE;
+(instancetype)new NS_UNAVAILABLE;
+(instancetype)alloc NS_UNAVAILABLE;

@end

