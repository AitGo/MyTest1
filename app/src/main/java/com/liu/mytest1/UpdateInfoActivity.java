package com.liu.mytest1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.liu.mytest1.adapter.ImageListAdapter;
import com.liu.mytest1.adapter.ImagePageAdapter;
import com.liu.mytest1.utils.getPhotoFromPhotoAlbum;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.BottomListPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @创建者 ly
 * @创建时间 2019/5/29
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class UpdateInfoActivity extends Activity implements EasyPermissions.PermissionCallbacks{

    private RecyclerView rv_image;
    private ImageListAdapter adapter;
    private List<CameraInfo> cameraInfos = new ArrayList<>();
    private File cameraSavePath;//拍照照片路径
    private Uri uri;//照片uri
    private String photoPath;
    private BottomListPopupView popupView;

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        rv_image = findViewById(R.id.rv_image);

        cameraInfos.add(new CameraInfo(CameraInfo.IMAGE));
        cameraInfos.add(new CameraInfo(CameraInfo.IMAGE));
        cameraInfos.add(new CameraInfo(CameraInfo.IMAGE));
        cameraInfos.add(new CameraInfo(CameraInfo.IMAGE));
        cameraInfos.add(new CameraInfo(CameraInfo.IMAGE));
        cameraInfos.add(new CameraInfo(CameraInfo.ADD));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_image.setLayoutManager(layoutManager);
        adapter = new ImageListAdapter(cameraInfos);
        rv_image.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(getApplicationContext(),cameraInfos.get(position).getItemType() +"",Toast.LENGTH_LONG).show();
                if(cameraInfos.get(position).getItemType() == CameraInfo.IMAGE) {
                    //修改已经存在的照片
                    popupView.show();
                }else if(cameraInfos.get(position).getItemType() == CameraInfo.ADD) {
                    //新增照片
                    popupView.show();
                }
            }
        });
        getPermission();
        initDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            Log.d("拍照返回图片路径:", photoPath);
//            Glide.with(this).load(photoPath).into(ivTest);
        }else if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Log.d("相册返回图片路径:", photoPath);
//            Glide.with(this).load(photoPath).into(ivTest);
        }
    }

    private void initDialog() {
        // 这种弹窗从 1.0.0版本开始实现了优雅的手势交互和智能嵌套滚动
        popupView = new XPopup.Builder(this)
                .asBottomList("请选择一项", new String[]{"相机", "相册"},
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                if(text.equals("相机")) {
                                    goCamera();
                                }else if(text.equals("相册")) {
                                    goPhotoAlbum();
                                }
                            }
                        });
    }

    //激活相册操作
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    //激活相机操作
    private void goCamera() {
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //第二个参数为 包名.fileprovider
            uri = FileProvider.getUriForFile(this, "com.example.hxd.pictest.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        this.startActivityForResult(intent, 1);
    }

    //获取权限
    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            //已经打开权限
            Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(this, "需要获取您的相册、照相使用权限", 1, permissions);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }
}
