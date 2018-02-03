/*
 * Copyright (c) 2017. V5KF.COM. All rights reserved.
 * Created by Chyrain on 17-10-24 下午4:09
 * Email: chyrain_v5kf@qq.com
 *
 * File: DeviceUtil.java
 * Last modified 17-10-24 下午4:09
 */

package com.chyrain.quizassistant.util;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 设备判断和与设备相关的接口处理
 * @author chyrain
 *
 */
public class DeviceUtil {
	public static final int MiPush = 1;
	public static final int XGPush = 2;
	public static final int XGPush_HMS = 3;
	public static final int XGPush_MeiZu = 4;
	public static final int XGPush_MiPush = 5;
	private static final String KEY_EMUI_VERSION_CODE = "ro.build.hw_emui_api_level";
	private static int usePushType = 0; // 1 miPush 2 XGPush （ 3 HMS 4 MeiZu 5 MiPush）
	
	public static int usePush() {
		return usePushType;
	}
	public static void setUsePush(int use) {
		usePushType = use;
	}
	
	public static boolean isMIUI() {
//		if (BuildConfig.DEBUG) {
//			return true;
//		} else 
			return android.os.Build.MODEL.toUpperCase(Locale.getDefault()).contains("MIUI") ||
				android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("xiaomi");
	}
	public static boolean isFlyme() {
		if (android.os.Build.MODEL.toLowerCase(Locale.getDefault()).contains("flyme")) {
			return true;
		}
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return method != null;
		} catch (final Exception e) {
		}
		/* 获取魅族系统操作版本标识*/
		String meizuFlymeOSFlag  = getSystemProperty("ro.build.display.id","");
		if (TextUtils.isEmpty(meizuFlymeOSFlag)){
			return false;
		}else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")){
			return  true;
		}else {
			return false;
		}
	}
	
	public static boolean isEMUI() {
		try {
        //BuildProperties 是一个工具类，下面会给出代码
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_EMUI_VERSION_CODE, null) != null;
        } catch (final IOException e) {
            return false;
        }
//		return android.os.Build.MODEL.toLowerCase(Locale.getDefault()).contains("emui");
	}

	/**
	 *   获取系统属性
	 * <h3>Version</h3> 1.0
	 * <h3>CreateTime</h3> 2016/6/18,9:35
	 * <h3>UpdateTime</h3> 2016/6/18,9:35
	 * <h3>CreateAuthor</h3> vera
	 * <h3>UpdateAuthor</h3>
	 * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
	 * @param key  ro.build.display.id
	 * @param defaultValue 默认值
	 * @return 系统操作版本标识
	 */
	private static String getSystemProperty(String key, String defaultValue) {
		try {
			Class<?> clz = Class.forName("android.os.SystemProperties");
			Method get = clz.getMethod("get", String.class, String.class);
			return (String)get.invoke(clz, key, defaultValue);
		} catch (ClassNotFoundException e) {
			Logger.e("DeviceUtil", "SystemUtil=================>"+e.getMessage());
			return null;
		} catch (NoSuchMethodException e) {
			Logger.e("DeviceUtil", "SystemUtil=================>"+e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			Logger.e("DeviceUtil", "SystemUtil=================>"+e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			Logger.e("DeviceUtil", "SystemUtil=================>"+e.getMessage());
			return null;
		} catch (InvocationTargetException e) {
			Logger.e("DeviceUtil", "SystemUtil=================>"+e.getMessage());
			return null;
		}
	}
	
	/**
	 * 小米设置状态栏字体颜色
	 * @param darkmode
	 * @param activity
	 */
	public static void setStatusBarDarkModeOfMIUI(boolean darkmode, Activity activity) {
	    Class<? extends Window> clazz = activity.getWindow().getClass();
	    try {
	        int darkModeFlag = 0;
	        Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
	        Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
	        darkModeFlag = field.getInt(layoutParams);
	        Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
	        extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
	    } catch (Exception e) {
	    	Logger.e("MIUI", "setStatusBarDarkIcon: failed");
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 魅族设置状态栏字体颜色
	 * @param window
	 * @param dark
	 * @return
	 */
	public static boolean setStatusBarDarkIconOfFlyme(Window window, boolean dark) {
	    boolean result = false;
	    if (window != null) {
	        try {
	            WindowManager.LayoutParams lp = window.getAttributes();
	            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
	            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
	            darkFlag.setAccessible(true);
	            meizuFlags.setAccessible(true);
	            int bit = darkFlag.getInt(null);
	            int value = meizuFlags.getInt(lp);
	            if (dark) {
	                value |= bit;
	            } else {
	                value &= ~bit;
	            }
	            meizuFlags.setInt(lp, value);
	            window.setAttributes(lp);
	            result = true;
	        } catch (Exception e) {
	            Logger.e("MeiZu", "setStatusBarDarkIcon: failed");
	        }
	    }
	    return result;
	}
}
