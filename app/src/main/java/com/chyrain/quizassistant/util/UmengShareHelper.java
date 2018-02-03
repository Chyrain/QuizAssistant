package com.chyrain.quizassistant.util;

import android.app.Activity;

import com.chyrain.quizassistant.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.Map;

public class UmengShareHelper {
	
	private Activity mActivity;
	
	public UmengShareHelper(Activity activity) {
		mActivity = activity;
	}
	
	public void share(String title, String text, String imageurl, String targeturl) {
		UMImage thumb =  new UMImage(mActivity, imageurl);
		UMWeb web = new UMWeb(targeturl);
//		UMImage icon =  new UMImage(mActivity, R.mipmap.share_icon);
		web.setTitle(title);//标题
		web.setThumb(thumb);  //缩略图
		web.setDescription(text);//描述

		new ShareAction(mActivity).setDisplayList(
				SHARE_MEDIA.SINA,
				SHARE_MEDIA.QQ,
				SHARE_MEDIA.QZONE,
				SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE,
				SHARE_MEDIA.WEIXIN_FAVORITE,
				SHARE_MEDIA.EMAIL,
//				SHARE_MEDIA.SMS,
				SHARE_MEDIA.MORE)
//			.withTitle(title)
//			.withText(text)
//			.withMedia(new UMImage(mActivity, R.drawable.share_img))
//			.withMedia(new UMImage(mActivity, imageurl))
//			.withTargetUrl(targeturl)
				.withMedia(web)
				.withText(text)
//				.withMedia(thumb)
				.setCallback(umShareListener)
				.open();
	}

	public void share(String title, String text, int imageId, String targeturl) {
		UMImage thumb =  new UMImage(mActivity, imageId);
		UMWeb web = new UMWeb(targeturl);
		web.setTitle(title);//标题
		web.setThumb(thumb);  //缩略图
		web.setDescription(text);//描述

		new ShareAction(mActivity).setDisplayList(
				SHARE_MEDIA.SINA,
				SHARE_MEDIA.QQ,
				SHARE_MEDIA.QZONE,
				SHARE_MEDIA.WEIXIN,
				SHARE_MEDIA.WEIXIN_CIRCLE,
				SHARE_MEDIA.WEIXIN_FAVORITE,
				SHARE_MEDIA.EMAIL,
//				SHARE_MEDIA.SMS,
				SHARE_MEDIA.MORE)
				.withMedia(web)
				.withText(text)
//				.withMedia(thumb)
				.setCallback(umShareListener)
				.open();
	}

	
	private UMShareListener umShareListener = new UMShareListener() {

		@Override
		public void onStart(SHARE_MEDIA platform) {
			//分享开始的回调，可以用来处理等待框，或相关的文字提示
			Logger.w("Umeng", " 开始分享");
		}

        @Override
        public void onResult(SHARE_MEDIA platform) {
            Logger.i("plat","platform"+platform);
            if(platform.name().equals("WEIXIN_FAVORITE")){
            	Logger.d("Umeng", " 收藏成功啦");
            }else{
            	Logger.d("Umeng", " 分享成功啦");
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
        	Logger.e("Umeng", " 分享失败啦");
            if(t!=null){
            	Logger.d("throw","throw:"+t.getMessage());
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
        	Logger.w("Umeng", " 分享取消了");
        }
    };

	UMAuthListener authListener = new UMAuthListener() {
		@Override
		public void onStart(SHARE_MEDIA platform) {
			//授权开始的回调，可以用来处理等待框，或相关的文字提示
			Logger.w("Umeng", "Auth onStart");
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
			Logger.w("Umeng", "Auth onComplete");
		}

		@Override
		public void onError(SHARE_MEDIA platform, int action, Throwable t) {
			Logger.w("Umeng", "Auth onError");
		}

		@Override
		public void onCancel(SHARE_MEDIA platform, int action) {
			Logger.w("Umeng", "Auth onCancel");
		}
	};
}
