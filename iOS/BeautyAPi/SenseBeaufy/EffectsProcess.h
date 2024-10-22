//
//  Effects.h
//  Effects
//
//  Created by sunjian on 2021/5/8.
//  Copyright © 2021 sjuinan. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <OpenGLES/EAGL.h>
#if __has_include("st_mobile_common.h")
#import "EffectsDetector.h"
#import "st_mobile_common.h"
#import "st_mobile_effect.h"
#endif
#import "EffectMacro.h"

@protocol EFEffectsProcessDelegate <NSObject>

- (void)updateEffectsFacePoint:(CGPoint)point;
#if __has_include("st_mobile_common.h")
- (void)updateCommonObjectPosition:(st_rect_t)rect;
#endif
- (void)updateKeyPoinst:(NSArray *)keyPoints;

@end

@interface EffectsProcess : NSObject

@property (nonatomic, weak)   id<EFEffectsProcessDelegate> delegate;

@property (nonatomic, assign) GLuint inputTexture;

@property (nonatomic, assign) GLuint outputTexture;

@property (nonatomic) CVPixelBufferRef outputPixelBuffer;

@property (nonatomic, assign) uint64_t detectConfig;

@property (nonatomic, assign) EFDetectConfigMode configMode;

/// Authentication
/// @param licensePath Authorization File Path
+ (BOOL)authorizeWithLicensePath:(NSString *)licensePath;

/// Authentication
/// @param licenseData Authorized file data
+ (BOOL)authorizeWithLicenseData:(NSData *)licenseData;

/// Initialize the object
/// @param type type
- (instancetype)initWithType:(EffectsType)type  glContext:(EAGLContext *)glContext;

#if __has_include("st_mobile_common.h")
/// Load the model
/// @param modelPath model file path (multiple models can be placed in one file, SDK internal traversal to load Model)
- (st_result_t)setModelPath:(NSString *)modelPath;
- (st_result_t)setModelPath:(NSString *)modelPath withFirstPhaseFinished:(void(^)(void))finishedCallback;

/// Set up special effects
/// @param type Special effect type
/// @param path Special effect material path
- (st_result_t)setEffectType:(st_effect_beauty_type_t)type path:(NSString *)path;

/// Set up special effects
/// @param type Special effect type
/// @param model Special effects material model
- (st_result_t)setEffectType:(st_effect_beauty_type_t)type model:(int)model;

/// Set the special effect intensity value
/// @param type Special effect type
/// @param value Intensity value
- (st_result_t)setEffectType:(st_effect_beauty_type_t)type value:(float)value;

/// Set the style intensity
/// @param package Id package ID
/// @param type type
/// @param value Intensity value
- (st_result_t)setPackageId:(int)packageId groupType:(st_effect_beauty_group_t)type strength:(float)value;

- (st_result_t)setTryon:(st_effect_tryon_info_t *)tryonInfo andTryonType:(st_effect_beauty_type_t)tryonType;

- (st_result_t)getTryon:(st_effect_tryon_info_t *)tryonInfo andTryonType:(st_effect_beauty_type_t)tryonType;

- (st_result_t)setBeautyParam:(st_effect_beauty_param_t)param andVal:(float)val;

- (st_result_t)getBeautyParam:(st_effect_beauty_param_t)param andVal:(float *)val;

- (st_result_t)get3dBeautyPartsSize:(int *)partSize;

- (st_result_t)get3dBeautyParts:(st_effect_3D_beauty_part_info_t[])parts fromSize:(int)partSize;

- (st_result_t)set3dBeautyPartsStrength:(st_effect_3D_beauty_part_info_t *)parts andVal:(int)partSize;
- (st_result_t)f_set3dBeautyPartsStrength:(st_effect_3D_beauty_part_info_t[])parts andVal:(int)partSize;

- (st_result_t)disableOverlap:(BOOL)isDisableOverlap;
- (st_result_t)disableModuleReorder:(BOOL)isDisableModuleReorder;

/// Set up stickers
/// @param stickerPath stickerPath sticker resource path
/// @param callback Callback Block
- (void)setStickerWithPath:(NSString *)stickerPath
                  callBack:(void(^)(st_result_t state, int stickerId, uint64_t action))callback;

- (void)setStickerWithPath:(NSString *)stickerPath callBackCustomEventIncluded:(void(^)(st_result_t state, int stickerId, uint64_t action, uint64_t customEvent))callback;

/// Add stickers
/// @param stickerPath stickerPath sticker resource path
/// @param callback Callback Block
- (void)addStickerWithPath:(NSString *)stickerPath
                  callBack:(void(^)(st_result_t state, int sticker, uint64_t action))callback;

/// Replay the sticker
/// @param packageId packageId
-(void)replayStickerWithPackage:(int)packageId;

- (void)addStickerWithPath:(NSString *)stickerPath callBackCustomEventIncluded:(void(^)(st_result_t state, int stickerId, uint64_t action, uint64_t customEvent))callback;

-(void)changeStickerWithPath:(NSString *)stickerPath callBackCustomEventIncluded:(void(^)(st_result_t state, int stickerId, uint64_t action, uint64_t customEvent))callback;

/// Get the sticker information of the material
/// @param package_id package_id
/// @param modules Sticker Information
-(st_result_t)getModulesInPackage:(int)package_id modules:(st_effect_module_info_t*)modules;

/// Set the sticker information
/// @param module_info sticker information
-(st_result_t)setModuleInfo:(st_effect_module_info_t *)module_info;

/// Obtain the information of overwriting effective beauty
/// @param callback callback block
- (void)getOverLap:(void(^)(st_effect_beauty_info_t *beauty_info))callback;

/// Get the information of overwriting effective beauty
/// If there is st_effect_beauty_info_t, release it when used it;
- (st_effect_beauty_info_t *)getOverlapInfo:(int *)count;

/// Remove the sticker
/// @param stickerId Sticker Id
- (st_result_t)removeSticker:(int)stickerId;

- (st_result_t)cleareStickers;

- (st_result_t)detectWithPixelBuffer:(CVPixelBufferRef)pixelBuffer
                              rotate:(st_rotate_type)rotate
                      cameraPosition:(AVCaptureDevicePosition)position
                         humanAction:(st_mobile_human_action_t *)detectResult
                        animalResult:(st_mobile_animal_result_t *)animalResult;

-(st_result_t)resetHumanAction;

-(st_result_t)setHumanActionParam:(st_human_action_param_type)type andValue:(float)value;
-(st_result_t)setEffectParam:(st_effect_param_t)param andValue:(float)value;

- (st_result_t)renderPixelBuffer:(CVPixelBufferRef)pixelBuffer
                          rotate:(st_rotate_type)rotate
                     humanAction:(st_mobile_human_action_t)detectResult
                    animalResult:(st_mobile_animal_result_t *)animalResult
                      outTexture:(GLuint)outTexture
                  outPixelFormat:(st_pixel_format)fmt_out
                         outData:(unsigned char *)img_out;

-(st_result_t)detectAttribute:(unsigned char *)imageData pixelFormat:(st_pixel_format)pixel_format imageWidth:(int)image_width imageHeight:(int)image_height imageStride:(int)image_stride orientation:(st_rotate_type)orientation withGenderCallback:(void(^)(BOOL isMale))callback;

-(st_result_t)detectAttribute:(unsigned char *)imageData pixelFormat:(st_pixel_format)pixel_format imageWidth:(int)image_width imageHeight:(int)image_height detectResult:(st_mobile_human_action_t)detectResult withGenderCallback:(void(^)(BOOL isMale))callback;

/// Process video data
/// @param pixelBuffer Video Data
/// @param rotate The rotation direction of the current mobile phone
/// @param position Current mobile phone camera direction
/// @param outTexture Target texture, only RGBA texture is supported
/// @param fmt_out Output image type, support NV21, BGR, BGRA, NV12, RGBA, YUV420P formats
/// @param img_out Output the image data array, which requires the user to allocate memory. If it is null, it does not output buffer.
- (st_result_t)processPixelBuffer:(CVPixelBufferRef)pixelBuffer
                            rotate:(st_rotate_type)rotate
                    cameraPosition:(AVCaptureDevicePosition)position
                        outTexture:(GLuint)outTexture
                    outPixelFormat:(st_pixel_format)fmt_out
                           outData:(unsigned char *)img_out;

/// Process image data
/// @param data Image data
/// @param dataSize Image data size
/// @param width Image width
/// @param height Image height
/// @param stride image data span
/// @param rotate the rotation direction of the mobile phone
/// @param pixelFormat Image Format
/// @param outTexture Output texture, only RGBA texture is supported
/// @param fmt_out Output image type, support NV21, BGR, BGRA, NV12, RGBA, YUV420P formats
/// @param outData Output data
- (st_result_t)processData:(unsigned char *)data
                       size:(int)dataSize
                      width:(int)width
                     height:(int)height
                     stride:(int)stride
                     rotate:(st_rotate_type)rotate
                pixelFormat:(st_pixel_format)pixelFormat
               inputTexture:(GLuint)inputTexture
                 outTexture:(GLuint)outTexture
             outPixelFormat:(st_pixel_format)fmt_out
                    outData:(unsigned char *)outData;


/// Create a texture
/// @param width Texture width
/// @param height Texture height
- (GLuint)createTextureWidth:(int)width height:(int)height;

/// Create an OpenGL object
/// @param width Pixel width
/// @param height Pixel height
/// @param texture texture
/// @param pixelBuffer Pixel Cache
- (void)createGLObjectWith:(int)width
                    height:(int)height
                   texture:(GLuint *)texture
               pixelBuffer:(CVPixelBufferRef *)pixelBuffer
                 cvTexture:(CVOpenGLESTextureRef *)cvTexture;

- (id<MTLTexture>)createMetalTextureWithDevice:(id<MTLDevice>)metalDevice width:(int)width height:(int)height pixelBuffer:(CVPixelBufferRef)pixelBuffer;

/// Delete a texture
/// @param texture texture
- (void)deleteTexture:(GLuint *)texture
          pixelBuffer:(CVPixelBufferRef *)pixelBuffer
            cvTexture:(CVOpenGLESTextureRef *)cvTexture;;

/// Get the texture
/// @param pixelBuffer pixelBuffer
- (GLuint)getTexutreWithPixelBuffer:(CVPixelBufferRef)pixelBuffer;

/// Face detection
/// @param pixelBuffer Pixel Cache
/// @ [out] Return the test result value
- (st_mobile_human_action_t)detectHumanActionWithPixelBuffer:(CVPixelBufferRef)pixelBuffer;

/// Get the shape of the face
/// @param p_face Detected face information
/// @out p_face_shape Human face shape
- (st_face_shape_t)detectFaceShape:(st_mobile_face_t)p_face;

/// Set the general object position
/// @param rect object box
- (void)setObjectTrackRect:(st_rect_t)rect;

#endif

/// Whether the authentication is successful
- (BOOL)isAuthrized;

-(void)getMeshList;

@end
