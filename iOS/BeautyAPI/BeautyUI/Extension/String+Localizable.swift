//
//  String+Localizable.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/8/14.
//

import Foundation
extension String {
    var beauty_localized: String {
        guard let bundlePath = Bundle.main.path(forResource: "BeautyResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath)
        else {
            return self
        }
        
        guard var lang = NSLocale.preferredLanguages.first else {
            return self
        }
        if lang.contains("zh") {
            lang = "zh-Hans"
        } else {
            lang = "en"
        }
        
        guard let langPath = bundle.path(forResource: lang, ofType: "lproj") , let detailBundle = Bundle(path: langPath) else {
            return self
        }
        let retStr = NSLocalizedString(self,tableName: "Localizable", bundle:detailBundle ,comment: "")
        return retStr
    }
}
