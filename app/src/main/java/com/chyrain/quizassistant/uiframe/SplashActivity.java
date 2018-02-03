package com.chyrain.quizassistant.uiframe;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.util.DeviceUtil;
import com.chyrain.quizassistant.util.Util;

public class SplashActivity extends BaseActivity {

    private Handler mHandler;

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

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoActivityAndFinishThis(MainActivity.class);
            }
        }, 3000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
