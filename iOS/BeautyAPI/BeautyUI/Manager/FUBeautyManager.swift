//
//  FUBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/12.
//

import UIKit

class FUBeautyManager: NSObject {
    
    public lazy var render = FUBeautyRender()
    
    private static var _sharedManager: FUBeautyManager?
    static var shareManager: FUBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = FUBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override init() {
        super.init()
        if FUBeautyRender.checkLicense() == false {
            AUIToast.show(text: "Auth Failed, Please check your license file")
        }
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if path == "init" {
            render.setBeautyPreset()
        } else {
            render.setBeautyWithPath(path ?? "", key: key ?? "", value: Float(value))
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat, isCombined: Bool) {
        render.setStyleWithPath(path ?? "", key: key ?? "", value: Float(value), isCombined: isCombined)
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?) {
        render.setStickerWithPath(path ?? "")
    }
    
    func reset(datas: [BeautyModel]) {
        render.reset()
    }
    
    func resetStyle(datas: [BeautyModel]) {
        render.resetStyle()
    }
    
    func resetFilter(datas: [BeautyModel]) {
        
    }
    
    func resetSticker(datas: [BeautyModel]) {
        render.resetSticker()
    }
    
    func destroy() {
        render.destroy()
        FUBeautyManager._sharedManager = nil
    }
}
