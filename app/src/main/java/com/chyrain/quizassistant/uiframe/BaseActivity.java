package com.chyrain.quizassistant.uiframe;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.view.SystemBarTintManager;
import com.tencent.android.tpush.XGPushManager;

import org.simple.eventbus.EventBus;

import me.leolin.shortcutbadger.ShortcutBadger;


public abstract class BaseActivity extends AppCompatActivity {
    protected Toast mToast;

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

        // 清除appicon红点
        Config.getConfig(this).saveInt("app_badge_count", 0);
        ShortcutBadger.removeCount(getApplicationContext());
        XGPushManager.cancelAllNotifaction(this);
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

    public void gotoActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void gotoActivity(Intent intent) {
        startActivity(intent);
    }

    public void gotoActivityAndFinishThis(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish();
    }

    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    public void ShowToast(final String text) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = Toast.makeText(getApplicationContext(), text,
                                Toast.LENGTH_LONG);
                    } else {
                        mToast.setText(text);
                    }
                    mToast.show();
                }
            });

        }
    }

    public void ShowToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), resId,
                            Toast.LENGTH_LONG);
                } else {
                    mToast.setText(resId);
                }
                mToast.show();
            }
        });
    }

    public void ShowShortToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), resId,
                            Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(resId);
                }
                mToast.show();
            }
        });
    }

    public void ShowShortToast(final String text) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), text,
                            Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(text);
                }
                mToast.show();
            }
        });
    }

    @TargetApi(21)
    public void setStatusbarColor(int color) { // 显示颜色为window背景色
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            //getWindow().setStatusBarColor(colorBurn(color)); // 加深
            getWindow().setStatusBarColor(color); // 不加深
        }
    }

    @TargetApi(21)
    public void setNavigationBarColor(int color) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(color);
        }
    }

    public void setWindowBackground(int color) {
        //getWindow().getDecorView().setBackgroundColor(color);
        // [修改]不设置窗口背景，改为默认状态栏遮罩
        if (Build.VERSION.SDK_INT >= 21) {
            //getWindow().setBackgroundDrawable(new ColorDrawable(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
        }
    }
}
