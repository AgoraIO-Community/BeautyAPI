//
//  EFGlobalSingleton.h
//  SenseMeEffects
//
//  Copyright © 2021 SoftSugar. All rights reserved.
//

#import <Foundation/Foundation.h>

static NSString * const EFGlobalSingletonMaleKey = @"EFGlobalSingletonMaleKey";

@interface EFGlobalSingleton : NSObject

@property (nonatomic, assign) int efTouchTriggerAction; // Click the screen to trigger the event to save
@property (nonatomic, assign) BOOL efHasSegmentCapability; // Identify whether there is a skin segmentation capability
@property (nonatomic, assign) BOOL isMale; // Identify the gender of the current user (different default parameters)
@property (nonatomic, assign) BOOL needDelay; // Whether it is a tryon shoe test/test watch needs to turn on the future frame
@property (nonatomic, assign) BOOL isTryonShoes; // Is it a tryon shoe test (barefoot tips)
@property (nonatomic, assign) BOOL isPortraitOnly; // Is it gan image (only supports horizontal screen)

+(instancetype)sharedInstance;
-(instancetype)init NS_UNAVAILABLE;
+(instancetype)new NS_UNAVAILABLE;
+(instancetype)alloc NS_UNAVAILABLE;

@end

