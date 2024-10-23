//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

@objc
enum BeautyFactoryType: Int {
    case byte
    case sense
    case fu
    case cosmos
    
    var title: String {
        switch self {
        case .byte: return "byte"
        case .sense: return "sense"
        case .fu: return "fu"
        case .cosmos: return "cosmos"
        }
    }
}

class BeautyBaseModel: NSObject, Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// Whether to adjust in both directions
    var enableNegative: Bool = false
}

@objc
class BeautyModel: BeautyBaseModel {
    @objc
    static var beautyType: BeautyFactoryType = .sense
    /// The path of special effects materials relative to ComposeMakeup.bundle/ComposeMakeup
    var path: String?
    /// key Functions in the material key
    var key: String?
    /// Special effect intensity (0~1)
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
