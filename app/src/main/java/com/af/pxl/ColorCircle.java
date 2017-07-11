package com.af.pxl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

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

    void setColor(int color){
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
        p.setColor(Utils.invertColor(color));
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x/2, y/2, x/2f-x/12f, p);
        System.out.println("REDRAW");
    }
}
