package com.aefyr.pxl.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Aefyr on 01.08.2017.
 */

public class ColorRect extends View {
    int color = Color.RED;

    private ColorExplosionFX colorExplosionFX;


    public ColorRect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorExplosionFX = new ColorExplosionFX(this);
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setColorWithExplosion(final int newColor, int explosionX, int explosionY, int startRadius){
        colorExplosionFX.spawnExplosion(explosionX, explosionY, startRadius, calculateExplosionRadius(), 250, newColor).doOnExplosionEnd(new Runnable() {
            @Override
            public void run() {
                setColor(newColor);
            }
        });
    }

    private int calculateExplosionRadius(){
        //TODO Maybe I should add a proper calculation here (to determine the distance to the most faraway corner of the rect, but this will do for now
        return (int) Math.sqrt(Math.pow(getWidth(), 2)+Math.pow(getHeight(), 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(color);

        colorExplosionFX.renderOn(canvas);
    }
}
