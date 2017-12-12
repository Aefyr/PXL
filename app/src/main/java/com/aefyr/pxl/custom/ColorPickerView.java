package com.aefyr.pxl.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Aefyr on 13.06.2017.
 */

public class ColorPickerView extends View {

    public float[] color = {1, 1, 1};
    Paint p;
    float realWidth;


    LinearGradient gradient;

    MODE currentMode;
    float grades;
    int modeInt;

    int[] colors;
    float[] positions;


    public enum MODE {
        HUE, SATURATION, VIBRANCE
    }


    public ColorPickerView(Context context) {
        super(context);
        initialize();
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    void initialize() {
        setMode(MODE.HUE);
        p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.HSVToColor(color));
    }

    public void setMode(MODE mode) {
        currentMode = mode;
        if (currentMode == MODE.HUE) {
            modeInt = 0;
            grades = 360;
            colors = new int[361];
            positions = new float[361];

            float[] d = color.clone();
            d[1] = d[2] = 1;
            for (int x = 0; x <= 360; x++) {
                d[0] = (float) x;
                colors[x] = Color.HSVToColor(d);
                positions[x] = (float) x / 360f;
            }
        } else if (currentMode == MODE.SATURATION) {
            modeInt = 1;
            grades = 100;
            colors = null;
            positions = null;
        } else if (currentMode == MODE.VIBRANCE) {
            modeInt = 2;
            grades = 100;
            colors = null;
            positions = null;
        }

    }


    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        realWidth = w;
        if (modeInt == 0) {
            gradient = new LinearGradient(0, 0, getWidth(), getHeight(), colors, positions, Shader.TileMode.CLAMP);
            p.setShader(gradient);
        }
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event){
        if(paint != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            float[] newColor = color.clone();
            newColor[0] = event.getX()/realWidth * 360;
            paint.setCurrentColor(Color.HSVToColor(newColor));
        }
        return true;
    }*/

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentMode == MODE.HUE)
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        else {
            float[] color1 = color.clone();
            color1[modeInt] = 0;
            float[] color2 = color.clone();
            color2[modeInt] = 1;
            gradient = new LinearGradient(0, 0, canvas.getWidth(), canvas.getHeight(), Color.HSVToColor(color1), Color.HSVToColor(color2), Shader.TileMode.CLAMP);

            p.setShader(gradient);

            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }
    }
}
