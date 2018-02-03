package com.chyrain.quizassistant.uiframe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.util.Logger;

public class WebViewActivity extends BaseActivity {

	private WebView mWebView;
	private String mUrl;
	private int mTitleId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        handleIntent();
        
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        if (mTitleId != 0) {
        	setTitle(mTitleId);
        }
        WebChromeClient wvcc = new WebChromeClient() {  
            @Override  
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                Logger.d("ANDROID_LAB", "TITLE=" + title);
                if (mTitleId == 0 && title != null && !title.isEmpty()) {
                	setTitle(title);  
                }
            }  
        };  
        // 设置setWebChromeClient对象  
        mWebView.setWebChromeClient(wvcc);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http")) {
                    view.loadUrl(url);
                    return true;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }
        });

        mWebView.loadUrl(mUrl);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void handleIntent() {
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		mTitleId = intent.getIntExtra("title", 0);
		if (null == mUrl || mUrl.isEmpty()) {
			Logger.w("webViewActivity", "Got null url.");
			finish();
			return;
		}
		Logger.w("webViewActivity", "Got url:" + mUrl);
	}

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_browser:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(mUrl);
                intent.setData(content_url);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
