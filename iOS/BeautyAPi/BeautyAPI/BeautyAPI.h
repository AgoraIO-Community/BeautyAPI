//
//  BeautyAPI.h
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/31.
//

#import <Foundation/Foundation.h>
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
#import <AgoraRtcKit/AgoraRtcKit.h>
#else
#import <AVFoundation/AVFoundation.h>
#endif

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, BeautyPresetMode) {
    /// Default beauty parameters
    BeautyPresetModeDefault = 0,
    /// External custom beauty parameters, external self-setting
    BeautyPresetModeCustom = 1
};

@protocol BeautyRenderDelegate <NSObject>

- (CVPixelBufferRef)onCapture: (CVPixelBufferRef)pixelBuffer;
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (AgoraVideoFormat)getVideoFormatPreference;
#endif

- (void)destroy;

@optional
- (void)setBeautyPreset;

- (void)setMakeup: (BOOL)isSelected;

- (void)setSticker: (BOOL)isSelected;

- (void)reset;

@end


typedef NS_ENUM(NSInteger, CaptureMode) {
    CaptureModeAgora = 0,
    CaptureModeCustom = 1
};

@interface BeautyStats : NSObject
/// The least time-consuming beauty
@property (nonatomic, assign)double minCostMs;
/// Beauty is the most time-consuming
@property (nonatomic, assign)double maxCostMs;
/// Beauty takes an average time.
@property (nonatomic, assign)double averageCostMs;

@end

typedef NS_ENUM(NSInteger, MirrorMode) {
    /// The local remote end is mirrored, and the front default
    MirrorMode_LOCAL_REMOTE = 0,
    /// Only local mirroring, no mirroring at the remote end, used for phone call scenes, e-commerce live broadcast scenes (ensure that the bulletin board text behind the e-commerce live broadcast is positive); this mode is reversed because the local remote is reversed, so there must be a side of the direction of the text sticker.
    MirrorMode_LOCAL_ONLY = 1,
    /// Remote mirroring only
    MirrorMode_REMOTE_ONLY= 2,
    /// The local remote end is not mirrored, and the back defaults.
    MirrorMode_NONE
};

@interface CameraConfig : NSObject
// Front default mirror image
@property(nonatomic, assign) MirrorMode frontMirror;
// Rear default mirror image
@property(nonatomic, assign) MirrorMode backMirror;
@end

@interface BeautyConfig : NSObject
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
// The rtc object passed in from the outside cannot be empty.
@property(nonatomic, weak)AgoraRtcEngineKit *rtcEngine;
#endif
// The beauty SDK interface object (different manufacturers are different) passed in from the outside cannot be empty.
@property(nonatomic, weak)id<BeautyRenderDelegate>beautyRender;
// Whether to automatically register the data callback processing internally
@property(nonatomic, assign)CaptureMode captureMode;
// Event callback, including beauty time-consuming
@property(nonatomic, copy)void (^eventCallback)(BeautyStats *stats);
// Statistical interval Unit: seconds Default: 1s
@property(nonatomic, assign)NSInteger statsDuration;
// Whether to turn on statistics
@property(nonatomic, assign)BOOL statsEnable;
// Configure camera mirroring
@property(nonatomic, strong)CameraConfig *cameraConfig;

@end

@interface BeautyAPI : NSObject

/**
 *  Render
 **/
@property (nonatomic, weak) id<BeautyRenderDelegate>beautyRender;

/**
 * Create and initialize the beauty scenario API. If registerVideoFrameObserver has been called externally, create must be called after that.
 *
 * @param config config
 *
 * @return 0: Success, Non 0: See error code
 **/
- (int)initialize: (BeautyConfig *)config;

/**
 * Beauty switch
 *
 * @param enable Whether to turn on the beauty
 *
 * @return 0: Success, Non 0: See error code
 **/
- (int)enable: (BOOL)enable;

/**
 * Read the status of the beauty switch
 **/
@property (nonatomic, readonly, assign) BOOL isEnable;

/**
 * Is it the front camera?
 **/
@property (nonatomic, assign, readonly) BOOL isFrontCamera;

/**
 * Switch the camera
 *
 *
 * @return 0: Success; Non 0: See the error code
 **/
- (int)switchCamera;

/**
 * Set the camera mirroring mode, and pay attention to the front and rear should be controlled separately.
 *
 *
 * @return 0: Success; Non 0: See the error code
 **/
- (int)updateCameraConfig: (CameraConfig *)cameraConfig;

/**
 * Local view rendering, internal to deal with mirroring problems
 *
 * @param view Rendering view
 * @param renderMode Render zoom mode
 * @return 0: Success, Non 0: See error code
 **/
#if __has_include(<AgoraRtcKit/AgoraRtcKit.h>)
- (int)setupLocalVideo: (UIView *)view renderMode: (AgoraVideoRenderMode)renderMode;
#endif

/**
 * The mirror processing method only needs to be called when useCustom is true.
 *
 * @return Do you need mirroring when collecting?
 **/
- (BOOL)getMirrorApplied;
/**
 * The beauty processing method only needs to be called when useCustom is true, otherwise an error will be reported.
 *
 *
 * @return 0: Success; Non 0: See the error code
 **/
- (int)onFrame: (CVPixelBufferRef)pixelBuffer callback: (void (^)(CVPixelBufferRef))callback;

/**
 * Set the best default parameters for beauty
 *
 * @param mode Beauty parameter mode
 *
 * @return 0: Success; Non 0: See the error code
 **/
- (int)setBeautyPreset: (BeautyPresetMode)mode;

/**
 * Destroy the beauty scene API.
 * When creating useCustome=true, rtcEngine.registerVideoFrameObserver(null) will be called to mediate and bind the data.
 *
 * @return 0: Success; Non 0: See the error code table
 **/
- (int)destroy;

/**
 * @return Version number
 **/
- (NSString *)getVersion;

@end

typedef NS_ENUM(NSInteger, LogLevel) {
    LogLevelInfo,
    LogLevelError,
    LogLevelDebug
};
@interface LogUtil : NSObject

+ (void)log:(NSString *)message;

+ (void)log:(NSString *)message level:(LogLevel)level;

@end

NS_ASSUME_NONNULL_END
