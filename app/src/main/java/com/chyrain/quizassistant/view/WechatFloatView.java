package com.chyrain.quizassistant.view;

import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.chyrain.quizassistant.util.Util;

public class WechatFloatView extends AppCompatImageView {

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    /**
     * 图片的类型，圆形or圆角
     */
    private int type;
    public static final int TYPE_CIRCLE = 0;
    public static final int TYPE_ROUND = 1;
    /**
     * 圆角大小的默认值
     */
    private static final int BODER_RADIUS_DEFAULT = 20;
    private static final String TAG = null;
    /**
     * 圆角的大小
     */
    private int mBorderRadius;

    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;
    /**
     * 圆角的半径
     */
    private int mRadius;
    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;
    /**
     * view的宽度
     */
    private int mWidth;
    private RectF mRoundRect;

    /* 位置 */
    private float mTouchStartX;
    private float mTouchStartY;
    private float mX;
    private float mY;
    private float x;
    private float y;
    private long mStartTime;
    private WindowManager wm =(WindowManager)getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    private WindowManager.LayoutParams wmParams;

    // 长按条件符合标志
    private boolean longClickFlag = false;

//	public WechatFloatView(Context context) {
//		super(context);
//		// TODO Auto-generated constructor stub
//	}

    public WechatFloatView(Context context, WindowManager.LayoutParams params) {
        super(context);
        wmParams = params;

        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        mBorderRadius = Util.dp2px(BODER_RADIUS_DEFAULT, context);// 默认为10dp
        type = TYPE_CIRCLE;// 默认为Circle
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 如果类型是圆形，则强制改变view的宽高一致，以小值为准
         */
        if (type == TYPE_CIRCLE) {
            mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
            mRadius = mWidth / 2;
            setMeasuredDimension(mWidth, mWidth);
        }
    }

    /**
     * 初始化BitmapShader
     */
    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

//		Bitmap bmp = drawableToBitamp(drawable);
        Bitmap bmp = getBitmapFromDrawable(drawable);

        if (bmp == null) {
            invalidate();
            return;
        }

        // 将bmp作为着色器，就是在指定区域内绘制bmp
        mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
        float scale = 1.0f;
        if (type == TYPE_CIRCLE) {
            // 拿到bitmap宽或高的小值
            int bSize = Math.min(bmp.getWidth(), bmp.getHeight());
            scale = mWidth * 1.0f / bSize;

        } else if (type == TYPE_ROUND) {
//			Log.e("TAG",
//					"b'w = " + bmp.getWidth() + " , " + "b'h = "
//							+ bmp.getHeight());
            if (!(bmp.getWidth() == getWidth() && bmp.getHeight() == getHeight())) {
                // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
                scale = Math.max(getWidth() * 1.0f / bmp.getWidth(),
                        getHeight() * 1.0f / bmp.getHeight());
            }

        }
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//		Log.e("TAG", "onDraw");
        if (getDrawable() == null) {
            return;
        }
        setUpShader();

        if (type == TYPE_ROUND) {
            canvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius,
                    mBitmapPaint);
        } else {
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
            // drawSomeThing(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 圆角图片的范围
        if (type == TYPE_ROUND)
            mRoundRect = new RectF(0, 0, w, h);
    }

    /**
     * 这是cdy修改后的转换方法，之前的方法有错误
     * */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
                }, 600);
                break;
            case MotionEvent.ACTION_MOVE:   //捕获手指触摸移动动作
                if (!longClickFlag && Math.abs(x - mX) < getWidth() && Math.abs(y - mY) < getHeight()) {
                    if ((new Date()).getTime() - mStartTime > 600) {
                        mTouchStartX=mTouchStartY=0;
                        return performLongClick();
                    }
                }
                updateViewPosition(x, y);
                break;
            case MotionEvent.ACTION_UP:    //捕获手指触摸离开动作
                if (Math.abs(x - mX) < getWidth() && Math.abs(y - mY) < getHeight()) {
                    if ((new Date()).getTime() - mStartTime > 600) {
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
//		Logger.d(TAG, "updateViewPosition: " + x + "," + y);
        //更新浮动窗口位置参数
        wmParams.x=(int)( x-mTouchStartX);
        wmParams.y=(int) (y-mTouchStartY);
        wm.updateViewLayout(this, wmParams);  //刷新显示
    }
}

