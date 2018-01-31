package com.chyrain.quizassistant.job;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.aitask.AITask;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.service.ScreenListener;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.NotifyHelper;

/**
 * AccessibilityEvent接收答题类型基类处理
 * @author Chyrain
 * <p>Created: 2017-1-25 上午11:17:24.</p>
 * <p>Email: <a href="mailto:chyrain_v5kf@qq.com">chyrain_v5kf@qq.com</a></p>
 * <p>Blog: <a href="http://chyrain.github.io">chyrain.github.io</a></p>
 */
public abstract class DatiAccessbilityJob extends BaseAccessbilityJob {
    private static final String TAG = "DatiAccessbilityJob";

    public static final int WINDOW_OTHER = -1; // 锁屏页
    public static final int WINDOW_NONE = 0; // 未知页面

    String mCurrChat = "";
    int mContentChangeCount = 0;
    int mLastWindow = WINDOW_NONE;
    int mCurrentWindow = WINDOW_NONE;

    QuizBean mCurrentQuiz;
    ScreenListener mScreenListener;
    boolean isReceivingHongbao;
//    AITask mAITask;
    PendingIntent mPendingIntent; // 收到通知时赋值

    public abstract String getJobKey();
    public abstract void onReceiveAnswer(QuizBean quiz);
    public abstract String getAppName();

    @Override
    public void onCreateJob(WxBotService service) {
        super.onCreateJob(service);

        Logger.i(TAG + ":" + getTargetPackageName(), "onCreateJob: " + getTargetPackageName());

//        mAITask = new AITask(getJobKey(), new AITask.TaskRequestCallback() {
//
//            @Override
//            public void onReceiveAnswer(QuizBean quiz) {
//                Logger.d(TAG + ":" + getTargetPackageName(), quiz.getIndex() + " [onReceiveAnswer] title: " + quiz.getTitle() +
//                        "  answers: " + quiz.getAnswers() + " result: " + quiz.getResult());
//            }
//
//            @Override
//            public void onReceiveNextAnswer(final QuizBean quiz) {
//                mCurrentQuiz = quiz;
//                Logger.w(TAG + ":" + getTargetPackageName(), quiz.getIndex() + " [onReceiveNextAnswer] title: " + quiz.getTitle() +
//                        "  answers: " + quiz.getAnswers() +  "  answer: " + quiz.getResult());
//                Logger.e(TAG, "clickAtNodeWithContent 查找点击:" + quiz.getResult());
//                onReceiveAnswer(quiz);
//            }
//        });

        /** 屏幕状态监听 **/
        mScreenListener = new ScreenListener(getContext());
        mScreenListener.begin(new ScreenListener.ScreenStateListener() {

            @Override
            public void onUserPresent() {
                Logger.e(TAG + ":" + getTargetPackageName(), "onUserPresent");
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
                Logger.e(TAG + ":" + getTargetPackageName(), "onScreenOn");
                if (isReceivingHongbao && mPendingIntent != null
                        && getConfig().isEnableAutoUnlock()) {
                    NotifyHelper.send(mPendingIntent);
                    NotifyHelper.send(mPendingIntent);
                    mPendingIntent = null;
                }
                mLastWindow = mCurrentWindow;
                mCurrentWindow = WINDOW_OTHER;
                onEnableChange(true);
            }

            @Override
            public void onScreenOff() {
                Logger.e(TAG + ":" + getTargetPackageName(), "onScreenOff");
                mCurrentWindow = mLastWindow;
                mLastWindow = WINDOW_OTHER;
                onEnableChange(false);
            }
        });
    }

    @Override
    public void onStopJob() {
        mScreenListener.unregisterListener();
    }

    @Override
    public void onEnableChange(boolean enable) {
        Logger.d(TAG, TAG + ".onEnableChange: " + enable + " mCurrentWindow: " + mCurrentWindow);
//        if (enable && mCurrentWindow == WINDOW_QUIZ_PAGE) {
//            mAITask.startTask();
//        } else {
//            mAITask.stopTask();
//        }
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void openNotification(Notification notification) {
        isReceivingHongbao = true;
        //以下是精华，将微信的通知栏消息打开
        mPendingIntent = notification.contentIntent;
//        boolean lock = NotifyHelper.isLockScreen(getContext());
        boolean unlock = mScreenListener.isScreenOn();
        Logger.d(TAG, "[newHongBaoNotification=>openNotification]  unlock:" + unlock);
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

    protected void handleReceiveQuizAnswer() {
//        if (getConfig().isEnableShowAnswer()) {
//            // 选择
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mCurrentQuiz != null) {
//                        Toast.makeText(getContext(), "推荐答案：" + mCurrentQuiz.getResult(), Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        }
        // 查找答案并处理
        handleNodeWithContent(mCurrentQuiz.getResult());
    }

    /**
     * 点击带指定文本的node
     * @param content 内容
     */
    protected void clickAtNodeWithContent(String content) {
        Logger.d(TAG, "[clickAtNodeWithContent] content:" + content);
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Logger.e(TAG, "[clickAtNodeWithContent] rootWindow为空");
            return;
        }
        AccessibilityNodeInfo targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, content);
        if(targetNode != null) {
            Logger.e(TAG, "自动点击(" + content + "):" + targetNode);
            AccessibilityHelper.performClick(targetNode);
        }
    }

    /**
     * 点击带指定id的node
     * @param id 布局id
     */
    protected void clickAtNodeWithId(String id) {
        Logger.d(TAG, "[clickAtNodeWithId] id:" + id);
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Logger.e(TAG, "[clickAtNodeWithId] rootWindow为空");
            return;
        }
        AccessibilityNodeInfo targetNode = AccessibilityHelper.findNodeInfosById(nodeInfo, getTargetPackageName() + ":id/" + id);
        if(targetNode != null) {
            Logger.e(TAG, "[clickAtNodeWithId] 自动点击(id=" + id + "):" + targetNode);
            AccessibilityHelper.performClick(targetNode);
            mCurrentQuiz = null; //已点击则置空
        }
    }

    /**
     * 点击带指定文本的node
     * @param content 内容
     */
    protected void handleNodeWithContent(String content) {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Logger.e(TAG + ":" + getTargetPackageName(), "[clickAtNodeWithContent]:" + content + " rootWindow为空");
            return;
        }
        AccessibilityNodeInfo targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, content);
        if(targetNode != null) {
            if (getConfig().isEnableAutoTrust()) {
                Logger.e(TAG + ":" + getTargetPackageName(), "自动点击(" + content + "):" + targetNode);
                AccessibilityHelper.performClick(targetNode);
                mCurrentQuiz = null; //已点击则置空
            }
            if (getConfig().isEnableShowAnswer()) {
                try {
                    targetNode.setText(targetNode.getText() + "（推荐）");
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 内容是否包含答题信息
     * @param ticker 内容
     * @return
     */
    protected boolean shouldResponseToNotifyContent(String ticker) {
        return ticker.contains("答题开始") || ticker.contains("开始答题") || ticker.contains("答题就要开始")
                || ticker.contains("答题即将开始") || ticker.contains("答题马上开始")
                || ticker.contains("答题狂欢马上开始") || ticker.contains("本场奖金");
    }
}
