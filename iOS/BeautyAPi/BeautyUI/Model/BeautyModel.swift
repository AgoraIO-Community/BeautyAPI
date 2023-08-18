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
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/beauty_IOS_lite"
            model.key = "smooth"
            model.value = 0.3
        } else if beautyType == .sense {
            model.path = ""
            model.key = "103"
            model.value = 0.55
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "blurLevel"
            model.value = 0.55
        }
        model.name = "show_beauty_item_beauty_smooth".beauty_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/beauty_IOS_lite"
            model.key = "whiten"
            model.value = 0.5
        } else if beautyType == .sense {
            model.path = ""
            model.key = "101"
            model.value = 0.2
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "whiten"
            model.value = 0.2
        }
        model.name = "show_beauty_item_beauty_whiten".beauty_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Overall"
            model.value = 0.15
            model.enableNegative = true
        } else if beautyType == .sense {
            model.path = ""
            model.key = "201"
            model.value = 0.4
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "thin"
            model.value = 0.4
        }
        model.name = "show_beauty_item_beauty_overall".beauty_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Zoom_Cheekbone"
            model.value = 0.3
            model.enableNegative = true
        } else if beautyType == .sense {
            model.path = ""
            model.key = "318"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "cheek"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_cheekbone".beauty_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Eye"
            model.value = 0.15
            model.enableNegative = true
        } else if beautyType == .sense {
            model.path = ""
            model.key = "202"
            model.value = 0.3
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "enlarge"
            model.value = 0.3
        }
        model.name = "show_beauty_item_beauty_eye".beauty_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Nose"
            model.value = 0.15
            model.enableNegative = true
        } else if beautyType == .sense {
            model.path = ""
            model.key = "306"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "nose"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_nose".beauty_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Chin"
            model.value = 0.46
        } else if beautyType == .sense {
            model.path = ""
            model.key = "303"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "chin"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_chin".beauty_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Zoom_Jawbone"
            model.value = 0.46
            model.enableNegative = true
        } else if beautyType == .sense {
            model.path = ""
            model.key = "320"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "lowerJaw"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_jawbone".beauty_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_Forehead"
            model.value = 0.4
        } else if beautyType == .sense {
            model.path = ""
            model.key = "304"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "forehead"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_forehead".beauty_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/reshape_lite"
            model.key = "Internal_Deform_ZoomMouth"
            model.value = 0.16
        } else if beautyType == .sense {
            model.path = ""
            model.key = "309"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "mouth"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_mouth".beauty_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
    
        model = BeautyModel()
        if beautyType == .byte {
            model.path = "/beauty_4Items"
            model.key = "BEF_BEAUTY_WHITEN_TEETH"
            model.value = 0.2
        } else if beautyType == .sense {
            model.path = ""
            model.key = "317"
            model.value = 0
        } else if beautyType == .fu {
            model.path = "face_beautification"
            model.key = "toothWhiten"
            model.value = 0
        }
        model.name = "show_beauty_item_beauty_teeth".beauty_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        if beautyType == .byte {
            model = BeautyModel()
            model.path = "/style_makeup/baixi"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_baixi".beauty_localized
            model.icon = "meiyan_fgz_baixi"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/tianmei"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_tianmei".beauty_localized
            model.icon = "meiyan_fgz_tianmei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/cwei"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_cwei".beauty_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "/style_makeup/yuanqi"
            model.key = "Makeup_ALL"
            model.value = 0.6
            model.name = "show_beauty_item_effect_yuanqi".beauty_localized
            model.icon = "meiyan_fgz_yuanqi"
            dataArray.append(model)
            
        } else if beautyType == .sense {
            model = BeautyModel()
            model.path = "qise.zip"
            model.key = "Makeup_ALL"
            model.value = 0.5
            model.name = "show_beauty_item_effect_cwei".beauty_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.key = "Makeup_ALL"
            model.path = "wanneng.zip"
            model.value = 0.5
            model.name = "show_beauty_item_effect_yuanqi".beauty_localized
            model.icon = "meiyan_fgz_yuanqi"
            dataArray.append(model)
            
        } else if beautyType == .fu {
            model = BeautyModel()
            model.path = "graphics/face_makeup"
            model.key = "xinggan"
            model.value = 0.5
            model.name = "show_beauty_item_effect_sexy".beauty_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "graphics/face_makeup"
            model.key = "tianmei"
            model.value = 0.5
            model.name = "show_beauty_item_effect_sweet".beauty_localized
            model.icon = "meiyan_fgz_cwei"
            dataArray.append(model)
        }
    
        return dataArray
    }
    
    static func createFilterData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .byte {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".beauty_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_02_14"
            model.value = 0.4
            model.name = "show_beauty_item_filter_cream".beauty_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_07_06"
            model.value = 0.4
            model.name = "show_beauty_item_filter_mokalong".beauty_localized
            model.icon = "meiyan_lj_makalong"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_03_20"
            model.value = 0.4
            model.name = "show_beauty_item_filter_oxgen".beauty_localized
            model.icon = "meiyan_lj_yangqi"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_11_09"
            model.value = 0.4
            model.name = "show_beauty_item_filter_wuyu".beauty_localized
            model.icon = "meiyan_lj_wuyu"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_31_Po9"
            model.value = 0.4
            model.name = "show_beauty_item_filter_po9".beauty_localized
            model.icon = "meiyan_lj_haibian"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_05_10"
            model.value = 0.4
            model.name = "show_beauty_item_filter_lolita".beauty_localized
            model.icon = "meiyan_lj_luolita"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_06_03"
            model.value = 0.4
            model.name = "show_beauty_item_filter_mitao".beauty_localized
            model.icon = "meiyan_lj_mitao"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_09_19"
            model.value = 0.4
            model.name = "show_beauty_item_filter_yinhua".beauty_localized
            model.icon = "meiyan_lj_yinghua"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_12_08"
            model.value = 0.4
            model.name = "show_beauty_item_filter_beihaidao".beauty_localized
            model.icon = "meiyan_lj_beihaidao"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "Filter_45_S3"
            model.value = 0.4
            model.name = "show_beauty_item_filter_s3".beauty_localized
            model.icon = "meiyan_lj_lvtu"
            dataArray.append(model)
        }
        return dataArray
    }
    
    static func createStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .byte {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".beauty_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "matting_bg"
            model.name = "show_beauty_item_sticker_matting".beauty_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "background_blur"
            model.name = "show_beauty_item_sticker_background".beauty_localized
            model.icon = "meiyan_lj_makalong"
            dataArray.append(model)
            
        } else if beautyType == .sense {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".beauty_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "lianxingface.zip"
            model.name = "show_beauty_item_sticker_lianxing".beauty_localized
            model.icon = "meiyan_lj_naiyou"
            dataArray.append(model)
            
        } else if beautyType == .fu {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".beauty_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "CatSparks"
            model.name = "show_beauty_item_sticker_cat".beauty_localized
            model.icon = "CatSparks"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "sdlu"
            model.name = "show_beauty_item_sticker_milu".beauty_localized
            model.icon = "sdlu"
            dataArray.append(model)
            
            model = BeautyModel()
            model.path = "fu_zh_fenshu"
            model.name = "show_beauty_item_sticker_milu".beauty_localized
            model.icon = "xueqiu_lm_fu"
            dataArray.append(model)
        }
        return dataArray
    }
    
    static func createAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        if beautyType == .sense {
            var model = BeautyModel()
            model.name = "show_beauty_item_none".beauty_localized
            model.icon = "show_beauty_none"
            model.isSelected = true
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_contrast".beauty_localized
            model.icon = "show_beauty_ic_adjust_contrast"
            model.path = ""
            model.key = "601"
            model.value = 0
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_saturation".beauty_localized
            model.icon = "show_beauty_ic_adjust_saturation"
            model.path = ""
            model.key = "602"
            model.value = 0
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_sharpen".beauty_localized
            model.icon = "show_beauty_ic_adjust_sharp"
            model.path = ""
            model.key = "603"
            model.value = 0.5
            dataArray.append(model)
            
            model = BeautyModel()
            model.name = "show_beauty_item_adjust_clarity".beauty_localized
            model.icon = "show_beauty_ic_adjust_clear"
            model.path = ""
            model.key = "604"
            model.value = 1.0
            dataArray.append(model)
        }
        return dataArray
    }
}
