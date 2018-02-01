package com.chyrain.quizassistant.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.chyrain.quizassistant.BuildConfig;
import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.aitask.AITask;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.job.ChongdingAccessbilityJob;
import com.chyrain.quizassistant.job.DatiAccessbilityJob;
import com.chyrain.quizassistant.job.HjsmAccessbilityJob;
import com.chyrain.quizassistant.job.HuajiaoAccessbilityJob;
import com.chyrain.quizassistant.job.InkeAccessbilityJob;
import com.chyrain.quizassistant.job.XiguaAccessbilityJob;
import com.chyrain.quizassistant.job.ZhishiAccessbilityJob;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.NotifyHelper;

/**
 * <p>Created by LeonLee on 15/2/17 下午10:25.</p>
 * <p><a href="mailto:codeboy2013@163.com">Email:codeboy2013@163.com</a></p>
 *
 * 答题辅助服务
 */
public class WxBotService extends AccessibilityService {

    private static final String TAG = "WxBotService";
    private static WxBotService service;
    private AITask mAITask;
    private DatiAccessbilityJob mCurrentJob;

    /**
     * 所支持的答题平台任务类名
     */
    private static final Class<?>[] ACCESSBILITY_JOBS= {
            ZhishiAccessbilityJob.class,
            ChongdingAccessbilityJob.class,
            XiguaAccessbilityJob.class,
            InkeAccessbilityJob.class, // 暂不支持
            HuajiaoAccessbilityJob.class,
            HjsmAccessbilityJob.class
    };
    /**
     * 所支持的答题平台任务对象实例
     */
    private List<DatiAccessbilityJob> mAccessbilityJobs;
    private HashMap<String, DatiAccessbilityJob> mPkgAccessbilityJobMap;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "WxBotService [onCreate]");
        // 注册对象
        EventBus.getDefault().register(this);
        
        NotifyHelper.sSoundPool = new SoundPool(10, AudioManager.STREAM_RING, 0);
        NotifyHelper.sSoundPool.load(getApplicationContext(), R.raw.hongbao,1);
        mAccessbilityJobs = new ArrayList<>();
        mPkgAccessbilityJobMap = new HashMap<>();

        //初始化辅助插件工作
        for(Class<?> clazz : ACCESSBILITY_JOBS) {
            try {
                Object object = clazz.newInstance();
                if(object instanceof DatiAccessbilityJob) {
                    DatiAccessbilityJob job = (DatiAccessbilityJob) object;
                    job.onCreateJob(this);
                    mAccessbilityJobs.add(job);
                    mPkgAccessbilityJobMap.put(job.getTargetPackageName(), job);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        // FloatView改在MainActivity
//        boolean showFloat = Config.getConfig(getApplicationContext()).isEnableFloatButton();
//        if (showFloat) {
//            showFloatView();
//        }
        mAITask = new AITask(mAccessbilityJobs.get(0), new AITask.TaskRequestCallback() {

            @Override
            public void onReceiveAnswer(DatiAccessbilityJob job, QuizBean quiz) {
                Logger.d(TAG + ":" + job.getTargetPackageName(), quiz.getIndex() + " [onReceiveAnswer] title: " + quiz.getTitle() +
                        "  answers: " + quiz.getAnswers() + " result: " + quiz.getResult());
            }

            @Override
            public void onReceiveNextAnswer(DatiAccessbilityJob job, final QuizBean quiz) {
                Logger.w(TAG + ":" + job.getTargetPackageName(), quiz.getIndex() + " [onReceiveNextAnswer] title: " + quiz.getTitle() +
                        "  answers: " + quiz.getAnswers() +  "  answer: " + quiz.getResult());
                job.onReceiveAnswer(quiz);
                EventBus.getDefault().post(quiz, Config.EVENT_TAG_UPDATE_QUIZ);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "[onDestroy] WxBotService");
        // 注销对象
        EventBus.getDefault().unregister(this);
        
        if(mPkgAccessbilityJobMap != null) {
            mPkgAccessbilityJobMap.clear();
        }
        if(mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (DatiAccessbilityJob job : mAccessbilityJobs) {
                job.onStopJob();
            }
            mAccessbilityJobs.clear();
        }

        if (mAITask != null) {
            mAITask.stopTask();
        }

        service = null;
        mAccessbilityJobs = null;
        mPkgAccessbilityJobMap = null;
        //发送广播，已经断开辅助服务
        //Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        //sendBroadcast(intent);
        Logger.d("Service", "已关闭辅助服务");
        EventBus.getDefault().post(this, Config.EVENT_TAG_ROBOT_SERVICE_DISCONNECT);
    }

    @Override
    public void onInterrupt() {
    	Logger.w(TAG, "[onInterrupt]");
        Toast.makeText(this, "中断辅助服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Logger.i(TAG, "[WxBotService -> onServiceConnected]");
        service = this;
        //发送广播，已经连接上了
        //Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        //sendBroadcast(intent);
        EventBus.getDefault().post(this, Config.EVENT_TAG_ROBOT_SERVICE_CONNECT);
        Logger.d("Service", "已连接辅助服务");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//    	Logger.d(TAG, "[AccessibilityService -> onAccessibilityEvent] -> " + event);
        if(BuildConfig.DEBUG) {
            Logger.d(TAG, event.getPackageName() + "上的事件--->" + AccessibilityEvent.eventTypeToString(event.getEventType()));
        }
        String pkn = String.valueOf(event.getPackageName());
        if(mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            if(!getConfig().isAgreement()) {
                return;
            }
            for (DatiAccessbilityJob job : mAccessbilityJobs) {
                if(pkn.equals(job.getTargetPackageName()) && job.isEnable()) {
                    if (job != mCurrentJob) {
                        mCurrentJob = job;
                        onAccessibilityJobChange(job);
                    }
                    if (mAITask.isTaskStoped()) {
                        mAITask.startTask();
                    }
                    job.onReceiveJob(event);
                }
            }
        }
    }

    public static boolean isEnable(Context context) {
        return isRunning() && Config.getConfig(context).isEnableWechat();
    }

    /**
     * 服务总开关状态变化
     * @param enable 是否开启服务
     */
    public void onEnableChange(boolean enable) {
        Logger.i(TAG, "onEnableChange: " + enable);
        if(mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (DatiAccessbilityJob job : mAccessbilityJobs) {
                job.onEnableChange(enable);
            }
        }
        if (mAITask != null) {
//            if (enable) {
//                mAITask.startTask();
//            } else {
//                mAITask.stopTask();
//            }
            if (!enable) {
                mAITask.stopTask();
            }
        }
    }

    public void onAccessibilityJobChange(DatiAccessbilityJob accessbilityJob) {
        mAITask.setAccessbilityJob(accessbilityJob);
        EventBus.getDefault().post(accessbilityJob, Config.EVENT_TAG_ACCESSBILITY_JOB_CHANGE);
    }

    public Config getConfig() {
        return Config.getConfig(this);
    }

    /** 接收通知栏事件*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void handeNotificationPosted(IStatusBarNotification notificationService) {
    	if(notificationService == null) {
            return;
        }
        if(service == null || service.mPkgAccessbilityJobMap == null) {
            return;
        }
        String pack = notificationService.getPackageName();
//        if(BuildConfig.DEBUG) {
//        	Logger.v(TAG, "onNotificationPosted: " + pack);
//        }
        DatiAccessbilityJob job = service.mPkgAccessbilityJobMap.get(pack);
        if(job == null) {
            return;
        }
        if(BuildConfig.DEBUG) {
        	Logger.v(TAG, "job.onNotificationPosted");
        }
        job.onNotificationPosted(notificationService);
    }


    /**
     * 判断当前服务是否正在运行
     **/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if(info == null) {
            return false;
        }
        assert accessibilityManager != null;
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        return isConnect;
    }

    /** 快速读取通知栏服务是否启动*/
    public static boolean isNotificationServiceRunning() {
        //部份手机没有NotificationService服务
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        try {
            return WxBotNotificationService.isRunning();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

//    public static boolean isBackground(String packageName, Context context) {
//        ActivityManager activityManager = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
//                .getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//            Logger.d(context.getPackageName(), "此appimportace ="
//                    + appProcess.importance
//                    + ",context.getClass().getName()="
//                    + context.getClass().getName());
//            if (appProcess.processName.equals(packageName)) {
//                /*
//                BACKGROUND=400 EMPTY=500 FOREGROUND=100
//                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
//                 */
//                Logger.i(context.getPackageName(), "此appimportace ="
//                        + appProcess.importance
//                        + ",context.getClass().getName()="
//                        + context.getClass().getName());
//                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    Log.i(context.getPackageName(), "处于后台"
//                            + appProcess.processName);
//                    return true;
//                } else {
//                    Log.i(context.getPackageName(), "处于前台"
//                            + appProcess.processName);
//                    return false;
//                }
//            }
//        }
//        return false;
//    }
//
//    @Subscriber(tag = Config.EVENT_TAG_STOP_WXBOT, mode=ThreadMode.MAIN)
//	private void stopWxBotService(WxBotNotificationService service) {
//		Logger.i("event-tag", "EVENT_TAG_STOP_WXBOT");
//		this.stopSelf();
//	}
//
//	/** 打开本界面 **/
//	private void startMainActivity() {
//		Intent i = new Intent(this, MainActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//		startActivity(i);
//	}
//
//	private Context getActivityContext() {
//		Activity act = V5Application.getContextActivity();
//		return act != null ? act : this;
//	}
	
	/** event **/
    
    @Subscriber(tag = Config.EVENT_TAG_UPDATE_FLOAT_STATUS, mode=ThreadMode.MAIN)
    private void onUpdateFloatStatusEvent(Boolean light) {
    	Logger.i("event-tag", "EVENT_TAG_UPDATE_FLOAT_STATUS");
//    	updateWFV(light);
        onEnableChange(light);
    }
}
