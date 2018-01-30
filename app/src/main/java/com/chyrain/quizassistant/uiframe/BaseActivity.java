package com.chyrain.quizassistant.uiframe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.chyrain.quizassistant.V5Application;
import com.tencent.android.tpush.XGPushManager;

import org.simple.eventbus.EventBus;

/**
 * Created by chyrain on 21/01/2018.
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V5Application.activityCreateStatistics(this);
        // 注册对象
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        XGPushManager.onActivityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        V5Application.activityResumeStatistics(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        V5Application.activityPauseStatistics(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        V5Application.activityDestroyStatistics(this);
        // 注销对象
        EventBus.getDefault().unregister(this);
    }

    protected void gotoWebViewActivity(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putInt("title", 0);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    protected void gotoWebViewActivity(String url, int titleId) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putInt("title", titleId);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
