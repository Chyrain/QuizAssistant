package com.chyrain.quizassistant.aitask;

import android.text.TextUtils;

import com.chyrain.quizassistant.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by chyrain on 28/01/2018.
 */

public class QuizBean {
    private static final String TAG = "QuizBean";

    private int index; // 题目序号
    private String title;
    private ArrayList<String> answers;
    private String result;
    private int ansIndex; // 答案序号

    private boolean random; // 答不上，随机选答

    public QuizBean(String title, ArrayList<String> answers, String result) {
        this.title = title;
        this.answers = answers;
        this.result = result;
    }

    public QuizBean(JSONObject json) throws JSONException {
        parseAnswer(json);
    }

    private void parseAnswer(JSONObject json) throws JSONException {
        title = json.getString("title");
        result = json.getString("result");
        answers = new ArrayList<String>();
        JSONArray arr = json.getJSONArray("answers");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i ++) {
                answers.add(arr.getString(i));
            }
        }
        if (TextUtils.isEmpty(result)) {
            result = json.getString("recommend");
        }
        if (TextUtils.isEmpty(result) || json.optInt("error") == 1) {
            // 啊呀，这题汪仔还在想（没有答案，随机一个）
            int rId = (int)Math.floor(Math.random() * 3);
            // 随机获取 0、1、2
            result = answers.get(rId);
            setRandom(true);
            Logger.w(TAG, "随机获取答案id：" + rId + " 答案：" + result);
        }
        // 答案序号
        if (result != null) {
            for (int j = 0; j < arr.length(); j++) {
                if (result.equals(arr.getString(j))) {
                    ansIndex = j;
                    break;
                }
            }
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getAnsIndex() {
        return ansIndex;
    }

    public void setAnsIndex(int ansIndex) {
        this.ansIndex = ansIndex;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }
}
