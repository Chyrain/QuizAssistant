package com.chyrain.quizassistant.uiframe;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.update.VersionInfo;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.view.CustomAlertDialog;
import com.chyrain.quizassistant.view.SystemBarTintManager;
import com.tencent.android.tpush.XGPushManager;

import org.simple.eventbus.EventBus;

import java.io.File;

import me.leolin.shortcutbadger.ShortcutBadger;


public abstract class BaseActivity extends AppCompatActivity {
    protected Toast mToast;
    protected CustomAlertDialog mCustomAlertDialog;
    protected boolean isForeground = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        V5Application.activityCreateStatistics(this);
        // 注册对象
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        XGPushManager.onActivityStarted(this);

        // 清除appicon红点
        Config.getConfig(this).saveInt("app_badge_count", 0);
        ShortcutBadger.removeCount(getApplicationContext());
        XGPushManager.cancelAllNotifaction(this);

        isForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        XGPushManager.onActivityStoped(this);

        isForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        V5Application.activityResumeStatistics(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        V5Application.activityPauseStatistics(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        V5Application.activityDestroyStatistics(this);
        // 注销对象
        EventBus.getDefault().unregister(this);
    }

    protected void gotoWebViewActivity(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putInt("title", 0);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    protected void gotoWebViewActivity(String url, int titleId) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putInt("title", titleId);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void gotoActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void gotoActivity(Intent intent) {
        startActivity(intent);
    }

    public void gotoActivityAndFinishThis(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        finish();
    }

    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    public void ShowToast(final String text) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = Toast.makeText(getApplicationContext(), text,
                                Toast.LENGTH_LONG);
                    } else {
                        mToast.setText(text);
                    }
                    mToast.show();
                }
            });

        }
    }

    public void ShowToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), resId,
                            Toast.LENGTH_LONG);
                } else {
                    mToast.setText(resId);
                }
                mToast.show();
            }
        });
    }

    public void ShowShortToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), resId,
                            Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(resId);
                }
                mToast.show();
            }
        });
    }

    public void ShowShortToast(final String text) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseActivity.this.getApplicationContext(), text,
                            Toast.LENGTH_SHORT);
                } else {
                    mToast.setText(text);
                }
                mToast.show();
            }
        });
    }

    protected void AlertDialogShow() {
        if (mCustomAlertDialog != null && this.isForeground) {
            mCustomAlertDialog.show();
        }
    }

//    public void showAlertDialog(int contentId, View.OnClickListener positiveBtnListener, View.OnClickListener negativeBtnListener) {
//        dismissAlertDialog();
//        mCustomAlertDialog =
//                new CustomAlertDialog(this).builder()
//                        .setTitle("提示")
//                        .setMsg(contentId)
//                        .setCancelable(false)
//                        .setPositiveButton("确定", positiveBtnListener)
//                        .setNegativeButton("取消", negativeBtnListener);
//        AlertDialogShow();
//    }
//
//    public void showAlertDialog(int contentId) {
//        dismissAlertDialog();
//        mCustomAlertDialog =
//                new CustomAlertDialog(this).builder()
//                        .setTitle("提示")
//                        .setMsg(contentId)
//                        .setCancelable(false)
//                        .setNegativeButton("确定", null);
//        AlertDialogShow();
//    }
//
//    public void showAlertDialog(int contentId, View.OnClickListener negativeListener) {
//        dismissAlertDialog();
//        mCustomAlertDialog =
//                new CustomAlertDialog(this).builder()
//                        .setTitle("提示")
//                        .setMsg(contentId)
//                        .setCancelable(false)
//                        .setNegativeButton("确定", negativeListener);
//        AlertDialogShow();
//    }

    public void dismissAlertDialog() {
        if (mCustomAlertDialog != null && mCustomAlertDialog.isShowing()) {
            mCustomAlertDialog.dismiss();
            mCustomAlertDialog = null;
        }
    }

    @TargetApi(21)
    public void setStatusbarColor(int color) { // 显示颜色为window背景色
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            //getWindow().setStatusBarColor(colorBurn(color)); // 加深
            getWindow().setStatusBarColor(color); // 不加深
        }
    }

    @TargetApi(21)
    public void setNavigationBarColor(int color) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(color);
        }
    }

    public void setWindowBackground(int color) {
        //getWindow().getDecorView().setBackgroundColor(color);
        // [修改]不设置窗口背景，改为默认状态栏遮罩
        if (Build.VERSION.SDK_INT >= 21) {
            //getWindow().setBackgroundDrawable(new ColorDrawable(color));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
        }
    }

    /**
     * 提示更新
     */
    protected void alertUpdateInfo(final VersionInfo vInfo) {
        if (!vInfo.isCheckManual() && isIgnoreVersion(vInfo)) { // 不提示自动更新
            Logger.d("ActivityBase", "已忽略该版：" + vInfo.getVersion());
            return;
        }
        dismissAlertDialog();
        // [修改]显示确认更新对话框
        String title = TextUtils.isEmpty(vInfo.getDisplayTitle()) ? "【版本更新（" + vInfo.getVersion() + "）】" : vInfo.getDisplayTitle();
        mCustomAlertDialog = new CustomAlertDialog(this).builder()
                .setTitle(title)
                .setMsg(vInfo.getDisplayMessage())
//			.setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                .setCancelable(false);
//        if (vInfo.getLevel() < 4) { // 添加"忽略该版"选项
//            CheckBox ignoreCB = new CheckBox(this);
//            ignoreCB.setText(R.string.ignore);
//            ignoreCB.setTextColor(0xFF000000);
//            ignoreCB.setButtonDrawable(R.drawable.update_button_check_selector);
//            ignoreCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        Config.getConfig(getApplicationContext()).saveBoolean(vInfo.getVersion(), true);
//                    } else {
//                        Config.getConfig(getApplicationContext()).remove(vInfo.getVersion());
//                    }
//                }
//            });
//            if (isIgnoreVersion(vInfo)) {
//                ignoreCB.setChecked(true);
//            }
//            mCustomAlertDialog.setView(ignoreCB);
//        }

        String updateBtn = "下载更新";
        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "quizassistant");
        File apkfile = new File(filePath, vInfo.getApkName());
        if (apkfile.exists()) { // 已存在安装包，直接提示安装
            updateBtn = "安装（已下载）";
        }
        if (vInfo.getLevel() == 5) { // 第5等级的level说明更新很重要，解决前一版本的重大bug
            mCustomAlertDialog.setNegativeButton(updateBtn, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 允许下载
                    Intent i = new Intent(Config.ACTION_ON_UPDATE);
                    i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_DOWNLOAD);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                }
            });
        } else {
            mCustomAlertDialog.setPositiveButton(updateBtn, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 允许下载
                    Intent i = new Intent(Config.ACTION_ON_UPDATE);
                    i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_DOWNLOAD);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                }
            }).setNegativeButton("下次再说", new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 不下载，下次再试
                    Intent i = new Intent(Config.ACTION_ON_UPDATE);
                    i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_CANCEL);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                }
            });
        }
        AlertDialogShow();
    }
    /**
     * 是否忽略的版本
     * @param vInfo VersionInfo
     * @return boolean
     */
    protected boolean isIgnoreVersion(VersionInfo vInfo) {
        return vInfo.getLevel() < 4 && Config.getConfig(this).readBoolean(vInfo.getVersion());
    }
}
