package com.chyrain.quizassistant.service;

import org.simple.eventbus.EventBus;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.util.Logger;

/**
 * <p>Created 16/2/4 下午11:16.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WxBotNotificationService extends NotificationListenerService {

    private static final String TAG = "[WxBotNotificationService]";

    private static WxBotNotificationService service;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
    	Logger.i(TAG, "WxBotNotification [onCreate]");
    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
//        if(BuildConfig.DEBUG) {
//        	Logger.d(TAG, "onNotificationPosted");
//        }
        if(!getConfig().isAgreement()) {
            return;
        }
        if(!getConfig().isEnableNotificationService()) {
            return;
        }
        WxBotService.handeNotificationPosted(new IStatusBarNotification() {
            @Override
            public String getPackageName() {
                return sbn.getPackageName();
            }

            @Override
            public Notification getNotification() {
                return sbn.getNotification();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
//        if(BuildConfig.DEBUG) {
//        	Logger.d(TAG, TAG+"onNotificationRemoved");
//        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
    public void onListenerConnected() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        Logger.i(TAG, "[WxBotNotificationService -> onListenerConnected]");
        service = this;
        //发送广播，已经连接上了
        //Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        //sendBroadcast(intent);
        EventBus.getDefault().post(this, Config.EVENT_TAG_NOTIFY_LISTENER_SERVICE_CONNECT);
        Logger.d("Service", "已连接答题助手通知栏服务");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "WxBotNotification [onDestroy]");
        service = null;
        //发送广播，已经断开
        //Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        //sendBroadcast(intent);
        EventBus.getDefault().post(this, Config.EVENT_TAG_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        Logger.d("Service", "已关闭答题助手通知栏服务");
    }

    /** 是否启动通知栏监听*/
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        return true;
    }
}
