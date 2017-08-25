package com.dev.testselectpics;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dev.testselectpics.utils.ImageCompressUtils;
import com.dev.testselectpics.utils.SDPathUtils;
import com.dev.testselectpics.view.ClipImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 图片裁剪
 */
@SuppressWarnings("ALL")
public class PreviewActivity extends Activity {

    @Bind(R.id.src_pic)
    ClipImageView srcPic;

    private int degrees = 0;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.sp_crop_image);
        ButterKnife.bind(this);

        initBitmap();
    }

    // ////////////////////////////////////////////////////////////////////////////////

    /**
     * 初始化图片资源
     */
    private void initBitmap() {
        try {
            ImageCompressUtils compress = new ImageCompressUtils();
            ImageCompressUtils.CompressOptions options = new ImageCompressUtils.CompressOptions();
            options.uri = getIntent().getData();
            options.maxWidth = getWindowManager().getDefaultDisplay().getWidth();
            options.maxHeight = getWindowManager().getDefaultDisplay().getHeight();
            bitmap = compress.compressFromUri(this, options);

            float f = getBitmapDegree(SDPathUtils.getCachePath() + "temp.jpg");

            bitmap = rotateBitmapByDegree(bitmap, f);
            srcPic.setImageBitmap(bitmap);
        } catch (Exception e) {
            finish();
        }
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("旋转角度", "" + degree);
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, float degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 图片旋转
     */
    private Bitmap setBitmapRotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
    }

    @OnClick({R.id.btn_left, R.id.btn_right, R.id.btn_crop_n, R.id.btn_crop_y})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_left:
                degrees -= 90;
                srcPic.setImageBitmap(setBitmapRotate(bitmap, degrees));
                break;
            case R.id.btn_right:
                degrees += 90;
                srcPic.setImageBitmap(setBitmapRotate(bitmap, degrees));
                break;
            case R.id.btn_crop_n:
                PreviewActivity.this.finish();
                break;
            case R.id.btn_crop_y:
                // 此处获取剪裁后的bitmap
                degrees = 0;
                Bitmap bitmap = srcPic.clip();

                // 由于Intent传递bitmap不能超过40k,此处使用二进制数组传递
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] bitmapByte = baos.toByteArray();

                Intent intent = new Intent();
                intent.putExtra("bitmap", bitmapByte);

				/*
                 * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，
				 * 这样就可以在onActivityResult方法中得到Intent对象，
				 */
                setResult(3, intent);
                try {
                    // 释放图片资源
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
                // 结束当前这个Activity对象的生命
                finish();
                break;
        }
    }
    // ////////////////////////////////////////////////////////////////////////////////
}
