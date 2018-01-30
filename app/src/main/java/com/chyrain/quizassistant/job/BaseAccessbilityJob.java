package com.chyrain.quizassistant.job;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.aitask.AITask;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.service.ScreenListener;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.NotifyHelper;

/**
 * AccessibilityEvent处理任务基类
 * @author Chyrain
 * <p>Created: 2016-12-12 上午11:36:29.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://www.chyrain.com">chyrain.com</a></p>
 * @copyright <a href="http://www.v5kf.com">深圳市智客网络科技有限公司</a>
 * @edit
 *		TODO
 */
public abstract class BaseAccessbilityJob implements AccessbilityJob {
    private static final String TAG = "BaseAccessbilityJob";

//    public static final String BUTTON_CLASS_NAME = "android.widget.Button";
//    public static final String IMAGE_CLASS_NAME = "android.widget.ImageView";
//    public static final String TEXTVIEW_CLASS_NAME = "android.widget.TextView";
//    public static final String VIEWGROUP_CLASS_NAME = "android.view.ViewGroup";

    protected Handler mHandler = null;
    protected WxBotService service;

    @Override
    public void onCreateJob(WxBotService service) {
        this.service = service;
        this.mHandler = getHandler();
    }

    protected Handler getHandler() {
        if(mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    public Context getContext() {
        return service.getApplicationContext();
    }

    public Config getConfig() {
        return service.getConfig();
    }

    public WxBotService getService() {
        return service;
    }

//    protected boolean clickByText(AccessibilityNodeInfo nodeInfo, String str) {
//        if (null != nodeInfo) {
//            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(str);
//            if (null != list && list.size() > 0) {
//                AccessibilityNodeInfo node = list.get(list.size() - 1);
//                if (node.isClickable()) {
//                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                } else {
//                    AccessibilityNodeInfo parentNode = node;
//                    for (int i = 0; i < 5; i++) {
//                        if (null != parentNode) {
//                            parentNode = parentNode.getParent();
//                            if (null != parentNode && parentNode.isClickable()) {
//                                return parentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//	}
//
//	protected AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo node) {
//        if (node == null)
//            return null;
//
//        //非layout元素
//        if (node.getChildCount() == 0) {
//            if ("android.widget.Button".equals(node.getClassName())) {
//                return node;
//            } else
//                return null;
//        }
//
//        //layout元素，遍历找button
//        for (int i = 0; i < node.getChildCount(); i++) {
//            AccessibilityNodeInfo button = findOpenButton(node.getChild(i));
//            if (button != null)
//                return button;
//        }
//        return null;
//	}
}
