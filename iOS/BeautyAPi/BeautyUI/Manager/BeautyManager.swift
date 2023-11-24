//
//  BeautyManager.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/1/16.
//

import UIKit

class BeautyManager: NSObject {
    private static var _sharedManager: BeautyManager?
    static var shareManager: BeautyManager {
        get {
            if let sharedManager = _sharedManager { return sharedManager }
            let sharedManager = BeautyManager()
            _sharedManager = sharedManager
            return sharedManager
        }
        set {
            _sharedManager = nil
        }
    }
    var isEnableBeauty: Bool = true {
        didSet {
            beautyAPI.enable(isEnableBeauty)
        }
    }
    lazy var cameraConfig: CameraConfig = {
        let cameraConfig = CameraConfig()
        cameraConfig.frontMirror = .LOCAL_REMOTE
        cameraConfig.backMirror = .NONE
        return cameraConfig;
    }()
    private lazy var beautyAPI = BeautyAPI()
        
    func initBeautyAPI(rtcEngine: AgoraRtcEngineKit, captureMode: CaptureMode) -> BeautyAPI {
        let config = BeautyConfig()
        config.rtcEngine = rtcEngine
        config.captureMode = captureMode
        config.cameraConfig = cameraConfig
        switch BeautyModel.beautyType {
        case .byte:
            config.beautyRender = ByteBeautyManager.shareManager.render
            
        case .sense:
            config.beautyRender = SenseBeautyManager.shareManager.render
            
        case .fu:
            config.beautyRender = FUBeautyManager.shareManager.render
            
        case .cosmos:
            config.beautyRender = CosmosBeautyManager.shareManager.render
        }
        config.statsEnable = false
        config.statsDuration = 1
        config.eventCallback = { stats in
            print("min == \(stats.minCostMs)")
            print("max == \(stats.maxCostMs)")
            print("averageCostMs == \(stats.averageCostMs)")
        }
        let result = beautyAPI.initialize(config)
        if result != 0 {
            print("initialize error == \(result)")
        }
        beautyAPI.enable(true)
        return beautyAPI
    }
    
    func setBeauty(path: String?, key: String?, value: CGFloat) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.setBeauty(path: path, key: key, value: value)
        }
    }
    
    func setStyle(path: String?, key: String?, value: CGFloat) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
        case .fu:
            FUBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.setStyle(path: path, key: key, value: value)
        }
    }
    
    func setFilter(path: String?, value: CGFloat) {
        guard let path = path else { return }
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .sense:
            SenseBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .fu:
            FUBeautyManager.shareManager.setFilter(path: path, value: value)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.setFilter(path: path, value: value)
        }
    }
    
    func setSticker(path: String?, datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.setSticker(path: path)
            
        case .sense:
            SenseBeautyManager.shareManager.setSticker(path: path, datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.setSticker(path: path)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.setSticker(path: path)
        }
    }
    
    func reset(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.reset(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.reset(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.reset(datas: datas)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.reset(datas: datas)
        }
    }
    
    func resetStyle(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetStyle(datas: datas)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.resetStyle(datas: datas)
        }
    }
    
    func resetFilter(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetFilter(datas: datas)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.resetFilter(datas: datas)
        }
    }
    
    func resetSticker(datas: [BeautyModel]) {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .sense:
            SenseBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .fu:
            FUBeautyManager.shareManager.resetSticker(datas: datas)
            
        case .cosmos:
            CosmosBeautyManager.shareManager.resetSticker(datas: datas)
        }
    }
    
    func destroy() {
        switch BeautyModel.beautyType {
        case .byte:
            ByteBeautyManager.shareManager.destroy()
            
        case .sense:
            SenseBeautyManager.shareManager.destroy()
            
        case .fu:
            FUBeautyManager.shareManager.destroy()
            
        case .cosmos:
            CosmosBeautyManager.shareManager.destroy()
        }
        BeautyManager._sharedManager = nil
    }
}
