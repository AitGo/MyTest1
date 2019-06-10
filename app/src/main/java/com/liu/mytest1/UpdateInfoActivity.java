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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.liu.mytest1.adapter.ImageListAdapter;
import com.liu.mytest1.diagnose.CameraInfo;
import com.liu.mytest1.diagnose.ImageBean;
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
public class UpdateInfoActivity extends Activity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private RecyclerView rv_image;
    private Button btn_back,btn_confirm;
    private EditText et_address,et_name,et_tel,et_orientation;
    private ImageListAdapter adapter;
    private List<ImageBean> imageBeans = new ArrayList<>();
    private CameraInfo cameraInfo;
    private File cameraSavePath;//拍照照片路径
    private Uri uri;//照片uri
    private String photoPath;
    private BottomListPopupView popupView;
    private int index = -1;

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);
        rv_image = findViewById(R.id.rv_image);
        btn_back = findViewById(R.id.btn_back);
        btn_confirm = findViewById(R.id.btn_confirm);
        et_address = findViewById(R.id.et_address);
        et_name = findViewById(R.id.et_name);
        et_tel = findViewById(R.id.et_tel);
        et_orientation = findViewById(R.id.et_orientation);

        btn_back.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

        cameraInfo = (CameraInfo) getIntent().getParcelableExtra("cameraInfo");

        et_address.setText(cameraInfo.getAddress());
        et_name.setText(cameraInfo.getName());
        et_tel.setText(cameraInfo.getTel());
        et_orientation.setText(cameraInfo.getOrientation());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_image.setLayoutManager(layoutManager);
        ImageBean bean = new ImageBean();
        imageBeans = bean.cameraInfo2ImageBean(cameraInfo);
        adapter = new ImageListAdapter(imageBeans);
        rv_image.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                index = position;
                if(imageBeans.get(position).getItemType() == ImageBean.IMAGE) {
                    //修改已经存在的照片
                    popupView.show();
                }else if(imageBeans.get(position).getItemType() == ImageBean.ADD) {
                    //新增照片
                    popupView.show();
                }
            }
        });
        getPermission();
        initDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_confirm:
                Intent intent = getIntent();
//                intent.putExtra("cameraInfoId",cameraInfos);
                setResult(Activity.RESULT_OK, intent);
                finish();
                finish();
                break;
        }
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

        if(index != -1) {
            if(index != imageBeans.size()-1) {
                //修改照片
                Toast.makeText(getApplicationContext(),"修改照片" + index,Toast.LENGTH_LONG).show();

            }else {
                //新增照片
                Toast.makeText(getApplicationContext(),"新增照片",Toast.LENGTH_LONG).show();

            }
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
