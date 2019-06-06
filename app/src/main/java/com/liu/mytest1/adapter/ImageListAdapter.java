package com.liu.mytest1.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.liu.mytest1.diagnose.CameraInfo;
import com.liu.mytest1.R;
import com.liu.mytest1.diagnose.ImageBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/5/30
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImageListAdapter extends BaseMultiItemQuickAdapter<ImageBean, BaseViewHolder> {


    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ImageListAdapter(List<ImageBean> data) {
        super(data);
        addItemType(ImageBean.IMAGE, R.layout.item_image);
        addItemType(ImageBean.ADD, R.layout.item_image_add);
    }

    @Override
    protected void convert(BaseViewHolder helper, ImageBean item) {
        switch (helper.getItemViewType()) {
            case ImageBean.IMAGE:
//                helper.setImageResource(R.id.iv_img,R.mipmap.ic_launcher_round);
                Glide.with(mContext).load(item.getUrl()).into((ImageView) helper.getView(R.id.iv_img));
                break;
            case ImageBean.ADD:
                helper.setImageResource(R.id.iv_add,R.mipmap.add);
                break;
        }

    }
}
