//
//  UIImage+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import UIKit

extension UIImage {
    @objc
    static func show_beautyImage(name: String?) -> UIImage? {
        guard let imageName = name else { return nil }
        return sceneImage(name: imageName, bundleName: "BeautyResource")
    }
}


extension UIImage {
    @objc
    static func sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: nil)
    }

    @objc
    static func sceneImage(name: String, bundleName: String?) -> UIImage? {
        guard let bundleName = bundleName else {
            assertionFailure("sceneImageBundleName == nil")
            return nil
        }
        
        guard let bundlePath = Bundle.main.path(forResource: bundleName, ofType: "bundle"),
              let bundle = Bundle(path: bundlePath)
        else {
            assertionFailure("image bundle == nil")
            return nil
        }

        let components = name.components(separatedBy: ".")
        let pureName = components.first ?? name
        let suffix = (components.count == 2 ? components.last : nil) ?? "png"
        let scale = Int(UIScreen.main.scale)
        var scales = [1, 2, 3].filter { value in
            return value != scale
        }
        scales.insert(scale, at: 0)
        
        let lang = getLang()
        for value in scales {
            let imageName1 = value > 1 ? "\(pureName)-\(lang)@\(value)x" : pureName
            let imageName2 = value > 1 ? "\(pureName)@\(value)x" : pureName
            if let path = bundle.path(forResource: imageName1, ofType: suffix) {
                let image = UIImage(contentsOfFile: path)
                return image
            }
            if let path = bundle.path(forResource: imageName2, ofType: suffix) {
                let image = UIImage(contentsOfFile: path)
                return image
            }
        }
        return nil
    }
    
    private static func getLang() -> String {
        guard let lang = NSLocale.preferredLanguages.first else {
            return "en"
        }
        if lang.contains("zh") {
            return "zh-Hans"
        }
        return "en"
    }
}
