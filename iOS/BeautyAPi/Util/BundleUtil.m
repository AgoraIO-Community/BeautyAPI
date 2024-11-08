//
//  BundleUtil.m
//  APIExample
//
//  Created by zhaoyongqiang on 2022/10/25.
//  Copyright © 2022 Agora Corp. All rights reserved.
//

#import "BundleUtil.h"

@implementation BundleUtil

+ (NSBundle*)bundleWithBundleName:(NSString *)bundleName podName:(NSString *)podName {
    if(bundleName == nil && podName == nil) {
        return nil;
    } else if (bundleName == nil) {
        bundleName = podName;
    } else if (podName == nil) {
        podName = bundleName;
    }
    if([bundleName containsString:@".bundle"]){
        bundleName=[bundleName componentsSeparatedByString:@".bundle"].firstObject;
    }
    //Without using framwork
    NSURL *associateBundleURL = [[NSBundle mainBundle]URLForResource:bundleName withExtension:@"bundle"];
    //Use framework form
    if (!associateBundleURL) {
        associateBundleURL = [[NSBundle mainBundle]URLForResource: @"Frameworks" withExtension:nil];
        associateBundleURL = [associateBundleURL URLByAppendingPathComponent:podName];
//        associateBundleURL = [associateBundleURL URLByAppendingPathExtension:@"framework"];
        NSBundle *associateBunle = [NSBundle bundleWithURL:associateBundleURL];
        associateBundleURL = [associateBunle URLForResource:bundleName withExtension:@"bundle"];
    }
    //The production environment directly returns empty.
    return associateBundleURL ? [NSBundle bundleWithURL:associateBundleURL]: nil;
}

@end
