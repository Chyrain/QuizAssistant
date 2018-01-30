package com.chyrain.quizassistant.util;

/**
 * Created by chyrain on 21/01/2018.
 */

import android.content.Context;

import com.chyrain.quizassistant.V5Application;

public abstract class HttpResponseHandler {
    private Context mContext;
    public HttpResponseHandler() {
        this.setContext(V5Application.getInstance());
    }
    public HttpResponseHandler(Context context) {
        this.setContext(context);
    }
    public abstract void onSuccess(int statusCode, String responseString);
    public abstract void onFailure(int statusCode, String responseString);
    public Context getContext() {
        return mContext;
    }
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
}
