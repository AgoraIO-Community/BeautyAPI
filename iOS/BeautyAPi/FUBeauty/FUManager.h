//
//  FUManager.h
//  FULiveDemo
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@protocol VideoFilterDelegate <NSObject>

- (CVPixelBufferRef)processFrame:(CVPixelBufferRef)frame;

@end


@protocol FUManagerProtocol <NSObject>

//It is used to detect whether there are AI faces and human figures.
- (void)faceUnityManagerCheckAI;

@end

@interface FUManager : NSObject <VideoFilterDelegate>

@property (nonatomic, weak) id<FUManagerProtocol>delegate;

+ (FUManager *)shareManager;

/// Destroy all the props
- (void)destoryItems;

/// Update the beauty skin polishing effect (set different skin polishing effects according to the confidence of face detection)
- (void)updateBeautyBlurEffect;

- (void)setBuauty: (BOOL)isSelected;
- (void)setMakeup: (BOOL)isSelected;
- (void)setSticker: (BOOL)isSelected;
- (void)setFilter: (BOOL)isSelected;

@end
