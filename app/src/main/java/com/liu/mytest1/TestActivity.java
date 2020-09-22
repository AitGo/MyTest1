package com.liu.mytest1;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @创建者 ly
 * @创建时间 2020/9/17
 * @描述
 * @更新者 $
 * @更新时间 $
 * @更新描述
 */
public class TestActivity extends Activity implements View.OnClickListener {

    private Button btn;
    private ImageView img;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        img = findViewById(R.id.iv_img);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(this);

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        long currentTimeMillis = System.currentTimeMillis();
        String s = currentTimeMillis + "";
    }

    private void initImg(int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),R.mipmap.banner,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.banner, options);
        img.setImageBitmap(bitmap);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize = 1;
        if(outWidth > reqWidth || outHeight > reqHeight) {
            int widthRatio = Math.round((float) outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) outHeight / (float) reqHeight);
            inSampleSize = widthRatio < heightRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
//        initImg(410,240);
        initImg(img.getWidth(),img.getHeight());

    }
}
