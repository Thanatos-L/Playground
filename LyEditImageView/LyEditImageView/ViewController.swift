//
//  ViewController.swift
//  LyEditImageView
//
//  Created by Li,Yan(MMS) on 2017/6/14.
//  Copyright © 2017年 Li,Yan(MMS). All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func viewWillAppear(_ animated: Bool) {
        let view = LyEditImageView(frame: self.view.frame)
        let image = UIImage(named: "IMG_2796.JPG")!
        view.initWithImage(image: image)
        self.view.addSubview(view)
        self.view.backgroundColor = UIColor.clear
    }
    
    
    override var prefersStatusBarHidden: Bool {
        return true
    }

}
