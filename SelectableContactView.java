package com.wayww;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by wayww on 2017/2/21.
 * github: https://github.com/covetcode
 */

public class SelectableContactView extends View {

    private static final String TAG = "SelectableContactView";
    private String mText;
    private Drawable mDrawable;

    public final static int DEFAULT_ANIMATION_DURATION = 300;

    private boolean isShowShadow= true;

    private int backgroundColor;
    private int textColor;
    private int selectColor;
    private int shadowColor;
    private int tickColor;

    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    private Paint mPathPaint;
    private Path tickPath;
    private Path tickAnimationPath;
    private PathMeasure mPathMeasure;
    private ValueAnimator mAnimator;
    private Rect textBound;
    private Matrix mDrawMatrix;

    //实际宽高
    private int height;
    private int width;
    //背景的大小，直径
    private int baseSize;
    //内容的大小，文字和图像，直径
    private int contentSize;
    //选中勾和边的宽度，
    private int strokeWidth;


    private float animationValue;
    private boolean isSelected;


    public SelectableContactView(Context context) {
        this(context,null);
    }

    public SelectableContactView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SelectableContactView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SelectableContactView, defStyleAttr, 0);
        mText = a.getString(R.styleable.SelectableContactView_text);
        backgroundColor = a.getColor(R.styleable.SelectableContactView_backgroundColor, Color.BLUE);
        textColor = a.getColor(R.styleable.SelectableContactView_textColor, Color.WHITE);
        selectColor = a.getColor(R.styleable.SelectableContactView_selectColor, Color.WHITE);
        tickColor = a.getColor(R.styleable.SelectableContactView_tickColor, Color.RED);
        shadowColor = a.getColor(R.styleable.SelectableContactView_shadowColor, Color.GRAY);
        isShowShadow = a.getBoolean(R.styleable.SelectableContactView_showShadow, false);
        int mResource = a.getResourceId(R.styleable.SelectableContactView_src, 0);
        a.recycle();

        if (mResource !=0){
            mDrawable = getResources().getDrawable(mResource);
            mDrawMatrix = new Matrix();
        }

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);

        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setColor(tickColor);
        mPathPaint.setStyle(Paint.Style.STROKE);
        textBound = new Rect();

        mAnimator = ValueAnimator.ofFloat(0,1);
        mAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationValue = (float) animation.getAnimatedValue();
                postInvalidateOnAnimation(width / 2 - contentSize / 2, height / 2 - contentSize / 2, width / 2 + contentSize / 2, height / 2 + contentSize / 2);
            }
        });

        tickPath = new Path();
        tickAnimationPath = new Path();
        mPathMeasure = new PathMeasure();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getMeasuredHeight();
        width = getMeasuredWidth();

        //取短的一边
        int w = width - getPaddingLeft() - getPaddingRight();
        int h = height - getPaddingTop() - getPaddingBottom();
        baseSize = w > h ? h : w;
        contentSize = baseSize*3/4;
        strokeWidth = baseSize/16;
        mPathPaint.setStrokeWidth(strokeWidth);
        computeTick();
        setShowShadow(isShowShadow);
        if (mDrawable != null) {
            configureBounds();
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        //绘制背景
        mBackgroundPaint.setColor(backgroundColor);
        canvas.drawCircle(width / 2, height / 2, baseSize / 2, mBackgroundPaint);

        //绘制图像
        if (mDrawable != null && animationValue != 1) {
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }

        //绘制文字
        if (!TextUtils.isEmpty(mText) && animationValue != 1){
            int w = contentSize*3/4;
            mTextPaint.setTextSize(w);
            mTextPaint.setTextSize(w * w / mTextPaint.measureText(mText));
            mTextPaint.getTextBounds(mText, 0, mText.length(), textBound);
            canvas.drawText(mText,width/2-textBound.centerX(),height/2-textBound.centerY(),mTextPaint);
        }

        //绘制动画
        if (animationValue != 0){
            mBackgroundPaint.setColor(selectColor);
            canvas.drawCircle(width / 2, height / 2, (baseSize / 2 - strokeWidth) * animationValue, mBackgroundPaint);
        }

        //钩动画
        if (animationValue != 0){
            mPathMeasure.setPath(tickPath,false);
            mPathMeasure.getSegment(0,mPathMeasure.getLength()*animationValue,tickAnimationPath,true);
            canvas.drawPath(tickAnimationPath,mPathPaint);
            tickAnimationPath.reset();
        }

    }

    public void setText(String text){
        mText = text;
        invalidate();
    }

    public String getText() {
        return mText;
    }

    public void select(){
        isSelected = true;
        mAnimator.start();
    }

    public void deselect() {
        isSelected = false;
        mAnimator.reverse();
    }

    public void setShowShadow(boolean b) {
        isShowShadow = b;
        if (isShowShadow){
            mBackgroundPaint.setShadowLayer(strokeWidth,strokeWidth/3,strokeWidth/3,shadowColor);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }else {
            mBackgroundPaint.clearShadowLayer();
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        postInvalidate();
    }

    public boolean isShowShadow(){
        return isShowShadow;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public boolean isInAnimating(){
        return mAnimator != null && mAnimator.isRunning();
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        postInvalidate();
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
        if (width*height != 0){
            configureBounds();
        }
        postInvalidate();

    }
    public void setShadowColor(int color){
        shadowColor = color;
        postInvalidate();
    }


    public void setTextColor(int color) {
        textColor = color;
        postInvalidate();
    }

    public void setSelectColor(int color){
        selectColor = color;
        postInvalidate();
    }

    public void setTickColor(int color){
        tickColor = color;
        postInvalidate();
    }


    private void configureBounds(){
        float scale;
        int dHeight = mDrawable.getIntrinsicHeight();
        int dWidth = mDrawable.getIntrinsicWidth();
        if (dHeight*dWidth == 0){
            mDrawable = null;
            return;
        }
        if (dHeight>dWidth){
            scale = (float) contentSize / (float)dHeight ;
        }else {
            scale = (float)contentSize / (float)dWidth ;
        }

        if (mDrawMatrix == null){
            mDrawMatrix = new Matrix();
        }
        mDrawMatrix.reset();
        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate(width / 2 - dWidth*scale / 2,height / 2 - dHeight*scale / 2);
        mDrawable.setBounds(0,0,dWidth,dHeight);
      //  mDrawable.setBounds(width / 2 - dWidth / 2, height / 2 - dHeight / 2, width / 2 + dWidth / 2, height / 2 + dHeight / 2);

    }
    private void computeTick() {
        tickPath.moveTo(width/2-contentSize/3,height/2);
        tickPath.lineTo(width/2-contentSize/6,height/2+contentSize/3);
        tickPath.lineTo(width/2+contentSize/3,height/2-contentSize/3);
    }

}
