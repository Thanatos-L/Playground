package com.example.liyan45.lygallerybox

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.CompletionInfo
import java.io.File
import java.util.*

/**
 * Created by liyan45 on 17/4/10.
 */
class AlbumBean {
    var topImagePath : String? = null
    var folderName : String? = null
    var imageCounts : Int? = null
    var albumFolder : File? = null

    override fun toString(): String {
        return "ImageBean{" +
                "folderName='" + folderName + '\'' +
                ", topImagePath='" + topImagePath + '\'' +
                ", imageCounts=" + imageCounts +
                '}'
    }

    companion object  {
        fun getAllAlbumFromLocalStorage(context : Context, completeCallback : AlbumListCallback) {
            object : AlbumMultiTask<Void, Void, ArrayList<AlbumBean>>() {
                override fun doInBackground(vararg params: Void?): ArrayList<AlbumBean>? {
                    var albumList = ArrayList<AlbumBean>()
                    var mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    if (context == null)
                        return albumList
                    // 拿到resolver和系统相册的content provider 交互
                    var mContentResolver = context.contentResolver
                    // 查询拍摄日期，倒序
                    val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + "desc"
                    val countCursor = mContentResolver.query(mImageUri, arrayOf("COUNT(*)"),
                            MediaStore.Images.Media.MIME_TYPE + "=? or "
                                    + MediaStore.Images.Media.MIME_TYPE + "=?",
                            arrayOf("image/jpeg", "image/png"), null)
                    if (countCursor == null)
                        return null
                    countCursor.moveToFirst()
                    val photoCount = countCursor.getInt(0)
                    Log.i("lyab"," 手机照片总数 : " + photoCount)
                    countCursor.close()
                    if (photoCount == 0)
                        return null
                    val albumMap = HashMap<String, AlbumBean>()
                    var index = 0
                    // 获取相册列表
                    while (index < photoCount) {
                        val limit = " limit " + index + ",500"
                        val mCursor = mContentResolver.query(mImageUri, arrayOf(MediaStore.Images.Media.DATA),
                                MediaStore.Images.Media.MIME_TYPE + "=? or " +
                        MediaStore.Images.Media.MIME_TYPE + "=?", arrayOf("image/jpeg", "image/png"),
                                sortOrder + limit)
                        if (mCursor == null)
                            return null
                        val size = mCursor.count
                        if (size == 0)
                            continue
                        for (i in 0..size) {
                            mCursor.moveToPosition(i)
                            val path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            val entity = SelectPhotoAdapter.SelectPhotoEntity()
                            // 将图片的uri放到对象里去
                            entity.url = path
                            // 获取该图片的父路径名
                            val parentFolder = File(path).parentFile.absolutePath
                            // 根据父路径名将图片放入到相册的HashMap中
                            if (albumMap.containsKey(parentFolder)) { // 相册已经被纪录
                                val album = albumMap.get(parentFolder);
                                album?.imageCounts = album?.imageCounts?.plus(1)
                            } else {
                                val albumFolder = File(parentFolder)
                                if (! albumFolder.exists())
                                    continue
                                val mAlbumBean = AlbumBean()
                                mAlbumBean.albumFolder = albumFolder
                                mAlbumBean.folderName = albumFolder.name
                                mAlbumBean.imageCounts = 1
                                mAlbumBean.topImagePath = path
                                albumMap.put(parentFolder, mAlbumBean)
                            }
                        }
                        mCursor.close()
                    }
                    val keyset = albumMap.keys
                    for (albumPath in keyset) {
                        albumList.add(albumMap.get(albumPath)!!)
                    }
                    Log.i("lyab", "所有图片扫描完毕,相册列表是" + albumList)
                    return albumList
                }

                override fun onPostExecute(result: ArrayList<AlbumBean>?) {
                    super.onPostExecute(result)
                    if (result == null)
                        return
                    completeCallback.onSuccess(result)
                }
            }.executeDependSDK()
        }

        fun getAlbumPhotosFromLocalStorage(context: Context, album : AlbumBean,
                                           completeCallback: AlbumPhotosCallback) {
            object : AlbumMultiTask<Void, Void, ArrayList<SelectPhotoAdapter.SelectPhotoEntity>>() {
                override fun doInBackground(vararg params: Void?): ArrayList<SelectPhotoAdapter.SelectPhotoEntity>?{
                    val photoList = ArrayList<SelectPhotoAdapter.SelectPhotoEntity>()
                    val mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    if (context == null || album == null)
                        return photoList
                    val mContentResolver = context.contentResolver
                    // 日期倒序获取图片
                    val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
                    Log.i("Alex", "查询条件是==" + album.albumFolder?.getAbsolutePath())
                    // 只查询 jpeg和png 和最近500张照片
                    var mCursor : Cursor? = null
                    if ("Recently".equals(album.folderName)) {
                        mCursor = mContentResolver.query(mImageUri, arrayOf(MediaStore.Images.Media.DATA),
                                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE
                        + "=?", arrayOf("image/jpeg", "image/png"), sortOrder + "LIMIT 500")
                    } else {
                        mCursor = mContentResolver.query(mImageUri, arrayOf(MediaStore.Images.Media.DATA),
                                "(" + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE
                        + "=?)" + " AND " + MediaStore.Images.Media.DATA + " LIKE '" + album.albumFolder?.absolutePath + "%'",
                                arrayOf("image/jpeg", "image/png"), sortOrder)
                    }
                    if (mCursor == null)
                        return null
                    val size = mCursor.count
                    if (size == 0)
                        return null
                    // 遍历全部图片
                    for (i in 0..size) {
                        mCursor.moveToPosition(i)
                        // 获取图片路劲
                        val path = mCursor.getString(0)
                        val entity = SelectPhotoAdapter.SelectPhotoEntity()
                        entity.url = path
                        // 排除相册互相包含的情况
                        if (!"Recently".equals(album.folderName) &&
                                !File(path).parentFile.absolutePath.equals(album.albumFolder?.absolutePath))
                            continue
                        photoList.add(entity)
                    }
                    mCursor.close()
                    return photoList
                }

                override fun onPostExecute(result: ArrayList<SelectPhotoAdapter.SelectPhotoEntity>?) {
                    super.onPostExecute(result)
                    if (result == null)
                        return
                    completeCallback.onSuccess(result)
                }
            }.executeDependSDK()
        }

    }

    interface AlbumListCallback {
        fun onSuccess(albumeList : ArrayList<AlbumBean>)
    }

    interface AlbumPhotosCallback {
        fun onSuccess(photos: ArrayList<SelectPhotoAdapter.SelectPhotoEntity>)
    }

}