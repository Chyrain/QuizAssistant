package com.chyrain.quizassistant.uiframe;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.job.DatiAccessbilityJob;
import com.chyrain.quizassistant.service.WxBotNotificationService;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.update.UpdateService;
import com.chyrain.quizassistant.update.VersionInfo;
import com.chyrain.quizassistant.util.BitmapUtils;
import com.chyrain.quizassistant.util.DeviceUtil;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.Util;
import com.chyrain.quizassistant.view.QuizFloatView;
import com.chyrain.quizassistant.R;
import com.tencent.android.tpush.XGPushConfig;
import com.umeng.socialize.UMShareAPI;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientConfig;

import abc.abc.abc.AdManager;
import abc.abc.abc.nm.sp.SpotManager;
import abc.abc.abc.update.AppUpdateInfo;
import abc.abc.abc.update.CheckAppUpdateCallBack;

public class MainActivity extends BaseSettingsActivity implements CheckAppUpdateCallBack {

    private static final String TAG = "MainActivity";
    /** 微信的包名*/
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    private Dialog mTipsDialog;
    private MainFragment mMainFragment;
    private Activity mActivity = this;

    // 浮动开关按钮
    private WindowManager wm = null;
    private WindowManager.LayoutParams wmParams = null;
    private QuizFloatView wFV = null;
    private boolean permitForFV = false;
    // 更新receiver
    private CheckUpdateReceiver mUpdateReceiver;
    // 免责dialog
    private AlertDialog mAgreementDialog;

    @Override
    public void onBackPressed() {
        Logger.d(TAG, "[onBackPressed]");
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V5Application.activityStartMain(this);
        Logger.w(TAG, "[onCreate] "+TAG);
        setTitle(R.string.app_name);

        String title = "8.哈撒花撒啊开大餐";
        String[] rsts = title.split("\\.");
        if (rsts.length > 0) {
            Logger.d(TAG, rsts + " success title.split(\".\"): " + rsts[0]);
        } else {
            Logger.d(TAG, rsts + " fail title.split(\".\"): " + rsts);
        }

        // 权限请求
        if(Build.VERSION.SDK_INT>=23){
            String[] mPermissionList = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SET_DEBUG_APP,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
//                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_APN_SETTINGS
            };
            ActivityCompat.requestPermissions(this,mPermissionList,123);
        }

        // 显示悬浮窗
        boolean showFloat = Config.getConfig(getApplicationContext()).isEnableFloatButton();
        if (showFloat && Config.getConfig(this).readBoolean("app_once_token")) {
            showFloat();
        }
        initReceiver();
        startUpdateService();
    }

    protected void initReceiver() {
        mUpdateReceiver = new CheckUpdateReceiver();
		/* 注册广播接收 */
        IntentFilter filter=new IntentFilter();
        filter.addAction(Config.ACTION_ON_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, filter);
    }

    /**
     * 开启友盟自动更新
     */
    protected void startUpdateService() {
//        AdManager.getInstance(this).asyncCheckAppUpdate(this);
        (new Handler(getMainLooper())).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isForeground) {
                    Intent i = new Intent(getApplicationContext(), UpdateService.class);
                    startService(i);
                }
            }
        }, 5000);
    }

    @Override
    public void onCheckAppUpdateFinish(AppUpdateInfo updateInfo) {
        if (updateInfo == null) {
            Logger.w(TAG, "[onCheckAppUpdateFinish] null");
            return;
        }
        Logger.i(TAG, "[onCheckAppUpdateFinish] AppUpdateInfo version:" + updateInfo.getVersionName()
            + " build:" + updateInfo.getVersionCode() + " tips:" + updateInfo.getUpdateTips());
        // 检查更新回调，注意，这里是在 UI 线程回调的，因此您可以直接与 UI 交互，但不可以进行长时间的操作（如在这里访问网络是不允许的）
        if (updateInfo == null || updateInfo.getUrl() == null) {
            // 当前已经是最新版本
            Logger.i(TAG, "[onCheckAppUpdateFinish] 当前已经是最新版本");
        }
        else {
            Logger.i(TAG, "[onCheckAppUpdateFinish] 有更新信息");
            // 有更新信息，开发者应该在这里实现下载新版本
            Intent i = new Intent(this, UpdateService.class);
            startService(i);
        }
    }

//    public void getRunningApp() {
//        ActivityManager am = (ActivityManager) getApplicationContext()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> processes = am
//                .getRunningAppProcesses();
//        ActivityManager.RunningAppProcessInfo processInfo = processes.get(0);
//        String appPackageName = processInfo.processName.toString();
//        for (ActivityManager.RunningAppProcessInfo info : processes) {
//            String packageName = info.processName.toString();
//            Logger.i(TAG, "getRunningApp:" + appPackageName );
//        }
//
//        Logger.e(TAG, "getRunningApp:" + appPackageName );
//    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isAgreement = Config.getConfig(this).isAgreement();
        if(!isAgreement) {
            showAgreementDialog();
        } else {
            if(WxBotService.isRunning()) {
                if(mTipsDialog != null) {
                    mTipsDialog.dismiss();
                }
            } else {
                showOpenAccessibilityServiceDialog();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.w(TAG, "[onDestroy] "+TAG);
        mTipsDialog = null;
        destroyFloatView();
        SpotManager.getInstance(this).onAppExit();
        // 打开过app记录
        Config.getConfig(this).saveBoolean("app_once_token", true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_service:
                openAccessibilityServiceSettings();
                V5Application.eventStatistics(this, "menu_service");
                return true;
//            case R.id.action_float:
//                Util.gotoPermission(this);
//                V5Application.eventStatistics(this, "menu_float");
//                break;
            case R.id.action_notify:
                openNotificationServiceSettings();
                V5Application.eventStatistics(this, "menu_notify");
                break;
            case R.id.action_share:
                showShareDialog();
                V5Application.eventStatistics(this, "menu_share");
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutMeActivity.class));
                V5Application.eventStatistics(this, "menu_about");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** 显示免责声明的对话框*/
    private void showAgreementDialog() {
        if (mAgreementDialog != null && mAgreementDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.agreement_title);
        builder.setMessage(getString(R.string.agreement_message, getString(R.string.app_name)));
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(true);
                V5Application.eventStatistics(MainActivity.this, "agreement", "true");

                // 检查辅助服务
                if(WxBotService.isRunning()) {
                    if(mTipsDialog != null) {
                        mTipsDialog.dismiss();
                    }
                } else {
                    showOpenAccessibilityServiceDialog();
                }
            }
        });
        builder.setNegativeButton("不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(false);
                V5Application.eventStatistics(MainActivity.this, "agreement", "false");
                if(mTipsDialog != null && mTipsDialog.isShowing()) {
                    mTipsDialog.dismiss();
                }
                finish();
            }
        });
        mAgreementDialog = builder.show();
    }

    /** 显示未开启辅助服务的对话框*/
    private void showOpenAccessibilityServiceDialog() {
        if(mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_service_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        mTipsDialog = builder.show();
    }

    /** 显示未开启悬浮窗权限的对话框*/
    private void showOpenOverlayPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.overlay_permission_title);
        builder.setMessage(getString(R.string.overlay_permission_message, getString(R.string.app_name)));
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.gotoPermission(mActivity);
            }
        });
        builder.show();
    }

    @Override
    public Fragment getSettingsFragment() {
        mMainFragment = new MainFragment();
        return mMainFragment;
    }

    protected void closeFloat() {
        Logger.d(TAG, "closeFloat");
        if (wFV != null) {
            wFV.setVisibility(View.GONE);
        }
    }

    protected boolean showFloat() {
        permitForFV = Util.checkOverlayPermission(mActivity);
        Logger.d(TAG, "showFloat permitForFV:" + permitForFV);
        if (permitForFV) {
            showFloatView();
            return true;
        } else {
            permitForFV = false;
            showOpenOverlayPermissionDialog();
            mMainFragment.updateShowFloatPreference();
            return false;
        }
    }

    private void showFloatView() {
        if (wFV != null) {
            wFV.setVisibility(View.VISIBLE);
        } else {
            createFloatView();
        }
    }

    private void destroyFloatView() {
        if (wm != null && wFV != null) {
            wm.removeView(wFV);
            wFV = null;
        }
    }

    private void createFloatView(){
        if (!permitForFV) {
            Toast.makeText(this, "请授予显示浮动窗口权限", Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "未授权");
            return;
        }
        if (wFV != null) {
            return;
        }
        Logger.i(TAG, "[显示浮动按钮]【createFloatView】:" + V5Application.getInstance());
        //设置LayoutParams(全局变量）相关参数
        wmParams = V5Application.getInstance().getWechatWmParams();
        wFV = new QuizFloatView(mActivity, wmParams);
        updateWFV(WxBotService.isEnable(mActivity));
        wFV.getAppIconIv().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Logger.i(TAG, "浮钮click");
                if (Config.getConfig(mActivity).isEnableWechat()) {
                    if (WxBotService.isRunning()) {
                        // 关闭服务
                        Config.getConfig(mActivity).setEnableWechat(false);
                        EventBus.getDefault().post(Boolean.FALSE, Config.EVENT_TAG_UPDATE_FLOAT_STATUS);
                        updateWFV(false);
                    } else {
                        // 进入页面
                        openSelf();
                    }
                } else {
                    // 开启服务
                    Config.getConfig(mActivity).setEnableWechat(true);
                    EventBus.getDefault().post(Boolean.TRUE, Config.EVENT_TAG_UPDATE_FLOAT_STATUS);
                    updateWFV(true);
                }
                // 更新显示
                mMainFragment.updateWechatPreference();
            }
        });
        wFV.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Logger.i(TAG, "浮钮longClick");
                openSelf();
                return true;
            }
        });
        wFV.getAppIconIv().setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Logger.i(TAG, "应用图标longClick");
                EventBus.getDefault().post(v, Config.EVENT_TAG_FLOAT_ICON_LONF_CLICK);
                return true;
            }
        });


        //获取WindowManager
        wm = (WindowManager)getApplicationContext().getSystemService(WINDOW_SERVICE);
        /**
         *以下都是WindowManager.LayoutParams的相关属性
         * 具体用途可参考SDK文档
         */
        wmParams.type = LayoutParams.TYPE_PHONE;   //设置window type
        wmParams.format= PixelFormat.RGBA_8888;   //设置图片格式，效果为背景透明

        //设置Window flag
        wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
    	/*
    	 * 下面的flags属性的效果形同“锁定”。
    	 * 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
    		wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL
    			| LayoutParams.FLAG_NOT_FOCUSABLE
    			| LayoutParams.FLAG_NOT_TOUCHABLE;
    	 */
        wmParams.gravity = Gravity.START|Gravity.TOP;   //调整悬浮窗口至左上角，便于调整坐标
        //以屏幕左上角为原点，设置x、y初始值
        wmParams.x = Util.dp2px(60, mActivity);
        wmParams.y = Util.dp2px(10, mActivity);
        //设置悬浮窗口长宽数据
        wmParams.width = wFV.getMeasuredWidth() > 80 ? wFV.getMeasuredWidth() : Util.dp2px(185, mActivity);
        wmParams.height = wFV.getMeasuredHeight() > 40 ? wFV.getMeasuredHeight() : Util.dp2px(40, mActivity);

        //显示myFloatView图像
        wm.addView(wFV, wmParams);
    }

    /** 更新浮动窗口显示状态 **/
    private void updateWFV(boolean light) {
        if (wFV == null) {
            Logger.e(TAG, "updateWFV: null wFV");
            return;
        }
        // wFV
        wFV.updateFloatEnable(light);
    }

    /** 打开本界面 **/
    private void openSelf() {
        // 模拟通知栏点击打开
        Intent intent = new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setClass(getApplication(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        try {
            pi.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    /** 打开辅助服务的设置*/
    private void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, DeviceUtil.isFlyme() ? R.string.meizu_tips : R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 打开通知栏设置*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openNotificationServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 分享*/
    private void showShareDialog() {
        V5Application.showShare(this);
    }

    /** 二维码*/
    private void showQrDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        View view = getLayoutInflater().inflate(R.layout.dialog_qr_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getString(R.string.qr_wx_id);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", id);
                clipboardManager.setPrimaryClip(clip);

                //跳到微信
                Intent wxIntent = getPackageManager().getLaunchIntentForPackage(
                        WECHAT_PACKAGENAME);
                if(wxIntent != null) {
                    try {
                        startActivity(wxIntent);
                    } catch (Exception e){}
                }

                Toast.makeText(getApplicationContext(), "已复制公众号到粘贴板", Toast.LENGTH_LONG).show();
                V5Application.eventStatistics(MainActivity.this, "copy_qr");
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    /** 显示捐赠的对话框*/
    private void showDonateDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        Toast.makeText(getApplicationContext(), "长按保存图片", Toast.LENGTH_LONG).show();
        View view = getLayoutInflater().inflate(R.layout.dialog_donate_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                File output = new File(android.os.Environment.getExternalStorageDirectory(), "chyrain_wechatpay_qr.jpg");
                if(!output.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.wechatpay_qr);
                    BitmapUtils.saveBitmap(MainActivity.this, output, bitmap);
                }
                Toast.makeText(MainActivity.this, "已保存到:" + output.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    public static class MainFragment extends BaseSettingsFragment {

        private SwitchPreference notificationPref; // 答题助手取消通知读取功能
        private SwitchPreference wechatPref;
        private SwitchPreference zscrPref;
        private SwitchPreference cddhPref;
        private SwitchPreference xiguaPref;
        private SwitchPreference huajiaoPref;
        private SwitchPreference hjsmPref;
        private SwitchPreference floatBtnPref;
        private SwitchPreference autoTrustPref;
//        private SwitchPreference showAnswerPref;
        private boolean notificationChangeByUser = true;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            addPreferencesFromResource(R.xml.main_settings);

            findPreference("KEY_INSTRUCTION").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity)getActivity()).gotoWebViewActivity(Config.INTRO_LINK, R.string.app_intro);
                    return true;
                }
            });

            // 通知监听开关
            notificationPref = (SwitchPreference) findPreference("KEY_NOTIFICATION_SERVICE_TEMP_ENABLE");
            notificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Toast.makeText(getActivity(), "该功能只支持安卓4.3以上的系统", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    boolean enable = (Boolean) newValue;
                    Logger.d(TAG, "notificationPref：" + enable);
                    Config.getConfig(getActivity()).setNotificationServiceEnable(enable);

                    if(enable && !WxBotService.isNotificationServiceRunning()) {
                        ((MainActivity)getActivity()).openNotificationServiceSettings();
                        return false;
                    }
                    V5Application.eventStatistics(getActivity(), "notify_service", String.valueOf(newValue));
                    return true;
                }
            });

            //全局开关
            wechatPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_WECHAT);
            wechatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Config.getConfig(getActivity()).setEnableWechat((Boolean) newValue);
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
//                        return false;
                    }
                    ((MainActivity)getActivity()).updateWFV((Boolean) newValue);
                    updateWechatPreference();
                    return true;
                }
            });

            //芝士超人开关
            zscrPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_ZSCR);
            zscrPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            //冲顶大会开关
            cddhPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_CDDH);
            cddhPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            //西瓜视频开关
            xiguaPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_XIGUA);
            xiguaPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            //花椒直播开关
            huajiaoPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_HUAJIAO);
            huajiaoPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            //黄金十秒开关
            hjsmPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_HJSM);
            hjsmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !WxBotService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            //浮动按钮开关
            floatBtnPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_FLOAT_BUTTON);
            floatBtnPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue) { // 开
                        return ((MainActivity)getActivity()).showFloat();
                    } else { // 关
                        ((MainActivity)getActivity()).closeFloat();
                    }
                    return true;
                }
            });

            findPreference("NOTIFY_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), NotifySettingsActivity.class));
                    return true;
                }
            });

            // KEY_AUTO_TRUST
            autoTrustPref = (SwitchPreference) findPreference(Config.KEY_AUTO_TRUST);
            autoTrustPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Config.getConfig(getActivity()).setNotificationServiceEnable((Boolean) newValue);
                    return true;
                }
            });

            // KEY_SHOW_ANSWER
//            showAnswerPref = (SwitchPreference) findPreference(Config.KEY_AUTO_TRUST);
//            showAnswerPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    Config.getConfig(getActivity()).setNotificationServiceEnable((Boolean) newValue);
//                    return true;
//                }
//            });

//            Preference preference = findPreference("KEY_FOLLOW_ME");
//            if(preference != null) {
//                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        ((MainActivity) getActivity()).showQrDialog();
//                        V5Application.eventStatistics(getActivity(), "about_author");
//                        return true;
//                    }
//                });
//            }
            findPreference("KEY_ABOUT").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), AboutMeActivity.class));
                    return true;
                }
            });

            findPreference("KEY_SHARE").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((MainActivity) getActivity()).showShareDialog();
                    return true;
                }
            });

            Preference preference = findPreference("KEY_DONATE_ME");
            if(preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((MainActivity) getActivity()).showDonateDialog();
                        V5Application.eventStatistics(getActivity(), "donate");
                        return true;
                    }
                });
            }
            findPreference("KEY_KEFU").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    V5ClientConfig config = V5ClientConfig.getInstance(getActivity());
                    // V5客服系统客户端配置
                    // config.setShowLog(true); // 显示日志，默认为true

                    /*** 客户信息设置 ***/
                    // 如果更改了用户信息，需要在设置前调用shouldUpdateUserInfo
                    // config.shouldUpdateUserInfo();
                    // 【建议】设置用户昵称
                    String token = XGPushConfig.getToken(getActivity()) != null ?
                            XGPushConfig.getToken(getActivity()).substring(0, 16) : XGPushConfig.getToken(getActivity());
                    config.setNickname("Quiz-" + token);
                    // 设置用户性别: 0-未知 1-男 2-女
//                    config.setGender(1);
                    // 【建议】设置用户头像URL
//                    config.setAvatar("http://debugimg-10013434.image.myqcloud.com/fe1382d100019cfb572b1934af3d2c04/thumbnail");
                    /**
                     *【建议】设置用户OpenId，以识别不同登录用户，不设置则默认由SDK生成，替代v1.2.0之前的uid,
                     *  openId将透传到座席端(长度32字节以内，建议使用含字母数字和下划线的字符串，尽量不用特殊字符，若含特殊字符系统会进行URL encode处理，影响最终长度和座席端获得的结果)
                     *	若您是旧版本SDK用户，只是想升级，为兼容旧版，避免客户信息改变可继续使用config.setUid，可不用openId
                     */
//                    config.setOpenId("android_sdk_test");
                    //config.setUid(uid); //【弃用】请使用setOpenId替代
                    // 设置用户VIP等级(0-5)
//                    config.setVip(0);
                    // 使用消息推送时需设置device_token:集成第三方推送(腾讯信鸽、百度云推)或自定义推送地址时设置此参数以在离开会话界面时接收推送消息
                    config.setDeviceToken(XGPushConfig.getToken(getActivity()));

//                    // [1.3.0新增]设置V5系统内置的客户基本信息，区别于setUserInfo，这是V5系统内置字段
//                    JSONObject baseInfo = new JSONObject();
//                    try {
//                        baseInfo.put("country", "中国");
//                        baseInfo.put("province", "广东");
//                        baseInfo.put("city", "深圳");
//                        baseInfo.put("language", "zh-cn");
//                        // nickname,gender,avatar,vip也可在此设置
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    config.setBaseInfo(baseInfo);

                    // 客户信息键值对，下面为示例（JSONObject）
                    JSONObject customContent = new JSONObject();
                    try {
                        customContent.put("Token", XGPushConfig.getToken(getActivity()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // 设置客户信息（自定义字段名称与值，自定义JSONObjectjian键值对，开启会话前设置，替代之前通过`setUserWillSendMessageListener`在消息中携带信息的方式，此方式更加安全便捷）
                    config.setUserInfo(customContent);
                    // 开启对话界面
                    V5ClientAgent.getInstance().startV5ChatActivity(getActivity());
                    return true;
                }
            });

//            findPreference("WECHAT_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    startActivity(new Intent(getActivity(), WechatSettingsActivity.class));
//                    return true;
//                }
//            });

            //初始状态
            updateWechatPreference();
        }

        /** 更新快速读取通知的设置 */
        public void updateNotifyPreference() {
            if(notificationPref == null) {
                return;
            }
            boolean running = WxBotService.isNotificationServiceRunning();
            boolean enable = Config.getConfig(getActivity()).isEnableNotificationService();
            Logger.i(TAG, "[updateNotifyPreference] running:"+running+" enable:"+enable);
            if( enable && running && !notificationPref.isChecked()) {
                V5Application.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
                notificationChangeByUser = false;
                notificationPref.setChecked(true);
            } else if((!enable || !running) && notificationPref.isChecked()) { //(!enable || !running)
                notificationChangeByUser = false;
                notificationPref.setChecked(false);
            }
        }

        /** 更新快速读取通知的设置*/
        public void updateWechatPreference() {
            if(wechatPref == null) {
                Logger.e(TAG, "updateWechatPreference: null wechatPref");
                return;
            }
            boolean enable = WxBotService.isEnable(getActivity());
            Logger.d(TAG, "[updateWechatPreference] enable:"+enable+" wechatPref.isChecked():"+wechatPref.isChecked());
            if(enable && !wechatPref.isChecked()) {
                V5Application.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
                notificationChangeByUser = false;
                wechatPref.setChecked(true);
            } else if(!enable && wechatPref.isChecked()) {
                notificationChangeByUser = false;
                wechatPref.setChecked(false);
            }
            if (wechatPref.isChecked()) {
                // 恢复
                zscrPref.setEnabled(true);
                cddhPref.setEnabled(true);
                xiguaPref.setEnabled(true);
                huajiaoPref.setEnabled(true);
                hjsmPref.setEnabled(true);
            } else {
                // 不可用
                zscrPref.setEnabled(false);
                cddhPref.setEnabled(false);
                xiguaPref.setEnabled(false);
                huajiaoPref.setEnabled(false);
                hjsmPref.setEnabled(false);
            }
        }

        public void updateShowFloatPreference() {
            if(floatBtnPref == null) {
                return;
            }
            boolean permit = ((MainActivity)getActivity()).permitForFV;
            boolean show = ((MainActivity)getActivity()).wFV != null
                    && ((MainActivity)getActivity()).wFV.getVisibility() == View.VISIBLE;
            if(permit && Config.getConfig(getActivity()).isEnableAutoUnlock()) {
                if(show && !floatBtnPref.isChecked()) {
                    floatBtnPref.setChecked(true);
                }
            } else {
                floatBtnPref.setChecked(false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateNotifyPreference();
            updateShowFloatPreference();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        //UMShareAPI.get(this).HandleQQError(mActivity, requestCode, umAuthListener);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        Logger.d("MainActivity", "onActivityResult intent:" + data);
        if (requestCode == Util.REQUEST_PERMISSION_SYSTEM_ALERT_WINDOW) {
            Logger.i("MainActivity", "resultCode:" + resultCode + " data:" + data);
            if (resultCode == 0) {
                permitForFV = Util.checkOverlayPermission(mActivity);
                showFloatView();
            }
            if(mMainFragment != null) {
                mMainFragment.updateShowFloatPreference();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Logger.i(TAG, "[onRequestPermissionsResult] code:" + requestCode
                + " permissions:" + permissions + " grantResults:" + grantResults);
    }

    /****** Update Broadcast receiver ******/
    class CheckUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            Logger.d("BaseLoginActivity", "[onReceive] " + intent.getAction());
            if (intent.getAction().equals(Config.ACTION_ON_UPDATE)) {
                Bundle bundle = intent.getExtras();
                int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
                switch (intent_type) {
                    case Config.EXTRA_TYPE_UP_ENABLE:
                        // 显示确认更新对话框
//				String version = bundle.getString("version");
//				String displayMessage = bundle.getString("displayMessage");
//				Logger.i(TAG, "【新版特性】：" + displayMessage);
                        VersionInfo versionInfo = (VersionInfo) bundle.getSerializable("versionInfo");
                        if (isForeground) {
                            alertUpdateInfo(versionInfo);
                        }
                        break;
                }
            }
        }
    }


    /***** event *****/

    @Subscriber(tag = Config.EVENT_TAG_IN_NOTIFY, mode=ThreadMode.MAIN)
    private void receiveNotification(String type) {
        Logger.i("event-tag", "<v5kf>EVENT_TAG_IN_NOTIFY");
        EventBus.getDefault().post(type, Config.EVENT_TAG_IN_NOTIFY); // 发送事件
    }

    @Subscriber(tag = Config.EVENT_TAG_ROBOT_SERVICE_CONNECT, mode=ThreadMode.MAIN)
    private void serviceConnect(WxBotService service) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_ROBOT_SERVICE_CONNECT");
        Toast.makeText(this, "已连接答题辅助服务", Toast.LENGTH_SHORT).show();
        if (mTipsDialog != null) {
            mTipsDialog.dismiss();
        }
        if(mMainFragment != null) {
            mMainFragment.updateWechatPreference();
            mMainFragment.updateShowFloatPreference();
            mMainFragment.updateNotifyPreference();
        }
        boolean showFloat = Config.getConfig(getApplicationContext()).isEnableFloatButton();
        if (showFloat) {
            showFloat();
        }
        updateWFV(WxBotService.isEnable(mActivity));
    }

    @Subscriber(tag = Config.EVENT_TAG_ROBOT_SERVICE_DISCONNECT, mode=ThreadMode.MAIN)
    private void serviceDisconnect(WxBotService service) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_ROBOT_SERVICE_DISCONNECT");
        Toast.makeText(this, "已关闭答题辅助服务", Toast.LENGTH_SHORT).show();
        if(mMainFragment != null) {
            mMainFragment.updateWechatPreference();
            mMainFragment.updateShowFloatPreference();
            mMainFragment.updateNotifyPreference();
        }
        showOpenAccessibilityServiceDialog();
        updateWFV(WxBotService.isEnable(mActivity));
    }

    @Subscriber(tag = Config.EVENT_TAG_NOTIFY_LISTENER_SERVICE_DISCONNECT, mode=ThreadMode.MAIN)
    private void notifyServiceDisconnect(WxBotNotificationService service) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_NOTIFY_LISTENER_SERVICE_DISCONNECT");
        Toast.makeText(getApplicationContext(), "已关闭通知栏服务", Toast.LENGTH_LONG).show();
        if(mMainFragment != null) {
            mMainFragment.updateWechatPreference();
            mMainFragment.updateShowFloatPreference();
            mMainFragment.updateNotifyPreference();
        }
    }

    @Subscriber(tag = Config.EVENT_TAG_NOTIFY_LISTENER_SERVICE_CONNECT, mode=ThreadMode.MAIN)
    private void notifyServiceConnect(WxBotNotificationService service) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_NOTIFY_LISTENER_SERVICE_CONNECT");
        Toast.makeText(getApplicationContext(), "已连接通知栏监听服务", Toast.LENGTH_LONG).show();
        if(mMainFragment != null) {
            mMainFragment.updateWechatPreference();
            mMainFragment.updateShowFloatPreference();
            mMainFragment.updateNotifyPreference();
        }
    }

    @Subscriber(tag = Config.EVENT_TAG_ACCESSBILITY_JOB_CHANGE, mode=ThreadMode.MAIN)
    private void notifyServiceConnect(DatiAccessbilityJob accessbilityJob) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_ACCESSBILITY_JOB_CHANGE key: " + accessbilityJob.getAppName()
            + " key: " + accessbilityJob.getJobKey());
//        Toast.makeText(MainActivity.this, "答题助手切换到 " + accessbilityJob.getAppName(), Toast.LENGTH_LONG).show();
        if (wFV != null) {
            wFV.updateFloatJob(accessbilityJob.getAppName(), accessbilityJob.getJobKey());
        }
    }

    @Subscriber(tag = Config.EVENT_TAG_UPDATE_QUIZ, mode=ThreadMode.MAIN)
    private void updateCurrentQuiz(QuizBean quiz) {
        Logger.i(TAG, "<v5kf>EVENT_TAG_UPDATE_QUIZ key: " + quiz);
        if (wFV != null) {
            wFV.updateFloatQuiz(quiz);
        }

//        if (Config.getConfig(MainActivity.this).isEnableShowAnswer()) {
//            // 选择
//            if (quiz != null) {
//                Toast.makeText(MainActivity.this, "推荐答案：" + quiz.getResult(), Toast.LENGTH_LONG).show();
//            }
//        }
    }
}

