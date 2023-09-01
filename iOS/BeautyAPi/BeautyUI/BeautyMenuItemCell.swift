//
//  BeautyMenuItemCell.swift
//  BeautyAPi
//
//  Created by zhaoyongqaing on 2022/11/4.
//

import UIKit

class BeautyMenuItemCell: UICollectionViewCell {
    // 图
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.show_beautyImage(name: "show_beauty_none")
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    // 名称
    private lazy var nameLabel: UILabel = {
        let nameLabel = UILabel()
        nameLabel.font = .systemFont(ofSize: 11)
        nameLabel.textColor = UIColor(hex: "989DBA")
        nameLabel.numberOfLines = 2
        nameLabel.text = "show_beauty_item_beauty_whiten".beauty_localized
        return nameLabel
    }()
    // 选中标识
    private lazy var indicatorImgView: UIImageView = {
        let indicatorImgView = UIImageView()
        indicatorImgView.isHidden = true
        indicatorImgView.image = UIImage.show_beautyImage(name: "show_beauty_selected")
        return indicatorImgView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: BeautyModel) {
        nameLabel.text = model.name
        imageView.image = UIImage.show_beautyImage(name: model.icon)
        indicatorImgView.isHidden = !model.isSelected
        nameLabel.font = model.isSelected ? .systemFont(ofSize: 12) : .systemFont(ofSize: 11)
        nameLabel.textColor = model.isSelected ? .white : UIColor(hex: "989DBA")
    }
    
    private func createSubviews(){
        // 图
        contentView.addSubview(imageView)
        imageView.translatesAutoresizingMaskIntoConstraints = false
        
        // 选中标识
        contentView.addSubview(indicatorImgView)
        indicatorImgView.translatesAutoresizingMaskIntoConstraints = false
    
        imageView.centerXAnchor.constraint(equalTo: indicatorImgView.centerXAnchor).isActive = true
        imageView.centerYAnchor.constraint(equalTo: indicatorImgView.centerYAnchor).isActive = true
        
        indicatorImgView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        indicatorImgView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor).isActive = true
        indicatorImgView.widthAnchor.constraint(equalToConstant: 52).isActive = true
        indicatorImgView.heightAnchor.constraint(equalToConstant: 52).isActive = true
        
        // 名称
        contentView.addSubview(nameLabel)
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.centerXAnchor.constraint(equalTo: indicatorImgView.centerXAnchor).isActive = true
        nameLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
    }
}
