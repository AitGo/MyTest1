package com.liu.mytest1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.liu.mytest1.diagnose.CameraInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;


public class PolyonActivity extends Activity implements View.OnClickListener {
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
    private Button btn_edit,btn_reomve;

    private GeocodeSearch geocoderSearch;

    private File mediaStorageDir;
    private String mFilepath;

    private List<CameraInfo> allLatLngs = new ArrayList<CameraInfo>();
    private List<Marker> allMarkers = new ArrayList<>();
    private Polyline polyline;
    private boolean isEdit = false;
    private Polygon polygon = null;
    private PolygonOptions polygonOptions = new PolygonOptions();
    private int nearestLatLngIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_position = (ImageButton)findViewById(R.id.btn_position);
        btn_edit = findViewById(R.id.btn_edit);
        btn_reomve = findViewById(R.id.btn_remove);

        btn_position.setOnClickListener(this);
        btn_edit.setOnClickListener(this);
        btn_reomve.setOnClickListener(this);

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
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                allLatLngs.add(new CameraInfo(latLng,1));
                addMarker(true);
                if(allLatLngs.size() > 2) {
                    refreshPolygonOptions();
                    createAreaStyle();
                    polygon = aMap.addPolygon(polygonOptions);
                }
            }
        });

        //添加拖拽事件（必须要长按才可以拖拽）
        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //开始拖拽时，把集合里的该点删除掉
                int i = (int) marker.getObject();
                Log.e("polyon", "移除第" + i + "个");
                allLatLngs.remove(i);
                allMarkers.remove(i);
                refreshPolygonOptions();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束时，创建新点
                allLatLngs.add((int) marker.getObject(), new CameraInfo(marker.getPosition(), CameraInfo.ABLE));
                allMarkers.add((int) marker.getObject(), marker);
                //判断是否需要创建新的点
                if (!isCreateMarker(marker)) {
                    //不需要
                    //如果拖拽的是状态为0的点，则不需要创建新的点，而是替换两侧的点的坐标（注意是替换set方法）。
                    replaceTwoMarker(marker);
                    refreshPolygonOptions();
                    addMarker(true);
                    createAreaStyle();
                    aMap.addPolygon(polygonOptions);
                } else {
                    //需要
                    refreshPolygonOptions();
                    addMarker(true);
                    createAreaStyle();
                    aMap.addPolygon(polygonOptions);
                    //在拖拽点两侧添加maker
                    addTwoMarker(marker);
                    addMarker(false);
                }
            }
        });

//        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
//            @Override
//            public void onTouch(MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        float down_x = motionEvent.getX();
//                        float down_y = motionEvent.getY();
//                        Point downPoint = new Point();
//                        downPoint.set((int) down_x, (int) down_y);
//                        LatLng downLatLng = aMap.getProjection().fromScreenLocation(downPoint);
//                        nearestLatLngIndex = getNearestLatLng(downLatLng);
//                        if (nearestLatLngIndex != -1) {
//                            //说明用户想拖拽该点
//                            //开始拖拽时，把集合里的该点删除掉
//                            allLatLngs.remove(nearestLatLngIndex);
//                            refreshPolygonOptions();
//                        }
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        float up_x = motionEvent.getX();
//                        float up_y = motionEvent.getY();
//                        Point upPoint = new Point();
//                        upPoint.set((int) up_x, (int) up_y);
//                        LatLng upLatLng = aMap.getProjection().fromScreenLocation(upPoint);
//                        //拖拽结束时，创建新点
//                        allLatLngs.add(nearestLatLngIndex, new CameraInfo(upLatLng, CameraInfo.ABLE));
//                        //判断是否需要创建新的点
//                        if (!isCreateMarker(allMarkers.get(nearestLatLngIndex))) {
//                            //不需要
//                            //如果拖拽的是状态为0的点，则不需要创建新的点，而是替换两侧的点的坐标（注意是替换set方法）。
//                            replaceTwoMarker(allMarkers.get(nearestLatLngIndex));
//                            refreshPolygonOptions();
//                            addMarker(true);
//                            createAreaStyle();
//                            aMap.addPolygon(polygonOptions);
//                        } else {
//                            //需要
//                            refreshPolygonOptions();
//                            addMarker(true);
//                            createAreaStyle();
//                            aMap.addPolygon(polygonOptions);
//                            //在拖拽点两侧添加maker
//                            addTwoMarker(allMarkers.get(nearestLatLngIndex));
//                            addMarker(false);
//                        }
//                        break;
//                }
//            }
//        });

        // 设置定位监听
        aMap.setLocationSource(new LocationSource() {
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
        });

        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkersToMap();
            }
        });
// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
// 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_position:
                mlocationClient.startLocation();//启动定位
//                setaMap();
                break;
            case R.id.btn_edit:
                for(CameraInfo latLngs : allLatLngs) {
                    Log.e("latlon","(float)" + latLngs.getLatLng().latitude + "," + "(float)" + latLngs.getLatLng().longitude);
                }
                break;
            case R.id.btn_remove:
                allLatLngs.clear();
                aMap.clear();
                break;
        }
    }

    private void drawPoint(LatLng latLng) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        // markerOption.snippet("西安市：111");
        markerOption.draggable(true);
        markerOption.setFlat(true);
        aMap.addMarker(markerOption);
    }

    /**
     * 获取所有点里离该点最近的点的索引值，阈值为2，如果所有值都比2大，则表示没有最近的点(返回-1)
     *
     * @param latLng
     */
    @NonNull
    private int getNearestLatLng(LatLng latLng) {
        for (int i = 0; i < allLatLngs.size(); i++) {
            float distance = AMapUtils.calculateLineDistance(latLng, allLatLngs.get(i).getLatLng());
            Log.e(TAG, distance + "");
            if (((int) distance) < 2) {
                return i;
            }
        }
        return -1;
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
    private void checkPermissions(String... permissions) {
        //获取权限列表
        List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            //list.toarray将集合转化为数组
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }


    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        //for (循环变量类型 循环变量名称 : 要被遍历的对象)
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {      //没有授权
                showMissingPermissionDialog();              //显示提示信息
                isNeedCheck = false;
            }
        }
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("title");
        builder.setMessage("msg");

        // 拒绝, 退出应用
        builder.setNegativeButton("cancle",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton("setting",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }
    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
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
        if (isNeedCheck) {
            checkPermissions(needPermissions);
        }
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

    public void setaMap2() {
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                Log.d("aMap","onMapScreenShot");
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int status) {
                mediaStorageDir = new File( getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "aMap");
                Log.d("aMap","onSnapshotReady");
                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs()) {
                        Log.d("aMap","Failed to create directory");
                        return;
                    }
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                BitmapFactory.Options option = new BitmapFactory.Options();
                // Bitmap sampling factor, size = (Original Size)/(inSampleSize)
                option.inSampleSize = 4;
                if(null == bitmap){
                    return;
                }
                try {
                    String path = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
                    mFilepath = path;
                    Log.d("aMap","filepath1: " + mFilepath);
                    File mediaFile = new File(path);
                    FileOutputStream out = new FileOutputStream(mediaFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); //100-best quality
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("aMap","filepath2: " + mFilepath);
//                        Message msg = new Message();
//                        msg.what = 1;
//                        msg.obj = mFilepath;
//                        mHandler.sendMessage(msg);
            }
        });
    }

    public void setaMap() {
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


    /**
     * 根据总得点集合，刷新polygonOptions集合的数据
     */
    private void refreshPolygonOptions() {
        if (polygonOptions.getPoints().size() != CameraInfo.UNABLE) {
            polygonOptions.getPoints().clear();
        }
        for (int i = 0; i < allLatLngs.size(); i++) {
            polygonOptions.add(allLatLngs.get(i).getLatLng());
        }
    }

    /**
     * 判断拖拽的点松手后是否需要创建新点
     */
    private boolean isCreateMarker(Marker marker) {
        int index = (int) marker.getObject();
        if (index == 0) {
            Log.e(TAG, "需要添加两个Marker");
            return false;
        }
        if (index == allLatLngs.size() - 1) {
            if (allLatLngs.get(index - 1).getState() == CameraInfo.ABLE && allLatLngs.get(0).getState() == CameraInfo.ABLE) {
                Log.e(TAG, "需要添加两个Marker");
                return true;
            }
        }
        if (allLatLngs.get(index - 1).getState() == CameraInfo.ABLE) {
            Log.e(TAG, "需要添加两个Marker");
            return true;
        }
        Log.e(TAG, "不需要添加两个Marker");
        return false;
    }

    /**
     * 在拖拽点两侧添加maker
     */
    private void addTwoMarker(Marker marker) {
        int index = (int) marker.getObject();
        if (index == 0) {
            //判断拖拽的点两侧点的状态，如果为1，则可以新建两个点
            if (allLatLngs.get(1).getState() == CameraInfo.ABLE && allLatLngs.get(allLatLngs.size() - 1).getState() == CameraInfo.ABLE) {
                CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(0), allLatLngs.get(1));
                CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(0), allLatLngs.get(allLatLngs.size() - 1));
                allLatLngs.add(1, centerPoint1);
                allLatLngs.add(allLatLngs.size() - 1, centerPoint2);
                Log.e(TAG, "在第" + 1 + "个添加marker");
                Log.e(TAG, "在第" + (allLatLngs.size() - 1) + "个添加marker");
            }
            return;
        }
        if (index == allLatLngs.size() - 1) {
            if (allLatLngs.get(index - 1).getState() == CameraInfo.ABLE && allLatLngs.get(0).getState() == CameraInfo.ABLE) {
                CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(index - 1), allLatLngs.get(index));
                CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(index), allLatLngs.get(0));
                allLatLngs.add(index, centerPoint1);
                allLatLngs.add(index + 2, centerPoint2);
                Log.e(TAG, "在第" + index + "个添加marker");
                Log.e(TAG, "在第" + (index + 2) + "个添加marker");
                return;
            }
        }
        if (allLatLngs.get(index - 1).getState() == CameraInfo.ABLE && allLatLngs.get(index + 1).getState() == CameraInfo.ABLE) {
            CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(index - 1), allLatLngs.get(index));
            CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(index), allLatLngs.get(index + 1));
            allLatLngs.add(index, centerPoint1);
            allLatLngs.add(index + 2, centerPoint2);
            Log.e(TAG, "在第" + (index) + "个添加marker");
            Log.e(TAG, "在第" + (index + 2) + "个添加marker");
        }
    }

    /**
     * 如果拖拽的是状态为1的点，则不需要创建新的点，而是替换两侧的点的坐标（注意是替换set方法）。
     */
    private void replaceTwoMarker(Marker marker) {
        int index = (int) marker.getObject();
        if (index == 0) {
            CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(0), allLatLngs.get(2));
            CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(0), allLatLngs.get(allLatLngs.size() - 2));
            allLatLngs.set(1, centerPoint1);
            allLatLngs.set(allLatLngs.size() - 1, centerPoint2);
            Log.e(TAG, "替换第" + 1 + "个marker");
            Log.e(TAG, "替换第" + (allLatLngs.size() - 1) + "个marker");
            return;
        }
        if (index == allLatLngs.size() - 2) {
            CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(0), allLatLngs.get(index));
            CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(index), allLatLngs.get(index - 2));
            allLatLngs.set(index + 1, centerPoint1);
            allLatLngs.set(index - 1, centerPoint2);
            Log.e(TAG, "替换第" + (index + 1) + "个marker");
            Log.e(TAG, "替换第" + (index - 1) + "个marker");
            return;
        }
        CameraInfo centerPoint1 = getCenterPoint(allLatLngs.get(index - 2), allLatLngs.get(index));
        CameraInfo centerPoint2 = getCenterPoint(allLatLngs.get(index + 2), allLatLngs.get(index));
        allLatLngs.set(index - 1, centerPoint1);
        allLatLngs.set(index + 1, centerPoint2);
        Log.e(TAG, "替换第" + (index - 1) + "个marker");
        Log.e(TAG, "替换第" + (index + 1) + "个marker");
    }

    /**
     * 获取两个点的中心点坐标
     * @param latLng1
     * @param latLng2
     * @return
     */
    private CameraInfo getCenterPoint(CameraInfo latLng1, CameraInfo latLng2) {
        BigDecimal bdLL1Lat = new BigDecimal(latLng1.getLatLng().latitude);
        BigDecimal bdLL1Lng = new BigDecimal(latLng1.getLatLng().longitude);
        BigDecimal bdLL2Lat = new BigDecimal(latLng2.getLatLng().latitude);
        BigDecimal bdLL2Lng = new BigDecimal(latLng2.getLatLng().longitude);
        BigDecimal d1 = (bdLL1Lat.add(bdLL2Lat)).divide(new BigDecimal("2"));
        BigDecimal d2 = (bdLL1Lng.add(bdLL2Lng)).divide(new BigDecimal("2"));
        return new CameraInfo(new LatLng(d1.doubleValue(), d2.doubleValue()), CameraInfo.UNABLE);
    }

    /**
     * 绘制图形的颜色样式
     */
    private void createAreaStyle() {
        int strokeColor = Color.parseColor("#00FFFF");
        int fillColor = Color.parseColor("#11000000");
        for (CameraInfo cameraInfo : allLatLngs) {
            polygonOptions.add(cameraInfo.getLatLng());
        } // 设置多边形的边框颜色，32位 ARGB格式，默认为黑色
        polygonOptions.strokeColor(strokeColor); // 设置多边形的边框宽度，单位：像素
        polygonOptions.strokeWidth(10); // 设置多边形的填充颜色，32位ARGB格式
        polygonOptions.fillColor(fillColor); // 注意要加前两位的透明度 // 在地图上添加一个多边形（polygon）对象
    }

    /**
     * 添加marker
     * @param isClear
     */
    private void addMarker(boolean isClear) {
        if (isClear) {
            aMap.clear();
        }
        for (int i = 0; i < allLatLngs.size(); i++) { // 在地图上添一组图片标记（marker）对象，并设置是否改变地图状态以至于所有的marker对象都在当前地图可视区域范围内显示
            MarkerOptions options = new MarkerOptions();
            options.position(allLatLngs.get(i).getLatLng()).draggable(true).visible(true);
            Marker marker = aMap.addMarker(options);
            marker.setObject(i);
            if (allLatLngs.get(i).getState() == CameraInfo.ABLE) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.point1));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.point));
            }
            allMarkers.add(marker);
        }
    }
}
