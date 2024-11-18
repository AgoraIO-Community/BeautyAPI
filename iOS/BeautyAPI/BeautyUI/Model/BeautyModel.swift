//
//  ByteBeautyModel.swift
//  testNewAgoraSDK
//
//  Created by zhaoyongqiang on 2022/11/17.
//

import UIKit

@objc
enum BeautyFactoryType: Int {
    // ByteDance
    case byte
    // SenseTime
    case sense
    // FaceUnity
    case fu
    // Cosmos
    case cosmos
    
    var title: String {
        switch self {
        case .byte: return "Byte"
        case .sense: return "Sense"
        case .fu: return "Fu"
        case .cosmos: return "Cosmos"
        }
    }
}

class BeautyBaseModel: NSObject, Codable {
    var icon: String?
    var name: String?
    var isSelected: Bool = false
    /// Whether to enable bidirectional adjustment
    var enableNegative: Bool = false
}

@objc
class BeautyModel: BeautyBaseModel {
    @objc
    static var beautyType: BeautyFactoryType = .sense
    /// Effect material path relative to ComposeMakeup.bundle/ComposeMakeup
    var path: String?
    /// Key for the material's functionality
    var key: String?
    /// Effect intensity (0~1)
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
