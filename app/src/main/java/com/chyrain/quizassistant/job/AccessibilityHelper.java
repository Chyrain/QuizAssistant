package com.chyrain.quizassistant.job;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import com.chyrain.quizassistant.util.Logger;

import java.lang.reflect.Field;
import java.util.List;

/**
 * <p>Created 16/2/4 上午9:49.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public final class AccessibilityHelper {

    private AccessibilityHelper() {}

    /** 通过id查找*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if(list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    /** 通过id查找
     * @return */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
    public static List<AccessibilityNodeInfo> findNodesInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
    		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
    		if(list != null && !list.isEmpty()) {
    			return list;
    		}
    	}
    	return null;
    }

    /** 通过文本查找*/
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /** 通过关键字查找*/
    public static AccessibilityNodeInfo findNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
        for(String key : texts) {
            AccessibilityNodeInfo info = findNodeInfosByText(nodeInfo, key);
            if(info != null) {
                return info;
            }
        }
        return null;
    }

    /** 通过组件名字查找*/
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if(className.equals(node.getClassName())) {
                return node;
            }
        }
        return null;
    }

    /** 找父组件*/
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if(nodeInfo == null) {
            return null;
        }
        if(TextUtils.isEmpty(className)) {
            return null;
        }
        if(className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    public static AccessibilityNodeInfo findClickableChildNodeInfo(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return null;
        }
        if (nodeInfo.isClickable()) {
            return nodeInfo;
        }
        if (nodeInfo.getChildCount() > 0) {
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo item = nodeInfo.getChild(i);
                if (item.isClickable()) {
                    return item;
                } else {
                    item = findClickableChildNodeInfo(item);
                    if (item != null) {
                        return item;
                    }
                }
            }
        } else {
            return null;
        }
        return null;
    }

    public static void performClickChildOfNodeInfo(AccessibilityNodeInfo nodeInfo, String id) {
        if(nodeInfo == null || id == null) {
            return;
        }
        AccessibilityNodeInfo targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
        Logger.i("AccessibilityHelper", "[performClickChildOfNodeInfo] 查找点击(id=" + id + ") targetNode:" + targetNode);
        if(targetNode != null) {
            Logger.e("AccessibilityHelper", "[performClickChildOfNodeInfo] 查找点击(id=" + id + "):" + targetNode);
            AccessibilityNodeInfo clickNode = findClickableChildNodeInfo(targetNode);
            Logger.e("AccessibilityHelper", "[performClickChildOfNodeInfo] 成功点击(id=" + id + "):" + clickNode);
            performClick(clickNode);
        }
    }

    private static final Field sSourceNodeField;

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    public static long getSourceNodeId (AccessibilityNodeInfo nodeInfo) {
        if(sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    /** 返回主界面事件*/
    public static void performHome(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /** 返回事件*/
    public static void performBack(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /** 点击事件*/
    public static void performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return;
        }
        if(nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(nodeInfo.getParent());
        }
    }
}
