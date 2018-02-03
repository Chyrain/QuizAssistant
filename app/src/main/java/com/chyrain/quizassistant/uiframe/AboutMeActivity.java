package com.chyrain.quizassistant.uiframe;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.R;

import org.w3c.dom.Text;

public class AboutMeActivity extends BaseActivity {

	private TextView mVersionTv;
	private TextView mUpdateTv; // 更新信息
	private ViewGroup mHomeRl;
	private ViewGroup mUpdateRl;
	long[] mHits = new long[5];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		findView();
		initView();
		
		// show action back
		ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
	private void findView() {
		mVersionTv = (TextView) findViewById(R.id.id_version_tv);
		mHomeRl = (ViewGroup) findViewById(R.id.layout_home);
		mUpdateRl = (ViewGroup) findViewById(R.id.layout_update);
		mUpdateTv = (TextView) findViewById(R.id.id_update_tv);
	}

	private void initView() {
		mVersionTv.setText(String.format(getString(R.string.v5_version_info), V5Application.getInstance().getVersion()));

		findViewById(R.id.layout_me).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gotoWebViewActivity(Config.URL_ME, R.string.me);
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
				// TODO 检查更新
			}
		});
	}

	@Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
