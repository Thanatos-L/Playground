package com.example.liyan45.lygallerybox;

import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by liyan45 on 17/4/11.
 */

public class SelectPhotoAdapter extends ArrayAdapter<SelectPhotoAdapter.SelectPhotoEntity> implements View.OnClickListener {

    public static class SelectPhotoEntity implements Serializable, Parcelable {
        public String url;
        public int isSelect;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeInt(this.isSelect);
        }

        public SelectPhotoEntity() {
        }

        protected SelectPhotoEntity(Parcel in) {
            this.url = in.readString();
            this.isSelect = in.readInt();
        }

        public static final Parcelable.Creator<SelectPhotoEntity> CREATOR =
                new Parcelable.Creator<SelectPhotoEntity>() {
                    @Override
                    public SelectPhotoEntity createFromParcel(Parcel source) {
                        return new SelectPhotoEntity(source);
                    }

                    @Override
                    public SelectPhotoEntity[] newArray(int size) {
                        return new SelectPhotoEntity[size];
                    }
                };

        @Override
        public String toString() {
            final StringBuffer stringBuffer = new StringBuffer("SelectEntity{");
            stringBuffer.append("url=").append(url).append('\'');
            stringBuffer.append(", isSelect=").append(isSelect);
            stringBuffer.append("}");
            return stringBuffer.toString();
        }

        @Override
        public int hashCode() {
            if (url != null)
                return url.hashCode();
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SelectPhotoEntity) {
                return obj.hashCode() == this.hashCode();
            }
            return super.equals(obj);
        }
    }

    public static final String CAMERA_PHOTO_PATH = "/ly/camera";
    public String cameraPhotoUrl;
    private Activity mActivity;
    public ArrayList<SelectPhotoEntity> allPhotoList;
    int maxSelectedPhotoCount = 9;

    public static final int REQ_CAMEARA = 1000;
    private File mFile;
    private LyAlbumImageLoader mLyAlbumImageLoader;
    private int destWidth, destHeight;
    int screennWidth;

    HashSet<SelectPhotoEntity> selectPhotoEntities = new HashSet<>(maxSelectedPhotoCount);

    public SelectPhotoAdapter(Activity activity, ArrayList<SelectPhotoEntity> arrayList) {
        super(activity, R.layout.adapter_select_photo, arrayList);
        this.mActivity = activity;
        this.allPhotoList = arrayList;
        this.mLyAlbumImageLoader = new LyAlbumImageLoader();
        this.screennWidth = getScreenWidth(activity);
        this.destWidth = (screennWidth - 20) / 3;
        this.destHeight = (screennWidth - 20) / 3;
    }

    @Override
    public int getCount() {
        // 加1是相机图标
        return  allPhotoList.size() + 1;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("lyab","要现实的position是 " + position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.adapter_select_photo, parent, false);
            viewHolder.lyPhoto = (RelativeLayout) convertView.findViewById(R.id.lyPhoto);
            viewHolder.iv_photo = (ImageView) convertView.findViewById(R.id.iv_photo);
            viewHolder.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewHolder.iv_photo.getLayoutParams() != null) {
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) viewHolder.iv_photo.getLayoutParams();
            layoutParams.width = destWidth;
            layoutParams.height = destHeight;
            viewHolder.iv_photo.setLayoutParams(layoutParams);
        }
        viewHolder.iv_select.setVisibility(View.VISIBLE);
        viewHolder.iv_select.setImageDrawable(getDrawable(mActivity, R.drawable.unchoose));
        viewHolder.lyPhoto.setOnClickListener(null);

        if (position == 0) { // 第一个位置的小相机
            // 防止回调覆盖了imageView原来的bitmap
            mLyAlbumImageLoader.setAsyncBitmapFromSD(null, viewHolder.iv_photo, 0, false, false, false);
            viewHolder.iv_photo.setImageDrawable(getDrawable(mActivity, R.drawable.cameraadd));
            viewHolder.iv_select.setVisibility(View.GONE);
            viewHolder.lyPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraPhotoUrl = System.currentTimeMillis() + "lyPhoto.jpeg";
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                            + CAMERA_PHOTO_PATH);
                    if (!dir.exists())
                        dir.mkdirs();
                    try {
                        SharedPreferences sp = mActivity.getSharedPreferences("Camera", Context.MODE_PRIVATE);
                        if (sp != null) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("photoUrl", cameraPhotoUrl);
                            editor.apply();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFile = new File(dir, cameraPhotoUrl);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
                    mActivity.startActivityForResult(intent, REQ_CAMEARA);
                }
            });
        } else if ((allPhotoList != null) && (position >= 0) && (allPhotoList.size() >= position) &&
                (allPhotoList.get(position - 1) != null)) {
            final SelectPhotoEntity photoEntity = allPhotoList.get(position - 1);
            final String filePath = photoEntity.url;
            viewHolder.iv_select.setVisibility(View.VISIBLE);
            if (checkIsExistedInSelectedPhotoArrayList(photoEntity)) {
                viewHolder.iv_select.setImageDrawable(getDrawable(mActivity, R.drawable.choose));
            } else {
                viewHolder.iv_select.setImageDrawable(getDrawable(mActivity, R.drawable.unchoose));
            }
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lyPhoto:
                Log.i("lyab", "点击了lyphoto");
                SelectPhotoEntity entity = (SelectPhotoEntity) v.getTag(R.id.lyPhoto);
                ImageView ivSelect = (ImageView) v.findViewById(R.id.iv_select);
                if (mActivity == null) {
                    return;
                }
                if (checkIsExistedInSelectedPhotoArrayList(entity)) {
                    ivSelect.setImageDrawable(getDrawable(mActivity, R.drawable.unchoose));
                    removeSelectedPhoto(entity);
                } else if (!isFullInSelectedPhotoArrayList()) {
                    ivSelect.setImageDrawable(getDrawable(mActivity, R.drawable.choose));
                    addSelectedPhoto(entity);
                } else {
                    return;
                }
                if (mActivity instanceof CallBackActivity)((CallBackActivity)mActivity).updateSelectActivityViewUI();
                break;
        }
    }

    class ViewHolder {
        public RelativeLayout lyPhoto;
        public ImageView iv_photo;
        public ImageView iv_select;
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public static Drawable getDrawable(Context context, int id) {
        if ((context == null) || (id < 0)) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, null);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    /**
     * 从系统相册里面取出图片的uri
     */
    public static void get500PhotoFromLocalStorage(final Context context, final LookUpPhotosCallback completeCallback) {
        new AlbumMultiTask<Void, Void, ArrayList<SelectPhotoEntity>>() {
            @Override
            protected ArrayList<SelectPhotoEntity> doInBackground(Void... params) {
                ArrayList<SelectPhotoEntity> allPhotoArrayList = new ArrayList<>();
                String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";//设置拍摄日期为倒序
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Log.i("lyab","get500PhotoFromLocalStorage 查找图片");
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(mImageUri, new String[]{MediaStore.Images.Media.DATA},
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"}, sortOrder + " limit 500");
                if (cursor != null) {
                    return allPhotoArrayList;
                }
                int size = cursor.getCount();
                if (size == 0) {
                    return allPhotoArrayList;
                }
                for (int i = 0; i < size; i++) {
                    cursor.moveToPosition(i);
                    String path = cursor.getString(0);
                    SelectPhotoEntity entity = new SelectPhotoEntity();
                    entity.url = path;
                    allPhotoArrayList.add(entity);
                }
                cursor.close();
                return allPhotoArrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<SelectPhotoEntity> arrayList) {
                super.onPostExecute(arrayList);
                if (arrayList == null) {
                    return;
                }
                if (completeCallback != null) {
                    completeCallback.onSuccess(arrayList);
                }
            }
        }.executeDependSDK();
    }

    /**
     * 查询照片成功的回调函数
     */
    public interface LookUpPhotosCallback {
        void onSuccess(ArrayList<SelectPhotoEntity> photoArrayList);
    }

    /**
     * 点击更新UI
     */
    public interface CallBackActivity{
        void updateSelectActivityViewUI();

    }

    public boolean checkIsExistedInSelectedPhotoArrayList(SelectPhotoEntity photoEntity) {
        if (selectPhotoEntities == null || selectPhotoEntities.size() == 0)
            return false;
        if (selectPhotoEntities.contains(photoEntity))
            return true;
        return false;
    }
    public void removeSelectedPhoto(SelectPhotoEntity photo) {
        selectPhotoEntities.remove(photo);
    }
    public boolean isFullInSelectedPhotoArrayList() {
//        if (maxSelectedPhotoCount > 0 && selectPhotoEntities.size() < maxSelectedPhotoCount) {
//            return false;
//        } else {
//            return true;
//        }
        return (maxSelectedPhotoCount > 0 && selectPhotoEntities.size() < maxSelectedPhotoCount);
    }
    public void addSelectedPhoto(SelectPhotoEntity photo) {
        selectPhotoEntities.add(photo);
    }
}
