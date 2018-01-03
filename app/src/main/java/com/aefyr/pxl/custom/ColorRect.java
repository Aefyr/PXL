package com.aefyr.pxl.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Aefyr on 01.08.2017.
 */

public class ColorRect extends View {
    int color = Color.RED;


    public ColorRect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    private boolean explosionInProgress;
    private int currentExplosionRadius = 0;
    private int explosionColor;
    private int explosionX, explosionY;
    private Paint explosionPaint;
    public void setColorWithExplosion(int newColor, int explosionX, int explosionY, int startRadius){
        currentExplosionRadius = startRadius;
        ValueAnimator explosionAnimator = ValueAnimator.ofInt(currentExplosionRadius, calculateExplosionRadius());
        explosionColor = newColor;
        this.explosionX = explosionX;
        this.explosionY = explosionY;
        if(explosionPaint==null) {
            explosionPaint = new Paint();
            explosionPaint.setAntiAlias(true);
        }
        explosionPaint.setColor(explosionColor);
        explosionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                currentExplosionRadius = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        explosionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                explosionInProgress = false;
                color = explosionColor;
            }
        });
        explosionAnimator.setDuration(250);
        explosionInProgress = true;
        explosionAnimator.start();

    }

    private int calculateExplosionRadius(){
        //TODO Maybe I should add a proper calculation here (to determine the distance to the most faraway corner of the rect, but this will do for now
        return (int) Math.sqrt(Math.pow(getWidth(), 2)+Math.pow(getHeight(), 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(color);
        if(explosionInProgress){
            canvas.drawCircle(explosionX, explosionY, currentExplosionRadius, explosionPaint);
        }
    }
}
