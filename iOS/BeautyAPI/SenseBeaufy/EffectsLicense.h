//
//  License.h
//  Effects
//
//  Created by sunjian on 2021/5/8.
//  Copyright © 2021 sjuinan. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface EffectsLicense : NSObject

/// Authentication
/// @param licensePath Authorize the file path
+ (BOOL)authorizeWithLicensePath:(NSString *)licensePath;

/// Authentication
/// @param dataLicense Authorize file data
+ (BOOL)authorizeWithLicenseData:(NSData *)dataLicense;
@end

NS_ASSUME_NONNULL_END


