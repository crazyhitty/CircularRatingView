/*
 * MIT License
 *
 * Copyright (c) 2017 Kartik Sharma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.crazyhitty.chdev.ks.customratingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Locale;

/**
 * Author:      Kartik Sharma
 * Email Id:    cr42yh17m4n@gmail.com
 * Created:     3/10/2017 11:44 AM
 * Description: Unavailable
 */

public class RatingView extends View {
    private static final String TAG = "RatingView";

    private static final float ARC_STROKE_WIDTH = 50.0f;
    private static final float ARC_START_ANGLE = 270.0f;
    private static final float DEFAULT_MAX = 100.0f;

    private Paint arcPaint;
    private Paint arcPaint2;
    private Paint linePaint;
    private Paint textPaint;

    private RectF arcRect;
    private Rect textRect;
    private int[] colors;
    private int[] colors1;

    private String rating = "0.0";

    private int size;
    private float max = DEFAULT_MAX;
    private float progress = 0;
    private float sweepAngle = 0;

    public RatingView(Context context) {
        super(context);
        initPaint();
    }

    public RatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public RatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RatingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public void setProgress(int progress) {
        if (progress > max) {
            throw new IllegalArgumentException("Progress cannot be larger than Max value");
        }
        this.progress = progress;
        this.rating = String.format(Locale.US, "%.1f", progress * 10 / max);
        changeArcProgress();
        Log.d(TAG, "sweepAngle: " + sweepAngle + " ; progress: " + progress + " ; max: " + max);
        invalidate();
    }

    private void initPaint() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStrokeWidth(ARC_STROKE_WIDTH);

        arcPaint2 = new Paint();
        arcPaint2.setAntiAlias(true);
        arcPaint2.setStyle(Paint.Style.STROKE);
        arcPaint2.setStrokeCap(Paint.Cap.ROUND);
        arcPaint2.setStrokeWidth(ARC_STROKE_WIDTH / 3);

        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1.0f);

        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.textColor));
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(200.0f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        int size = Math.min(width, height);

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
        initDimensions(size);
    }

    private void initDimensions(int size) {
        this.size = size;
        arcRect = new RectF(ARC_STROKE_WIDTH / 2, ARC_STROKE_WIDTH / 2, size - ARC_STROKE_WIDTH / 2, size - ARC_STROKE_WIDTH / 2);

        textRect = new Rect();
        textPaint.getTextBounds(rating, 0, rating.length(), textRect);

        colors = new int[]{Color.TRANSPARENT,
                ContextCompat.getColor(getContext(), R.color.endColor)};
        colors1 = new int[]{ContextCompat.getColor(getContext(), R.color.startColor),
                ContextCompat.getColor(getContext(), R.color.endColor)};

        changeArcProgress();
    }

    private void changeArcProgress() {
        sweepAngle = progress * (360 / max);
        float[] positions = new float[]{0.0f / 360.0f, sweepAngle / 360.0f};

        Shader shader1 = new SweepGradient(size / 2, size / 2, colors, positions);
        Shader shader2 = new RadialGradient(size / 2, size / 2, 25.0f, Color.TRANSPARENT, colors[1], Shader.TileMode.MIRROR);

        float shaderRotation = 262f;

        Matrix shaderMatrix = new Matrix();
        shaderMatrix.setRotate(shaderRotation, size / 2, size / 2);

        shader1.setLocalMatrix(shaderMatrix);
        shader2.setLocalMatrix(shaderMatrix);

        ComposeShader composeShader = new ComposeShader(shader1, shader2, PorterDuff.Mode.MULTIPLY);

        arcPaint.setShader(composeShader);

        Shader shader3 = new SweepGradient(size / 2, size / 2, colors1, positions);
        shaderMatrix.setRotate(shaderRotation, size / 2, size / 2);
        shader3.setLocalMatrix(shaderMatrix);

        arcPaint2.setShader(shader3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(arcRect, ARC_START_ANGLE, sweepAngle - 2, false, arcPaint);
        canvas.drawArc(arcRect, ARC_START_ANGLE, sweepAngle, false, arcPaint2);

        canvas.drawText(rating, size / 2, size / 2 + textRect.height() / 2, textPaint);

        //canvas.drawLine(size / 2, 0, size / 2, canvas.getHeight(), linePaint);
        //canvas.drawLine(0, size / 2, canvas.getWidth(), size / 2, linePaint);
    }
}