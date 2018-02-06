package com.chyrain.quizassistant.uiframe;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.util.DeviceUtil;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.Util;

import abc.abc.abc.AdManager;
import abc.abc.abc.nm.sp.SplashViewSettings;
import abc.abc.abc.nm.sp.SpotListener;
import abc.abc.abc.nm.sp.SpotManager;
import abc.abc.abc.nm.sp.SpotRequestListener;
import abc.abc.abc.onlineconfig.OnlineConfigCallBack;

public class SplashActivity extends BaseActivity {

    private Handler mHandler;
    private ViewGroup splashContainer;
    private Runnable callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(false);
        }

        if (android.os.Build.VERSION.SDK_INT >= 23){ // Build.VERSION_CODES.M
            //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            if (DeviceUtil.isMIUI()) {
                DeviceUtil.setStatusBarDarkModeOfMIUI(false, this);
            } else if (DeviceUtil.isFlyme()) {
                DeviceUtil.setStatusBarDarkIconOfFlyme(getWindow(), false);
            }
        }

        //底部虚拟按钮背景
        setNavigationBarColor(Util.getColor(R.color.colorPrimary));

		/*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        splashContainer = (ViewGroup) findViewById(R.id.splash_container);

        mHandler = new Handler();
        callback = new Runnable() {
            @Override
            public void run() {
                gotoActivityAndFinishThis(MainActivity.class);
            }
        };

        Logger.i("AdManager", "enable AD:" + Config.getConfig(this).isEnableAd());
        // 广告
        AdManager.getInstance(this).init("acaecce79c2609f5", "afad7c640ff80597", true);
        if (Config.getConfig(this).isEnableAd()) {
            mHandler.postDelayed(callback, 4000);

            // 预加载插屏广告
            SpotManager.getInstance(this).requestSpot(new SpotRequestListener() {
                @Override
                public void onRequestSuccess() {
                    Logger.i("SplashActivity", "SpotManager.onRequestSuccess");
                    //Config.getConfig(SplashActivity.this).saveInt("SpotRequest", 1);
                }

                @Override
                public void onRequestFailed(int i) {
                    Logger.e("SplashActivity", "SpotManager.onRequestFailed:" + i);
                }
            });

            // 开屏广告
            final SplashViewSettings splashViewSettings = new SplashViewSettings();
            splashViewSettings.setAutoJumpToTargetWhenShowFailed(true);
            splashViewSettings.setTargetClass(MainActivity.class);
            // 使用默认布局参数
            splashViewSettings.setSplashViewContainer(splashContainer);
//        // 使用自定义布局参数
//        splashViewSettings.setSplashViewContainer(ViewGroup splashViewContainer,
//                ViewGroup.LayoutParams splashViewLayoutParams);
            SpotManager.getInstance(this).showSplash(this, splashViewSettings, new SpotListener() {
                @Override
                public void onShowSuccess() {
                    Logger.i("SplashActivity", "SpotManager.onShowSuccess");
                    // 使用默认布局参数
                    splashContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onShowFailed(int i) {
                    Logger.e("SplashActivity", "SpotManager.onShowFailed:" + i);
                }

                @Override
                public void onSpotClosed() {
                    Logger.i("SplashActivity", "SpotManager.onSpotClosed");

                }

                @Override
                public void onSpotClicked(boolean b) {
                    Logger.e("SplashActivity", "SpotManager.onSpotClicked:" + b);
                }
            });
        } else {
            mHandler.postDelayed(callback, 1000);
        }

        // 在线参数(是否显示广告key:enable_ab)
        AdManager.getInstance(this).asyncGetOnlineConfig("enableAd", new OnlineConfigCallBack() {
            @Override
            public void onGetOnlineConfigSuccessful(String key, String value) {
                // 获取在线参数成功
                Logger.i("", "获取在线参数成功:" + key + "->" + value);
                if (key != null) {
                    boolean ad = Boolean.valueOf(value);
                    Config.getConfig(getApplicationContext()).setEnableAd(ad);
                }
            }

            @Override
            public void onGetOnlineConfigFailed(String key) {
                // 获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
                Logger.e("", "获取在线参数失败:" + key);
            }
        });

//        // 插屏广告
//        SpotManager.getInstance(this).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);
//        // SpotManager.ANIMATION_TYPE_ADVANCED
//        SpotManager.getInstance(this).setAnimationType(SpotManager.ANIMATION_TYPE_ADVANCED);
//        SpotManager.getInstance(this).showSpot(this, new SpotListener() {
//            @Override
//            public void onShowSuccess() {
//                Logger.i("SplashActivity", "SpotManager.onShowSuccess");
//            }
//
//            @Override
//            public void onShowFailed(int i) {
//                Logger.e("SplashActivity", "SpotManager.onShowFailed:" + i);
//            }
//
//            @Override
//            public void onSpotClosed() {
//                Logger.i("SplashActivity", "SpotManager.onSpotClosed");
//
//            }
//
//            @Override
//            public void onSpotClicked(boolean b) {
//                Logger.e("SplashActivity", "SpotManager.onSpotClicked:" + b);
//            }
//        });
    }

//    @Override
//    public void onBackPressed() {
//        // 如果有需要，可以点击后退关闭插播广告。
//        if (SpotManager.getInstance(this).isSpotShowing()) {
//            SpotManager.getInstance(this).hideSpot();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        // 插屏广告
//        SpotManager.getInstance(this).onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // 插屏广告
//        SpotManager.getInstance(this).onStop();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 开屏展示界面的 onDestroy() 回调方法中调用
        SpotManager.getInstance(this).onDestroy();
//        // 插屏广告
//        SpotManager.getInstance(this).onDestroy();
        mHandler.removeCallbacks(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
