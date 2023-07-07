Pod::Spec.new do |spec|
  spec.name         = "BeautyAPI"
  spec.version      = "1.0.0"
  spec.summary      = "ZhaoYongqiang"
  spec.description  = "BeautyAPI"
  spec.homepage     = "https://github.com/AgoraIO-Community/BeautyAPI"
  spec.license      = "MIT"
  spec.author       = { "ZYQ" => "zhaoyongqiang@agora.io" }
  spec.source       = { :git => "https://github.com/AgoraIO-Community/BeautyAPI.git", :tag => spec.version }

  # 默认加载所有
  spec.default_subspec = "Core"

  # All
  spec.subspec "All" do |ss|
    ss.dependency 'BeautyAPI/Core'
    ss.dependency 'BeautyAPI/Sensetime'
    ss.dependency 'BeautyAPI/FU'
    ss.dependency 'BeautyAPI/Bytes'
  end

  # Core
  spec.subspec "Core" do |ss|
    ss.source_files  = "BeautyAPI/*.{h,m}"
    ss.public_header_files = 'BeautyAPI/*.{h}'
  end

  # bundle
  spec.subspec "Bundle" do |ss|
    ss.source_files  = "Util/*.{h,m}"
    ss.public_header_files = 'Util/*.{h}'
  end

  spec.subspec "Sensetime" do |ss|
    ss.source_files = 'BeautyAPI/SenseRender/*.{h,m}'
    ss.public_header_files = 'BeautyAPI/SenseRender/*.{h}'
    ss.dependency "BeautyAPI/Core"
    ss.framework  = "Foundation"
  end

  spec.subspec "FU" do |ss|
    ss.source_files = 'BeautyAPI/FURender/*.{h,m}'
    ss.public_header_files = 'BeautyAPI/FURender/*.{h}'
    ss.dependency "BeautyAPI/Core"
    ss.dependency "BeautyAPI/Bundle"
    ss.dependency 'FURenderKit'
    ss.framework  = "Foundation"
  end

  spec.subspec "Bytes" do |ss|
    ss.source_files = 'BeautyAPI/BytesRender/*.{h,m}'
    ss.public_header_files = 'BeautyAPI/BytesRender/*.{h}'
    ss.dependency "BeautyAPI/Core"
    ss.framework  = "Foundation"
  end
  
  spec.dependency 'AgoraRtcEngine_iOS'
  spec.ios.deployment_target = '10.0'
  spec.requires_arc  = true
end
