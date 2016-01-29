//
//  rootPage.swift
//  tutorialScreen
//
//  Created by MoonSlides on 16/1/29.
//  Copyright © 2016年 李龑. All rights reserved.
//

import UIKit

class rootPage: UIViewController, UIPageViewControllerDataSource {
    
    var pageViewController: UIPageViewController?
    var arrayPageTitles: NSArray?
    var arrayImages: NSArray?
    let introText1 = "first screen"
    let introText2 = "mid screen"
    let introText3 = "last screen"

    override func viewDidLoad() {
        super.viewDidLoad()
        arrayPageTitles = [introText1, introText2, introText3]
        arrayImages = ["introductionPic1.jpg", "introductionPic2.jpg", "introductionPic3.jpg"]
        self.pageViewController = self.storyboard?.instantiateViewControllerWithIdentifier("PageViewController") as? UIPageViewController
        self.pageViewController?.dataSource = self
        let startingViewController = self.viewControllerAtIndex(0)
        let viewControllers: NSArray = [startingViewController!]
        self.pageViewController?.setViewControllers(viewControllers as? [UIViewController], direction: .Forward, animated: false, completion: nil)
        
        self.pageViewController!.view.frame = CGRectMake(0, 0, self.view.frame.width, self.view.frame.height)
        self.addChildViewController(pageViewController!)
        self.view.addSubview(pageViewController!.view)
        self.pageViewController?.didMoveToParentViewController(self)
    }
    
    func viewControllerAtIndex(index: UInt) -> tutorialPage? {
        if ((self.arrayPageTitles!.count == 0) || (Int(index) >= self.arrayPageTitles!.count)) {
            return nil
        }
        
        let pageContentViewController = self.storyboard?.instantiateViewControllerWithIdentifier("PageContentViewController") as! tutorialPage
        pageContentViewController.imageFile = self.arrayImages![Int(index)] as! String
        pageContentViewController.textTitle = self.arrayPageTitles![Int(index)] as! String
        pageContentViewController.pageIndex = index
        
        return pageContentViewController
    }
    func pageViewController(pageViewController: UIPageViewController, viewControllerBeforeViewController viewController: UIViewController) -> UIViewController? {
        var index = (viewController as! tutorialPage).pageIndex
        if (index == 0 || Int(index) == NSNotFound) {
            return nil;
        }
        index--
        return self.viewControllerAtIndex(index)
    }
    
    func pageViewController(pageViewController: UIPageViewController, viewControllerAfterViewController viewController: UIViewController) -> UIViewController? {
        var index = (viewController as! tutorialPage).pageIndex
        
        if Int(index) == NSNotFound {
            return nil
        }
        
        index++
        
        if Int(index) == arrayPageTitles!.count {
            return nil
        }
        
        return self.viewControllerAtIndex(index)
    }
    
    func presentationCountForPageViewController(pageViewController: UIPageViewController) -> Int {
        return self.arrayPageTitles!.count
    }
    
    func presentationIndexForPageViewController(pageViewController: UIPageViewController) -> Int {
        return 0
    }
}
