package com.chyrain.quizassistant.aitask;

import android.text.TextUtils;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.util.HttpResponseHandler;
import com.chyrain.quizassistant.util.HttpUtil;
import com.chyrain.quizassistant.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
        public void onReceiveAnswer(QuizBean quiz);
        public void onReceiveNextAnswer(QuizBean quiz);
    }

    private String key;
    private TaskRequestCallback callback;
    private String cbPrefix;
    private int maxQuizIndex;
    private TaskThread mThread;

    private HashMap<String, String> mQuizMap;

    public AITask(String key, TaskRequestCallback _cb) {
        Date date = new Date();
        this.key = key;
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

    public String getKey() {
        return key;
    }

    public void setKey(String url) {
        this.key = url;
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
                    // 获取服务器消息
                    if (key == null || this.callback == null) {
                        Logger.w(TAG, "未设置参数url 和 callback");
                        isRunning = false;
                        return;
                    }
                    final String _url = "http://140.143.49.31/api/ans2?key=" + key + "&wdcallback=" + cbPrefix + "&_=" + (new Date()).getTime();
                    //Logger.i(TAG, "[startTask] request: " + this.url);
                    Map<String, String> headers = new HashMap<>();
                    //headers.put("Content-Type", "text/html; charset=utf-8");
                    headers.put("Host", "140.143.49.31");
                    headers.put("Accept", "*/*");
                    headers.put("Connection", "keep-alive");
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.4; SAMSUNG-SM-N900A Build/tt) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Mobile Safari/537.36 SogouSearch Android1.0 version3.0 AppVersion/5903");
                    headers.put("Referer", "http://nb.sa.sogou.com");
                    headers.put("X-Requested-With", "com.sogou.activity.src");;
                    // 同步请求
                    HttpUtil.httpSync(
                            _url,
                            HttpUtil.HttpMethod.GET,
                            null,
                            headers,
                            new HttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, String responseString) {
//                            Logger.d(TAG, "[onSuccess] statusCode" + statusCode + " responseString：" + responseString);
                            String jsonStr = responseString.substring(cbPrefix.length() + 1, responseString.length() - 1);
                            Logger.v(TAG, "httpSync: " + _url + "\n[onSuccess] json：" + jsonStr);
                            try {
                                JSONObject json = new JSONObject(jsonStr);
                                if (json.getInt("code") == 0) {
                                    JSONArray results = json.getJSONArray("result");
                                    for (int i = 0; i < results.length(); i++) {
                                        String item = results.getString(i);
                                        JSONObject answer = new JSONObject(item);
                                        String channel = answer.getString("channel");
                                        QuizBean quiz = new QuizBean(answer);
                                        String title = quiz.getTitle();
                                        String result = quiz.getResult();
                                        int ansIndex = quiz.getAnsIndex();

                                        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(result)
                                                && key.equals(channel)) {
                                            int index = 0;
                                            try {
                                                // 解析题目序号
                                                String[] rsts = title.split("\\.");
                                                if (rsts.length > 0) {
                                                    index = Integer.valueOf(rsts[0]);
                                                }
                                            } catch (Exception e) {
//                                                e.printStackTrace();
                                            }

                                            quiz.setIndex(index);
                                            if (!mQuizMap.containsKey(title)) {
                                                mQuizMap.put(title, result);
                                                // 新题
                                                callback.onReceiveNextAnswer(quiz);
                                            }
//                                            if (index > maxQuizIndex || (maxQuizIndex == 12 && index == 1) || (index == 0 && maxQuizIndex == 0)) {
//                                                maxQuizIndex = index;
//                                                callback.onReceiveNextAnswer(title, result, index, ansIndex);
//                                            }
                                            callback.onReceiveAnswer(quiz);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
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

}
