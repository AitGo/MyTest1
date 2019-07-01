package com.liu.mytest1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.liu.mytest1.diagnose.CameraInfo;

import java.util.ArrayList;

/**
 * @创建者 ly
 * @创建时间 2019/5/29
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImagePageActivity extends FragmentActivity {

    private ViewPager vp_img;
    private TextView tv_page;
    private ArrayList<String> urlList = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_page);
        tv_page = findViewById(R.id.tv_page_size);
        vp_img = findViewById(R.id.vp_image);

        CameraInfo cameraInfo = getIntent().getParcelableExtra("cameraInfo");
        urlList.addAll(cameraInfo.getImages());

        vp_img.setAdapter(new PictureSlidePagerAdapter(getSupportFragmentManager()));
        vp_img.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tv_page.setText(String.valueOf(position+1)+"/"+urlList.size());
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private  class PictureSlidePagerAdapter extends FragmentStatePagerAdapter {

        public PictureSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(urlList.get(position));
        }

        @Override
        public int getCount() {
            return urlList.size();
        }
    }
}
