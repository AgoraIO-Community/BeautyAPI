//
//  Confis.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/6/1.
//

import Foundation

enum Configs {
    static let resolution: [String: CGSize] = ["VD_1920X1080": CGSize(width: 1080, height: 1920),
                                               "VD_1280X720": CGSize(width: 720, height: 1280),
                                               "VD_960X540": CGSize(width: 540, height: 960),
                                               "VD_840X480": CGSize(width: 480, height: 840),
                                               "VD_640X360": CGSize(width: 360, height: 640)]
    static let fps: [String] = ["FPS_15", "FPS_24", "FPS_30", "FPS_60"]
    static let roles: [String] = ["Broascast", "Audience"]
    static let captures: [String] = ["Agora", "Custom"]
    static let beautyTypes: [String] = ["sensetime", "fu", "bytes"]
    static let mirrorTypes: [String: MirrorMode] = ["local_remote": .LOCAL_REMOTE, "local_only" : .LOCAL_ONLY, "remote_only": .REMOTE_ONLY, "none": .NONE]
}


