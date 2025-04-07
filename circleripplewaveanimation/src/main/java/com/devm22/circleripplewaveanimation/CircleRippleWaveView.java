package com.devm22.circleripplewaveanimation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class CircleRippleWaveView extends View {

    private final List<Float> waveScales = new ArrayList<>();
    private final List<Float> waveAlphas = new ArrayList<>();
    private final List<Float> waveTargetAlphas = new ArrayList<>();
    private final List<ValueAnimator> runningAnimators = new ArrayList<>();

    private int waveCount = 3;
    private int animationStep = 0;
    private boolean isAnimating = false;
    private int mainWaveColor = Color.GREEN;
    private long animationSpeed = 500;
    private String centerText = null;
    private Bitmap centerImage = null;
    private Paint textPaint;
    private float centerImagePadding = 0f;
    private int centerTextColor = Color.WHITE;
    private float centerTextSize = 48f;
    private int centerImageTint = Color.TRANSPARENT;
    private boolean textAllCaps = false;
    private int centerTextStyle = Typeface.NORMAL;

    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CircleRippleWaveView(Context context) {
        super(context);
        init();
    }

    public CircleRippleWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveAnimationView);

        setWaveCount(a.getInt(R.styleable.WaveAnimationView_waveCount, 3));
        setMainWaveColor(a.getColor(R.styleable.WaveAnimationView_mainWaveColor, Color.GREEN));
        setAnimationSpeed(a.getInt(R.styleable.WaveAnimationView_animationSpeed, 500));

        centerTextColor = a.getColor(R.styleable.WaveAnimationView_centerTextColor, Color.WHITE);
        centerTextSize = a.getDimension(R.styleable.WaveAnimationView_centerTextSize, 48f);
        centerImagePadding = a.getDimension(R.styleable.WaveAnimationView_centerImagePadding, 0f);
        centerImageTint = a.getColor(R.styleable.WaveAnimationView_centerImageTint, Color.TRANSPARENT);
        textAllCaps = a.getBoolean(R.styleable.WaveAnimationView_textAllCaps, false);
        centerTextStyle = a.getInt(R.styleable.WaveAnimationView_textStyle, Typeface.NORMAL);

        textPaint.setColor(centerTextColor);
        textPaint.setTextSize(centerTextSize);
        textPaint.setTypeface(Typeface.defaultFromStyle(centerTextStyle));

        int centerImageResId = a.getResourceId(R.styleable.WaveAnimationView_centerImage, -1);
        if (centerImageResId != -1) {
            setCenterImageResource(context, centerImageResId);
        }

        String rawText = a.getString(R.styleable.WaveAnimationView_centerText);
        if (rawText != null) {
            setCenterText(rawText);
        }

        a.recycle();

    }

    public CircleRippleWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }




    private void init() {
        wavePaint.setStyle(Paint.Style.FILL);
        setWaveCount(3);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(centerTextColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(centerTextSize);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;

        Path clipPath = new Path();
        clipPath.addCircle(centerX, centerX, centerX, Path.Direction.CW);
        canvas.clipPath(clipPath);

        for (int i = 0; i < waveCount; i++) {
            float alpha = waveAlphas.get(i);
            if (alpha > 0f) {
                float radius = centerX * waveScales.get(i);
                float targetAlpha = waveTargetAlphas.get(i);
                wavePaint.setColor(applyAlpha(mainWaveColor, alpha * targetAlpha));
                canvas.drawCircle(centerX, centerX, radius, wavePaint);
            }
        }

        if (centerImage != null) {
            float centerY = getHeight() / 2f;
            float imageSize = Math.min(getWidth(), getHeight()) * 0.4f - (centerImagePadding * 2);
            imageSize = Math.max(0, imageSize);
            float halfSize = imageSize / 2f;

            RectF imageRect = new RectF(
                    centerX - imageSize / 2f,
                    centerY - imageSize / 2f,
                    centerX + imageSize / 2f,
                    centerY + imageSize / 2f
            );

            Paint imagePaint = null;
            if (centerImageTint != Color.TRANSPARENT) {
                imagePaint = new Paint();
                imagePaint.setColorFilter(new PorterDuffColorFilter(centerImageTint, PorterDuff.Mode.SRC_IN));
            }

            canvas.drawBitmap(centerImage, null, imageRect, imagePaint);
        } else if (centerText != null) {
            float centerY = getHeight() / 2f;
            float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(centerText, centerX, textY, textPaint);
        }
    }


    private int applyAlpha(int baseColor, float alphaFactor) {
        int alpha = Math.round(Color.alpha(baseColor) * alphaFactor);
        return (baseColor & 0x00FFFFFF) | (alpha << 24);
    }


    private void runNextStep() {
        if (!isAnimating || waveCount <= 1) return;

        int totalSteps = (waveCount - 1) * 2;
        int step = animationStep % totalSteps;

        int waveIndex;
        boolean isFadingIn;

        if (step < waveCount - 1) {
            waveIndex = step + 1;
            isFadingIn = true;
        } else {
            waveIndex = totalSteps - step;
            isFadingIn = false;
        }

        float from = isFadingIn ? 0f : 1f;
        float to = isFadingIn ? 1f : 0f;

        animateWaveAlpha(waveIndex, from, to);

        animationStep++;
        handler.postDelayed(this::runNextStep, animationSpeed);
    }


    private void animateWaveAlpha(int index, float from, float to) {
        if (index == 0) return;

        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(animationSpeed);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            waveAlphas.set(index, (float) animation.getAnimatedValue());
            invalidate();
        });
        animator.start();
        runningAnimators.add(animator);
    }


    public void startAnimation() {
        stopAnimation();
        isAnimating = true;
        animationStep = 0;
        runNextStep();
    }


    public void stopAnimation() {
        isAnimating = false;
        handler.removeCallbacksAndMessages(null);

        for (ValueAnimator animator : runningAnimators) {
            animator.cancel();
        }
        runningAnimators.clear();

        for (int i = 0; i < waveCount; i++) {
            waveAlphas.set(i, (i == 0) ? 1f : 0f);
        }
        invalidate();
    }


    public void reset() {
        stopAnimation();
        setWaveCount(waveCount); // resets state
    }


    public boolean isRunning() {
        return isAnimating;
    }


    public void setMainWaveColor(int color) {
        this.mainWaveColor = color;
        invalidate();
    }

    public void setWaveCount(int count) {
        waveCount = Math.max(1, count);

        waveScales.clear();
        waveAlphas.clear();
        waveTargetAlphas.clear();

        for (int i = 0; i < waveCount; i++) {
            float scale = 0.4f + (0.6f * i / (waveCount - 1));
            waveScales.add(scale);

            float alphaLevel = i == 0 ? 1f : (1f / (float) Math.pow(2, i));
            waveTargetAlphas.add(alphaLevel);

            waveAlphas.add(i == 0 ? 1f : 0f);
        }

        invalidate();
    }

    public void setAnimationSpeed(long millis) {
        animationSpeed = Math.max(100, millis);
    }

    public void setCenterText(String text) {
        if (textAllCaps && text != null) {
            this.centerText = text.toUpperCase();
        } else {
            this.centerText = text;
        }
        invalidate();
    }

    public void setCenterTextStyle(int style) {
        centerTextStyle = style;
        textPaint.setTypeface(Typeface.defaultFromStyle(style));
        invalidate();
    }

    public void setTextAllCaps(boolean allCaps) {
        this.textAllCaps = allCaps;
        // Re-apply text case
        setCenterText(this.centerText);
    }

    public void setCenterTextColor(int color) {
        this.centerTextColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    public void setCenterTextSize(float sizeInPx) {
        this.centerTextSize = sizeInPx;
        textPaint.setTextSize(sizeInPx);
        invalidate();
    }

    public void setCenterImage(Bitmap bitmap) {
        this.centerImage = bitmap;
        invalidate();
    }

    public void setCenterImageResource(Context context, @DrawableRes int resId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, resId);
        if (vectorDrawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(
                    vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            setCenterImage(bitmap);
        }
    }

    public void setCenterImagePadding(float paddingInPx) {
        this.centerImagePadding = paddingInPx;
        invalidate();
    }

    public void setCenterImageTint(int color) {
        this.centerImageTint = color;
        invalidate();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}


