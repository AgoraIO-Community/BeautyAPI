//
//  BeautyBaseVC.swift
//  BeautyAPi
//
//  Created by zhaoyongqaing on 2022/11/4.
//

import UIKit
import JXCategoryView

enum BeautyMenuType: CaseIterable {
    case beauty
    case style
    case adjust
    case sticker
    
    var title: String {
        switch self {
        case .beauty: return "create_beauty_setting_beauty_face".beauty_localized
        case .style: return "create_beauty_setting_special_effects".beauty_localized
        case .adjust: return "create_beauty_setting_special_adjust".beauty_localized
        case .sticker: return "create_beauty_setting_sticker".beauty_localized
        }
    }
}

class BeautyBaseVC: UIViewController {
    
    var selectedItem: ((_ item: String)->())?
    var dismissed: (()->())?
    
    private lazy var slider: UISlider = {
        let slider = UISlider()
        slider.minimumTrackTintColor = UIColor(hex: "7A59FB")
        slider.maximumTrackTintColor = UIColor(hex: "FFFFFF", alpha: 0.7)
        slider.addTarget(self, action: #selector(onTapSliderHandler(sender:)), for: .valueChanged)
        return slider
    }()
    private let titles = BeautyMenuType.allCases.filter({
        if BeautyModel.beautyType != .sense {
            return $0 != .adjust
        }
        return true
    }).map({ $0.title })
    private let vcs = BeautyMenuType.allCases.filter({
        if BeautyModel.beautyType != .sense {
            return $0 != .adjust
        }
        return true
    }).map({ BeautyMenuItemVC(type: $0) })
    
    // Background
    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = UIColor(hex: "#151325")
        bgView.layer.cornerRadius = 20
        bgView.layer.masksToBounds = true
        bgView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        return bgView
    }()
    
    // Contrast button
    private lazy var compareButton: UIButton = {
        let compareButton = UIButton(type: .custom)
        compareButton.setImage(UIImage.show_beautyImage(name: "show_beauty_compare"), for: .selected)
        if #available(iOS 13.0, *) {
            compareButton.setImage(UIImage.show_beautyImage(name: "show_beauty_compare")?
                .withTintColor(UIColor(hex: "#7A59FB") ?? .clear,
                               renderingMode: .alwaysOriginal), for: .normal)
        }
        compareButton.addTarget(self, action: #selector(didClickCompareButton(sender:)), for: .touchUpInside)
        compareButton.backgroundColor = UIColor(hex: "#000000", alpha: 0.25)
        compareButton.isSelected = true
        compareButton.layer.cornerRadius = 18
        compareButton.layer.masksToBounds = true
        return compareButton
    }()
    
    // Indicator bar
    private lazy var indicator: JXCategoryIndicatorLineView = {
        let indicator = JXCategoryIndicatorLineView()
        indicator.indicatorWidth = 30
        indicator.indicatorHeight = 2
        indicator.indicatorColor = .white
        return indicator
    }()
    
    private lazy var segmentedView: JXCategoryTitleView = {
        let segmentedView = JXCategoryTitleView()
        segmentedView.isTitleColorGradientEnabled = true
        segmentedView.titles = titles
        segmentedView.titleFont = .systemFont(ofSize: 14)
        segmentedView.titleSelectedFont = .systemFont(ofSize: 15)
        segmentedView.titleColor = UIColor(hex: "989DBA")
        segmentedView.titleSelectedColor = .white
        segmentedView.backgroundColor = .clear
        segmentedView.defaultSelectedIndex = 0
        segmentedView.delegate = self
        segmentedView.indicators = [self.indicator]
        return segmentedView
    }()
        
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
        self.view.backgroundColor = .clear
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private var beautyFaceVC: BeautyMenuItemVC? {
        didSet {
            beautyFaceVC?.selectedItemClosure = { [weak self] value, isHiddenValue in
                guard let self = self else { return }
                self.slider.isHidden = isHiddenValue
                self.compareButton.isHidden = isHiddenValue
                self.slider.setValue(Float(value), animated: true)
            }
            beautyFaceVC?.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        beautyFaceVC = vcs.first
    }

    private func setupUI(){
        view.backgroundColor = .clear
        
        // slider
        view.addSubview(slider)
        slider.translatesAutoresizingMaskIntoConstraints = false
        slider.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 22).isActive = true
        slider.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -83).isActive = true
        slider.heightAnchor.constraint(equalToConstant: 30).isActive = true
        slider.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -214).isActive = true
        
        view.addSubview(compareButton)
        compareButton.translatesAutoresizingMaskIntoConstraints = false
        compareButton.centerYAnchor.constraint(equalTo: slider.centerYAnchor).isActive = true
        compareButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20).isActive = true
        compareButton.widthAnchor.constraint(equalToConstant: 36).isActive = true
        compareButton.heightAnchor.constraint(equalToConstant: 36).isActive = true
        
        view.addSubview(bgView)
        bgView.translatesAutoresizingMaskIntoConstraints = false
        bgView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        bgView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        bgView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        bgView.heightAnchor.constraint(equalToConstant: 203).isActive = true
       
        bgView.addSubview(segmentedView)
        segmentedView.translatesAutoresizingMaskIntoConstraints = false
        segmentedView.leadingAnchor.constraint(equalTo: bgView.leadingAnchor).isActive = true
        segmentedView.trailingAnchor.constraint(equalTo: bgView.trailingAnchor).isActive = true
        segmentedView.topAnchor.constraint(equalTo: bgView.topAnchor, constant: 10).isActive = true
        segmentedView.heightAnchor.constraint(equalToConstant: 30).isActive = true
        
        if let listContainerView = JXCategoryListContainerView(type: .scrollView, delegate: self) {
            segmentedView.listContainer = listContainerView
            bgView.addSubview(listContainerView)
            listContainerView.translatesAutoresizingMaskIntoConstraints = false
            listContainerView.leadingAnchor.constraint(equalTo: bgView.leadingAnchor).isActive = true
            listContainerView.trailingAnchor.constraint(equalTo: bgView.trailingAnchor).isActive = true
            listContainerView.topAnchor.constraint(equalTo: segmentedView.bottomAnchor, constant: 25).isActive = true
            listContainerView.heightAnchor.constraint(equalToConstant: 70).isActive = true
        }
    }
    

    @objc
    private func onTapSliderHandler(sender: UISlider) {
        beautyFaceVC?.changeValueHandler(value: CGFloat(sender.value))
    }
}

extension BeautyBaseVC {
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
        dismissed?()
    }
    
    @objc private func didClickCompareButton(sender: UIButton){
        sender.isSelected = !sender.isSelected
        BeautyManager.shareManager.isEnableBeauty = sender.isSelected
    }
}


extension BeautyBaseVC: JXCategoryViewDelegate {
    func categoryView(_ categoryView: JXCategoryBaseView!, didSelectedItemAt index: Int) {
        beautyFaceVC = vcs[index]
        if index == vcs.count - 1 {
            compareButton.isHidden = true
        }
    }
}

extension BeautyBaseVC: JXCategoryListContainerViewDelegate {
    
    func number(ofListsInlistContainerView listContainerView: JXCategoryListContainerView?) -> Int {
        titles.count
    }
    
    func listContainerView(_ listContainerView: JXCategoryListContainerView?,
                           initListFor index: Int) -> JXCategoryListContentViewDelegate? {
        vcs[index]
    }
}
