package com.chyrain.quizassistant.job;

import com.chyrain.quizassistant.service.IStatusBarNotification;
import com.chyrain.quizassistant.service.WxBotService;

import android.view.accessibility.AccessibilityEvent;

/**
 * <p>Created 16/1/16 上午12:32.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public interface AccessbilityJob {
    String getTargetPackageName();
    void onCreateJob(WxBotService service);
    void onReceiveJob(AccessibilityEvent event);
    void onStopJob();
    void onNotificationPosted(IStatusBarNotification service);
    boolean isEnable();
    void onEnableChange(boolean enable);
}
