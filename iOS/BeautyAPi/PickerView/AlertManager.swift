//
//  AlertManager.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit
import AVFoundation

public let cl_screenWidht = UIScreen.main.bounds.width
public let cl_screenHeight = UIScreen.main.bounds.height
class AlertManager: NSObject {
    private struct AlertViewCache {
        var view: UIView?
        var index: Int = 0
        var isDismiss: Bool = false
        var bottomCons: NSLayoutConstraint? = nil
    }
    enum AlertPosition {
        case center
        case bottom
    }
    
    private static var vc: UIViewController?
    private static var containerView: UIView?
    private static var currentPosition: AlertPosition = .center
    private static var viewCache: [AlertViewCache] = []
    
    static var hiddenViewClosure: (() -> Void)?
    
    public static func show(view: UIView,
                            alertPostion: AlertPosition = .center,
                            didCoverDismiss: Bool = true,
                            controller: UIViewController? = nil) {
        let index = viewCache.isEmpty ? 0 : viewCache.count
        viewCache.append(AlertViewCache(view: view, index: index, isDismiss: didCoverDismiss))
        currentPosition = alertPostion
        if vc == nil {
            containerView = UIButton(frame: CGRect(x: 0, y: 0, width: cl_screenWidht, height: cl_screenHeight))
            containerView?.backgroundColor = UIColor(red: 0.0/255, green: 0.0/255, blue: 0.0/255, alpha: 0.0)
        }
        if didCoverDismiss {
            (containerView as? UIButton)?.addTarget(self, action: #selector(tapView), for: .touchUpInside)
        }
        guard let containerView = containerView else { return }
        containerView.addSubview(view)
        view.translatesAutoresizingMaskIntoConstraints = false
        view.alpha = 0
        if alertPostion == .center {
            view.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
            view.centerYAnchor.constraint(equalTo: containerView.centerYAnchor).isActive = true
        }else{
            let bottomAnchor = view.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
            if let cacheIndex = viewCache.firstIndex(where: { $0.view == view }) {
                var cache = viewCache[cacheIndex]
                cache.bottomCons = bottomAnchor
                viewCache[cacheIndex] = cache
            }
            view.leadingAnchor.constraint(equalTo: containerView.leadingAnchor).isActive = true
            view.trailingAnchor.constraint(equalTo: containerView.trailingAnchor).isActive = true
        }
        if vc == nil {
            if controller != nil {
                vc = controller
                vc?.view.addSubview(containerView)
                showAlertPostion(alertPostion: alertPostion, view: view)
            } else {
                vc = UIViewController()
                vc?.view.backgroundColor = UIColor.clear
                vc?.view.addSubview(containerView)
                vc?.modalPresentationStyle = .custom
                UIViewController.topViewController()?.present(vc!, animated: false) {
                    showAlertPostion(alertPostion: alertPostion, view: view)
                }
            }
        } else {
            showAlertPostion(alertPostion: alertPostion, view: view)
        }
//        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name:  UIResponder.keyboardWillShowNotification, object: nil)
        
//        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name:  UIResponder.keyboardWillHideNotification, object: nil)
    }

    private static func showAlertPostion(alertPostion: AlertPosition, view: UIView) {
        containerView?.layoutIfNeeded()
        if alertPostion == .center {
            showCenterView(view: view)
        }else{
            let bottomAnchor = viewCache.first(where: { $0.view == view })?.bottomCons
            bottomAnchor?.constant = view.frame.height
            bottomAnchor?.isActive = true
            containerView?.layoutIfNeeded()
            showBottomView(view: view)
        }
    }
    
    private static func showCenterView(view: UIView){
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0/255,
                                                     green: 0.0/255,
                                                     blue: 0.0/255,
                                                     alpha: 0.5)
            view.alpha = 1.0
        })
    }
    
    private static func showBottomView(view: UIView){
        if !viewCache.isEmpty {
            viewCache.forEach({ $0.view?.alpha = 0 })
        }
        view.alpha = 1.0
        let bottomAnchor = viewCache.first(where: { $0.view == view })?.bottomCons
        bottomAnchor?.constant = 0
        bottomAnchor?.isActive = true
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.backgroundColor = UIColor(red: 0.0/255,
                                                     green: 0.0/255,
                                                    blue: 0.0/255,
                                                    alpha: 0.5)
            containerView?.superview?.layoutIfNeeded()
        })
    }

    static func updateViewHeight() {
        UIView.animate(withDuration: 0.25, animations: {
            containerView?.layoutIfNeeded()
        })
    }
    
    static func hiddenView(all: Bool = true, completion: (() -> Void)? = nil){
        if currentPosition == .bottom {
            guard let lastView = viewCache.last?.view,
                    let bottomAnchor = viewCache.last?.bottomCons else { return }
            bottomAnchor.constant = lastView.frame.height
            bottomAnchor.isActive = true
        }
        UIView.animate(withDuration: 0.25, animations: {
            if all || viewCache.count <= 1 {
                containerView?.backgroundColor = UIColor(red: 255.0/255,
                                                         green: 255.0/255,
                                                         blue: 255.0/255,
                                                         alpha: 0.0)
                containerView?.layoutIfNeeded()
            }
            if currentPosition == .center {
                viewCache.last?.view?.alpha = 0
            }
        }, completion: { (_) in
            if all || viewCache.count <= 1 {
                viewCache.removeAll()
                vc?.dismiss(animated: false, completion: completion)
                vc = nil
                containerView?.removeFromSuperview()
            } else {
                viewCache.removeLast()
                viewCache.last?.view?.alpha = 1
            }
        })
    }
    
    @objc
    private static func tapView(){
        guard viewCache.last?.isDismiss == true else { return }
        DispatchQueue.main.asyncAfter(deadline: DispatchTime(uptimeNanoseconds: UInt64(0.1))) {
            self.hiddenView(all: self.viewCache.isEmpty, completion: hiddenViewClosure)
        }
    }
    
    private static var originFrame:CGRect = .zero
    @objc private static func keyboardWillShow(notification: Notification) {
        originFrame = containerView!.frame
        let keyboardHeight = (notification.userInfo?["UIKeyboardBoundsUserInfoKey"] as? CGRect)?.height
        let y = cl_screenHeight - (keyboardHeight ?? 304) - containerView!.frame.height
        UIView.animate(withDuration: 0.25) {
            containerView?.frame.origin.y = y
        }
    }
    @objc private static func keyboardWillHide(notification: Notification) {
        UIView.animate(withDuration: 0.25) {
//            containerView?.frame = originFrame
            containerView?.frame.origin.y = 0
        }
    }
}

extension UIViewController {
    static var keyWindow: UIWindow? {
        // Get connected scenes
        return UIApplication.shared.connectedScenes
        // Keep only active scenes, onscreen and visible to the user
            .filter { $0.activationState == .foregroundActive }
        // Keep only the first `UIWindowScene`
            .first(where: { $0 is UIWindowScene })
        // Get its associated windows
            .flatMap({ $0 as? UIWindowScene })?.windows
        // Finally, keep only the key window
            .first(where: \.isKeyWindow)
    }
    static func topViewController(_ viewController: UIViewController? = nil) -> UIViewController? {
        let viewController = viewController ?? keyWindow?.rootViewController
        
        if let navigationController = viewController as? UINavigationController,
            !navigationController.viewControllers.isEmpty
        {
            return self.topViewController(navigationController.viewControllers.last)
            
        } else if let tabBarController = viewController as? UITabBarController,
            let selectedController = tabBarController.selectedViewController
        {
            return self.topViewController(selectedController)
            
        } else if let presentedController = viewController?.presentedViewController {
            return self.topViewController(presentedController)
            
        }
        return viewController
    }
}

class TTAlertBaseViewController: UIViewController {
    
}
