//
//  BeautyViewController.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/6/1.
//

import UIKit

class BeautyViewController: UIViewController {
    @IBOutlet weak var localView: UIView!
    @IBOutlet weak var remoteView: UIView!
    @IBOutlet weak var settingButton: UIButton!
    @IBOutlet weak var cameraButton: UIButton!
    
    private lazy var rtcEngine: AgoraRtcEngineKit = {
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        let rtc = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        rtc.setClientRole(.broadcaster)
        rtc.enableAudio()
        rtc.enableVideo()
        rtc.setDefaultAudioRouteToSpeakerphone(true)
        return rtc
    }()
    
    private lazy var beautyAPI = BeautyAPI()
    private lazy var render = BeautyRender()
    public var channleName: String?
    public var resolution: CGSize = .zero
    public var fps: String?
    public var beautyType: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupRTC()
        setupBeautyAPI()
        setupUI()
        title = channleName
    }
    
    @IBAction func onClickSwitchCameraButton(_ sender: Any) {
        rtcEngine.switchCamera()
        beautyAPI.isFrontCamera = !beautyAPI.isFrontCamera
    }
    @IBAction func onClickSettingButton(_ sender: Any) {
        let settingView = CLSettingCellView()
        settingView.title(title: "设置")
            .switchCell(title: "美颜开关", isOn: beautyAPI.isEnable)
            .textCell(title: "编码分辨率", desc: "VD_\(resolution.height)X\(resolution.width)")
            .textCell(title: "编码帧率", desc: "FPS_\(fps ?? "")")
            .config()
        settingView.show()
        settingView.didSwitchValueChangeClosure = { [weak self] _, isOn in
            guard let self = self else { return }
            self.beautyAPI.enable(isOn)
        }
        settingView.didCellItemClosure = { [weak self] indexPath in
            guard let self = self else { return }
            if indexPath.row == 1 {
                let pickerView = PickerView()
                pickerView.dataArray = Configs.resolution.map({ $0.key })
                pickerView.pickerViewSelectedValueClosure = { value in
                    self.resolution = Configs.resolution[value] ?? .zero
                    settingView.updateTextCellDesc(indexPath: indexPath, desc: value)
                    self.updateVideoEncodeConfig()
                }
                pickerView.show()
                
            } else if indexPath.row == 2 {
                let pickerView = PickerView()
                pickerView.dataArray = Configs.fps
                pickerView.pickerViewSelectedValueClosure = { value in
                    self.fps = value.replacingOccurrences(of: "FPS_", with: "")
                    settingView.updateTextCellDesc(indexPath: indexPath, desc: value)
                    self.updateVideoEncodeConfig()
                }
                pickerView.show()
            }
        }
    }
    @IBAction func onClickBeautyButton(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.setTitleColor(sender.isSelected ? .orange : .systemPink, for: sender.isSelected ? .selected : .normal)
        if sender.isSelected {
            beautyAPI.setOptimizedDefault()
        } else {
            beautyAPI.resetOptimizedDefault()
        }
    }
    @IBAction func onClickStyleButton(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.setTitleColor(sender.isSelected ? .orange : .systemPink, for: sender.isSelected ? .selected : .normal)
        render.videoProcessing.setMakeup(sender.isSelected)
    }
    @IBAction func onClickStickerButton(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.setTitleColor(sender.isSelected ? .orange : .systemPink, for: sender.isSelected ? .selected : .normal)
        render.videoProcessing.setSticker(sender.isSelected)
    }
    @IBAction func onClickFilterButton(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        sender.setTitleColor(sender.isSelected ? .orange : .systemPink, for: sender.isSelected ? .selected : .normal)
        render.videoProcessing.setFilter(sender.isSelected)
    }
    
    private func setupBeautyAPI() {
        let config = BeautyConfig()
        config.rtcEngine = rtcEngine
        config.useCustome = false
        config.beautyRender = render
        let result = beautyAPI.initialize(config)
        if result != 0 {
            print("initialize error == \(result)")
        }
        beautyAPI.enable(true)
    }
    
    private func setupRTC() {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = 0
        videoCanvas.view = localView
        videoCanvas.renderMode = .hidden
        videoCanvas.mirrorMode = .disabled
        rtcEngine.setupLocalVideo(videoCanvas)
        rtcEngine.startPreview()
        
        // useCostome为true时需要注册delegate
//        rtcEngine.setVideoFrameDelegate(self)
        
        updateVideoEncodeConfig()
        
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.autoSubscribeAudio = true
        mediaOption.autoSubscribeVideo = true
        mediaOption.publishCameraTrack = true
        mediaOption.publishMicrophoneTrack = true
        let result = rtcEngine.joinChannel(byToken: nil, channelId: channleName ?? "", uid: 0, mediaOptions: mediaOption)
        if result != 0 {
            print("join failed")
        }
    }
    
    private func updateVideoEncodeConfig() {
        let fps = AgoraVideoFrameRate(rawValue: Int(fps ?? "15") ?? 15) ?? .fps15
        let videoEncodeConfig = AgoraVideoEncoderConfiguration(size: resolution,
                                                               frameRate: fps,
                                                               bitrate: fps.rawValue,
                                                               orientationMode: .fixedPortrait,
                                                               mirrorMode: .enabled)
        rtcEngine.setVideoEncoderConfiguration(videoEncodeConfig)
    }
    
    private func setupUI() {
        cameraButton.backgroundColor = .black.withAlphaComponent(0.4)
        cameraButton.layer.cornerRadius = 25
        cameraButton.layer.masksToBounds = true
        cameraButton.setTitle("", for: .normal)
        cameraButton.setImage(UIImage(systemName: "camera")?.withTintColor(.white, renderingMode: .alwaysOriginal), for: .normal)
        
        settingButton.backgroundColor = .black.withAlphaComponent(0.4)
        settingButton.layer.cornerRadius = 25
        settingButton.layer.masksToBounds = true
        settingButton.setTitle("", for: .normal)
        settingButton.setImage(UIImage(systemName: "gearshape")?.withTintColor(.white, renderingMode: .alwaysOriginal), for: .normal)
    }
    
    deinit {
        beautyAPI.destory()
    }
}
extension BeautyViewController: AgoraVideoFrameDelegate {
    func onCapture(_ videoFrame: AgoraOutputVideoFrame, sourceType: AgoraVideoSourceType) -> Bool {
        guard let pixelBuffer = videoFrame.pixelBuffer else { return true }
        beautyAPI.onFrame(pixelBuffer) { pixelBuffer in
            videoFrame.pixelBuffer = pixelBuffer
        }
        
        return true
    }
    
    func getVideoFormatPreference() -> AgoraVideoFormat {
        .cvPixelNV12
    }
    
    func getVideoFrameProcessMode() -> AgoraVideoFrameProcessMode {
        .readWrite
    }
    
    func getMirrorApplied() -> Bool {
        beautyAPI.isFrontCamera
    }
    
    func getRotationApplied() -> Bool {
        false
    }
}



/// agora rtc engine delegate events
extension BeautyViewController: AgoraRtcEngineDelegate {
    /// callback when warning occured for agora sdk, warning can usually be ignored, still it's nice to check out
    /// what is happening
    /// Warning code description can be found at:
    /// en: https://api-ref.agora.io/en/voice-sdk/ios/3.x/Constants/AgoraWarningCode.html
    /// cn: https://docs.agora.io/cn/Voice/API%20Reference/oc/Constants/AgoraWarningCode.html
    /// @param warningCode warning code of the problem
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        
    }
    
    /// callback when error occured for agora sdk, you are recommended to display the error descriptions on demand
    /// to let user know something wrong is happening
    /// Error code description can be found at:
    /// en: https://api-ref.agora.io/en/voice-sdk/macos/3.x/Constants/AgoraErrorCode.html#content
    /// cn: https://docs.agora.io/cn/Voice/API%20Reference/oc/Constants/AgoraErrorCode.html
    /// @param errorCode error code of the problem
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        
        
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        
    }
    
    /// callback when a remote user is joinning the channel, note audience in live broadcast mode will NOT trigger this event
    /// @param uid uid of remote joined user
    /// @param elapsed time elapse since current sdk instance join the channel in ms
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        // the view to be binded
        videoCanvas.view = remoteView
        videoCanvas.renderMode = .hidden
        rtcEngine.setupRemoteVideo(videoCanvas)
    }
    
    /// callback when a remote user is leaving the channel, note audience in live broadcast mode will NOT trigger this event
    /// @param uid uid of remote joined user
    /// @param reason reason why this user left, note this event may be triggered when the remote user
    /// become an audience in live broadcasting profile
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        // to unlink your view from sdk, so that your view reference will be released
        // note the video will stay at its last frame, to completely remove it
        // you will need to remove the EAGL sublayer from your binded view
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = nil
        videoCanvas.renderMode = .hidden
        rtcEngine.setupRemoteVideo(videoCanvas)
    }
    
    /// Reports the statistics of the current call. The SDK triggers this callback once every two seconds after the user joins the channel.
    /// @param stats stats struct
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
        
    }
    
    /// Reports the statistics of the uploading local audio streams once every two seconds.
    /// @param stats stats struct
    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        
    }
    
    /// Reports the statistics of the video stream from each remote user/host.
    /// @param stats stats struct
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
        
    }
    
    /// Reports the statistics of the audio stream from each remote user/host.
    /// @param stats stats struct for current call statistics
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        
    }
}

