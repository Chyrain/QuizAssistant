package com.chyrain.quizassistant.job;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.chyrain.quizassistant.BuildConfig;
import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.service.IStatusBarNotification;
import com.chyrain.quizassistant.service.ScreenListener;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.NotifyHelper;

/**
 * 微信AccessibilityEvent和通知事件接收处理
 * @author Chyrain
 * <p>Created: 2016-12-25 上午11:17:24.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://www.chyrain.com">chyrain.com</a></p>
 * @company <a href="http://www.v5kf.com">深圳市智客网络科技有限公司</a>
 * @edit 
 * 		2017/1/03 [修改] 抢红包息屏通知栏自动抢
 */
public class WechatAccessbilityJob extends BaseAccessbilityJob {

    private static final String TAG = "WechatAccessbilityJob";

    /** 微信的包名*/
    public static final String WECHAT_PACKAGENAME = "com.tencent.mm";

    /** 红包消息的关键字*/
    private static final String HONGBAO_TEXT_KEY = "[微信红包]";

    private static final String BUTTON_CLASS_NAME = "android.widget.Button";
    private static final String IMAGE_CLASS_NAME = "android.widget.ImageView";
    private static final String TEXTVIEW_CLASS_NAME = "android.widget.TextView";
    private static final String VIEWGROUP_CLASS_NAME = "android.view.ViewGroup";


    /** 不能再使用文字匹配的最小版本号 */
    private static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700

    private static final int WINDOW_NONE = 0; // 未知页面
    private static final int WINDOW_LUCKYMONEY_RECEIVEUI = 1; // 点击红包显示页面
    private static final int WINDOW_LUCKYMONEY_DETAIL = 2; // 红包详情页
    private static final int WINDOW_LAUNCHER = 3; // 微信
    private static final int WINDOW_SESSION = 4; // 微信会话列表页
    private static final int WINDOW_CHAT= 5; // 微信聊天内容页
    private static final int WINDOW_OTHER = -1; // 锁屏页

    private String mCurrChat = "";
    private int mContentChangeCount = 0;
    private int mLastWindow = WINDOW_NONE;
    private int mCurrentWindow = WINDOW_NONE;

    private boolean isReceivingHongbao;
    private PackageInfo mWechatPackageInfo = null;
    private HashMap<String, Integer> gotNodes = new HashMap<>();
    ScreenListener mScreenListener;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新安装包信息
            updatePackageInfo();
        }
    };

	private PendingIntent mPendingIntent; // 收到通知时赋值

    @Override
    public void onCreateJob(WxBotService service) {
        super.onCreateJob(service);

        updatePackageInfo();

        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");

        getContext().registerReceiver(broadcastReceiver, filter);
        
        /** 屏幕状态监听 **/
        mScreenListener = new ScreenListener(getContext());
        mScreenListener.begin(new ScreenListener.ScreenStateListener() {

            @Override
            public void onUserPresent() {
                Logger.e("onUserPresent", "onUserPresent");
                if (isReceivingHongbao && mPendingIntent != null
                		&& getConfig().isEnableAutoUnlock()) {
                    NotifyHelper.send(mPendingIntent);
                    NotifyHelper.send(mPendingIntent);
                    mPendingIntent = null;
        		}
                mLastWindow = mCurrentWindow;
            	mCurrentWindow = WINDOW_OTHER;
            }

            @Override
            public void onScreenOn() {
            	Logger.e("onScreenOn", "onScreenOn");
            	if (isReceivingHongbao && mPendingIntent != null
            			&& getConfig().isEnableAutoUnlock()) {
                    NotifyHelper.send(mPendingIntent);
                    NotifyHelper.send(mPendingIntent);
                    mPendingIntent = null;
        		}
            	mLastWindow = mCurrentWindow;
            	mCurrentWindow = WINDOW_OTHER;
            }

            @Override
            public void onScreenOff() {
            	Logger.e("onScreenOff", "onScreenOff");
            	mCurrentWindow = mLastWindow;
            	mLastWindow = WINDOW_OTHER;
            }
        });
    }

    @Override
    public void onStopJob() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {}
        mScreenListener.unregisterListener();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(IStatusBarNotification sbn) {
    	if (!isEnable()) {
    		return;
    	}
        Notification nf = sbn.getNotification();
        String text = String.valueOf(sbn.getNotification().tickerText);
    	Logger.d(TAG, "[job -> onNotificationPosted] -> " + text);
        notificationEvent(text, nf);
    }

    @Override
    public boolean isEnable() {
        return getConfig().isEnableWechat();
    }

    @Override
    public void onEnableChange(boolean enable) {
        //
    }

    @Override
    public String getTargetPackageName() {
        return WECHAT_PACKAGENAME;
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if(data == null || !(data instanceof Notification)) {
                return;
            }
            if(WxBotService.isNotificationServiceRunning() && getConfig().isEnableNotificationService()) { //开启快速模式，不处理
                return;
            }
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
        	Logger.i(TAG, "TYPE_WINDOW_CONTENT_CHANGED -> isReceivingHongbao：" + isReceivingHongbao 
        			+ " mCurrentWindow:" + mCurrentWindow);
        	if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
        		openHongBao(event);
        	} else {
	            if(mCurrentWindow < WINDOW_LAUNCHER) { //不在聊天界面或聊天列表，不处理
	                return;
	            }
	            if(isReceivingHongbao) {
	                handleChatListHongBao(false);
	            }
        	}
        }
    }

    /** 是否为群聊天*/
    private boolean isMemberChatUi(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return false;
        }
        String id = "com.tencent.mm:id/ces";
        int wv = getWechatVersion();
        if(wv <= 680) {
            id = "com.tencent.mm:id/ew";
        } else if(wv <= 700) {
            id = "com.tencent.mm:id/cbo";
        }
        String title = null;
        AccessibilityNodeInfo target = AccessibilityHelper.findNodeInfosById(nodeInfo, id);
        if(target != null) {
            title = String.valueOf(target.getText());
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("返回");

        if(list != null && !list.isEmpty()) {
            AccessibilityNodeInfo parent = null;
            for(AccessibilityNodeInfo node : list) {
                if(!"android.widget.ImageView".equals(node.getClassName())) {
                    continue;
                }
                String desc = String.valueOf(node.getContentDescription());
                if(!"返回".equals(desc)) {
                    continue;
                }
                parent = node.getParent();
                break;
            }
            if(parent != null) {
                parent = parent.getParent();
            }
            if(parent != null) {
                if( parent.getChildCount() >= 2) {
                    AccessibilityNodeInfo node = parent.getChild(1);
                    if("android.widget.TextView".equals(node.getClassName())) {
                        title = String.valueOf(node.getText());
                    }
                }
            }
        }


        if(title != null && title.endsWith(")")) {
            return true;
        }
        return false;
    }

    /** 通知栏事件*/
    private void notificationEvent(String ticker, Notification nf) {
        String text = ticker;
        int index = text.indexOf(":");
        if(index != -1) {
            text = text.substring(index + 1);
        }
        text = text.trim();
        if(text.contains(HONGBAO_TEXT_KEY)) { //红包消息
            newHongBaoNotification(nf);
        } else { // 其他消息判断
        	
        }
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newHongBaoNotification(Notification notification) {
        isReceivingHongbao = true;
        //以下是精华，将微信的通知栏消息打开
        mPendingIntent = notification.contentIntent;
//        boolean lock = NotifyHelper.isLockScreen(getContext());
        boolean unlock = mScreenListener.isScreenOn();
        Logger.d(TAG, "[newHongBaoNotification] unlock:" + unlock);
        if(unlock) {
            NotifyHelper.send(mPendingIntent);
            mPendingIntent = null;
        } else {
        	// 尝试自动解锁
        	if (getConfig().isEnableAutoUnlock()) {
	        	NotifyHelper.wakeUpAndUnlock(getContext());
	        	NotifyHelper.UnlockMe(V5Application.getContextActivity());
	        	
	        	NotifyHelper.send(mPendingIntent);
                NotifyHelper.send(mPendingIntent);
        	}
        	
            NotifyHelper.showNotify(getContext(), String.valueOf(notification.tickerText), mPendingIntent);
        }
        
//        if(lock || (getConfig().getWechatMode() != Config.WX_HONGBAO_MODE_0 
//        		&& getConfig().getWechatMode() != Config.WX_HONGBAO_MODE_1)) {
//            NotifyHelper.playEffect(getContext(), getConfig());
//        }
        NotifyHelper.playEffect(getContext(), getConfig());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
    	Logger.i(TAG, "[openHongBao] -> " + event.getClassName() + " mContentChangeCount:" + mContentChangeCount
    			+ " mLastWindow:" + mLastWindow + " event=>" + event);
    	if ("com.tencent.mm.ui.base.p".equals(event.getClassName())) {
    		// 打开红包：正在加载
    		if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
    			mContentChangeCount = 2;
    		}
    	} else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) { // STATE_CHANGE
            mCurrentWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
            //点中了红包，下一步就是去拆红包
            handleLuckyMoneyReceive();
            mLastWindow = WINDOW_LUCKYMONEY_RECEIVEUI;
        } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) { // STATE_CHANGE
            mCurrentWindow = WINDOW_LUCKYMONEY_DETAIL;
            if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
    			mContentChangeCount = 2;
    		}

            //拆完红包后看详细的纪录界面
            if(getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_GOHOME) { //返回主界面，以便收到下一次的红包通知
            	AccessibilityHelper.performBack(getService()); // 先返回上一页再回到桌面，避免LuckyMoneyDetailUI无限循环
            	AccessibilityHelper.performHome(getService());
            } else if (getConfig().getWechatAfterGetHongBaoEvent() == Config.WX_AFTER_GET_BACK) {//返回上一页
            	AccessibilityHelper.performBack(getService());
            }
            mLastWindow = WINDOW_LUCKYMONEY_DETAIL;
        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) { // STATE_CHANGE
        	//从桌面或者其他界面进入微信
            mCurrentWindow = WINDOW_LAUNCHER;
        	if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1 && 
        			(mLastWindow != WINDOW_LUCKYMONEY_DETAIL && mLastWindow != WINDOW_LUCKYMONEY_RECEIVEUI)) {
        		if (mContentChangeCount > 0) {
            		mContentChangeCount--;
            		if (mContentChangeCount < 0) {
            			mContentChangeCount = 0;
            		}
        		} else {
		            //在聊天界面,去点中红包
		            handleChatListHongBao(false);
        		}
        	} else if (getConfig().getWechatMode() != Config.WX_HONGBAO_MODE_1 && (mLastWindow != WINDOW_LUCKYMONEY_DETAIL && mLastWindow != WINDOW_LUCKYMONEY_RECEIVEUI)){
        		if (mContentChangeCount > 0) {
            		mContentChangeCount--;
            		if (mContentChangeCount < 0) {
            			mContentChangeCount = 0;
            		}
        		} else {
		            //在聊天界面,去点中红包
		            handleChatListHongBao(false);
        		}
        	}
        	mLastWindow = WINDOW_LAUNCHER;
        	
        	// 进入微信
        } else if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) { // CONTENT_CHANGE
        	Logger.i(TAG, "WX_HONGBAO_MODE_1 else { mContentChangeCount:" + mContentChangeCount);
        	//mCurrentWindow = WINDOW_LAUNCHER;
        	if (mContentChangeCount == 0 && (mLastWindow != WINDOW_LUCKYMONEY_DETAIL && mLastWindow != WINDOW_LUCKYMONEY_RECEIVEUI)) {
	            //在聊天界面,去点中红包
	            handleChatListHongBao(true);
        	} else if (mContentChangeCount > 0){
        		mContentChangeCount--;
        		if (mContentChangeCount < 0) {
        			mContentChangeCount = 0;
        		}
        	}
        }
//        else {
//        	mCurrentWindow = WINDOW_NONE;
//        	mLastWindow = WINDOW_NONE;
//        }
        // 记录已抢过的红包不再抢
    }

    private void getCurrChat(AccessibilityNodeInfo nodeInfo) {
    	Logger.d(TAG, "getCurrChat");
    	mCurrChat = "微信内";
		AccessibilityNodeInfo chatNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.tencent.mm:id/a1d");
		if(chatNode != null) {
        	Logger.v(TAG, "chatNode:" + chatNode);
            mCurrentWindow = WINDOW_CHAT;
        	mCurrChat = "聊天内容页";
            return;
        }
		AccessibilityNodeInfo sessNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.tencent.mm:id/bg6");
		if(sessNode != null) {
        	Logger.v(TAG, "sessNode:" + sessNode);
            mCurrentWindow = WINDOW_SESSION;
        	mCurrChat = "会话列表也";
            return;
        }
	}


    /**
     * 收到聊天里的红包
     * */
    @SuppressLint("NewApi") @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao(boolean contentChange) {
        int mode = getConfig().getWechatMode();
        if(mode == Config.WX_HONGBAO_MODE_4) { //只通知模式
            return;
        }
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        
        if(nodeInfo == null) {
            Logger.w(TAG, "rootWindow为空");
            return;
        }
        Logger.v(TAG, "[handleChatListHongBao]<AccessibilityNodeInfo>:" + nodeInfo);

        if(mode != Config.WX_HONGBAO_MODE_0 && mode != Config.WX_HONGBAO_MODE_1) {
            boolean isMember = isMemberChatUi(nodeInfo);
            if(mode == Config.WX_HONGBAO_MODE_2 && isMember) {//过滤群聊
                return;
            } else if(mode == Config.WX_HONGBAO_MODE_3 && !isMember) { //过滤单聊
                return;
            }
        }
        getCurrChat(nodeInfo);
        Logger.i(TAG, "[handleChatListHongBao]" + mCurrChat + isReceivingHongbao + " <-isReceivingHongbao. 查找红包  window => " + mCurrentWindow);
        if (mCurrentWindow == WINDOW_SESSION) {
    		// 方案3：查找消息红点id，判断是否红包
        	boolean red = findRedNodes(nodeInfo);
    		if(red) {
        		Logger.d(TAG, "-->没有红点（新消息）isReceivingHongbao:" + isReceivingHongbao);
        		if (isReceivingHongbao) {
        			AccessibilityNodeInfo hbInfo = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.tencent.mm:id/adu");
        			Logger.d(TAG, "-->[微信红包] findNodeInfosById:" + hbInfo);
        			if (hbInfo != null && hbInfo.getText().toString().contains("[微信红包]")) {
	        			if(BuildConfig.DEBUG) {
						   Logger.i(TAG, "-->[微信红包]:" + hbInfo);
	        			}
	        			isReceivingHongbao = true;
	        			AccessibilityHelper.performClick(hbInfo);
        			}
        		}
        	}
        } else if (mCurrentWindow == WINDOW_CHAT && isReceivingHongbao) {
        	List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        	if(list != null && list.size() > 0) {
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                Logger.i(TAG, list.size() + "<-size Node =>" + node);
                // 判断是否打开过此红包？？？
                if (node != null) {
                    //最新的红包领起
                    AccessibilityHelper.performClick(node);
                    isReceivingHongbao = false;
                }
            } else {
        		Logger.d(TAG, isReceivingHongbao + "<--isReceivingHongbao 没有\"领取红包\"（红包消息）");
        	}
        } else if (mCurrentWindow == WINDOW_CHAT && contentChange && getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
        	List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        	if(list != null && list.size() > 0) {
                AccessibilityNodeInfo node = list.get(list.size() - 1);
                Logger.i(TAG, list.size() + "<-size Node =>" + node);
                // 判断是否打开过此红包？？？
                if (node != null) {
                    //最新的红包领起
                    AccessibilityHelper.performClick(node);
                    isReceivingHongbao = false;
                }
            } else {
        		Logger.d(TAG, isReceivingHongbao + "<--isReceivingHongbao 没有\"领取红包\"（红包消息）");
        	}
        } else {
        	Logger.w(TAG, "无法识别到微信版本,采用兼容处理：");
        	if (isReceivingHongbao) {
            	List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
            	if(list != null && list.size() > 0) {
                    AccessibilityNodeInfo node = list.get(list.size() - 1);
                    Logger.i(TAG, list.size() + "<-size Node =>" + node);
                    // 判断是否打开过此红包？？？
                    if (node != null) {
                        //最新的红包领起
                        AccessibilityHelper.performClick(node);
                        isReceivingHongbao = false;
                    }
                } else {
            		Logger.d(TAG, isReceivingHongbao + "<--isReceivingHongbao 没有\"领取红包\"（红包消息）");
            	}
            } else if (contentChange && getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
            	List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
            	if(list != null && list.size() > 0) {
                    AccessibilityNodeInfo node = list.get(list.size() - 1);
                    Logger.i(TAG, list.size() + "<-size Node =>" + node);
                    // 判断是否打开过此红包？？？
                    if (node != null) {
                        //最新的红包领起
                        AccessibilityHelper.performClick(node);
                        isReceivingHongbao = false;
                    }
                } else {
            		Logger.d(TAG, isReceivingHongbao + "<--isReceivingHongbao 没有\"领取红包\"（红包消息）");
            	}
            }
        }
        mLastWindow = mCurrentWindow;
    }
    
    private boolean findRedNodes(AccessibilityNodeInfo nodeInfo) {
    	List<AccessibilityNodeInfo> redNodes = AccessibilityHelper.findNodesInfosById(nodeInfo, "com.tencent.mm:id/i0");// 新消息红点
    	if (redNodes != null && redNodes.size() > 0) {
	    	for (AccessibilityNodeInfo node : redNodes) {
	    		Logger.d(TAG, "-->红点:" + node);//////
	    		node = (node) != null ? node.getParent() : null;// = node.getParent()
	    		Logger.d(TAG, "-->红点父节点:" + node);
	    		if (node != null) {
	    			AccessibilityNodeInfo hbInfo = AccessibilityHelper.findNodeInfosById(node, "com.tencent.mm:id/adu");
	    			Logger.d(TAG, "-->[微信红包] findRedNodes:" + hbInfo);
	    			if (hbInfo != null && hbInfo.getText().toString().contains("[微信红包]")) {
	        			if(BuildConfig.DEBUG) {
						   Logger.i(TAG, "-->[微信红包] findRedNodes:" + hbInfo);
	        			}
	        			isReceivingHongbao = true;
	        			AccessibilityHelper.performClick(node);
	        			return true;
	    			}
	    		}
	    	}
    	}
//    	redNodes = AccessibilityHelper.findNodesInfosById(nodeInfo, "com.tencent.mm:id/adr");// 新消息红点
//    	if (redNodes != null && redNodes.size() > 0) {
//	    	for (AccessibilityNodeInfo node : redNodes) {
//	    		Logger.d(TAG, "-->红点:" + node.getText() + " content_descc:" + node.getContentDescription());//////
//	    		node = (node) != null ? node.getParent() : null;// = node.getParent()
//	    		Logger.d(TAG, "-->红点父节点:" + node);
//	    		if (node != null) {
//	    			AccessibilityNodeInfo hbInfo = AccessibilityHelper.findNodeInfosById(node, "com.tencent.mm:id/adu");
//	    			Logger.d(TAG, "-->[微信红包] findRedNodes:" + hbInfo.getText());
//	    			if (hbInfo != null && hbInfo.getText().toString().contains("[微信红包]")) {
//	        			if(BuildConfig.DEBUG) {
//						   Logger.i(TAG, "-->[微信红包] findRedNodes:" + hbInfo);
//	        			}
//	        			isReceivingHongbao = true;
//	        			AccessibilityHelper.performClick(node);
//	        			return true;
//	    			}
//    			}
//    		}
//    	}
    	return false;
    }
    
	/**
     * 点击聊天里的红包后，显示的界面
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive() {
    	Logger.i(TAG, "[handleLuckyMoneyReceive] wechatVersion:" + getWechatVersion());
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        AccessibilityNodeInfo targetNode = null;
		if (getConfig().getWechatMode() == Config.WX_HONGBAO_MODE_1) {
			mContentChangeCount = 2;
		}

        int event = getConfig().getWechatAfterOpenHongBaoEvent();
        int wechatVersion = getWechatVersion();
        if(event == Config.WX_AFTER_OPEN_HONGBAO) { //拆红包
            if (wechatVersion < USE_ID_MIN_VERSION) {
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "拆红包");
                Logger.e(TAG, "288打开红包按钮:" + targetNode);
            } else {
                String buttonId = "com.tencent.mm:id/b43";

                if(wechatVersion == 700) {
                    buttonId = "com.tencent.mm:id/b2c";
                }

                if(buttonId != null) {
                    targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, buttonId);
                    Logger.e(TAG, "298打开红包按钮:" + targetNode);
                }

                if(targetNode == null) {
                    //分别对应固定金额的红包 拼手气红包
                    AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, "发了一个红包", "给你发了一个红包", "发了一个红包，金额随机");
                    if(textNode != null) {
                    	Logger.e(TAG, "发了一个红包，金额随机:" + textNode);
                        for (int i = 0; i < textNode.getChildCount(); i++) {
                            AccessibilityNodeInfo node = textNode.getChild(i);
                            if (BUTTON_CLASS_NAME.equals(node.getClassName())) {
                                targetNode = node;
                                Logger.e(TAG, "310打开红包按钮:" + textNode);
                                break;
                            }
                        }
                    }
                }

                if(targetNode == null) { //通过组件查找
                    targetNode = AccessibilityHelper.findNodeInfosByClassName(nodeInfo, BUTTON_CLASS_NAME);
                    Logger.e(TAG, "319打开红包按钮:" + targetNode);
                }
            }
        } else if(event == Config.WX_AFTER_OPEN_SEE) { //看一看
            if(getWechatVersion() < USE_ID_MIN_VERSION) { //低版本才有 看大家手气的功能
                targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, "看看大家的手气");
                Logger.e(TAG, "325看看大家的手气按钮:" + targetNode);
            }
        } else if(event == Config.WX_AFTER_OPEN_NONE) {
            return;
        }
        
        if(targetNode != null) {
        	 Logger.e(TAG, "自动打开:" + targetNode);
            final AccessibilityNodeInfo n = targetNode;
            long sDelayTime = getConfig().getWechatOpenDelayTime();
            if(sDelayTime != 0) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AccessibilityHelper.performClick(n);
                    }
                }, sDelayTime);
            } else {
                AccessibilityHelper.performClick(n);
            }
            if(event == Config.WX_AFTER_OPEN_HONGBAO) {
                V5Application.eventStatistics(getContext(), "open_hongbao");
            } else {
                V5Application.eventStatistics(getContext(), "open_see");
            }
        } else { // 已抢
        	Logger.e(TAG, "已抢");
        	//分别对应固定金额的红包 拼手气红包
            AccessibilityNodeInfo textNode = AccessibilityHelper.findNodeInfosByTexts(nodeInfo, "手慢了，红包派完了");
            if(textNode != null) {
            	Logger.e(TAG, "手慢了，红包派完了:" + textNode);
            	// 没抢到，返回
//            	AccessibilityHelper.performBack(getService());
            }
        }
    }

    /** 获取微信的版本*/
    private int getWechatVersion() {
        if(mWechatPackageInfo == null) {
            return 0;
        }
        return mWechatPackageInfo.versionCode;
    }

    /** 更新微信包信息*/
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
