//
//  ViewController.swift
//  BeautyAPi
//
//  Created by zhaoyongqiang on 2023/5/30.
//

import UIKit

class ViewController: UIViewController {
    @IBOutlet weak var textFiled: UITextField!
    @IBOutlet weak var resolutionButton: UIButton!
    @IBOutlet weak var fpsButton: UIButton!
    @IBOutlet weak var roleButton: UIButton!
    @IBOutlet weak var captureButton: UIButton!
    private var beautyType: String = "sensetime"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let tap = UITapGestureRecognizer(target: self, action: #selector(onTapView))
        view.addGestureRecognizer(tap)
    }
    
    @objc
    private func onTapView() {
        view.endEditing(true)
    }
    
    @IBAction func onClickRoleButton(_ sender: UIButton) {
        let pickerView = PickerView()
        pickerView.dataArray = Configs.roles
        pickerView.pickerViewSelectedValueClosure = { value in
            sender.setTitle(value, for: .normal)
        }
        pickerView.show()
    }
    
    @IBAction func onClickCaptureButton(_ sender: UIButton) {
        let pickerView = PickerView()
        pickerView.dataArray = Configs.captures
        pickerView.pickerViewSelectedValueClosure = { value in
            sender.setTitle(value, for: .normal)
        }
        pickerView.show()
    }
    
    @IBAction func onClickResolutionButton(_ sender: UIButton) {
        let pickerView = PickerView()
        pickerView.dataArray = Configs.resolution.map({ $0.key })
        pickerView.pickerViewSelectedValueClosure = { value in
            sender.setTitle(value, for: .normal)
        }
        pickerView.show()
    }
    @IBAction func onClickFpsButton(_ sender: UIButton) {
        let pickerView = PickerView()
        pickerView.dataArray = Configs.fps
        pickerView.pickerViewSelectedValueClosure = { value in
            sender.setTitle(value, for: .normal)
        }
        pickerView.show()
    }

    @IBAction func onClickChoseBeautyType(_ sender: UIButton) {
        let pickerView = PickerView()
        pickerView.dataArray = Configs.beautyTypes
        pickerView.pickerViewSelectedValueClosure = { [weak self] value in
            sender.setTitle(value, for: .normal)
            self?.beautyType = value
        }
        pickerView.show()
    }
    
    @IBAction func onClickJoinChannelButton(_ sender: Any) {
        view.endEditing(true)
        let resolution = Configs.resolution[resolutionButton.titleLabel?.text ?? ""] ?? .zero
        let fps = fpsButton.titleLabel?.text?.replacingOccurrences(of: "FPS_", with: "")

        let identifier = "BeautyViewController"
        let storyBoard: UIStoryboard = UIStoryboard(name: identifier, bundle: nil)
        guard let newViewController = storyBoard.instantiateViewController(withIdentifier: identifier) as? BeautyViewController else {return}
        newViewController.resolution = resolution
        newViewController.fps = fps
        newViewController.channleName = textFiled.text
        newViewController.capture = captureButton.titleLabel?.text
        newViewController.role = roleButton.titleLabel?.text
        newViewController.beautyType = beautyType
        navigationController?.pushViewController(newViewController, animated: true)
    }
}
