package com.chyrain.quizassistant.uiframe;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.LinearLayout;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.util.Logger;

import abc.abc.abc.nm.bn.BannerManager;
import abc.abc.abc.nm.bn.BannerViewListener;

public abstract class BaseSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        getFragmentManager().beginTransaction().add(R.id.container, getSettingsFragment()).commitAllowingStateLoss();

        if(isShowBack()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // 获取要嵌入广告条的布局
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.ll_banner);
        if (Config.getConfig(this).isEnableAd()) {
            // 广告条
            // 获取广告条
            View bannerView = BannerManager.getInstance(this)
                    .getBannerView(this, new BannerViewListener() {
                        @Override
                        public void onRequestSuccess() {
                            Logger.i("SplashActivity", "SpotManager.onRequestSuccess");
                        }

                        @Override
                        public void onSwitchBanner() {
                            Logger.i("SplashActivity", "SpotManager.onSwitchBanner");
                        }

                        @Override
                        public void onRequestFailed() {
                            Logger.e("SplashActivity", "SpotManager.onRequestFailed");
                        }
                    });
            // 将广告条加入到布局中
            bannerLayout.addView(bannerView);
        } else {
            bannerLayout.setVisibility(View.GONE);
        }

    }

    protected boolean isShowBack() {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public abstract Fragment getSettingsFragment();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 展示广告条窗口的 onDestroy() 回调方法中调用
        BannerManager.getInstance(this).onDestroy();
    }
}