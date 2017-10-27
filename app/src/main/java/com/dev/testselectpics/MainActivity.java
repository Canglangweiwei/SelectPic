package com.dev.testselectpics;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dev.testselectpics.app.AppApplication;
import com.dev.testselectpics.utils.SDPathUtils;
import com.dev.testselectpics.view.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;

//@SuppressWarnings("ALL")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_TAKE_PHOTO_PERMISSION = 222;

    private static final int REQUEST_GALLERY_CODE = 1;      // 相册
    private static final int REQUEST_CAMERA_CODE = 2;       // 相机
    private static final int REQUEST_ZOOM_CODE = 3;         // 裁剪

    @Bind(R.id.iv_head)
    CircleImageView ivHeadLogo;

    private Dialog dialog;

    @Override
    protected int initContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initUi() {
        initSheetDialog();
    }

    @Override
    protected void initDatas() {

    }

    @Override
    protected void initListener() {

    }

    /**
     * 打开对话框
     */
    @OnClick({R.id.iv_head})
    void onClick() {
        if (!isFinishing()) {
            dialog.show();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String localImg;

    /**
     * 打开对话框
     */
    private void initSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.sp_photo_choose_dialog, null);
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.onWindowAttributesChanged(wl);
        dialog.setCanceledOnTouchOutside(true);
        Button btnCamera = (Button) view.findViewById(R.id.btn_to_camera);
        btnCamera.setOnClickListener(this);

        Button btnPhoto = (Button) view.findViewById(R.id.btn_to_photo);
        btnPhoto.setOnClickListener(this);

        Button btnCancel = (Button) view.findViewById(R.id.btn_to_cancel);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_CODE && data != null) {
            startPhotoZoom(data.getData());
        } else if (requestCode == REQUEST_CAMERA_CODE) {
            File temp = new File(SDPathUtils.getCachePath(), "temp.jpg");
            // 裁剪图片
            startPhotoZoom(Uri.fromFile(temp));
        } else if (requestCode == REQUEST_ZOOM_CODE) {
            if (data != null) {
                setPicToView(data);
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
        intent.setDataAndType(uri, "image/*");
        startActivityForResult(intent, REQUEST_ZOOM_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bitmap bitmap = null;
        byte[] bis = picdata.getByteArrayExtra("bitmap");
        bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
        localImg = System.currentTimeMillis() + ".JPEG";
        if (bitmap != null) {
            SDPathUtils.saveBitmap(bitmap, localImg);
            Log.e("本地图片绑定", SDPathUtils.getCachePath() + localImg);
            setImageUrl(ivHeadLogo, "file:/" + SDPathUtils.getCachePath() + localImg, R.mipmap.head_logo);
        }
    }

    private DisplayImageOptions options;

    /**
     * 加载图片
     *
     * @param imageView
     * @param imageUrl
     * @param emptyImgId
     */
    private void setImageUrl(ImageView imageView, String imageUrl, int emptyImgId) {
        if (options == null) {
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(emptyImgId)
                    .showImageForEmptyUri(emptyImgId)
                    .showImageOnFail(emptyImgId)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
        }
        Log.d("图片地址：", imageUrl);
        ImageLoader.getInstance().displayImage(imageUrl, imageView, options);
    }

    @Override
    public void onClick(View view) {
        dialog.dismiss();
        switch (view.getId()) {
            case R.id.btn_to_camera:// 打开相机
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PHOTO_PERMISSION);
                    return;
                } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_TAKE_PHOTO_PERMISSION);
                    return;
                } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_TAKE_PHOTO_PERMISSION);
                    return;
                } else {
                    takePhoto();
                }
                break;
            case R.id.btn_to_photo:// 打开相册
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, REQUEST_GALLERY_CODE);
                break;
            case R.id.btn_to_cancel:// 取消
                break;
        }
    }

    /**
     * 打开相机
     */
    private void takePhoto() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(SDPathUtils.getCachePath(), "temp.jpg")));
            startActivityForResult(openCameraIntent, REQUEST_CAMERA_CODE);
        } else {
            Uri imageUri = FileProvider.getUriForFile(MainActivity.this, "com.camera_photos.fileprovider", new File(SDPathUtils.getCachePath(), "temp.jpg"));
            openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(openCameraIntent, REQUEST_CAMERA_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_TAKE_PHOTO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 申请成功，可以拍照
                takePhoto();
            } else {
                Toast.makeText(this, "CAMERA PERMISSION DENIED", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        AppApplication.get(getApplicationContext()).finishAllActivity();
        super.onBackPressed();
    }
}
