package com.example.liyan45.lygallerybox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Switch
import java.io.File
import java.io.IOException
import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by liyan45 on 17/4/11.
 */
class LyAlbumImageLoader(context: Context) {
    val mContext : Context = context
    var imageCache : HashMap<String, SoftReference<Bitmap>>  = HashMap<String, SoftReference<Bitmap>>()
    var currentUrls : ConcurrentHashMap<ImageView, String> = ConcurrentHashMap<ImageView, String>()
    var defBitmap : Bitmap ? = null
    val PLACE_HOLDER : Int = R.drawable.qraved_bg_default
    var usePlaceHolder = false

    init {
        defBitmap = BitmapFactory.decodeResource(mContext.resources, PLACE_HOLDER)
    }

    companion object {
        fun readPictureDegree(path: String): Int {
            var degree = 0
            try {
                val exifInterface = ExifInterface(path)
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
                Log.i("lyab", "获取图片旋转角度出现异常", e)
                return degree
            }
            Log.i("lyab", "本张图片的旋转角度是 $path 角度是 $degree")
            return degree
        }

        fun getBitmapFromFile(uri: String?, options: BitmapFactory.Options?): Bitmap? {
            if (TextUtils.isEmpty(uri)  || options == null) {
                if(uri != null && uri.length < 4) {
                    return null
                }
            }
            try {
                if (!File(uri).isFile) {
                    return null
                }
                val bitmap = BitmapFactory.decodeFile(uri, options)
                return bitmap
            } catch (e: OutOfMemoryError) {
                Log.i("lyab","getBitmapFromFile OOM")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun rotateBitmap(bitmap: Bitmap, degree: Int, destroySource: Boolean):Bitmap {
            if (degree == 0) {
                return bitmap
            }
            Log.i("lyab", "旋转bitmap 宽度 " + bitmap.width + " 高度 " + bitmap.height + " 角度 " + degree)
            try {
                val matrix = Matrix()
                matrix.setRotate(degree.toFloat(), (bitmap.width/2).toFloat(), (bitmap.height/2).toFloat())
                val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (bitmap != null && destroySource) {
                    bitmap.recycle()
                }
                return bmp
            } catch (e: OutOfMemoryError) {
                Log.i("lyab", "rotateBitmap exception oom")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        fun storeThumbnail(context: Context, filename: String, bitmap: Bitmap): Boolean {
            if (bitmap == null) {
                return false
            }
            val file = File(context.cacheDir.absolutePath + "/" + filename)
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return false
                }
            } else {
                return true
            }

        }

    }

    /**从本地加载一张图片并使用imageView进行显示，
     * 可以设置是否根据图片的大小动态修改imageView的高度，宽度必须传入来控制显示图片的清晰度防止oom
     * @param uri 图片地址
     * @param imageView
     * @param imageViewWidth 单位px
     * @param resizeImageView 是否根据宽高重设imageview的宽高比例
     * @param autoRotate 是否根据EXIF信息旋转图片
     * @param storeThumbnail 是否在sd卡中存储缩略图
     * @param loadCompleteCallback 加载图片成功回调
     */
    fun loadBitmapFromSD(uri: String, imageView: ImageView, imageViewWidth: Int, resizeImageView: Boolean,
                         autoRotate: Boolean, storeThumbnail: Boolean, loadCompleteCallback: ImageCallback): Bitmap? {
        if (imageCache.containsKey(uri)) { // 如果之前就加载过图片
            val softReference = imageCache[uri]
            val bitmap = softReference?.get()
            if (bitmap != null) {
                Log.i("lyab", "从LRU拿出bitmap")
                return bitmap
            }
        }

        if (uri == null) {
            return null
        }

        if (storeThumbnail) { // 如果要求从缓存中拿，就先去磁盘上找
            val context = imageView.context
            object : AlbumMultiTask<Void, Void, Bitmap>() {
                override fun doInBackground(vararg params: Void?): Bitmap? {
                    val file = File(context.cacheDir.absolutePath + "/" + File(uri).name)
                    if (file.exists() && file.length() > 1000) {
                        var thumbnail : Bitmap? = null
                        val targetUrl = currentUrls.get(imageView)
                        if (!uri.equals(targetUrl)) {
                            Log.i("lyab", "缩略图已经过时")
                            return null
                        }
                        Log.i("lyab","从cache拿缩略图")
                        try {
                            thumbnail = BitmapFactory.decodeFile(file.absolutePath)
                        } catch (e: OutOfMemoryError) {
                            Log.i("lyab", "加载缩略图出现oom")
                        } catch (e: Exception) {

                        }
                        return thumbnail
                    }
                    return null
                }

                override fun onPostExecute(result: Bitmap?) {
                    super.onPostExecute(result)
                    val targetUrl = currentUrls.get(imageView)
                    if (uri != targetUrl){
                        Log.i("lyab", "缩略图已经过时")
                        return
                    }
                    if (result != null && loadCompleteCallback != null) {
                        loadCompleteCallback.imageLoaded(result, imageView, uri)
                        return
                    } else {
                        loadNewSDImage(uri, imageView, imageViewWidth, resizeImageView, autoRotate, storeThumbnail,
                                loadCompleteCallback)
                    }
                }
            }.executeDependSDK()
            // 如果返回默认bitmap就用默认图占位，如果返回null就用imageView父控件的背景色占位,null更快
            return if (usePlaceHolder) defBitmap else null
        }
        loadNewSDImage(uri, imageView, imageViewWidth, resizeImageView, autoRotate, storeThumbnail,
                loadCompleteCallback)
        // 如果返回默认bitmap就用默认图占位，如果返回null就用imageView父控件的背景色占位,null更快
        return if (usePlaceHolder) defBitmap else null
    }

    public interface ImageCallback {
        fun imageLoaded(imageBitmap: Bitmap, imageView: ImageView, uri: String)
    }

    fun loadNewSDImage(uri: String, imageView: ImageView, imageViewWidth: Int, resizeImageView: Boolean,
                       autoRotate: Boolean, storeThumbnail: Boolean, loadCompleteCallback: ImageCallback) {
        object : AlbumMultiTask<Void, Void, BitmapFactory.Options>() {
            // 这一块主要是用来拿宽高，确定要加载图片的大小的
            override fun doInBackground(vararg params: Void?): BitmapFactory.Options? {
                // 线程开启之后，由于滚动太快，已经过了一段时间，可能imageView要显示的图片已经换，就取消线程
                var imageSize : IntArray = intArrayOf(0,0)
                // 滑动非常快的时候会在此处中断
                val targetUrl = currentUrls.get(imageView)
                if (uri != targetUrl) {
                    Log.i("lyab", "图片已经过时了")
                    return null
                }
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(uri, options)
                val targetUrl1 = currentUrls.get(imageView)
                if (uri != targetUrl1) {
                    Log.i("lyab", "图片已经过时了")
                    return null
                }
                imageSize[0] = options.outWidth
                imageSize[1] = options.outHeight
                Log.i("lyab", "原图的分辨率是" + imageSize[0] + "  " + imageSize[1])
                Log.i("lyab", "目标宽度是" + imageViewWidth)
                if (imageSize[0] < 1) {
                    return null
                }
                var destWidth = imageViewWidth
                if (imageViewWidth > 400) {
                    destWidth = destWidth.times(0.7).toInt()
                }
                options.inSampleSize = calculateInSampleSize(options, destWidth,
                        destWidth*(options.outHeight/options.outWidth))
                options.inPurgeable = true
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565
                return options
            }

            override fun onPostExecute(options: BitmapFactory.Options?) {
                super.onPostExecute(options)
                val targetUrl = currentUrls.get(imageView)
                if (uri != targetUrl) {
                    Log.i("lyab", "图片已经过时了")
                    return null
                }
                if (options == null) {
                    return
                }
                asynGetBitmap(options, uri, imageView, imageViewWidth, resizeImageView, autoRotate, storeThumbnail,
                        loadCompleteCallback)
            }
        }.executeDependSDK()
    }

    /**
     * 一个在子线程里从文件获取bitmap，并存到LRU缓存的方法
     * @param catchedOptions
     * @param uri
     * @param imageView
     * @param autoRotate
     * @param imageCallback
     */
    fun asynGetBitmap(cacheOptions: BitmapFactory.Options, uri: String, imageView: ImageView, imageViewWidth: Int,
                      resizeImageView: Boolean, autoRotate: Boolean, storeThumbnail: Boolean,
                      imageCallback: ImageCallback) {
        if (resizeImageView && imageViewWidth > 0) { // 如果给出imageViewWith，就改变传入的iamgeview
            var imageViewHeight : Int

        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            //宽度比例值
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            //取比例值中的较大值作为inSampleSize
            inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
        }

        return inSampleSize
    }


}