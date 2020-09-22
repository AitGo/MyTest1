package com.liu.mytest1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MultiPointItem;
import com.amap.api.maps.model.MultiPointOverlay;
import com.amap.api.maps.model.MultiPointOverlayOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.liu.mytest1.adapter.PointsListAdapter;
import com.liu.mytest1.base.Constants;
import com.liu.mytest1.diagnose.CameraInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends Activity implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnMapLoadedListener, LocationSource, EasyPermissions.PermissionCallbacks, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter {
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
    private int cameraInfoId;

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
        aMap.setInfoWindowAdapter(this);
        //绑定信息窗点击事件
        aMap.setOnInfoWindowClickListener(this);
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
            }
        });

        initPoint();
    }

    String[] lats = new String[]{"27.555471","27.729443","27.729443","27.729835","27.728546","27.729811","27.729446"};
    String[] lons = new String[]{"112.071082","112.005062","112.005919","112.007109","112.006503","112.007218","112.011528"};
    private void initPoint() {
        MultiPointOverlayOptions overlayOptions = new MultiPointOverlayOptions();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.camera_blue);
        overlayOptions.icon(bitmap);//设置图标
        overlayOptions.anchor(0.5f,0.5f); //设置锚点
        MultiPointOverlay multiPointOverlay = aMap.addMultiPointOverlay(overlayOptions);
        List<MultiPointItem> list = new ArrayList<MultiPointItem>();
        for(int i = 0; i < 100; i++) {
            //创建MultiPointItem存放，海量点中某单个点的位置及其他信息
            Random random = new Random();
            double lat = Double.valueOf(lats[random.nextInt(6)]);
            double lon =  Double.valueOf(lons[random.nextInt(6)]);
            LatLng latLng = new LatLng(lat,lon);
            MultiPointItem multiPointItem = new MultiPointItem(latLng);
            multiPointItem.setObject("position:" + i);
            list.add(multiPointItem);
        }
        MultiPointOverlayOptions overlayOptions1 = new MultiPointOverlayOptions();
        BitmapDescriptor bitmap1 = BitmapDescriptorFactory.fromResource(R.mipmap.camera_red);
        overlayOptions1.icon(bitmap1);//设置图标
        overlayOptions1.anchor(0.5f,0.5f); //设置锚点
        MultiPointOverlay multiPointOverlay1 = aMap.addMultiPointOverlay(overlayOptions1);
        List<MultiPointItem> list1 = new ArrayList<MultiPointItem>();
        for(int i = 0; i < 100; i++) {
            //创建MultiPointItem存放，海量点中某单个点的位置及其他信息
            Random random = new Random();
            double lat = Double.valueOf(lats[random.nextInt(6)]);
            double lon =  Double.valueOf(lons[random.nextInt(6)]);
            LatLng latLng = new LatLng(lat,lon);
            MultiPointItem multiPointItem = new MultiPointItem(latLng);
            multiPointItem.setObject("position:" + i);
            list1.add(multiPointItem);
        }
        multiPointOverlay1.setItems(list1);
        multiPointOverlay.setItems(list);//将规范化的点集交给海量点管理对象设置，待加载完毕即可看到海量点信息

        // 定义海量点点击事件
        AMap.OnMultiPointClickListener multiPointClickListener = new AMap.OnMultiPointClickListener() {
            // 海量点中某一点被点击时回调的接口
            // 返回 true 则表示接口已响应事件，否则返回false
            @Override
            public boolean onPointClick(MultiPointItem pointItem) {
                String object = (String) pointItem.getObject();
                Log.e("point", object);
                return false;
            }
        };
        // 绑定海量点点击事件
        aMap.setOnMultiPointClickListener(multiPointClickListener);
    }

    private void initData() {

        LatLng latLng1 = new LatLng((double) 28.208663, (double) 112.913565);
        LatLng latLng2 = new LatLng((double) 28.206535, (double) 112.90216);
        LatLng latLng3 = new LatLng((double) 28.210941, (double) 112.912213);

        CameraInfo cameraInfo1 = new CameraInfo();
        cameraInfo1.setId(1);
        cameraInfo1.setAddress("湖南省长沙市岳麓区玉兰路433号长沙西中心");
        cameraInfo1.setLatLng(latLng1);
        cameraInfo1.setName("张三");
        cameraInfo1.setTel("13124562458");
        cameraInfo1.setOrientation("向南");
        List<String> images1 = new ArrayList<>();
        images1.add("http://k.zol-img.com.cn/sjbbs/7692/a7691515_s.jpg");
        images1.add("http://pic37.nipic.com/20140113/8800276_184927469000_2.png");
        images1.add("http://pic18.nipic.com/20120204/8339340_144203764154_2.jpg");
        images1.add("http://img3.imgtn.bdimg.com/it/u=4249265489,2863718384&fm=26&gp=0.jpg");
        cameraInfo1.setImages(images1);
        cameraInfos.add(cameraInfo1);

        CameraInfo cameraInfo2 = new CameraInfo();
        cameraInfo2.setId(2);
        cameraInfo2.setAddress("长沙航天医院");
        cameraInfo2.setLatLng(latLng2);
        cameraInfo2.setName("李四");
        cameraInfo2.setTel("13124562458");
        cameraInfo2.setOrientation("向南");
        cameraInfo2.setImages(images1);
        cameraInfos.add(cameraInfo2);

        CameraInfo cameraInfo3 = new CameraInfo();
        cameraInfo3.setId(3);
        cameraInfo3.setAddress("长沙汽车西站四合院");
        cameraInfo3.setLatLng(latLng3);
        cameraInfo3.setName("王五");
        cameraInfo3.setTel("13124562458");
        cameraInfo3.setOrientation("向南");
        cameraInfo3.setImages(images1);
        cameraInfos.add(cameraInfo3);

        drawPoint(latLng1,cameraInfo1);
        drawPoint(latLng2,cameraInfo2);
        drawPoint(latLng3,cameraInfo3);
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
                aMap.clear();
                break;
            case R.id.btn_update:
                Intent intent1 = new Intent(this,UpdateInfoActivity.class);
                intent1.putExtra("cameraInfo",findCameraInfo(cameraInfos,cameraInfoId));
                startActivityForResult(intent1,Constants.REQUEST_INFO_UPDATE);
                break;
            case R.id.btn_image:
                Intent intent = new Intent(this,ImagePageActivity.class);
                intent.putExtra("cameraInfo",findCameraInfo(cameraInfos,cameraInfoId));
                startActivityForResult(intent,Constants.REQUEST_INFO_IMAGE);
                break;
        }
    }

    private CameraInfo findCameraInfo(List<CameraInfo> cameraInfos, int cameraInfoId) {
        for(CameraInfo cameraInfo : cameraInfos) {
            if(cameraInfoId == cameraInfo.getId()) {
                return cameraInfo;
            }
        }
        return null;
    }

    private int findCameraInfoIndex(List<CameraInfo> cameraInfos, int cameraInfoId) {
        for(CameraInfo cameraInfo : cameraInfos) {
            if(cameraInfoId == cameraInfo.getId()) {
                return cameraInfos.indexOf(cameraInfo);
            }
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_INFO_UPDATE) {
                Toast.makeText(getApplicationContext(),findCameraInfoIndex(cameraInfos,cameraInfoId) + "",Toast.LENGTH_SHORT).show();
                markers.get(findCameraInfoIndex(cameraInfos,cameraInfoId)).remove();
                addMarker(cameraInfos.get(findCameraInfoIndex(cameraInfos,cameraInfoId)).getLatLng(),R.mipmap.camera_blue, cameraInfos.get(findCameraInfoIndex(cameraInfos,cameraInfoId)),checkMarkers);
            }
        }
    }

    private void drawPoint(LatLng latLng,CameraInfo cameraInfo) {
        addMarker(latLng,R.mipmap.camera_yellow, cameraInfo,markers);
    }

    private void addMarker(LatLng latLng,int markerIcon, CameraInfo cameraInfo,List<Marker> markerList) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.draggable(true);
        markerOption.setFlat(true);

        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),markerIcon)));
        Marker marker = aMap.addMarker(markerOption);
        marker.setObject(cameraInfo);
        markerList.add(marker);
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

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        //关联布局
        View view = LayoutInflater.from(this).inflate(R.layout.layout_info_item, null);
        TextView name = (TextView) view.findViewById(R.id.info_name);
        TextView address = (TextView) view.findViewById(R.id.info_address);
        TextView tel = (TextView) view.findViewById(R.id.info_tel);
        TextView orientation = (TextView) view.findViewById(R.id.info_orientation);

        Button update = view.findViewById(R.id.btn_update);
        Button image = view.findViewById(R.id.btn_image);

        update.setOnClickListener(this);
        image.setOnClickListener(this);

        CameraInfo cameraInfo = (CameraInfo) marker.getObject();
        name.setText(cameraInfo.getName());
        address.setText(cameraInfo.getAddress());
        tel.setText(cameraInfo.getTel());
        orientation.setText(cameraInfo.getOrientation());
        cameraInfoId = cameraInfo.getId();
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
