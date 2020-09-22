package com.liu.mytest1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.liu.mytest1.utils.WordUtil;

/**
 * @创建者 ly
 * @创建时间 2020/9/22
 * @描述
 * @更新者 $
 * @更新时间 $
 * @更新描述
 */
public class WordActivity extends Activity implements View.OnClickListener {

    private Button btnOpen;
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        btnOpen = findViewById(R.id.btn_open);
        btnOpen.setOnClickListener(this);
        mWebView = findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);// 设置WebView可触摸放大缩小
        settings.setUseWideViewPort(true);

    }

    @Override
    public void onClick(View v) {
        String path = "/sdcard/test2.doc";
        // tm-extractors-0.4.jar与poi的包在编译时会冲突，二者只能同时导入一个
        WordUtil wu = new WordUtil(path);
//        Log.d(TAG, "htmlPath=" + wu.htmlPath);
        mWebView.loadUrl("file:///" + wu.htmlPath);
    }
}
