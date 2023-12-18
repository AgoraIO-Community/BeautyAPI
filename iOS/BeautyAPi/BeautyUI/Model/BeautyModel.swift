//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

@objc
enum BeautyFactoryType: Int {
    // 字节
    case byte
    // 商汤
    case sense
    // 相芯
    case fu
    // 宇宙
    case cosmos
    
    var title: String {
        switch self {
        case .byte: return "火山引擎"
        case .sense: return "商汤"
        case .fu: return "相芯"
        case .cosmos: return "宇宙"
        }
    }
}

class BeautyBaseModel: NSObject, Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// 是否双向调节
    var enableNegative: Bool = false
}

@objc
class BeautyModel: BeautyBaseModel {
    @objc
    static var beautyType: BeautyFactoryType = .sense
    /// 特效素材相对于 ComposeMakeup.bundle/ComposeMakeup 的路径
    var path: String?
    /// key 素材中的功能 key
    var key: String?
    /// 特效强度 （0~1）
    var value: CGFloat = 0
    
    static func createBeautyData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createByteBeautyData()
        case .sense: return createSenseBeautyData()
        case .fu: return createFUBeautyData()
        case .cosmos: return createCosmosBeautyData()
        }
    }
    
    static func createStyleData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesStyleData()
        case .sense: return createSenseStyleData()
        case .fu: return createFUStyleData()
        case .cosmos: return createCosmosStyleData()
        }
    }
    
    static func createAnimojiData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return []
        case .sense: return []
        case .fu: return createFUAnimojiData()
        case .cosmos: return []
        }
    }
    
    static func createFilterData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesFilterData()
        case .sense: return []
        case .fu: return []
        case .cosmos: return []
        }
    }
    
    static func createStickerData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return createBytesStickerData()
        case .sense: return createSenseStickerData()
        case .fu: return createFUStickerData()
        case .cosmos: return createCosmosStickerData()
        }
    }
    
    static func createAdjustData() -> [BeautyModel] {
        switch beautyType {
        case .byte: return []
        case .sense: return createSenseAdjustData()
        case .fu: return []
        case .cosmos: return []
        }
    }
}
