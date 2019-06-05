package com.liu.mytest1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.liu.mytest1.adapter.PointsListAdapter;
import com.liu.mytest1.adapter.WindowAdapter;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends Activity implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnMapLoadedListener, LocationSource, EasyPermissions.PermissionCallbacks {
    MapView mMapView = null;
    private AMap aMap;
    LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private static final int PERMISSON_REQUESTCODE = 0;
    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;
    Marker screenMarker = null;

    private ImageButton btn_position;
    private Button btn_edit;
    public DrawerLayout drawerLayout;
    private RelativeLayout leftLayout;
    private RecyclerView rv_points;
    private PointsListAdapter adapter;
    private List<CameraInfo> cameraInfos = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Marker> checkMarkers = new ArrayList<>();

    private GeocodeSearch geocoderSearch;

    private File mediaStorageDir;
    private String mFilepath;

    private List<LatLng> latLngs = new ArrayList<LatLng>();
    private Polyline polyline;
    private boolean isEdit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取权限
        getPermission();

        btn_position = (ImageButton)findViewById(R.id.btn_position);
        btn_edit = findViewById(R.id.btn_edit);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        leftLayout = findViewById(R.id.main_left_drawer_layout);
        rv_points = findViewById(R.id.rv_points);
        rv_points.setLayoutManager(new LinearLayoutManager(this));


        btn_position.setOnClickListener(this);
        btn_edit.setOnClickListener(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();

        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setZoomControlsEnabled(true);

        mUiSettings.setMyLocationButtonEnabled(false); //显示默认的定位按钮

        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

        geocoderSearch = new GeocodeSearch(getApplicationContext());

        //设置自定义弹窗
        aMap.setInfoWindowAdapter(new WindowAdapter(this));
        //绑定信息窗点击事件
        aMap.setOnInfoWindowClickListener(new WindowAdapter(this));
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);

        // 设置定位监听
        aMap.setLocationSource(this);

        aMap.setOnMapLoadedListener(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);


        initData();
        adapter = new PointsListAdapter(R.layout.item_points,cameraInfos);
        rv_points.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                drawerLayout.closeDrawer(leftLayout);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraInfos.get(position).getLatLng(),17));
                markers.get(position).remove();
                addMarker(cameraInfos.get(position).getLatLng(),R.mipmap.camera_blue,checkMarkers);
            }
        });
    }

    private void initData() {

        LatLng latLng1 = new LatLng((double) 28.208663, (double) 112.913565);
        LatLng latLng2 = new LatLng((double) 28.206535, (double) 112.90216);
        LatLng latLng3 = new LatLng((double) 28.210941, (double) 112.912213);

        CameraInfo cameraInfo1 = new CameraInfo();
        cameraInfo1.setAddress("长沙西中心");
        cameraInfo1.setLatLng(latLng1);
        cameraInfo1.setName("张三");
        cameraInfo1.setTel("13124562458");
        cameraInfo1.setOrientation("向南");
        cameraInfos.add(cameraInfo1);

        CameraInfo cameraInfo2 = new CameraInfo();
        cameraInfo2.setAddress("长沙航天医院");
        cameraInfo2.setLatLng(latLng2);
        cameraInfo2.setName("李四");
        cameraInfo2.setTel("13124562458");
        cameraInfo2.setOrientation("向南");
        cameraInfos.add(cameraInfo2);

        CameraInfo cameraInfo3 = new CameraInfo();
        cameraInfo3.setAddress("长沙汽车西站四合院");
        cameraInfo3.setLatLng(latLng3);
        cameraInfo3.setName("王五");
        cameraInfo3.setTel("13124562458");
        cameraInfo3.setOrientation("向南");
        cameraInfos.add(cameraInfo3);

        drawPoint(latLng1);
        drawPoint(latLng2);
        drawPoint(latLng3);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_position:
                mlocationClient.startLocation();//启动定位
                break;
            case R.id.btn_edit:
                if (drawerLayout.isDrawerOpen(leftLayout)) {
                    drawerLayout.closeDrawer(leftLayout);
                } else {
                    drawerLayout.openDrawer(leftLayout);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void drawPoint(LatLng latLng) {
        addMarker(latLng,R.mipmap.camera_yellow,markers);
    }

    private void addMarker(LatLng latLng,int markerIcon,List<Marker> markerList) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        // markerOption.snippet("西安市：111");
        markerOption.draggable(true);
        markerOption.setFlat(true);
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),markerIcon)));
        markerList.add(aMap.addMarker(markerOption));
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        addMarkerInScreenCenter();
    }

    /**
     * 在屏幕中心添加一个Marker
     */
    private void addMarkerInScreenCenter() {
        LatLng latLng = aMap.getCameraPosition().target;
        Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
        screenMarker = aMap.addMarker(new MarkerOptions()
                .anchor(0.5f,0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.purple_pin)));
        //设置Marker在屏幕上,不跟随地图移动
        screenMarker.setPositionByPixels(screenPosition.x,screenPosition.y);
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation() {

        if (screenMarker != null ) {
            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = screenMarker.getPosition();
            Point point =  aMap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this,125);
            LatLng target = aMap.getProjection()
                    .fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if(input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f)*(1.5f - input)));
                    }
                }
            });
            //整个移动所需要的时间
            animation.setDuration(600);
            //设置动画
            screenMarker.setAnimation(animation);
            //开始动画
            screenMarker.startAnimation();

        } else {
            Log.e("amap","screenMarker is null");
        }
    }

    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 检查权限
     *
     * @param
     * @since 2.5.0
     */
    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, needPermissions)) {
            //已经打开权限
            Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(this, "需要获取您的存储、定位权限", 1, needPermissions);
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    public void getMapImage() {
        /**
         * 对地图进行截屏
         */
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                Log.d("aMap","onMapScreenShot");
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int status) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                if(null == bitmap){
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(
                            Environment.getExternalStorageDirectory() + "/test_"
                                    + sdf.format(new Date()) + ".png");
                    boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    try {
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    StringBuffer buffer = new StringBuffer();
                    if (b)
                        buffer.append("截屏成功 ");
                    else {
                        buffer.append("截屏失败 ");
                    }
                    if (status != 0)
                        buffer.append("地图渲染完成，截屏无网格");
                    else {
                        buffer.append( "地图未渲染完成，截屏有网格");
                    }
//                    ToastUtil.show(getApplicationContext(), buffer.toString());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        drawPoint(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        }else {
            marker.showInfoWindow();
        }
        return true;
    }

    @Override
    public void onMapLoaded() {
//        addMarkersToMap();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(getApplicationContext());
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation amapLocation) {
                    if (mListener != null&&amapLocation != null) {
                        if (amapLocation != null
                                &&amapLocation.getErrorCode() == 0) {
//                                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                            //设置地图显示中心点
                            LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                            StringBuffer buffer = new StringBuffer();
                            buffer.append(amapLocation.getCountry() + ""
                                    + amapLocation.getProvince() + ""
                                    + amapLocation.getCity() + ""
                                    + amapLocation.getProvince() + ""
                                    + amapLocation.getDistrict() + ""
                                    + amapLocation.getStreet() + ""
                                    + amapLocation.getStreetNum());
                            Log.e("marker position", buffer.toString());
                        } else {
                            String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                            Log.e("AmapErr",errText);
                        }
                    }
                }
            });

            aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {

                }

                @Override
                public void onCameraChangeFinish(CameraPosition cameraPosition) {
                    //屏幕中心的Marker跳动
                    startJumpAnimation();
                    Log.e("marker position",cameraPosition.target.longitude + "\n" + cameraPosition.target.latitude);
                    LatLonPoint point = new LatLonPoint(cameraPosition.target.latitude,cameraPosition.target.longitude);
                    RegeocodeQuery query = new RegeocodeQuery(point, 50,GeocodeSearch.AMAP);

                    geocoderSearch.getFromLocationAsyn(query);
                    geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                        @Override
                        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();

                            Log.e("marker position", address.getFormatAddress());
                        }

                        @Override
                        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                        }
                    });

                }
            });
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);

            //获取一次定位结果：
            //该方法默认为false。
            mLocationOption.setOnceLocation(true);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);

//                    //获取最近3s内精度最高的一次定位结果：
//                    //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
//                    mLocationOption.setOnceLocationLatest(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}
