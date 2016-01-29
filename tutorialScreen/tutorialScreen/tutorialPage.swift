//
//  tutorialScreen.swift
//  tutorialScreen
//
//  Created by MoonSlides on 16/1/29.
//  Copyright © 2016年 李龑. All rights reserved.
//

import UIKit

class tutorialPage: UIViewController {

    @IBOutlet weak var image: UIImageView!
    @IBOutlet weak var label: UILabel!
    
    var pageIndex:UInt = 0
    var imageFile:String = ""
    var textTitle:String = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.image.image = UIImage(named: imageFile)
        self.label.text = textTitle

    }
}
