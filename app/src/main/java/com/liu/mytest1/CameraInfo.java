package com.liu.mytest1;

import com.amap.api.maps.model.LatLng;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/5/23
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class CameraInfo implements MultiItemEntity {
    public static final int ABLE = 1;
    public static final int UNABLE = 0;
    private LatLng latLng;
    private String address;
    private String name;
    private String tel;
    private String orientation;
    private List<String> images;
    private int state;
    public static final int IMAGE = 1;
    public static final int ADD = 2;
    private int itemType;

    public CameraInfo(LatLng latLng, int state) {
        this.latLng = latLng;
        this.state = state;
    }

    public CameraInfo(int itemType) {
        this.itemType = itemType;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
