package com.liu.mytest1.diagnose;

import android.graphics.Camera;
import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2019/5/23
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class CameraInfo implements Parcelable {

    public static final int ABLE = 1;
    public static final int UNABLE = 0;
    private int id;
    private LatLng latLng;
    private String address;
    private String name;
    private String tel;
    private String orientation;
    private List<String> images;
    private int state;


    public CameraInfo(LatLng latLng, int state) {
        this.latLng = latLng;
        this.state = state;
    }

    public CameraInfo() {

    }

    protected CameraInfo(Parcel in) {
        id = in.readInt();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        address = in.readString();
        name = in.readString();
        tel = in.readString();
        orientation = in.readString();
        images = in.createStringArrayList();
        state = in.readInt();
    }

    public static final Creator<CameraInfo> CREATOR = new Creator<CameraInfo>() {
        @Override
        public CameraInfo createFromParcel(Parcel in) {
            return new CameraInfo(in);
        }

        @Override
        public CameraInfo[] newArray(int size) {
            return new CameraInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeParcelable(this.latLng,0);
        dest.writeString(this.address);
        dest.writeString(this.name);
        dest.writeString(this.tel);
        dest.writeString(this.orientation);
        dest.writeStringList(images);
        dest.writeInt(this.state);
    }
}
