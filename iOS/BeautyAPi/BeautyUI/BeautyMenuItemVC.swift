//
//  BeautyMenuItemVC.swift
//  BeautyAPi
//
//  Created by zhaoyongqaing on 2022/11/4.
//

import UIKit
import JXCategoryView

class BeautyMenuItemVC: UIViewController {
    
    var selectedItemClosure: ((_ value: CGFloat, _ isHiddenSldier: Bool) -> Void)?
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 15
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.register(BeautyMenuItemCell.self, forCellWithReuseIdentifier: NSStringFromClass(BeautyMenuItemCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.showsHorizontalScrollIndicator = false
        return collectionView
    }()
    private lazy var beautyData = BeautyModel.createBeautyData()
    private lazy var styleData = BeautyModel.createStyleData()
    private lazy var adjustData = BeautyModel.createAdjustData()
    private lazy var filterData = BeautyModel.createFilterData()
    private lazy var stickerData = BeautyModel.createStickerData()
     
    private lazy var dataArray: [BeautyModel] = {
        switch type {
        case .beauty: return beautyData
        case .style: return styleData
        case .adjust: return adjustData
        case .sticker: return stickerData
        }
    }()
    
    private var type: BeautyMenuType = .beauty
    private var defalutSelectIndex = 0
    
    init(type: BeautyMenuType) {
        super.init(nibName: nil, bundle: nil)
        self.type = type
        self.view.backgroundColor = .clear
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
        configDefaultSelect()
        defaultHandler()
    }
    
    func defaultHandler() {
        if let index = dataArray.firstIndex(where: { $0.isSelected }) {
            let model = dataArray[index]
            selectedItemClosure?(model.value, model.key == nil)
            defalutSelectIndex = index
            changeValueHandler(value: model.value)
        }
    }
    
    func changeValueHandler(value: CGFloat) {
        guard value > 0 else { return }
        setBeautyHandler(value: value, isReset: false)
    }
    
    func reloadData() {
        collectionView.reloadData()
    }
    
    private func setBeautyHandler(value: CGFloat, isReset: Bool) {
        let model = dataArray[defalutSelectIndex]
        model.value = value
        switch type {
        case .beauty, .adjust:
            if isReset {
                BeautyManager.shareManager.reset(datas: dataArray)
                return
            }
            BeautyManager.shareManager.setBeauty(path: model.path,
                                                     key: model.key,
                                                     value: model.value)
        case .style:
            if isReset {
                BeautyManager.shareManager.resetStyle(datas: dataArray)
                return
            }
            let styleModel = model as? FUBeautyModel
            BeautyManager.shareManager.setStyle(path: model.path,
                                                key: model.key,
                                                value: model.value,
                                                isCombined: styleModel?.isCombined ?? false)
            
        case .sticker:
            BeautyManager.shareManager.setSticker(path: model.path, datas: stickerData)
        }
    }
    
    private func setUpUI(){
        // 列表
        view.addSubview(collectionView)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
    
    // 默认选中
    private func configDefaultSelect(){
        CATransaction.begin()
        CATransaction.setCompletionBlock {
            let indexPath = IndexPath(item: self.defalutSelectIndex, section: 0)
            if self.collectionView.numberOfItems(inSection: 0)  > self.defalutSelectIndex {
                self.collectionView.selectItem(at: indexPath, animated: false, scrollPosition: .left)
            }
        }
        collectionView.reloadData()
        CATransaction.commit()
    }
}

extension BeautyMenuItemVC: UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: BeautyMenuItemCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(BeautyMenuItemCell.self),
                                                                          for: indexPath) as! BeautyMenuItemCell
        let model = dataArray[indexPath.item]
        cell.setupModel(model: model)
        if model.isSelected {
            selectedItemClosure?(model.value, model.key == nil)
            defalutSelectIndex = indexPath.item
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let preModel = dataArray[defalutSelectIndex]
        preModel.isSelected = false
        dataArray[defalutSelectIndex] = preModel
        collectionView.reloadItems(at: [IndexPath(item: defalutSelectIndex, section: 0)])
        
        defalutSelectIndex = indexPath.item
        let model = dataArray[indexPath.item]
        setBeautyHandler(value: model.value, isReset: model.key == nil)
        model.isSelected = true
        dataArray[indexPath.item] = model
        collectionView.reloadItems(at: [IndexPath(item: indexPath.item, section: 0)])
        
        if type == .sticker {
            selectedItemClosure?(0, true)
            return
        }
        selectedItemClosure?(model.value, model.path == nil)
    }
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        switch type {
        case .beauty:
            let model = dataArray[indexPath.item]
            let nsString = (model.name ?? "") as NSString
            let attributes = [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 11)]
            let size = nsString.size(withAttributes: attributes as [NSAttributedString.Key : Any])
            let w = size.width < 52 ? 52 : size.width
            return CGSize(width: w, height: 70)

        default:
            return CGSize(width: 52, height: 70)
        }
    }
}

extension BeautyMenuItemVC : JXCategoryListContentViewDelegate {
    
    func listView() -> UIView! {
        return view
    }
}
