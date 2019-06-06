package com.liu.mytest1.diagnose;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/6/5
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImageBean implements MultiItemEntity {

    public static final int IMAGE = 1;
    public static final int ADD = 2;

    private int itemType;
    private String url;

    public ImageBean() {}


    public ImageBean (int itemType) {
        this.itemType = itemType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public List<ImageBean> cameraInfo2ImageBean(CameraInfo cameraInfo) {
        List<ImageBean> imageBeans = new ArrayList<>();
        for(String url : cameraInfo.getImages()) {
            ImageBean bean = new ImageBean();
            bean.setUrl(url);
            bean.setItemType(IMAGE);
            imageBeans.add(bean);
        }
        ImageBean bean = new ImageBean(ADD);
        imageBeans.add(bean);
        return imageBeans;
    }
}
