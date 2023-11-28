package de.hnparda.tankcontrol.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import de.hnparda.tankcontrol.R;

public class TankProgressBar extends LinearLayout {

    ImageView progressBar;
    Bitmap wave = BitmapFactory.decodeResource(getResources(), R.drawable.wave);
    int progressBarHeightM;
    byte progressValue = 0;
    int progressBarHeight = 0;
    Rect waveRect = new Rect(0, 0, wave.getWidth(), wave.getHeight());
    private OnMeasureListener onMeasureListener;

    public TankProgressBar(Context context) {
        super(context);
        init();
    }

    public TankProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TankProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnMeasureListener(OnMeasureListener mEventListener) {
        this.onMeasureListener = mEventListener;
    }

    private void init() {
        this.setBackground(null);
        View v = inflate(getContext(), R.layout.tank_progress_bar, this);
        progressBar = v.findViewById(R.id.progress);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        progressBarHeight = progressBarHeight == 0 ? progressBar.getMeasuredHeight() : progressBarHeight;
        progressBar.getLayoutParams().height = progressBarHeight * 2;
        progressBarHeightM = progressBarHeight / 100;
        Animation animation = new TranslateAnimation(0, (progressBar.getMeasuredWidth() - this.getMeasuredWidth()) * -1, 0, 0);
        animation.setDuration(4000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        progressBar.setAnimation(animation);
        progressBar.setImageBitmap(getProgressBitmap(0));

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (onMeasureListener != null) {
            onMeasureListener.onMeasured();
        }
    }

    Bitmap getProgressBitmap(int progressValue) {

        Bitmap progress = Bitmap.createBitmap(progressBar.getMeasuredWidth(), progressBar.getMeasuredHeight(), Bitmap.Config.RGB_565);
        int progressMargin = progressBarHeightM * (100 - progressValue);
        Canvas progressCanvas = new Canvas(progress);
        Rect progressRect = new Rect(0, progressMargin, progress.getWidth(), progressMargin + progress.getHeight());
        progressCanvas.drawColor(getResources().getColor(R.color.gray, getContext().getTheme()));
        progressCanvas.drawBitmap(wave, waveRect, progressRect, null);
        return progress;
    }

    public void setProgress(byte prog) {
        int newProgress = prog - progressValue;
        progressValue = prog;
        Bitmap newProgressBitmap = getProgressBitmap(prog);
        int progressAnimMargin = (progressBarHeightM * newProgress) * -1;
        progressBar.animate().translationYBy(progressAnimMargin).setDuration(700).withEndAction(() -> {
            progressBar.animate().translationYBy(-1 * progressAnimMargin).setDuration(0).start();
            progressBar.setImageBitmap(newProgressBitmap);
        }).start();
    }


    public interface OnMeasureListener {
        void onMeasured();
    }

}