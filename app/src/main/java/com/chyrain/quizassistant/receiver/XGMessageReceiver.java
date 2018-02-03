/*
 * Copyright (c) 2017. V5KF.COM. All rights reserved.
 * Created by Chyrain on 17-10-24 下午4:09
 * Email: chyrain_v5kf@qq.com
 *
 * File: XGMessageReceiver.java
 * Last modified 17-10-24 下午4:09
 */

package com.chyrain.quizassistant.receiver;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.util.DeviceUtil;
import com.chyrain.quizassistant.util.Logger;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientConfig;

import org.json.JSONException;
import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;

public class XGMessageReceiver extends XGPushBaseReceiver {
	public static final String TAG = "XGMessageReceiver";
	private static final boolean DEBUG_TOAST = false;

	private void show(Context context, String text) {
		if (DEBUG_TOAST) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}

	// 通知展示
	@Override
	public void onNotifactionShowedResult(Context context,
			XGPushShowedResult notifiShowedRlt) {
		if (context == null || notifiShowedRlt == null) {
			return;
		}
		String text = "收到消息:" + notifiShowedRlt.toString();
		// 获取自定义key-value
		String customContent = notifiShowedRlt.getCustomContent();
		if (customContent != null && customContent.length() != 0) {
			try {
				JSONObject obj = new JSONObject(customContent);
				// key1为前台配置的key
				if (!obj.isNull("v5_client")) {
					String value = obj.getString("v5_client");
					Logger.d(TAG, "get custom value:" + value);
				}
				// ...
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// APP自主处理消息的过程...
		// appicon消息红点
		int badgeCount = Config.getConfig(context).readInt("app_badge_count") + 1;
		Logger.d(TAG, "[XGPush] 红点：" + badgeCount + " -> " + text);
		ShortcutBadger.applyCount(context, badgeCount);
		Config.getConfig(context).saveInt("app_badge_count", badgeCount);

		show(context, "您有1条新消息, " + "通知被展示 ， " + notifiShowedRlt.toString());
	}

	@Override
	public void onUnregisterResult(Context context, int errorCode) {
		if (context == null) {
			return;
		}
		String text = "";
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
			text = "反注册成功";
		} else {
			text = "反注册失败" + errorCode;
		}
		Log.d(TAG, text);
		show(context, text);

	}

	@Override
	public void onSetTagResult(Context context, int errorCode, String tagName) {
		if (context == null) {
			return;
		}
		String text = "";
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
			text = "\"" + tagName + "\"设置成功";
		} else {
			text = "\"" + tagName + "\"设置失败,错误码：" + errorCode;
		}
		Log.d(TAG, text);
		show(context, text);

	}

	@Override
	public void onDeleteTagResult(Context context, int errorCode, String tagName) {
		if (context == null) {
			return;
		}
		String text = "";
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
			text = "\"" + tagName + "\"删除成功";
		} else {
			text = "\"" + tagName + "\"删除失败,错误码：" + errorCode;
		}
		Log.d(TAG, text);
		show(context, text);

	}

	// 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
	@Override
	public void onNotifactionClickedResult(Context context,
			XGPushClickedResult message) {
		if (context == null || message == null) {
			return;
		}
		String text = "";
		if (message.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
			// 通知在通知栏被点击
			// APP自己处理点击的相关动作
			// 这个动作可以在activity的onResume也能监听，请看第3点相关内容
			text = "通知被打开 :" + message;
		} else if (message.getActionType() == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
			// 通知被清除啦。。。。
			// APP自己处理通知被清除后的相关动作
			text = "通知被清除 :" + message;
		}
		Logger.d(TAG, "广播接收到通知被点击:" + message.toString());
		// 获取自定义key-value
		String customContent = message.getCustomContent();
		if (customContent != null && customContent.length() != 0) {
			try {
				JSONObject obj = new JSONObject(customContent);
				// key1为前台配置的key
				if (!obj.isNull("v5_client")) {
					String value = obj.getString("v5_client");
					Logger.d(TAG, "get custom value:" + value);
					if (message.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
						V5ClientAgent.getInstance().startV5ChatActivity(context);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// APP自主处理的过程。。。
		Log.d(TAG, text);
		show(context, text);
	}

	@Override
	public void onRegisterResult(Context context, int errorCode,
			XGPushRegisterResult message) {
		if (context == null || message == null) {
			return;
		}
		String text = "";
		if (errorCode == XGPushBaseReceiver.SUCCESS) {
			text = message + "注册成功";
			// 在这里拿token
			String tmpDT = Config.getConfig(context).readString("device_token"), token = message.getToken();

			// 设置device_token
			if (DeviceUtil.usePush() < 2) {
				DeviceUtil.setUsePush(DeviceUtil.XGPush);
			}
			Config.getConfig(context).saveString("device_token", token);
			// 设置v5sdk的device_token
			V5ClientConfig.getInstance(context).setDeviceToken(token);
			Logger.i(TAG, "[XGPush] setDeviceToken for XGPush: " + token + " old_device_token:" + tmpDT);
		} else {
			text = message + "注册失败，错误码：" + errorCode;
		}
		Log.d(TAG, text);
		show(context, text);
	}

	// 消息透传
	@Override
	public void onTextMessage(Context context, XGPushTextMessage message) {
		String text = "收到消息:" + message.toString();
		// 获取自定义key-value
		String customContent = message.getCustomContent();
		if (customContent != null && customContent.length() != 0) {
			try {
				JSONObject obj = new JSONObject(customContent);
				// key1为前台配置的key
				if (!obj.isNull("v5_client")) {
					String value = obj.getString("v5_client");
					Logger.d(TAG, "get custom value:" + value);
				}
				// ...
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Log.d(TAG, text);
		show(context, text);
	}
}
