//
//  FUManager.h
//  FULiveDemo
//
//  Created by 刘洋 on 2017/8/18.
//  Copyright © 2017年 刘洋. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@protocol VideoFilterDelegate <NSObject>

- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame;

@end


@protocol FUManagerProtocol <NSObject>

// Used to detect AI faces and human shapes
- (void)faceUnityManagerCheckAI;

@end

@interface FUManager : NSObject <VideoFilterDelegate>

@property (nonatomic, weak) id<FUManagerProtocol>delegate;

+ (FUManager *)shareManager;

/// Destroy all props
- (void)destoryItems;

/// Update beauty skin smoothing effect (set different smoothing effects based on face detection confidence)
- (void)updateBeautyBlurEffect;

- (void)setBuauty: (BOOL)isSelected;
- (void)setMakeup: (BOOL)isSelected;
- (void)setSticker: (BOOL)isSelected;
- (void)setFilter: (BOOL)isSelected;

@end
