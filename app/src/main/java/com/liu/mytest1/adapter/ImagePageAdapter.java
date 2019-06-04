package com.liu.mytest1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/5/31
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImagePageAdapter extends PagerAdapter {

    private Context mContext;
    private List<Fragment> fragments;

    public ImagePageAdapter(Context mContext, List<Fragment> fragments) {
        this.mContext = mContext;
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return false;
    }
}
