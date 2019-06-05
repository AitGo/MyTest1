package com.liu.mytest1.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.liu.mytest1.CameraInfo;
import com.liu.mytest1.R;

import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/6/5
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class PointsListAdapter extends BaseQuickAdapter<CameraInfo, BaseViewHolder> {

    public PointsListAdapter(int layoutResId, @Nullable List<CameraInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CameraInfo item) {
        helper.setText(R.id.tv_address,item.getAddress());
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_tel,item.getTel());
        helper.setText(R.id.tv_orientation,item.getOrientation());
    }
}
