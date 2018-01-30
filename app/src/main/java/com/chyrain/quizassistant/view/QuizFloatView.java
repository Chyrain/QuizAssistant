package com.chyrain.quizassistant.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.chyrain.quizassistant.util.Util;

/**
 * Created by chyrain on 27/01/2018.
 */

public class QuizFloatView extends ViewGroup {

    public QuizFloatView(Context context, WindowManager.LayoutParams params) {
        super(context);
    }

    public QuizFloatView(Context context) {
        super(context);
    }

    public QuizFloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuizFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
       // TODO
    }
}
