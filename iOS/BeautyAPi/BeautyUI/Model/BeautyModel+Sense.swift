//
//  BeautyModel+Sense.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/11/23.
//

import UIKit

extension BeautyModel {
    static func createSenseBeautyData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "103"
        model.value = 0.55
        model.name = "show_beauty_item_beauty_smooth".beauty_localized
        model.icon = "meiyan_icon_mopi"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "101"
        model.value = 0.2
        model.name = "show_beauty_item_beauty_whiten".beauty_localized
        model.icon = "meiyan_icon_meibai"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "201"
        model.value = 0.4
        model.name = "show_beauty_item_beauty_overall".beauty_localized
        model.icon = "meiyan_icon_shoulian"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "318"
        model.value = 0
        model.name = "show_beauty_item_beauty_cheekbone".beauty_localized
        model.icon = "meiyan_icon_shouquangu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "202"
        model.value = 0.3
        model.name = "show_beauty_item_beauty_eye".beauty_localized
        model.icon = "meiyan_icon_dayan"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "306"
        model.value = 0
        model.name = "show_beauty_item_beauty_nose".beauty_localized
        model.icon = "meiyan_icon_shoubi"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "303"
        model.value = 0
        model.name = "show_beauty_item_beauty_chin".beauty_localized
        model.icon = "meiyan_icon_xiaba"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "320"
        model.value = 0
        model.name = "show_beauty_item_beauty_jawbone".beauty_localized
        model.icon = "meiyan_icon_xiahegu"
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "304"
        model.value = 0
        model.name = "show_beauty_item_beauty_forehead".beauty_localized
        model.icon = "meiyan_icon_etou"
        model.enableNegative = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = ""
        model.key = "309"
        model.value = 0
        model.name = "show_beauty_item_beauty_mouth".beauty_localized
        model.icon = "meiyan_icon_zuixing"
        model.enableNegative = true
        dataArray.append(model)
                         
        model = BeautyModel()
        model.path = ""
        model.key = "317"
        model.value = 0
        model.name = "show_beauty_item_beauty_teeth".beauty_localized
        model.icon = "meiyan_icon_meiya"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createSenseStyleData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
     
        model = BeautyModel()
        model.path = "oumei.zip"
        model.key = "Makeup_ALL"
        model.value = 0.5
        model.name = "show_beauty_item_effect_oumei".beauty_localized
        model.icon = "meiyan_fgz_cwei"
        dataArray.append(model)
        
        model = BeautyModel()
        model.key = "Makeup_ALL"
        model.path = "hunxue.zip"
        model.value = 0.5
        model.name = "show_beauty_item_effect_hunxue".beauty_localized
        model.icon = "meiyan_fgz_hunxue"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createSenseStickerData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
     
        var model = BeautyModel()
        model.name = "show_beauty_item_none".beauty_localized
        model.icon = "show_beauty_none"
        model.isSelected = true
        dataArray.append(model)
        
        model = BeautyModel()
        model.path = "ShangBanLe.zip"
        model.name = "show_beauty_item_sticker_shangbanle".beauty_localized
        model.icon = "ic_beauty_sticker_shangbanle"
        dataArray.append(model)
        
        return dataArray
    }
    
    static func createSenseAdjustData() -> [BeautyModel] {
        var dataArray = [BeautyModel]()
        
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
     
        return dataArray
    }
}
