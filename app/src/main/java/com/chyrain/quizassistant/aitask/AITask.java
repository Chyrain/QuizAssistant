package com.chyrain.quizassistant.aitask;

import android.text.TextUtils;
import android.util.Base64;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.job.DatiAccessbilityJob;
import com.chyrain.quizassistant.util.HttpResponseHandler;
import com.chyrain.quizassistant.util.HttpUtil;
import com.chyrain.quizassistant.util.Logger;
import com.tencent.android.tpush.XGPushConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chyrain on 25/01/2018.
 * AI http请求任务
 */
public class AITask {
    private static final String TAG = "AITask";

    public interface TaskRequestCallback {
        public void onReceiveAnswer(DatiAccessbilityJob accessbilityJob, QuizBean quiz);
        public void onReceiveNextAnswer(DatiAccessbilityJob accessbilityJob, QuizBean quiz);
    }

    private DatiAccessbilityJob accessbilityJob;
    private TaskRequestCallback callback;
    private String cbPrefix;
    private int maxQuizIndex;
    private TaskThread mThread;

    private HashMap<String, String> mQuizMap;

    public AITask(DatiAccessbilityJob accessbilityJob, TaskRequestCallback _cb) {
        Date date = new Date();
        this.accessbilityJob = accessbilityJob;
        this.cbPrefix = "jQuery1124027865239162929356_" + date.getTime();
//        String _url = "http://140.143.49.31/api/ans2?key=" + key + "&wdcallback=" + cbPrefix + "&_=";
        this.callback = _cb;
    }

    public boolean isTaskStoped() {
        return this.mThread == null || !this.mThread.isRunning;
    }

    /**
     * 开启轮询
     */
    public void startTask() {
        Logger.i(TAG, "startTask");
        if (this.mThread == null || !this.mThread.isAlive()) {
            this.mThread = new TaskThread(this.callback);
            this.mThread.start();
        }
        if (mQuizMap == null) {
            mQuizMap = new HashMap<>();
        }
    }

    public void stopTask() {
        Logger.i(TAG, "stopTask");
        if (this.mThread != null && this.mThread.isRunning) {
            this.mThread.setRunning(false);
            this.mThread.interrupt();
        }
        if (mQuizMap != null) {
            mQuizMap.clear();
        }
    }

    public DatiAccessbilityJob getAccessbilityJob() {
        return accessbilityJob;
    }

    public void setAccessbilityJob(DatiAccessbilityJob accessbilityJob) {
        this.accessbilityJob = accessbilityJob;
    }

    public int getMaxQuizIndex() {
        return maxQuizIndex;
    }

    public void setMaxQuizIndex(int maxQuizIndex) {
        this.maxQuizIndex = maxQuizIndex;
    }

    /**
     * 从服务器端获取消息
     *
     */
    class TaskThread extends Thread {
        private TaskRequestCallback callback;
        //运行状态，下一步骤有大用
        public boolean isRunning = true;

        public TaskThread(TaskRequestCallback _cb) {
            this.callback = _cb;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public void run() {
            Logger.i(TAG, "Thread.run:" + isRunning);
            while(isRunning){
                try {
                    final String key = accessbilityJob.getJobKey();
                    // 获取服务器消息
                    if (key == null || this.callback == null) {
                        Logger.w(TAG, "未设置参数url 和 callback");
                        isRunning = false;
                        return;
                    }
                    final  String _url = "https://wdpush.sogoucdn.com/api/anspush?key=" + key + "&wdcallback=" + cbPrefix + "&_=" + (new Date()).getTime();
//                    final String _url = "https://140.143.49.31/api/ans2?key=" + key + "&wdcallback=" + cbPrefix + "&_=" + (new Date()).getTime();
                    //Logger.i(TAG, "[startTask] request: " + this.url);
                    Map<String, String> headers = new HashMap<>();
                    // 开启了cookie验证，一年有效期，SGS-ID需要生成
                    String APP_SGS_ID = "SQ3Thn3ruhzpAr89JvBwGsUe2p2LUoWK";
                    if (!TextUtils.isEmpty(Config.DEVICE_TOKEN)) {
                        APP_SGS_ID = Config.DEVICE_TOKEN.substring(0, 32);
                    }
                    headers.put("Cookie","APP-SGS-ID=" + APP_SGS_ID + ";expires=1549992329924;path=/;domain=.sogoucdn.com");
//                    headers.put("Content-Type", "text/html; charset=utf-8");
                    headers.put("Host", "wdpush.sogoucdn.com");
                    headers.put("Accept", "*/*");
                    headers.put("Connection", "keep-alive");
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; SAMSUNG-SM-N900A Build/tt) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 SogouSearch Android1.0 version3.0 AppVersion/5917");
                    //headers.put("Referer", "https://assistant.sogoucdn.com");
                    headers.put("Referer", "https://assistant.sogoucdn.com/v5/cheat-sheet?channel=" + key);
                    headers.put("X-Requested-With", "com.sogou.activity.src");

                    // 同步请求
                    HttpUtil.httpSync(
                            _url,
                            HttpUtil.HttpMethod.GET,
                            null,
                            headers,
                            new HttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, String responseString) {
                            Logger.v(TAG, "httpSync: " + _url + "\n[onSuccess] responseString：" + responseString);
                            try {
                                if (responseString == null || responseString.length() <= cbPrefix.length()) {
                                    Logger.w(TAG, "responseString error:" + responseString);
                                    return;
                                }
                                String jsonStr = responseString.substring(cbPrefix.length() + 1, responseString.length() - 1);
                                JSONObject json = new JSONObject(jsonStr);
                                if (json.getInt("code") == 0 && json.getString("result") != null) {
                                    String json_result = new String(Base64.decode(json.getString("result").getBytes(), Base64.DEFAULT));
                                    Logger.d(TAG, "httpSync: " + _url + "\n[onSuccess] json_result：" + json_result);

                                    JSONArray results = new JSONArray(json_result);
                                    QuizBean lastQuiz = null;
                                    for (int i = 0; i < results.length(); i++) {
                                        String item = results.getString(i);
                                        JSONObject answer = new JSONObject(item);
                                        String channel = answer.getString("channel");
                                        QuizBean quiz = new QuizBean(answer);
                                        String title = quiz.getTitle();
                                        String result = quiz.getResult();
                                        int ansIndex = quiz.getAnsIndex();

                                        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(result)) {
                                            int index = 0;
                                            try {
                                                // 解析题目序号
                                                String[] rsts = title.split("\\.");
                                                if (rsts.length > 0) {
                                                    index = Integer.valueOf(rsts[0]);
                                                }
                                            } catch (Exception e) {
                                            }

                                            quiz.setIndex(index);
                                            String res = mQuizMap.get(title);
                                            if (TextUtils.isEmpty(res) || !res.equals(result)) {
                                                if (!quiz.isRandom()) {
                                                    mQuizMap.put(title, result);
                                                } else {
                                                    mQuizMap.put(title, "");
                                                }
                                                if (isMatchChannel(channel, key)) {
                                                    if (quiz.isRandom()) {
                                                        Logger.w(TAG, "答案" + (quiz.isRandom() ? "(随机)" : "") + "：" + result + " 题目：" + title);
                                                    } else {
                                                        Logger.e(TAG, "答案" + "：" + result + " 题目：" + title);
                                                    }
                                                    if (res == null || res.length() > 0) {
                                                        // 新题(第一次答题给出随机答案或者有答案的情况下)
                                                        callback.onReceiveNextAnswer(accessbilityJob, quiz);
                                                    }
                                                } else {
                                                    if (i == 1 && lastQuiz != null && lastQuiz.getAnswers() != null
                                                            && lastQuiz.getAnswers().size() >= 2) {
                                                        quiz.setResult("答题还没有开始");
                                                    } else {
                                                        quiz.setResult("获取答案失败");
                                                    }
                                                    quiz.setNoanswer(true);
                                                    if (res == null || res.length() > 0) {
                                                        // 新题(第一次答题给出随机答案或者有答案的情况下)
                                                        callback.onReceiveNextAnswer(accessbilityJob, quiz);
                                                    }
                                                }
                                            }
                                            callback.onReceiveAnswer(accessbilityJob, quiz);
                                        }
                                        lastQuiz = quiz;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, String responseString) {
                            Logger.w(TAG, "[onFailure] statusCode" + statusCode + " responseString：" + responseString);
                        }
                    });

                    // 休息500ms
                    Thread.sleep(Config.getConfig(V5Application.getInstance()).getThrottleTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isMatchChannel(String channel, String key) {
        if (key.equals(channel)) {
            return true;
        }
        switch (channel) {
            case "huajiao":
            case "hj": // huajiao
            case "bwyx":
            case "bwyj":
            case "xigua": //bwyx
            case "cddh":
            case "zscr":
            case "hjsm":
                return true;
        }
        return false;
    }
}
