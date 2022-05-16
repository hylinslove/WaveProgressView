package com.bizgo.waveprogressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * author: mengxianglong
 * date: 2022/5/16
 * desc: 波浪进度动画 view
 **/
public class WaveProgressView extends View {

    //上下文
    private final Context mContext;
    //文字画笔
    private Paint mTextPaint;
    //圆圈画笔
    private Paint mCirclePaint;
    //波浪画笔
    private Paint mWavePaint;
    //颜色
    private int textColor;
    private int circleColor;
    private int waveColor;
    //文字大小
    private float textSize;
    // view 宽度
    private int mWidth;
    // view 高度
    private int mHeight;
    // 圆圈宽度
    private float circleStrokeWidth;
    // 进度
    private int progress;
    //一个波浪周期宽度
    private int recycleWidth;
    //横向偏移量
    private int dx;
    //x 起始位置
    private int startX;
    //波浪动画
    private ValueAnimator waveValueAnimator;
    //进度动画
    private ValueAnimator progressValueAnimator;

    public WaveProgressView(Context context) {
        super(context);
        this.mContext = context;
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initTypeArray(attrs);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = measureSize(400, widthMeasureSpec);
        mHeight = measureSize(400, heightMeasureSpec);
        //view必须是个圆形，取宽高最小值
        mWidth = Math.min(mWidth, mHeight);
        mHeight = Math.min(mWidth, mHeight);
        setMeasuredDimension(mWidth, mHeight);
        recycleWidth = mWidth;
        startX = -recycleWidth;
        startAnimation();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                result = defaultSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = size;
                break;
        }
        return result;
    }

    /**
     * 初始化参数
     *
     * @param attrs attrs
     */
    private void initTypeArray(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.WaveProgressView);
        this.textColor = typedArray.getColor(R.styleable.WaveProgressView_font_color, Color.BLACK);
        this.circleColor = typedArray.getColor(R.styleable.WaveProgressView_stroke_color, Color.BLUE);
        this.waveColor = typedArray.getColor(R.styleable.WaveProgressView_wave_color, Color.BLUE);
        this.textSize = typedArray.getDimension(R.styleable.WaveProgressView_font_size, 36);
        this.circleStrokeWidth = typedArray.getDimension(R.styleable.WaveProgressView_stroke_width, 20);
        typedArray.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(circleStrokeWidth);

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setColor(waveColor);
        mWavePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        clipCircle(canvas);
        //绘制波浪
        drawWave(canvas);
        //绘制圆圈
        drawCircleBorder(canvas);
        //绘制文字
        drawProgressText(canvas);
    }

    /**
     * 裁剪画布为圆形
     *
     * @param canvas canvas
     */
    private void clipCircle(Canvas canvas) {
        Path circlePath = new Path();
        circlePath.addCircle(mWidth / 2, mHeight / 2, mWidth / 2, Path.Direction.CW);
        canvas.clipPath(circlePath);
    }

    /**
     * 画圆形边框
     *
     * @param canvas canvas
     */
    private void drawCircleBorder(Canvas canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mCirclePaint);
    }

    /**
     * 绘制进度文字
     *
     * @param canvas
     */
    private void drawProgressText(Canvas canvas) {
        canvas.drawText(progress + "%", mWidth / 2, mHeight / 2, mTextPaint);
    }

    /**
     * 绘制波浪
     *
     * @param canvas canvas
     */
    private void drawWave(Canvas canvas) {
        Path path = new Path();
        path.reset();
        float rate = 1 - ((float) progress / 100);
        path.moveTo(startX + dx, mHeight * rate);
        int quarterRecycle = recycleWidth / 4;
        int maxHeight = mHeight / 6;

        for (int i = 0; i < 4; i++) {
            if(i % 2 == 0) {
                path.rQuadTo(quarterRecycle, maxHeight, quarterRecycle * 2, 0);
            } else {
                path.rQuadTo(quarterRecycle, -maxHeight, quarterRecycle * 2, 0);
            }
        }

        path.lineTo(mWidth, mHeight);
        path.lineTo(0, mHeight);
        path.close();

        canvas.drawPath(path, mWavePaint);

    }

    public void startAnimation() {
        waveValueAnimator = ValueAnimator.ofInt(0, recycleWidth);
        waveValueAnimator.setDuration(1500);
        waveValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveValueAnimator.setInterpolator(new LinearInterpolator());
        waveValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dx = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        waveValueAnimator.start();
    }

    public void setProgress(int targetProgress) {
        progressValueAnimator = ValueAnimator.ofInt(this.progress, targetProgress);
        progressValueAnimator.setDuration(1000);
        progressValueAnimator.setInterpolator(new LinearInterpolator());
        progressValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (int) animation.getAnimatedValue();
            }
        });
        progressValueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(waveValueAnimator != null) {
            waveValueAnimator.removeAllListeners();
            waveValueAnimator.cancel();
            waveValueAnimator = null;
        }
        if(progressValueAnimator != null) {
            progressValueAnimator.removeAllListeners();
            progressValueAnimator.cancel();
            progressValueAnimator = null;
        }
    }
}
