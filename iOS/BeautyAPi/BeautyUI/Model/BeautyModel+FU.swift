//
//  BeautyModel+FU.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/23.
//

import UIKit

class FUBeautyModel: BeautyModel {
    /// 是否v8.0.0之后新组合妆（只用一个bundle）
    var isCombined: Bool = false
}

extension BeautyModel {
    static func createFUBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "blurLevel"
        model.value = 0.55
        model.name = "show_beauty_item_beauty_smooth".beauty_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "whiten"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_whiten".beauty_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "thin"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_overall".beauty_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "cheek"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekbone".beauty_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "enlarge"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_eye".beauty_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "nose"
        model.value = 0
        model.name = "show_beauty_item_beauty_nose".beauty_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "chin"
        model.value = 0
        model.name = "show_beauty_item_beauty_chin".beauty_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "lowerJaw"
        model.value = 0
        model.name = "show_beauty_item_beauty_jawbone".beauty_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "forehead"
        model.value = 0
        model.name = "show_beauty_item_beauty_forehead".beauty_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "mouth"
        model.value = 0
        model.name = "show_beauty_item_beauty_mouth".beauty_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "face_beautification"
        model.key = "toothWhiten"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".beauty_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUStyleData() -> [FUBeautyModel] {
        var dataArray = [FUBeautyModel]()
        var model = FUBeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = FUBeautyModel()
        model.path = "face_makeup"
        model.key = "makeup/xinggan"
        model.value = 0.5
        model.name = "show_beauty_item_effect_sexy".beauty_localized
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = FUBeautyModel()
        model.path = "face_makeup"
        model.key = "makeup/tianmei"
        model.value = 0.5
        model.name = "show_beauty_item_effect_sweet".beauty_localized
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = FUBeautyModel()
        model.path = ""
        model.key = "makeup/diadiatu"
        model.value = 0.8
        model.name = "show_beauty_item_effect_diadiatu".beauty_localized
        model.icon = "meiyan_fgz_cwei"
        model.isCombined = true
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUAnimojiData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "kaola_Animoji"
        model.name = "show_beauty_item_ar_kaola".beauty_localized
        model.icon = "kaola_Animoji"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "hashiqi_Animoji"
        model.value = 0.4
        model.name = "show_beauty_item_ar_hashiqi".beauty_localized
        model.icon = "hashiqi_Animoji"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createFUStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
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
        
        return dataArray
    }
    
}
