package com.liu.mytest1.adapter;

import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.liu.mytest1.CameraInfo;
import com.liu.mytest1.R;

import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/5/30
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImageListAdapter extends BaseMultiItemQuickAdapter<CameraInfo, BaseViewHolder> {


    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ImageListAdapter(List<CameraInfo> data) {
        super(data);
        addItemType(CameraInfo.IMAGE, R.layout.item_image);
        addItemType(CameraInfo.ADD, R.layout.item_image_add);
    }

    @Override
    protected void convert(BaseViewHolder helper, CameraInfo item) {
        switch (helper.getItemViewType()) {
            case CameraInfo.IMAGE:
                helper.setImageResource(R.id.iv_img,R.mipmap.ic_launcher_round);
                break;
            case CameraInfo.ADD:
                helper.setImageResource(R.id.iv_add,R.mipmap.add);
                break;
        }

    }
}
