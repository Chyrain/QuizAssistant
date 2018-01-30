package com.chyrain.quizassistant.job;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.service.IStatusBarNotification;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.Util;

import java.util.List;

/**
 * 微信AccessibilityEvent和通知事件接收处理
 * @author Chyrain
 * <p>Created: 2016-12-25 上午11:17:24.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://www.chyrain.com">chyrain.com</a></p>
 * Company <a href="http://www.v5kf.com">深圳市智客网络科技有限公司</a>
 * Edit:
 * 		2017/1/03 [修改] 辅助服务息屏唤醒
 */
public class ChongdingAccessbilityJob extends DatiAccessbilityJob {

    private static final String TAG = "ChongdingAccessbilityJob";

    /** 监听应用的包名(需添加到——service_config.xml) */
    private static final String THE_PACKAGENAME = "com.chongdingdahui.app"; //"com.chongdingdahui.app";
    private static final String THE_JOBKEY = "cddh";

    /** 红包消息的关键字*/
    private static final String APP_NAME = "[冲顶大会]";

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
        mScreenListener.unregisterListener();
        mAITask.stopTask();
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
        return getConfig().isEnableWechat() && getConfig().isEnableCddh();
    }

    @Override
    public void onEnableChange(boolean enable) {
        Logger.d(TAG, TAG + ".onEnableChange: " + enable + " mCurrentWindow: " + mCurrentWindow);
        if (enable && mCurrentWindow == WINDOW_QUIZ_PAGE) {
            mAITask.startTask();
        } else {
            mAITask.stopTask();
        }
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
        } else
        /*
         * 处理页面变化（检测到进入答题页面，开启轮训查找答案，然后接收到答案进行模拟点击处理）
         * 答题模式： 1.自动选择答案提交，2.指出答案，人工手动提交
         */
        if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) { /* 窗口切换 activity类名：event.getClassName() */
            Logger.i(TAG, event.getClassName() + " -> [TYPE_WINDOW_STATE_CHANGED] mContentChangeCount:" + mContentChangeCount
                    + " mLastWindow:" + mLastWindow + " mCurrentWindow:" + mCurrentWindow + " event=>" + event);
            if (event.getClassName().equals("com.chongdingdahui.app.ui.MainActivity")) {
                mLastWindow = mCurrentWindow;
                mCurrentWindow = WINDOW_MAIN_PAGE;
                Logger.i(TAG, "mCurrentWindow:" + mCurrentWindow + " activity=>" + event.getClassName());

                if (Config.getConfig(getContext()).isEnableWechat()) {
                    // 进入答题页面
                    clickAtNodeWithContent("点击观看");
                }
            } else if (event.getClassName().equals("com.chongdingdahui.app.ui.LiveActivity")) {
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
            // 进入答题页面查找"点击观看"按钮并点击
            if (mCurrentWindow == WINDOW_MAIN_PAGE || mCurrentWindow == WINDOW_OTHER_PAGE) {
                clickAtNodeWithContent("点击观看");
            } else if (mCurrentWindow == WINDOW_QUIZ_PAGE) {
                clickAtNodeWithContent("继续观看");//??
                if (mCurrentQuiz != null) {
                    handleReceiveQuizAnswer();
                }
            }
        }
    }

    /**
     * 处理通知栏事件
     */
    private void notificationEvent(String ticker, Notification nf) {
        if (Util.shouldResponseToNotifyContent(ticker)) {
            // 点击通知打开App
            openNotification(nf);
        }
    }

    @Override
    public void onReceiveAnswer(final QuizBean quiz) {
        mCurrentQuiz = quiz;
        Logger.w(TAG, quiz.getIndex() + " [onReceiveNextAnswer] title: " + quiz.getTitle() +
                "  answers: " + quiz.getAnswers() +  "  answer: " + quiz.getResult());
        Logger.e(TAG, "clickAtNodeWithContent 查找点击:" + quiz.getResult());

        if (getConfig().isEnableShowAnswer()) {
            // 选择
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentQuiz != null) {
                        Toast.makeText(getContext(), "推荐答案：" + mCurrentQuiz.getResult(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        handleReceiveQuizAnswer();
        // id点击处理
        if (getConfig().isEnableAutoTrust() && mCurrentQuiz != null) {
            // 点击答案选项id
            clickAtNodeWithId("answer" + mCurrentQuiz.getAnsIndex());
        }
    }
}
