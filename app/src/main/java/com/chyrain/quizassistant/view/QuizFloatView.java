package com.chyrain.quizassistant.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.aitask.QuizBean;
import com.chyrain.quizassistant.util.Logger;
import com.chyrain.quizassistant.util.Util;

import java.util.Date;

/**
 * 自定义答题信息显示的浮动View
 * Created by chyrain on 27/01/2018.
 */
public class QuizFloatView extends LinearLayout {
    private static final String TAG = "QuizFloatView";
    private static final int LONG_CLICK_TIME = 800;

    private WindowManager wm =(WindowManager)getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    private WindowManager.LayoutParams wmParams;

    private View mContainerView;
    private CircleImageView mAppIconIv;
    private TextView mAppNameTv;
    private TextView mAnswerTv;
    private TextView mUnableTipTv;

    // 长按条件符合标志
    private boolean longClickFlag = false;
    /* 位置 */
    private float mTouchStartX;
    private float mTouchStartY;
    private float mX;
    private float mY;
    private float x;
    private float y;
    private long mStartTime;

    // 状态
    private boolean isOpen;
    private boolean isAutoTrust;
    private QuizBean mCurrentQuiz;
    private String mCurrentApp;
    private int mCurrentImageId;

    public QuizFloatView(Context context, WindowManager.LayoutParams params) {
        super(context);

        initView(context, params);
    }

    private void initView(Context context, WindowManager.LayoutParams params) {
        wmParams = params;
        mCurrentImageId = R.mipmap.v5_avatar_robot_red;

        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_quiz_float, null);
        mAppIconIv = (CircleImageView) contentView.findViewById(R.id.iv_circle_app);
        mAppNameTv = (TextView) contentView.findViewById(R.id.tv_app_name);
        mAnswerTv = (TextView) contentView.findViewById(R.id.tv_quiz_answer);
        mUnableTipTv = (TextView) contentView.findViewById(R.id.tv_unable_tip);

        mContainerView = contentView; //.findViewById(R.id.layout_text)
        addView(contentView);

//        //设置悬浮窗口长宽数据
//        wmParams.width = Util.dp2px(getMeasuredWidth(), context);
//        wmParams.height = Util.dp2px(getMeasuredHeight(), context);
    }

    public void updateFloatAutoTrustEnable(boolean light) {
        this.isAutoTrust = light;
        if (mAppIconIv != null) {
            mAppIconIv.setImageResource(mCurrentImageId);  //这里简单的用自带的Icom来做演示
            if (!light) {
                Util.grayImageView(mAppIconIv);
            }
        }
    }

    public void updateFloatServiceEnable(boolean enable) {
        this.isOpen = enable;
        if (enable) {
            mContainerView.setEnabled(true);
            mUnableTipTv.setVisibility(View.GONE);
            mAppNameTv.setVisibility(View.VISIBLE);
            mAnswerTv.setVisibility(View.VISIBLE);
        } else {
            mContainerView.setEnabled(false);
            mUnableTipTv.setVisibility(View.VISIBLE);
            mAppNameTv.setVisibility(View.GONE);
            mAnswerTv.setVisibility(View.GONE);
        }
    }

    public void updateFloatQuiz(QuizBean quiz) {
        if (quiz != null) {
            mCurrentQuiz = quiz;
        } else {
            return;
        }
        Logger.i(TAG, "[updateFloatQuiz] result:" + quiz.getResult() + " quiz:" + mCurrentQuiz);
        if (mAnswerTv != null && mCurrentQuiz != null) {
            mAnswerTv.setText(mCurrentQuiz.getResult());
            if (mCurrentQuiz.isRandom()) {
                mAnswerTv.setTextColor(Util.getColor(R.color.answer_unknow_color));
                if (Config.getConfig(getContext()).getNoAnswerMode() == 1) {
                    mAnswerTv.setText("(随机)" + quiz.getResult());
                } else {
                    mAnswerTv.setText("抱歉，这题小五不会");
                }
            } else if (mCurrentQuiz.isUnsure()) {
                mAnswerTv.setTextColor(Util.getColor(R.color.answer_unsure_color));
            } else {
                mAnswerTv.setTextColor(Util.getColor(R.color.answer_color));
            }
//            mAnswerTv.setText((mCurrentQuiz.isRandom() ? "(随机)" : "") + mCurrentQuiz.getResult());
        }
        if (mAppNameTv != null) {
            mAppNameTv.setText(mCurrentApp +
                    (quiz.getIndex() > 0 ? quiz.getIndex() + "题" : "")
                    + "推荐答案:");
        }
    }

    public void updateFloatJob(String appName, String jobkey) {
        mCurrentApp = appName;
        switch (appName) {
            case "冲顶大会":
                mCurrentImageId = R.mipmap.settings_ic_cddh;
                break;
            case "芝士超人":
                mCurrentImageId = R.mipmap.settings_ic_zscr;
                break;
            case "映客直播":
                mCurrentImageId = R.mipmap.settings_ic_inke;
                break;
            case "花椒直播":
                mCurrentImageId = R.mipmap.settings_ic_huajiao;
                break;
            case "西瓜视频":
                mCurrentImageId = R.mipmap.settings_ic_xigua;
                break;
            case "黄金十秒":
                mCurrentImageId = R.mipmap.settings_ic_hjsm;
                break;
            default:
                mCurrentImageId = R.mipmap.v5_avatar_robot_red;
                break;
        }
        updateFloatAutoTrustEnable(this.isAutoTrust);
        updateFloatServiceEnable(this.isOpen);
        Logger.i(TAG, "[updateFloatJob] appName:" + appName + " jobKey:" + jobkey + " quiz:" + mCurrentQuiz);
        if (mAppNameTv != null) {
            mAppNameTv.setText(appName +
                (mCurrentQuiz != null && mCurrentQuiz.getIndex() > 0 ? mCurrentQuiz.getIndex() + "题" : "")
                + "推荐答案：");
        }
//        if (mAnswerTv != null) {
//            mAnswerTv.setText("小五给您推荐答案");
//        }
    }


    public CircleImageView getAppIconIv() {
        return mAppIconIv;
    }

    public TextView getAppNameTv() {
        return mAppNameTv;
    }

    public TextView getAnswerTv() {
        return mAnswerTv;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean performLongClick() {
        longClickFlag = true;
        return super.performLongClick();
    }

    /**
     * dp转 px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    private int dp2px(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    private int getStatusBarHeight(Context context) {
        /**
         * 获取状态栏高度——方法1
         * */
        int statusBarHeight1 = dp2px(25, context);
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取相对屏幕的坐标，即以屏幕左上角为原点
        x = event.getRawX();
        y = event.getRawY() - getStatusBarHeight(getContext());   //25是系统状态栏的高度
//		Logger.d(TAG, "onTouchEvent("+event.getAction()+"): " + x + "," + y);
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:    //捕获手指触摸按下动作
                //获取相对View的坐标，即以此View左上角为原点
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                mX = x;
                mY = y;
                mStartTime = (new Date()).getTime();
                longClickFlag = false;
                getHandler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (!longClickFlag && mTouchStartX != 0 && mTouchStartY != 0 && Math.abs(x - mX) < getWidth() && Math.abs(y - mY) < getHeight()) {
                            performLongClick();
                        }
                    }
                }, LONG_CLICK_TIME);
                break;
            case MotionEvent.ACTION_MOVE:   //捕获手指触摸移动动作
                if (!longClickFlag && Math.abs(x - mX) < getWidth() && Math.abs(y - mY) < getHeight()) {
                    if ((new Date()).getTime() - mStartTime > LONG_CLICK_TIME) {
                        mTouchStartX=mTouchStartY=0;
                        return performLongClick();
                    }
                }
                updateViewPosition(x, y);
                break;
            case MotionEvent.ACTION_UP:    //捕获手指触摸离开动作
                if (Math.abs(x - mX) < getWidth() && Math.abs(y - mY) < getHeight()) {
                    if ((new Date()).getTime() - mStartTime > LONG_CLICK_TIME) {
                        if (longClickFlag) {
                            mTouchStartX=mTouchStartY=0;
                            return true;
                        } else {
                            mTouchStartX=mTouchStartY=0;
                            return performLongClick();
                        }
                    } else {
                        mTouchStartX=mTouchStartY=0;
                        return performClick();
                    }
                } else {
                    updateViewPosition(x, y);
                    mTouchStartX=mTouchStartY=0;
                    return true;
                }
        }

        return false;
    }

    private void updateViewPosition(float x, float y){
//        //设置悬浮窗口长宽数据
//        wmParams.width = Util.dp2px(getMeasuredWidth(), getContext());
//        wmParams.height = Util.dp2px(getMeasuredHeight(), getContext());
        //更新浮动窗口位置参数
        wmParams.x=(int) (x - mTouchStartX);
        wmParams.y=(int) (y - mTouchStartY);
        wm.updateViewLayout(this, wmParams);  //刷新显示

        Logger.d(TAG, "updateFloatViewPosition: x:" + x + ", y:" + y + ", " +
                "width:" + wmParams.width + ", height:" + wmParams.height);
    }

//    public QuizFloatView(Context context) {
//        super(context);
//    }
//
//    public QuizFloatView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public QuizFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    @Override
//    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
//        // TODO
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
//        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
//        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
//
//        int height = 0;
//        int width = 0;
//        int count = getChildCount();
//        for (int i=0;i < count; i++) {
//            //测量子控件
//            View child = getChildAt(i);
//            measureChild(child, widthMeasureSpec, heightMeasureSpec);
//            //获得子控件的高度和宽度
//            int childHeight = child.getMeasuredHeight();
//            int childWidth = child.getMeasuredWidth();
//            //横向布局，得到最大高度，并且累加宽度
//            width += childWidth;
//            height = Math.max(childHeight, height);
//        }
//
//        setMeasuredDimension((measureWidthMode == MeasureSpec.EXACTLY) ? measureWidth: width, (measureHeightMode == MeasureSpec.EXACTLY) ? measureHeight: height);
//    }

}
