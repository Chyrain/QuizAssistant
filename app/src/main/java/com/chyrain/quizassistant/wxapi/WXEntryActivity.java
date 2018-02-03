package com.chyrain.quizassistant.wxapi;


import java.util.Map;

import android.content.Intent;

import com.chyrain.quizassistant.util.Logger;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.weixin.view.WXCallbackActivity;


public class WXEntryActivity extends WXCallbackActivity {

//	@Override
//	protected void handleIntent(Intent intent){
//
//	    mWxHandler.setAuthListener(new UMAuthListener() {
//	        @Override
//	        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
//	            Logger.i("Umeng", "UMWXHandler auth complete");
//	        }
//
//	        @Override
//	        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
//	        	Logger.e("Umeng", "UMWXHandler auth error");
//	        }
//
//	        @Override
//	        public void onCancel(SHARE_MEDIA platform, int action) {
//	        	Logger.d("Umeng", "UMWXHandler auth cancel");
//	        }
//	    });
//	    super.handleIntent(intent);
//	}
}
