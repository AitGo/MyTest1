package com.liu.mytest1.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.liu.mytest1.ImagePageActivity;
import com.liu.mytest1.R;
import com.liu.mytest1.UpdateInfoActivity;

/**
 * @创建者 ly
 * @创建时间 2019/5/24
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class WindowAdapter implements AMap.InfoWindowAdapter, AMap.OnMarkerClickListener,
        AMap.OnInfoWindowClickListener, View.OnClickListener {

    private Context context;
    private static final String TAG = "WindowAdapter";

    public WindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        //关联布局
        View view = LayoutInflater.from(context).inflate(R.layout.layout_info_item, null);
        //标题
        TextView name = (TextView) view.findViewById(R.id.info_name);
        //地址信息
        TextView address = (TextView) view.findViewById(R.id.info_address);
        //纬度
        TextView tel = (TextView) view.findViewById(R.id.info_tel);
        //经度
        TextView orientation = (TextView) view.findViewById(R.id.info_orientation);

        Button update = view.findViewById(R.id.btn_update);
        Button image = view.findViewById(R.id.btn_image);

        update.setOnClickListener(this);
        image.setOnClickListener(this);

//        name.setText(marker.getTitle());
//        address.setText(marker.getSnippet());
//        tel.setText(marker.getPosition().latitude + "");
//        orientation.setText(marker.getPosition().longitude + "");
        Log.e(TAG, "getInfoWindow: "+marker.getTitle() );
        Log.e(TAG, "getInfoWindow: "+marker.getSnippet() );
        Log.e(TAG, "getInfoWindow: "+marker.getPosition().latitude );
        Log.e(TAG, "getInfoWindow: "+marker.getPosition().longitude );
        return view;
    }

    //如果用自定义的布局，不用管这个方法,返回null即可
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    // marker 对象被点击时回调的接口
    // 返回 true 则表示接口已响应事件，否则返回false
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "Marker被点击了");
        return false;
    }

    //绑定信息窗点击事件
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.e(TAG, "InfoWindow被点击了");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update:
                Toast.makeText(context,"update",Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(context,UpdateInfoActivity.class);
                context.startActivity(intent1);
                break;
            case R.id.btn_image:
                Intent intent = new Intent(context,ImagePageActivity.class);
                context.startActivity(intent);
                break;
        }

    }
}
