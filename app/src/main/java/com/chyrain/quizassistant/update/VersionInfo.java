/*
 * Copyright (c) 2017. V5KF.COM. All rights reserved.
 * Created by Chyrain on 17-10-24 下午4:09
 * Email: chyrain_v5kf@qq.com
 *
 * File: VersionInfo.java
 * Last modified 17-10-24 下午4:09
 */

package com.chyrain.quizassistant.update;

import android.text.TextUtils;

import java.io.Serializable;

public class VersionInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2806822675010162800L;
	// 版本描述字符串
	private String version;
	// 版本更新时间
	private String updateTime;
	// 新版本更新下载地址(通用渠道)
	private String downloadURL;
	// 本应用所处渠道的下载地址（渠道：open_download\qq_download\qihoo360\wandoujia\xiaomi）
	private String channelURL;
	// 更新描述信息
	private String displayMessage;
	// 更新描述标题
	private String displayTitle;
	// 版本号
	private int versionCode;
	// app名称
	private String appName;
	
	private int level; // 更新等级：1-5，5表示有严重bug需要强制更新，1表示小修改，可选更新。

	private boolean checkManual;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 优先获取本渠道的更新地址
	 * @return
	 */
	public String getDownloadURL() {
		if (!TextUtils.isEmpty(channelURL)) {
			return channelURL;
		}
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public String getDisplayMessage() {
		return displayMessage;
	}

	public void setDisplayMessage(String displayMessage) {
		this.displayMessage = displayMessage;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getApkName() {
		String url = getDownloadURL();
		if (!TextUtils.isEmpty(url)) {
			int index = url.lastIndexOf("/");
			if (index != -1 && url.length() > (index + 1)) {
				return url.substring(index + 1);
			}
		}
		return null;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getChannelURL() {
		return channelURL;
	}

	public void setChannelURL(String channelURL) {
		this.channelURL = channelURL;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	public boolean isCheckManual() {
		return checkManual;
	}

	public void setCheckManual(boolean checkManual) {
		this.checkManual = checkManual;
	}

}
