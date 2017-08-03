package com.af.pxl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by Aefyr on 02.07.2017.
 */

public class ColorCircle extends View {
    private Paint p;
    int color;
    int borderColor;
    public ColorCircle(Context context) {
        super(context);
        initialize();
    }

    public ColorCircle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    void initialize(){
        p = new Paint();
        color = Color.WHITE;
        borderColor = Color.parseColor("#ffb0bec5");
        p.setColor(color);
        //p.setAntiAlias(true);
    }

    private boolean animated = false;
    private ObjectAnimator animator1;
    private ObjectAnimator animator12;
    private ObjectAnimator animator2;
    private ObjectAnimator animator22;
    public void enableAnimations(){
        animator1 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.1f);
        animator1.setDuration(100);

        animator12 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.1f);
        animator12.setDuration(100);

        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator2.start();
                animator22.start();
            }
        });

        animator2 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f);
        animator2.setDuration(100);

        animator22 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f);
        animator22.setDuration(100);

        animated = true;
    }

    public void animation(){
        if(!animated)
            return;

        animator1.start();
        animator12.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (Build.VERSION.SDK_INT >= 21)
            setOutlineProvider(new ColorCircleOutlineProvider());
    }

    @TargetApi(21)
    private class ColorCircleOutlineProvider extends ViewOutlineProvider{

        @Override
        public void getOutline(View view, Outline outline) {
            int x =getWidth()/12;
            outline.setOval(x, x, getWidth()-x, getHeight()-x);
        }
    }

    public void setColor(int color){
        this.color = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = canvas.getWidth();
        int y = canvas.getHeight();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setStrokeWidth(x/32);
        canvas.drawCircle(x/2, y/2, x/2f-x/12f, p);

        if (Build.VERSION.SDK_INT < 21) {
            p.setColor(Utils.invertColor(color));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(x / 2, y / 2, x / 2f - x / 12f, p);
        }
        System.out.println("REDRAW");
    }
}
