//
//  BeautyModel+Cosmos.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/23.
//

import UIKit

extension BeautyModel {
    static func createCosmosBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "smooth"
        model.value = 0.55
        model.name = "show_beauty_item_beauty_smooth".beauty_localized
        model.icon = "meiyan_icon_mopi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "white"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_whiten".beauty_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "thinFace"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_overall".beauty_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "cheekboneWidth"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekbone".beauty_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "eyeSize"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_eye".beauty_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "noseWidth"
        model.value = 0
        model.name = "show_beauty_item_beauty_nose".beauty_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "chinLength"
        model.value = 0
        model.name = "show_beauty_item_beauty_chin".beauty_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "jaw2Width"
        model.value = 0
        model.name = "show_beauty_item_beauty_jawbone".beauty_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "forehead"
        model.value = 0
        model.name = "show_beauty_item_beauty_forehead".beauty_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "mouthSize"
        model.value = 0
        model.name = "show_beauty_item_beauty_mouth".beauty_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
                         
        model = BeautyModel()
        model.path = ""
        model.key = "teeth_whiten"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".beauty_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createCosmosStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "makeup_style/heitonghua"
        model.key = "makeup"
        model.value = 0.9
        model.name = "show_beauty_item_effect_heitonghua".beauty_localized
        model.icon = "meiyan_fgz_yuanqi"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createCosmosStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "666"
        model.name = "666"
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "cat"
        model.name = "3d猫王"
        model.icon = "meiyan_lj_naiyou"
        dataArray.append(model)
        
        return dataArray
    }
    
}
