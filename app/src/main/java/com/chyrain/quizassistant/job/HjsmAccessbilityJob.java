package com.chyrain.quizassistant.job;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.service.IStatusBarNotification;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Logger;

import java.util.List;

/**
 * AccessibilityEvent和通知事件接收处理
 * @author Chyrain
 * <p>Created: 2017-1-25 上午11:17:24.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://chyrain.github.io">chyrain.github.io</a></p>
 * Edit
 * 		2017/1/03 [修改] 辅助服务息屏唤醒
 */
public class HjsmAccessbilityJob extends DatiAccessbilityJob {

    private static final String TAG = "HjsmAccessbilityJob";

    /** 监听应用的包名(需添加到——service_config.xml) */
    private static final String THE_PACKAGENAME = "com.yixia.liveshow";
    private static final String THE_JOBKEY = "hjsm";

    /** 红包消息的关键字*/
    private static final String APP_NAME = "黄金十秒";

    /** 当前所在页面 */
    private static final int WINDOW_MAIN_PAGE = 1; // 主页（答题前一页）
    private static final int WINDOW_QUIZ_PAGE = 2; // 答题页
    private static final int WINDOW_OTHER_PAGE = 3; // App内其他页面

    @Override
    public void onCreateJob(WxBotService service) {
        super.onCreateJob(service);
        Logger.i(TAG, "onCreateJob: " + THE_PACKAGENAME);
    }

    @Override
    public void onStopJob() {
        super.onStopJob();
        Logger.i(TAG, "onStopJob: " + THE_PACKAGENAME);
        //mScreenListener.unregisterListener();
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
    public String getJobKey() {
        return THE_JOBKEY;
    }

    @Override
    public String getAppName() {
        return APP_NAME;
    }

    @Override
    public String getTargetPackageName() {
        return THE_PACKAGENAME;
    }

    @Override
    public boolean isEnable() {
        return getConfig().isEnableWechat() && getConfig().isEnableHJSM();
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Logger.v(TAG, "onReceiveJob：" + event.getPackageName() + " type:" + AccessibilityEvent.eventTypeToString(event.getEventType()));

        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if(data == null || !(data instanceof Notification)) {
                return;
            }

            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                String text = String.valueOf(texts.get(0));
                notificationEvent(text, (Notification) data);
            }
        }
        /*
         * 处理页面变化（检测到进入答题页面，开启轮训查找答案，然后接收到答案进行模拟点击处理）
         * 答题模式： 1.自动选择答案提交，2.指出答案，人工手动提交
         */
        else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) { /* 窗口切换 activity类名：event.getClassName() */
            Logger.i(TAG, event.getClassName() + " -> [TYPE_WINDOW_STATE_CHANGED] mContentChangeCount:" + mContentChangeCount
                    + " mLastWindow:" + mLastWindow + " mCurrentWindow:" + mCurrentWindow + " event=>" + event);
            if (event.getClassName().equals("com.yixia.liveshow.controllers.activtiy.HomeActivity")) {
                mLastWindow = mCurrentWindow;
                mCurrentWindow = WINDOW_MAIN_PAGE;
                Logger.i(TAG, "mCurrentWindow:" + mCurrentWindow + " activity=>" + event.getClassName());

                if (getConfig().isEnableAutoTrust()) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 进入答题页面
                            clickAtNodeWithContent("进入直播间");
                        }
                    }, 2000);
                }
            } else if (event.getClassName().equals("com.yixia.liveplay.activity.AnswerActivity")) {
                mLastWindow = mCurrentWindow;
                mCurrentWindow = WINDOW_QUIZ_PAGE;
                Logger.i(TAG, "mCurrentWindow:" + mCurrentWindow + " activity=>" + event.getClassName());

                if (Config.getConfig(getContext()).isEnableWechat()) {
                    // 开启轮询
                    onEnableChange(true);
                }
            } else {
                mLastWindow = mCurrentWindow;
                mCurrentWindow = WINDOW_OTHER_PAGE;
                Logger.i(TAG, "mCurrentWindow:" + mCurrentWindow + " activity=>" + event.getClassName());
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) { /* 内容改变 组件类名：event.getClassName() */
            Logger.v(TAG, event.getClassName() + " -> [TYPE_WINDOW_CONTENT_CHANGED] mContentChangeCount:" + mContentChangeCount
                    + " mLastWindow:" + mLastWindow + " mCurrentWindow:" + mCurrentWindow + " event=>" + event);
            // 进入答题页面查找"点击进入"按钮并点击
            if ((mCurrentWindow == WINDOW_MAIN_PAGE || mCurrentWindow == WINDOW_OTHER_PAGE) && getConfig().isEnableAutoTrust()) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 进入答题页面
                        clickAtNodeWithContent("进入直播间");
                    }
                }, 2000);
            } else if (mCurrentWindow == WINDOW_QUIZ_PAGE) {
                //clickAtNodeWithContent("继续观看");
                handleReceiveQuizAnswer();
            }
        }
    }

    /**
     * 处理通知栏事件
     */
    private void notificationEvent(String ticker, Notification nf) {
        Logger.d(TAG, "notificationEvent ticker:" + ticker + " Notification:" + nf);
        if (shouldResponseToNotifyContent(ticker)) {
            // 点击通知打开App
            openNotification(nf);
        }
    }

    @Override
    public void onReceiveAnswer(final QuizBean quiz) {
        mCurrentQuiz = quiz;
        Logger.w(TAG, quiz.getIndex() + " [onReceiveNextAnswer] title: " + quiz.getTitle() +
                "  answers: " + quiz.getAnswers() +  "  answer: " + quiz.getResult());
        if (!quiz.isRandom()) {
            Logger.e(TAG, "clickAtNodeWithContent 查找点击:" + quiz.getResult());
            handleReceiveQuizAnswer();
            if (getConfig().isEnableAutoTrust()) { // 机器人托管自动回复
                String id = "";
                int ansIndex = quiz.getAnsIndex();
                if (ansIndex == 0) {
                    id = "option_one";
                } else if (ansIndex == 1) {
                    id = "option_two";
                } else if (ansIndex == 2) {
                    id = "option_three";
                }
                // 答题dialog ID：com.yixia.liveshow:id/answer_mode_dialog => inner_dialog_container
                // 倒计时ID ImageView:timer_down
                // 答题序号ID：com.yixia.liveshow:id/text_number text:6/10
                // 题目ID：com.yixia.liveshow:id/text_question
                // 点击答案选项id
                clickAtNodeWithId(id);
            }
        }
    }
}
