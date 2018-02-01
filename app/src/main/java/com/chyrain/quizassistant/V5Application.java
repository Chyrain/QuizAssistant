package com.chyrain.quizassistant;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.WindowManager;

import com.chyrain.quizassistant.util.Logger;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.callback.V5InitCallback;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class V5Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在主进程设置初始化相关的内容
        if (!isMainProcess()) {
            return;
        } else {
            Logger.i("V5Application", "onCreate isMainProcess");
        }

        // Umeng
        String tag = "v5quizassistant";
// 		MobclickAgent.startWithConfigure(new AnalyticsConfig(getApplicationContext(), "579b2005e0f55a5353002023", tag));
        UmengShareConfig();
        XGConfig(tag);
        mInstance = this;

        Logger.w("V5Application", "onCreate isMainProcess V5ClientAgent.init");
        V5ClientConfig.FILE_PROVIDER = "com.chyrain.quizassistant.fileprovider"; // 设置fileprovider的authorities
        V5ClientAgent.init(this, "105723", "19cfb0800f474",  new V5InitCallback() {

            @Override
            public void onSuccess(String response) {
                // TODO Auto-generated method stub
                Logger.i("V5Application", "V5ClientAgent.init(): " + response);
            }

            @Override
            public void onFailure(String response) {
                // TODO Auto-generated method stub
                Logger.e("V5Application", "V5ClientAgent.init(): " + response);
            }
        });
    }

    private void XGConfig(String tag) {
        // 开启logcat输出，方便debug，发布时请关闭
        XGPushConfig.enableDebug(this, true);
        // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
        // 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
        // 具体可参考详细的开发指南
        // 传递的参数为ApplicationContext
        Context context = getApplicationContext();
        XGPushManager.registerPush(context, new XGIOperateCallback() {

            @Override
            public void onSuccess(Object arg0, int arg1) {
                Logger.e("MainActivity", "信鸽注册成功token：" + (String)arg0);
            }

            @Override
            public void onFail(Object arg0, int arg1, String arg2) {
                Logger.e("MainActivity", "信鸽注册失败");
            }
        });
        XGPushManager.setTag(this, tag != null ? tag : "v5wxbot");
    }

    private void UmengShareConfig() {
//        com.umeng.socialize.Config.DEBUG = true;
//        PlatformConfig.setWeixin("wx83853a593404c73d", "ffc055842dfc65f75a311612b24d1565");
//        PlatformConfig.setSinaWeibo("409092997", "7500ea126022460b0e293b1351969dc9");
//        PlatformConfig.setQQZone("1105678497", "dCT4UTfMeQlbF8aY");
//        com.umeng.socialize.Config.REDIRECT_URL = "http://sns.whalecloud.com/sina2/callback";
//        UMShareAPI.get(this);
    }

    private static V5Application mInstance;
    public static V5Application getInstance() {
        return mInstance;
    }

    public static Activity getContextActivity() {
        for (WeakReference<Activity> activitiPref : mActivitiePrefs) {
            if (activitiPref.get() != null) {
                return activitiPref.get();
            }
        }
        return null;
    }

    /**
     * 分享本应用
     * @param activity
     */
    public static void showShare(final Activity activity) {
        // 友盟分享
//        UmengShareHelper shareHelper = new UmengShareHelper(activity);
//        shareHelper.share(activity.getString(R.string.share_title),
//                String.format(Locale.getDefault(), activity.getString(R.string.share_content_fmt), getInstance().getString(R.string.app_name)),
//                Config.SHARE_IMAGE_LINK,
//                Config.APP_LINK);
    }

    /** 显示分享*/
    public static void showShare(final Activity activity, final String shareUrl) {
    }

    /** 检查更新*/
    public static void checkUpdate(Activity activity) {

    }

    /** 首个activity启动调用*/
    public static void activityStartMain(Activity activity) {

    }

    /** 每个activity生命周期里的onCreate*/
    public static void activityCreateStatistics(Activity activity) {
        addActivity(activity);
    }

    /** 每个activity生命周期里的onResume*/
    public static void activityResumeStatistics(Activity activity) {
        MobclickAgent.onPageStart(activity.getClass().getName());
        MobclickAgent.onResume(activity);
    }

    /** 每个activity生命周期里的onPause*/
    public static void activityPauseStatistics(Activity activity) {
        MobclickAgent.onPageEnd(activity.getClass().getName());
        MobclickAgent.onPause(activity);
    }

    /** 每个activity生命周期里的onDestroy*/
    public static void activityDestroyStatistics(Activity activity) {
        removeActivity(activity);
    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event) {
        MobclickAgent.onEvent(context, event);
    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event, String tag) {
        MobclickAgent.onEvent(context, event, tag);
    }

    /** 关闭除指定activity外的所有activity **/
    public static void finishActivitiesExceptOne(Activity activity) {
        for (WeakReference<Activity> activitiPref : mActivitiePrefs) {
            if (null != activitiPref.get() && activitiPref.get() != activity) {
                activitiPref.get().finish();
            }
        }
    }
    /** 关闭所有activity **/
    public static void terminate() {
        finishActivitiesExceptOne(null);
    }

    /* App所有Activity管理 */
    private static List<WeakReference<Activity>> mActivitiePrefs = new CopyOnWriteArrayList<WeakReference<Activity>>();;

    /**
     * OnCreate中调用
     * @param activity Activity
     * @return void
     */
    private static void addActivity(Activity activity) {
        mActivitiePrefs.add(new WeakReference<Activity>(activity));
    }

    /**
     * OnDestory中调用
     * @param activity Activity
     * @return void
     */
    private static void removeActivity(Activity activity) {
        for (WeakReference<Activity> activitiPref : mActivitiePrefs) {
            if (activitiPref.get() == activity) {
                mActivitiePrefs.remove(activitiPref);
            }
        }
    }

    /** 版本号 **/
    public String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    /** 是否主进程 **/
    public boolean isMainProcess() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            Logger.d("V5Application", "processInfo:" + info.processName);
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private WindowManager.LayoutParams wmParams= new WindowManager.LayoutParams();
    public WindowManager.LayoutParams getWechatWmParams() {
        return wmParams;
    }
}
