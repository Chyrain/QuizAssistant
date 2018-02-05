package com.chyrain.quizassistant;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 配置
 * @author Chyrain
 * <p>Created: 2018-1-26 上午10:02:42.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://chyrain.github.io">chyrain.github.io</a></p>
 * Edit
 *		TODO
 */
public class Config {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final int LOG_LEVEL = 5;
    public static final int DEFAULT_THROTTLE_TIME = 1500; // 【答题】轮询时间间隔

    public static final int DEFAULT_NIGHT_START = 23;
    public static final int DEFAULT_NIGHT_END = 7;
//    public static final String URL_HOST = "https://chyrain.com/";//app/quizassistant/
//    public static final String URL_HOST = "https://chyrain.github.io/";//app/quizassistant/
    public static final String URL_HOST = "https://desk.v5kf.com/";//app/quizassistant/
    public static final String SHARE_IMAGE_LINK = URL_HOST + "app/quizassistant/share_img.png";
    public static final String DOWNLOAD_LINK = URL_HOST + "app/quizassistant/download/QuizAssistant.apk";
//    "http://chyrain.com/app/quizassistant/download/version.xml";
//    public static final String UPDATE_LINK = URL_HOST + "app/quizassistant/download/version.xml";
//    public static final String APP_LINK = URL_HOST + "app/quizassistant/app.html"; //index.html
    // 检查更新避免缓存，使用：chyrain.com
    public static final String UPDATE_LINK = "http://chyrain.com/app/quizassistant/download/version.xml";
    // webview页面打开用户可获取到URL页面均使用：chyrain.com
    public static final String APP_LINK = "http://chyrain.com/app/quizassistant/app.html"; //index.html
    public static final String INTRO_LINK = "http://chyrain.com/app/quizassistant/intro.html";
    public static final String URL_ABOUT = "http://chyrain.com/app/quizassistant/app.html";
    public static final String URL_ME = "https://chyrain.github.io/about/";

    /* 更新广播 */
    public static final String ACTION_ON_UPDATE = "com.chyrain.quizassistant.update.updateservice";
    /* Intent ACTION_ON_UPDATE 的广播消息类别 */
    public static final int EXTRA_TYPE_UP_ENABLE = 1;			/* 有更新 */
    public static final int EXTRA_TYPE_UP_DOWNLOAD_FINISH = 2;	/* 下载完成 */
    public static final int EXTRA_TYPE_UP_DOWNLOAD = 3;			/* 允许下载 */
    public static final int EXTRA_TYPE_UP_INSTALL = 4; 			/* 允许安装 */
    public static final int EXTRA_TYPE_UP_NO_NEWVERSION = 5; 	/* 无更新 */
    public static final int EXTRA_TYPE_UP_FAILED = 6; 			/* 获取更新失败 */
    public static final int EXTRA_TYPE_UP_CANCEL = 7; 			/* 取消下载更新 */
    public static final String EXTRA_KEY_INTENT_TYPE = "intent_type"; /* intent类别，每个携带Extra的intent必带此key */
    public static final int EXTRA_TYPE_NULL = 0;
    public static final String EXTRA_KEY_DOWN_ONLYWIFI = "only_wifi";

    // 自定义服务广播 -> eventbus
    @Deprecated
    public static final String ACTION_QIANGHONGBAO_SERVICE_DISCONNECT = "com.chyrain.quizassistant.ACCESSBILITY_DISCONNECT";
    @Deprecated
    public static final String ACTION_QIANGHONGBAO_SERVICE_CONNECT = "com.chyrain.quizassistant.ACCESSBILITY_CONNECT";
    // 自定义通知栏广播 -> eventbus
    @Deprecated
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT = "com.chyrain.quizassistant.NOTIFY_LISTENER_DISCONNECT";
    @Deprecated
    public static final String ACTION_NOTIFY_LISTENER_SERVICE_CONNECT = "com.chyrain.quizassistant.NOTIFY_LISTENER_CONNECT";

    public static final String PREFERENCE_NAME = "v5wxbot_config"; // shared preference name
    public static final String KEY_ENABLE_AD = "KEY_ENABLE_AD"; // 允许AD
    public static final String KEY_ENABLE_WECHAT = "KEY_ENABLE_WECHAT"; // 允许微信机器人？
    public static final String KEY_ENABLE_ZSCR = "KEY_ENABLE_ZSCR"; // 允许芝士超人
    public static final String KEY_ENABLE_ZSCR_INKE = "KEY_ENABLE_ZSCR_INKE"; // 允许芝士超人——映客直播版
    public static final String KEY_ENABLE_CDDH = "KEY_ENABLE_CDDH"; // 允许冲顶大会
    public static final String KEY_ENABLE_XIGUA = "KEY_ENABLE_XIGUA"; // 允许西瓜视频=>百万英雄
    public static final String KEY_ENABLE_INKE = "KEY_ENABLE_INKE"; // 允许映客直播=>芝士超人
    public static final String KEY_ENABLE_HUAJIAO = "KEY_ENABLE_HUAJIAO"; // 允许花椒直播=>百万赢家
    public static final String KEY_ENABLE_HJSM = "KEY_ENABLE_HJSM"; // 允许黄金十秒
    public static final String KEY_AUTO_TRUST = "KEY_AUTO_TRUST"; // 启用全自动托管
    public static final String KEY_SHOW_ANSWER = "KEY_SHOW_ANSWER"; // 启用答案推荐
    public static final String KEY_THROTTLE_TIME = "KEY_THROTTLE_TIME"; // 答案轮询间隔
    public static final String KEY_ENABLE_FLOAT_BUTTON = "KEY_ENABLE_FLOAT_BUTTON"; // 开启浮动开关
    public static final String KEY_NOTIFY_SOUND = "KEY_NOTIFY_SOUND"; // 声音？
    public static final String KEY_NOTIFY_VIBRATE = "KEY_NOTIFY_VIBRATE"; // 震动？
    private static final String KEY_AGREEMENT = "KEY_AGREEMENT"; // 同意免责协议？
    public static final String KEY_NOTIFICATION_SERVICE_ENABLE = "KEY_NOTIFICATION_SERVICE_ENABLE"; // 允许监听通知？

    public static final String KEY_WECHAT_AFTER_RESPONSE_TEXT = "KEY_WECHAT_AFTER_RESPONSE_TEEXT"; // 回复完消息处理方式
    public static final String KEY_WECHAT_AFTER_OPEN_HONGBAO = "KEY_WECHAT_AFTER_OPEN_HONGBAO"; // 点击红包后处理方式
    public static final String KEY_WECHAT_AFTER_GET_HONGBAO = "KEY_WECHAT_AFTER_GET_HONGBAO"; // 拆完红包后处理方式
    public static final String KEY_WECHAT_DELAY_TIME_MSG = "KEY_WECHAT_DELAY_TIME_MSG"; // 消息延迟
    public static final String KEY_WECHAT_MODE_TEXT = "KEY_WECHAT_MODE_TEXT"; // 收到文本消息处理模式
    public static final String KEY_WECHAT_MODE_HONGBAO = "KEY_WECHAT_MODE_HONGBAO"; // 收到红包处理模式
    public static final String KEY_KEFU = "KEY_KEFU"; //联系客服

    public static final String KEY_NOTIFY_NIGHT_ENABLE = "KEY_NOTIFY_NIGHT_ENABLE"; // 夜间免打扰？
    public static final String KEY_NOTIFY_NIGHT_START = "KEY_NOTIFY_NIGHT_START"; // 免打扰起始时间
    public static final String KEY_NOTIFY_NIGHT_END = "KEY_NOTIFY_NIGHT_END"; // 免打扰结束时间


    /**
     * 收到消息处理模式KEY_WECHAT_MODE_TEXT
     */
    public static final int WX_MSG_MODE_0 = 0;//自动回复(可配置延迟时间)
    public static final int WX_MSG_MODE_1 = 1;//填写机器人推荐回复，不发送
    public static final int WX_MSG_MODE_2 = 2;//不填写不回复(自动进入页面，手动回复)
    public static final int WX_MSG_MODE_3 = 3;//不处理

    /**
     * 发现红包处理模式KEY_WECHAT_MODE_HONGBAO
     */
    public static final int WX_HONGBAO_MODE_0 = 0;//自动抢
    public static final int WX_HONGBAO_MODE_1 = 1;//疯狂自动抢
    public static final int WX_HONGBAO_MODE_2 = 2;//抢单聊红包,群聊红包只通知
    public static final int WX_HONGBAO_MODE_3 = 3;//抢群聊红包,单聊红包只通知
    public static final int WX_HONGBAO_MODE_4 = 4;//通知手动抢(不自动抢)
    public static final int WX_HONGBAO_MODE_5 = 5;//不处理(无视红包)
    public static final String KEY_ENABLE_AUTO_UNLOCK = "KEY_ENABLE_AUTO_UNLOCK"; // 息屏自动解锁抢

    /**
     * 点击红包后KEY_WECHAT_AFTER_OPEN_HONGBAO
     */
    public static final int WX_AFTER_OPEN_HONGBAO = 0;//拆红包
    public static final int WX_AFTER_OPEN_SEE = 1; //看大家手气
    public static final int WX_AFTER_OPEN_NONE = 2; //静静地看着

    /**
     * 回复完消息后/拆开红包后
     */
    public static final int WX_AFTER_GET_GOHOME = 0; // 返回桌面
    public static final int WX_AFTER_GET_NONE = 1; //不处理
    public static final int WX_AFTER_GET_BACK = 2; // 返回上一页

    /**
     * Event Tag
     */
    public static final String EVENT_TAG_IN_NOTIFY = "ETAG_IN_NOTIFY";
    // 自定义服务event tag
    public static final String EVENT_TAG_ROBOT_SERVICE_CONNECT = "ETAG_ROBOT_SERVICE_CONNECT";
    public static final String EVENT_TAG_ROBOT_SERVICE_DISCONNECT = "EVENT_TAG_IN_NOTIFY";
    // 自定义通知栏event tag
    public static final String EVENT_TAG_NOTIFY_LISTENER_SERVICE_CONNECT = "ETAG_NOTIFY_LISTENER_SERVICE_CONNECT";
    public static final String EVENT_TAG_NOTIFY_LISTENER_SERVICE_DISCONNECT = "ETAG_NOTIFY_LISTENER_SERVICE_DISCONNECT";
    // 关闭服务
    public static final String EVENT_TAG_STOP_WXBOT = "EVENT_TAG_STOP_WXBOT";
    // 答题渠道变化 accessbilityJob
    public static final String EVENT_TAG_ACCESSBILITY_JOB_CHANGE = "EVENT_TAG_ACCESSBILITY_JOB_CHANGE";
    public static final String EVENT_TAG_UPDATE_QUIZ = "EVENT_TAG_UPDATE_QUIZ";
    // 浮动窗口按钮
    public static final String EVENT_TAG_FLOAT_CLICK = "EVENT_TAG_FLOAT_CLICK";
    public static final String EVENT_TAG_FLOAT_LONG_CLICK = "EVENT_TAG_FLOAT_LONG_CLICK";
    public static final String EVENT_TAG_SHOW_FLOAT = "EVENT_TAG_SHOW_FLOAT";
    public static final String EVENT_TAG_HIDE_FLOAT = "EVENT_TAG_HIDE_FLOAT";
    public static final String EVENT_TAG_UPDATE_FLOAT_STATUS = "EVENT_TAG_UPDATE_FLOAT_STATUS";

    private static Config current;

    /**
     * 获得Config单例对象
     * @param context
     * @return
     */
    public static synchronized Config getConfig(Context context) {
        if(current == null) {
            synchronized (Config.class) {
                if(current == null) {
                    current = new Config(context.getApplicationContext());
                }
            }
        }
        return current;
    }

    private SharedPreferences preferences;
    @SuppressWarnings("unused")
    private Context mContext;

    private Config(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /** 是否显示广告 */
    public boolean isEnableAd() {
        return preferences.getBoolean(KEY_ENABLE_AD, true);
    }
    public void setEnableAd(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_AD, enable);
        editor.commit();
    }

    /** 是否启动微信机器人和辅助服务(全局) **/
    public boolean isEnableWechat() {
        return preferences.getBoolean(KEY_ENABLE_WECHAT, true);
    }
    public void setEnableWechat(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_WECHAT, enable);
        editor.commit();
    }

    /** 是否启动芝士超人辅助服务 **/
    public boolean isEnableZscr() {
        return preferences.getBoolean(KEY_ENABLE_ZSCR, true);
    }
    public void setEnableZscr(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_ZSCR, enable);
        editor.commit();
    }

    /** 是否启动冲顶大会辅助服务 **/
    public boolean isEnableCddh() {
        return preferences.getBoolean(KEY_ENABLE_CDDH, true);
    }

    public void setEnableCddh(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_CDDH, enable);
        editor.commit();
    }

    /** 是否启动西瓜视频辅助服务 **/
    public boolean isEnableXigua() {
        return preferences.getBoolean(KEY_ENABLE_XIGUA, true);
    }

    public void setEnableXigua(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_XIGUA, enable);
        editor.commit();
    }

    /** 是否启动映客直播辅助服务 **/
    public boolean isEnableInke() {
        return preferences.getBoolean(KEY_ENABLE_INKE, true);
    }

    public void setEnableInke(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_INKE, enable);
        editor.commit();
    }

    /** 是否启动花椒直播辅助服务 **/
    public boolean isEnableHuajiao() {
        return preferences.getBoolean(KEY_ENABLE_HUAJIAO, true);
    }

    public void setEnableHuajiao(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_HUAJIAO, enable);
        editor.commit();
    }

    /** 是否启动黄金十秒辅助服务 **/
    public boolean isEnableHJSM() {
        return preferences.getBoolean(KEY_ENABLE_HJSM, true);
    }

    public void setEnableHJSM(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_ENABLE_HJSM, enable);
        editor.commit();
    }

    /** 是否启动机器人全自动托管，自动提交答案 **/
    public boolean isEnableAutoTrust() {
        return preferences.getBoolean(KEY_AUTO_TRUST, false);
    }

    public void setEnableAutoTrust(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_AUTO_TRUST, enable);
        editor.commit();
    }

    /** 是否显示机器人推荐答案 **/
    public boolean isEnableShowAnswer() {
        return preferences.getBoolean(KEY_SHOW_ANSWER, true);
    }

    public void setEnableShowAnswer(boolean enable) {
        Editor editor = preferences.edit();
        editor.putBoolean(KEY_SHOW_ANSWER, enable);
        editor.commit();
    }

    /** 是否显示浮动开关 **/
    public boolean isEnableFloatButton() {
        return preferences.getBoolean(KEY_ENABLE_FLOAT_BUTTON, true);
    }

    /** 是否显示浮动开关 **/
    public boolean isEnableAutoUnlock() {
        return preferences.getBoolean(KEY_ENABLE_AUTO_UNLOCK, true);
    }

    /** 答案轮询时间间隔(整数) **/
    public void setThrottleTime(int t) {
        preferences.edit().putInt(KEY_THROTTLE_TIME, t).apply();
    }

    /** 答案轮询时间间隔 **/
    public int getThrottleTime() {
        int defaultValue = DEFAULT_THROTTLE_TIME;
        String result = preferences.getString(KEY_THROTTLE_TIME, "" + DEFAULT_THROTTLE_TIME);
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 是否启动通知栏模式 **/
    public boolean isEnableNotificationService() {
        return preferences.getBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, false);
    }

    public void setNotificationServiceEnable(boolean enable) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SERVICE_ENABLE, enable).apply();
    }

    /** 是否开启声音 **/
    public boolean isNotifySound() {
        return preferences.getBoolean(KEY_NOTIFY_SOUND, true);
    }

    /** 是否开启震动 **/
    public boolean isNotifyVibrate() {
        return preferences.getBoolean(KEY_NOTIFY_VIBRATE, true);
    }

    /** 免费声明/免责声明 **/
    public boolean isAgreement() {
        return preferences.getBoolean(KEY_AGREEMENT, false);
    }

    /** 设置是否同意 **/
    public void setAgreement(boolean agreement) {
        preferences.edit().putBoolean(KEY_AGREEMENT, agreement).commit();
    }

    public void saveString(String key, String val) {
        preferences.edit().putString(key, val).commit();
    }
    public String readString(String key) {
        return preferences.getString(key, null);
    }
    public void saveInt(String key, int val) {
        preferences.edit().putInt(key, val).commit();
    }
    public int readInt(String key) {
        return preferences.getInt(key, 0);
    }

    public void saveBoolean(String key, boolean val) {
        preferences.edit().putBoolean(key, val).commit();
    }
    public boolean readBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void remove(String key) {
        preferences.edit().remove(key).commit();
    }


//  @deprecated

    /** 微信打开红包后的事件 **/
    public int getWechatAfterOpenHongBaoEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_AFTER_OPEN_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信回复消息后的事件 **/
    public int getWechatAfterSendMsgEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_AFTER_RESPONSE_TEXT, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信抢到红包后的事件 **/
    public int getWechatAfterGetHongBaoEvent() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_AFTER_GET_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 微信收消息后延时回复时间 **/
    public int getWechatMsgDelayTime() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_DELAY_TIME_MSG, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 获取抢微信收到消息的模式 **/
    public int getWechatMsgMode() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_MODE_TEXT, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 获取抢微信红包的模式 **/
    public int getWechatMode() {
        int defaultValue = 0;
        String result =  preferences.getString(KEY_WECHAT_MODE_HONGBAO, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(result);
        } catch (Exception e) {}
        return defaultValue;
    }

    /** 是否开启夜间免打扰模式 **/
    public boolean isNotifyNight() {
        return preferences.getBoolean(KEY_NOTIFY_NIGHT_ENABLE, false);
    }

    /** 夜间免打扰开启时间 **/
    public int getNotifyNightStart() {
        int defaultValue = DEFAULT_NIGHT_START;
        return preferences.getInt(KEY_NOTIFY_NIGHT_START, defaultValue);
    }

    /** 夜间免打扰结束时间 **/
    public int getNotifyNightEnd() {
        int defaultValue = DEFAULT_NIGHT_END;
        return preferences.getInt(KEY_NOTIFY_NIGHT_END, defaultValue);
    }

    /** 夜间免打扰开启时间(整数) **/
    public void setNotifyNightStart(int start) {
        preferences.edit().putInt(KEY_NOTIFY_NIGHT_START, start).apply();
    }

    /** 夜间免打扰结束时间(整数) **/
    public void setNotifyNightEnd(int end) {
        preferences.edit().putInt(KEY_NOTIFY_NIGHT_END, end).apply();
    }

    /** 夜间免打扰开启时间 **/
    public String getNotifyNightStartTime() {
        return String.format(Locale.getDefault(), "%2d:00", getNotifyNightStart());
    }

    /** 夜间免打扰结束时间 **/
    public String getNotifyNightEndTime() {
        return String.format(Locale.getDefault(), "%2d:00", getNotifyNightEnd());
    }

}

