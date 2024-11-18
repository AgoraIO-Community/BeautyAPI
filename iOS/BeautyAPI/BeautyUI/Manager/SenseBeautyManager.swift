//
//  SenseBeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/12.
//

import UIKit

class SenseBeautyManager: NSObject {
    public lazy var render: SenseBeautyRender = {
        let beautyRender = SenseBeautyRender()
        beautyRender.licenseEventCallback = {[weak self] success in
            if success {return}
            AUIToast.show(text: "Auth Failed, Please check your license file")
        }
        return beautyRender
    }()
    
    private var processor: VideoProcessingManager {
        return render.videoProcessing
    }
    
    private static var _sharedManager: SenseBeautyManager?
    static var shareManager: SenseBeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = SenseBeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    private var timer: Timer?
    private var datas: [BeautyModel] = [BeautyModel]()
    private var stickerId: Int32 = 0
    private var styleId: Int32 = 0
    var isEnableBeauty: Bool = true
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        if processor.effectsProcess.isAuthrized() == false {
            let model = BeautyModel()
            model.path = path
            model.value = value
            model.key = key
            datas.append(model)
            return
        }
        guard let key = UInt32(key ?? "0") else { return }
        processor.setEffectType(key, value: Float(value))
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        guard let path = path, !path.isEmpty, let key = key else { return }
        processor.addStylePath(path, groupId: key == "Makeup_ALL" ? 0 : 1, strength: value) { [weak self] stickerId in
            guard let self = self else { return }
            self.styleId = stickerId
        }
    }
    
    func setFilter(path: String?, value: CGFloat) { }
    
    func setSticker(path: String?, datas: [BeautyModel]) {
        if let path = path {
            processor.removeStickerId(stickerId)
            processor.setStickerWithPath(path) { [weak self] stickerId in
                self?.stickerId = stickerId
            }
        } else {
            resetSticker(datas: datas)
        }
    }
    
    func reset(datas: [BeautyModel]) {
        datas.forEach({
            guard $0.path != nil, let key = UInt32($0.key ?? "0") else { return }
            processor.setEffectType(key, value: 0)
        })
    }
    
    func resetStyle(datas: [BeautyModel]) {
        processor.removeStickerId(styleId)
        styleId = 0
    }
    
    func resetFilter(datas: [BeautyModel]) {
        datas.forEach({ item in
            item.isSelected = item.path == nil
            setFilter(path: item.path, value: 0)
        })
    }
    
    func resetSticker(datas: [BeautyModel]) {
        processor.removeStickerId(stickerId)
        stickerId = 0
    }
    
    func destroy() {
        render.destroy()
        SenseBeautyManager._sharedManager = nil
        datas.removeAll()
    }
}
