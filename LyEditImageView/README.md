# LyEditImageView

```
An view for zoom, pan, rotate and crop image built by swift in single file.
```
## Preview

Pan Crop View | Zoom, Rotate and Crop Image
---|---
row 1 col 1 | row 1 col 2

## Quick Start

To add a LyEditImageView in your viewController:
```
let editView = LyEditImageView(frame: frame)
let image = yourimage
editView.initWithImage(image: yourimage)
self.yourview.addSubview(editView)
```

Then get the cropped image:
```
let croppedImage = editView.getCroppedImage()

```
## For Who can speak Mandarin
```
```
## License
LyEditImageView is released under the MIT License.
