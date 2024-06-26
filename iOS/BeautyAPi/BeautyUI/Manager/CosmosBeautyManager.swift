//
//  CosmosBeautyManager.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/22.
//

import UIKit

class CosmosBeautyManager: NSObject {
    public lazy var render = CosmosBeautyRender()
    
    private static var _sharedManager: CosmosBeautyManager?
    static var shareManager: CosmosBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = CosmosBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    override init() {
        super.init()
        if render.render.isAuthSuccess() {return}
        AUIToast.show(text: "Auth Failed, Please check your license file")
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        render.setBeautyFactor(Float(value), forKey: key ?? "")
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        render.addMakeupPath(path ?? "", key: key ?? "", value: value)
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?) {
        render.addStickerPath(path ?? "")
    }
    
    func reset(datas: [BeautyModel]) {
        render.reset()
    }
    
    func resetStyle(datas: [BeautyModel]) {
        render.addMakeupPath("", key: "", value: 0)
    }
    
    func resetFilter(datas: [BeautyModel]) {
        
    }
    
    func resetSticker(datas: [BeautyModel]) {
        render.render.clearSticker()
    }
    
    func destroy() {
        render.destroy()
        CosmosBeautyManager._sharedManager = nil
    }
}
