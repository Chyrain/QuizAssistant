package com.chyrain.quizassistant.job;

import java.util.List;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.TextView;

import com.chyrain.quizassistant.service.IStatusBarNotification;

/**
 * QQ AccessibilityEvent和通知事件接收处理
 * @author Chyrain
 * <p>Created: 2017-1-5 上午11:26:17.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://www.chyrain.com">chyrain.com</a></p>
 * Copyright <a href="http://www.v5kf.com">深圳市智客网络科技有限公司</a>
 * Edit
 *		TODO
 */
public class QQAccessbilityJob extends BaseAccessbilityJob {
	/** QQ的包名*/
    private static final String QQ_PACKAGENAME = "com.tencent.mobileqq";
    
    
	@Override
	public String getTargetPackageName() {
		// TODO Auto-generated method stub
		return QQ_PACKAGENAME;
	}

	@Override
	public void onReceiveJob(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		handleJob(event);
	}

	@Override
	public void onStopJob() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotificationPosted(IStatusBarNotification service) {
		// TODO Auto-generated method stub
		
	}

    @Override
	public boolean isEnable() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public void onEnableChange(boolean enable) {
        //
    }
	
	/****** 自动安装应用的AccessibilityEvent示例 ******/
	private int code = INSTALL;
    private static final int INSTALL = 0;
    private static final int NEXT = 1;
    private static final int FINISH = 2;
    /**
     * 页面变化回调事件
     * @param event event.getEventType() 当前事件的类型;
     *              event.getClassName() 当前类的名称;
     *              event.getSource() 当前页面中的节点信息；
     *              event.getPackageName() 事件源所在的包名
     */
    public void handleJob(AccessibilityEvent event) {
        // 事件页面节点信息不为空
        if (event.getSource() != null) {
            // 判断事件页面所在的包名，这里是自己
            if (event.getPackageName().equals(getContext().getPackageName())) {
                switch (code) {
                    case INSTALL:
                    	clickWidgetByText(event, "安装", TextView.class.getName());
                        Log.d("test=======", "安装");
                        code = NEXT;
                        break;
                    case NEXT:
                    	clickWidgetByText(event, "下一步", Button.class.getName());
                        Log.d("test=======", "下一步");
                        code = FINISH;
                        break;
                    case FINISH:
                    	clickWidgetByText(event, "完成", TextView.class.getName());
                        Log.d("test=======", "完成");
                        code = INSTALL;
                        break;
                    default:
                        break;
                }
            }
        } else {
            Log.d("test=====", "the source = null");
        }
    }

    /**
     * 模拟点击
     * @param event 事件
     * @param text 按钮文字
     * @param widgetType 按钮类型，如android.widget.Button，android.widget.TextView
     */
    private void clickWidgetByText(AccessibilityEvent event, String text, String widgetType) {
        // 事件页面节点信息不为空
        if (event.getSource() != null) {
            // 根据Text搜索所有符合条件的节点, 模糊搜索方式; 还可以通过ID来精确搜索findAccessibilityNodeInfosByViewId
            List<AccessibilityNodeInfo> stop_nodes = event.getSource().findAccessibilityNodeInfosByText(text);
            // 遍历节点
            if (stop_nodes != null && !stop_nodes.isEmpty()) {
                AccessibilityNodeInfo node;
                for (int i = 0; i < stop_nodes.size(); i++) {
                    node = stop_nodes.get(i);
                    // 判断按钮类型
                    if (node.getClassName().equals(widgetType)) {
                        // 可用则模拟点击
                        if (node.isEnabled()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
        }
    }

}
