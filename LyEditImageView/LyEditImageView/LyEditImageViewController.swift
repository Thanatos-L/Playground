//
//  ViewController.swift
//  LyEditImageView
//
//  Created by Li,Yan(MMS) on 2017/6/10.
//  Copyright © 2017年 Li,Yan(MMS). All rights reserved.
//

import UIKit
import AVFoundation


class LyEditImageViewController: UIViewController {
    let CROP_VIEW_TAG = 1001
    let IMAGE_VIEW_TAG = 1002
    
    static let LEFT_UP_TAG = 1101
    static let LEFT_DOWN_TAG = 1102
    static let RIGHT_UP_TAG = 1103
    static let RIGHT_DOWN_TAG = 1104
    static let LEFT_LINE_TAG = 1105
    static let UP_LINE_TAG = 1106
    static let RIGHT_LINE_TAG = 1107
    static let DOWN_LINE_TAG = 1108
    
    let INIT_CROP_VIEW_SIZE = 100
    let screenHeight = UIScreen.main.bounds.size.height
    let screenWidth = UIScreen.main.bounds.size.width
    
    var pinchGestureRecognizer: UIPinchGestureRecognizer!
    var panGestureRecognizer: UIPanGestureRecognizer!
    var imageView: UIImageView!
    var touchStartPoint: CGPoint!
    var originImageViewFrame: CGRect!
    var cropView: CropView!
    var overLayView: OverLayView!
    
    var cropUpContraint: NSLayoutConstraint!
    var originalImageViewFrame: CGRect!
    
    var cropLeftMargin: CGFloat!
    var cropTopMargin: CGFloat!
    var cropRightMargin: CGFloat!
    var cropBottomMargin: CGFloat!
    var cropViewConstraints = [NSLayoutConstraint]()
    
    private func commitInit() {
        // set imageView
        initImageView()
        // set cropView
        initCropView()
        // set gesture
        initGestureRecognizer()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        commitInit()
        view.backgroundColor = UIColor.clear
        
        // Do any additional setup after loading the view, typically from a nib.
    }

    override var shouldAutorotate: Bool {
        return false
    }
    
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func setImage(image: UIImage) {
        imageView.image = image
    }
    
    func initGestureRecognizer() {
        pinchGestureRecognizer = UIPinchGestureRecognizer()
        pinchGestureRecognizer.addTarget(self, action: #selector(handlePinchGesture(gestureRecognizer:)))
        view.addGestureRecognizer(pinchGestureRecognizer)
        
        panGestureRecognizer = UIPanGestureRecognizer()
        panGestureRecognizer.addTarget(self, action: #selector(handlePanGesture(sender:)))
        imageView.addGestureRecognizer(panGestureRecognizer)
        
        let cropViewPanGesture = UIPanGestureRecognizer()
        cropViewPanGesture.addTarget(self, action: #selector(handlePanGesture(sender:)))
        cropView.addGestureRecognizer(cropViewPanGesture)
    }
    
    func initImageView() {
        imageView = UIImageView()
        let image = UIImage(named: "IMG_2796.JPG")!
        imageView.frame = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height);
        imageView.tag = IMAGE_VIEW_TAG;
        setImage(image: image)
        view.addSubview(self.imageView)
        let frame = AVMakeRect(aspectRatio: imageView.frame.size, insideRect: view.frame);
        imageView.frame = frame
        originImageViewFrame = frame
        imageView.isUserInteractionEnabled = true;
    }

    func initCropView() {
        initOverLayView()
        
        cropView = CropView(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        cropView.tag = CROP_VIEW_TAG;
        cropView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(cropView)

        cropRightMargin = (CGFloat)(originImageViewFrame.size.width / 2) - (CGFloat)(INIT_CROP_VIEW_SIZE / 2)
        cropLeftMargin = cropRightMargin
        cropTopMargin = (CGFloat)(originImageViewFrame.size.height / 2) - (CGFloat)(INIT_CROP_VIEW_SIZE / 2) + (CGFloat)((screenHeight - originImageViewFrame.size.height) / 2)
        cropBottomMargin = cropTopMargin
        updateCropViewLayout()
        
        cropView.initCropViewSubViews()
        
        adjustOverLayView()
    }
    
    func updateCropViewLayout() {
        self.view.removeConstraints(cropViewConstraints)
        let views = ["cropView":cropView!, "imageView":imageView!] as [String : UIView]
        let Hvfl = String(format: "H:|-%f-[cropView]-%f-|", cropLeftMargin, cropRightMargin);
        let Vvfl = String(format: "V:|-%f-[cropView]-%f-|", cropTopMargin, cropBottomMargin)
        let cropViewHorizentalConstraints = NSLayoutConstraint.constraints(withVisualFormat: Hvfl, options: [], metrics: nil, views: views)
        let cropViewVerticalConstraints = NSLayoutConstraint.constraints(withVisualFormat: Vvfl, options: [], metrics: nil, views: views)
        cropViewConstraints += cropViewHorizentalConstraints
        cropViewConstraints += cropViewVerticalConstraints
        self.view.addConstraints(cropViewVerticalConstraints)
        self.view.addConstraints(cropViewHorizentalConstraints)
        self.view.layoutIfNeeded()
    }
    
    func initOverLayView() {
        overLayView = OverLayView(frame: self.view.frame)
        //overLayView.setOverLayView(cropRect: self.cropView.frame);
        self.view.addSubview(overLayView)
    }
    
    func adjustOverLayView() {
        overLayView.setOverLayView(cropRect: self.cropView.frame);
        overLayView.setNeedsDisplay()
    }
    
    // MARK: Handle Gesture
    func handlePinchGesture(gestureRecognizer: UIPinchGestureRecognizer)  {
        NSLog("pinch")
    }
    
    func handlePanGesture(sender: UIPanGestureRecognizer) {
        let piceView = sender.view
        if sender.state == UIGestureRecognizerState.began {
            
            NSLog("%d",cropView.getCropViewTag())
        }
        if sender.state == UIGestureRecognizerState.changed {
            if piceView?.tag == CROP_VIEW_TAG {
                handleCropViewPanGesture(sender: sender)
            } else if piceView?.tag == IMAGE_VIEW_TAG {
                let translation = sender.translation(in: piceView?.superview)
                piceView?.center = CGPoint(x: (piceView?.center.x)! + translation.x, y: (piceView?.center.y)! + translation.y)
                sender.setTranslation(CGPoint.zero, in: piceView?.superview)
            }
        }
    }
    
    func handleCropViewPanGesture(sender: UIPanGestureRecognizer) {
        let tag:Int = cropView.getCropViewTag()
        let view = sender.view
        switch tag {
        case LyEditImageViewController.LEFT_UP_TAG:
            
            break
        case LyEditImageViewController.LEFT_DOWN_TAG:
            break
        case LyEditImageViewController.RIGHT_UP_TAG:
            break
        case LyEditImageViewController.RIGHT_DOWN_TAG:
            break
        case LyEditImageViewController.LEFT_LINE_TAG:
            break
        case LyEditImageViewController.UP_LINE_TAG:
            break
        case LyEditImageViewController.RIGHT_LINE_TAG:
            break
        case LyEditImageViewController.DOWN_LINE_TAG:
            break
        default:
            let translation = sender.translation(in: view?.superview)
            //view?.center = CGPoint(x: (view?.center.x)! + translation.x, y: (view?.center.y)! + translation.y)
            NSLog("trans %@", NSStringFromCGPoint(translation))
            cropTopMargin! += translation.y
            cropBottomMargin! -= translation.y
            cropRightMargin! -= translation.x
            cropLeftMargin! += translation.x
            updateCropViewLayout()
            sender.setTranslation(CGPoint.zero, in: view?.superview)
            break
        }
        // redraw overLayView after move cropView
        adjustOverLayView()
        NSLog("crop frame %@", NSStringFromCGRect(cropView.frame))
        NSLog("top %f, bottom %f left %f right %f", cropTopMargin, cropBottomMargin, cropLeftMargin, cropRightMargin)
    }
    
    func panImageView() {
        
    }
}

class CropView: UIView {
    
    
    var leftUpCornerPoint:UIView!
    var leftDownCornerPoint:UIView!
    var rightUpCornerPoint:UIView!
    var rightDownCornerPoint:UIView!
    
    var leftLine:UIView!
    var upLine:UIView!
    var rightLine:UIView!
    var downLine:UIView!
    
    var hittedViewTag: Int?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.isUserInteractionEnabled = true;
        self.backgroundColor = UIColor.clear
        self.clipsToBounds = false;
        self.frame = frame;
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    func initCropViewSubViews() {
        leftLine = UIView();
        leftLine.frame = CGRect(x: 0, y: 0, width: 1, height: self.frame.size.height);
        leftLine.backgroundColor = UIColor.white;
        self.addSubview(leftLine);
        leftLine.tag = LyEditImageViewController.LEFT_LINE_TAG;
        
        upLine = UIView();
        upLine.frame = CGRect(x: 0, y: 0, width: self.frame.size.width, height: 1);
        upLine.backgroundColor = UIColor.white;
        self.addSubview(upLine);
        upLine.tag = LyEditImageViewController.UP_LINE_TAG
        
        rightLine = UIView();
        rightLine.frame = CGRect(x: self.frame.size.width - 1, y: 0, width: 1, height: self.frame.size.height);
        rightLine.backgroundColor = UIColor.white;
        self.addSubview(rightLine);
        rightLine.tag = LyEditImageViewController.RIGHT_LINE_TAG
        
        downLine = UIView();
        downLine.frame = CGRect(x:0, y: self.frame.size.height - 1, width: self.frame.size.width, height: 1);
        downLine.backgroundColor = UIColor.white;
        self.addSubview(downLine);
        downLine.tag = LyEditImageViewController.DOWN_LINE_TAG
        
        leftUpCornerPoint = UIView()
        leftUpCornerPoint.frame = CGRect(x: -5, y: -5, width: 10, height: 10)
        leftUpCornerPoint.backgroundColor = UIColor.white
        self.addSubview(self.leftUpCornerPoint)
        leftUpCornerPoint.tag = LyEditImageViewController.LEFT_UP_TAG
        
        leftDownCornerPoint = UIView()
        leftDownCornerPoint.frame = CGRect(x: -5, y: self.frame.size.height - 5, width: 10, height: 10)
        leftDownCornerPoint.backgroundColor = UIColor.white
        self.addSubview(self.leftDownCornerPoint)
        leftDownCornerPoint.tag = LyEditImageViewController.LEFT_DOWN_TAG
        
        rightUpCornerPoint = UIView()
        rightUpCornerPoint.frame = CGRect(x: self.frame.size.width - 5, y: -5, width: 10, height: 10)
        rightUpCornerPoint.backgroundColor = UIColor.white
        self.addSubview(self.rightUpCornerPoint)
        rightUpCornerPoint.tag = LyEditImageViewController.RIGHT_UP_TAG
        
        rightDownCornerPoint = UIView()
        rightDownCornerPoint.frame = CGRect(x: self.frame.size.width - 5, y: self.frame.size.height - 5, width: 10, height: 10)
        rightDownCornerPoint.backgroundColor = UIColor.white
        self.addSubview(self.rightDownCornerPoint)
        rightDownCornerPoint.tag = LyEditImageViewController.RIGHT_DOWN_TAG
        
        for subview in subviews {
            subview.isUserInteractionEnabled = true;
            //test
            //subview.backgroundColor = UIColor.blue
        }
    }
    
    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        var pointInside = false
        
        for subview in subviews as [UIView] {
            if !subview.isHidden && subview.alpha > 0
                && subview.isUserInteractionEnabled {
                let extendFrame = CGRect(x: subview.frame.origin.x - 10, y: subview.frame.origin.y - 10, width: subview.frame.size.width + 20, height: subview.frame.size.height + 20)
                if extendFrame.contains(point) {
                    hittedViewTag = subview.tag
                    pointInside = true
                }
            }
        }
    
        if self.frame.contains(convert(point, to: self.superview)) {
            pointInside = true
            hittedViewTag = self.tag
        }
        
        return pointInside
    }
    
    func getCropViewTag() -> Int {
       if let tag = hittedViewTag {
            return tag
        }
        return 0;
    }
}

class OverLayView: UIView {
    var cropRect : CGRect?
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.isUserInteractionEnabled = false;
        self.backgroundColor = UIColor.clear;
    }
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    func setOverLayView(cropRect: CGRect) {
        
        self.cropRect = cropRect
        //setNeedsDisplay()
    }
    override func draw(_ rect: CGRect) {
        
        UIColor.init(red: 0.0, green: 0.0, blue: 0.0, alpha: 0.2).set()
        UIRectFill(self.frame)
        let intersecitonRect = self.frame.intersection(self.cropRect!)
        UIColor.init(red: 0.0, green: 0.0, blue: 0.0, alpha: 0.0).set()
        UIRectFill(intersecitonRect)
    }
}

