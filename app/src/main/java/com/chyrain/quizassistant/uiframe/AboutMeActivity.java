package com.chyrain.quizassistant.uiframe;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.update.UpdateService;
import com.chyrain.quizassistant.update.VersionInfo;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.Util;
import com.tencent.android.tpush.XGPushConfig;

import java.util.Calendar;

public class AboutMeActivity extends BaseActivity {

	private TextView mAppname;
	private TextView mVersionTv; // 更新信息
	private TextView mTokenTv; // token
	private ViewGroup mHomeRl;
	private ViewGroup mUpdateRl;
	private ProgressBar mUpdateProgress;
	private CheckUpdateReceiver mUpdateReceiver;
	long[] mHits = new long[5];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		findView();
		initView();

		mUpdateReceiver = new CheckUpdateReceiver();
		/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		filter.addAction(Config.ACTION_ON_UPDATE);
		LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, filter);
		
		// show action back
		ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReceiver);
	}

	private void findView() {
		mAppname = (TextView) findViewById(R.id.id_app_name);
		mHomeRl = (ViewGroup) findViewById(R.id.layout_home);
		mUpdateRl = (ViewGroup) findViewById(R.id.layout_update);
		mVersionTv = (TextView) findViewById(R.id.id_update_tv);
		mTokenTv = (TextView) findViewById(R.id.id_token_tv);
		mUpdateProgress = (ProgressBar) findViewById(R.id.id_update_progress);
	}

	private void initView() {
		mAppname.setText(getString(R.string.app_name) + "(" +
				String.format(getString(R.string.v5_version_info), V5Application.getInstance().getVersion())
				+ ")");
		mVersionTv.setText(String.format(getString(R.string.v5_version_info), V5Application.getInstance().getVersion()));

		// 手机识别码
		String key = Util.getIMEI(this);
		if (TextUtils.isEmpty(key)) {
			key = Config.DEVICE_TOKEN;
			if (TextUtils.isEmpty(key)) {
				key = "AD1303753897" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			} else if (key.length() > 16) {
				key = key.substring(0, 16);
			}
		}
		mTokenTv.setText(key);

		findViewById(R.id.layout_me).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gotoWebViewActivity(Config.URL_ME, R.string.me);
			}
		});

		findViewById(R.id.layout_token).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(mTokenTv.getText())) {
					Util.copyText(getApplicationContext(), mTokenTv.getText().toString());
					ShowToast(R.string.copy_token_tips);
				}
			}
		});
		mTokenTv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(mTokenTv.getText())) {
					Util.copyText(getApplicationContext(), mTokenTv.getText().toString());
					ShowToast(R.string.copy_token_tips);
				}
			}
		});

		mHomeRl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotoWebViewActivity(Config.URL_ABOUT, R.string.app_name);
			}
		});

		mUpdateRl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mVersionTv.setVisibility(View.GONE);
				mUpdateProgress.setVisibility(View.VISIBLE);
				Intent i = new Intent(getApplicationContext(), UpdateService.class);
				i.putExtra("check_manual", true);
				startService(i);
			}
		});
	}

	@Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

	/****** Update Broadcast receiver ******/
	class CheckUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			if (intent.getAction().equals(Config.ACTION_ON_UPDATE)) {
				Bundle bundle = intent.getExtras();
				int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
				switch (intent_type) {
					case Config.EXTRA_TYPE_UP_ENABLE:
						// 显示确认更新对话框
						String version = bundle.getString("version");
						String displayMessage = bundle.getString("displayMessage");
						Logger.i("AboutMeActivity", "【新版特性】：" + displayMessage);

						mVersionTv.setVisibility(View.VISIBLE);
						mUpdateProgress.setVisibility(View.GONE);
						mVersionTv.setText(String.format(getString(R.string.has_new), version));

						VersionInfo versionInfo = (VersionInfo) bundle.getSerializable("versionInfo");
						alertUpdateInfo(versionInfo);
						break;

					case Config.EXTRA_TYPE_UP_NO_NEWVERSION:
						mVersionTv.setVisibility(View.VISIBLE);
						mUpdateProgress.setVisibility(View.GONE);
						mVersionTv.setText(R.string.already_new);
						break;

					case Config.EXTRA_TYPE_UP_DOWNLOAD_FINISH:
						// 显示确认安装对话框
						//showInstallConfirmDialog();
						break;

					case Config.EXTRA_TYPE_UP_FAILED: // 更新失败
						mVersionTv.setVisibility(View.VISIBLE);
						mUpdateProgress.setVisibility(View.GONE);
						mVersionTv.setText(R.string.update_failed);
						break;
				}
			}
		}
	}
}
