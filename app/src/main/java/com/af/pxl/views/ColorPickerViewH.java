package com.af.pxl.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.af.pxl.Utils;

/**
 * Created by Aefyr on 13.06.2017.
 */

public class ColorPickerViewH extends View {

    public float[] color = {1, 1, 1};
    Paint p;
    float realWidth;

    LinearGradient gradient;

    Mode currentMode;
    float grades;
    int modeInt;

    int[] colors;
    float[] positions;


    public enum Mode {
        HUE, SATURATION, VIBRANCE
    }


    public ColorPickerViewH(Context context) {
        super(context);
        initialize();
    }

    public ColorPickerViewH(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    void initialize() {
        setMode(Mode.HUE);
        p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.HSVToColor(color));

    }

    public void setMode(Mode mode) {
        currentMode = mode;
        if (currentMode == Mode.HUE) {
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
        } else if (currentMode == Mode.SATURATION) {
            modeInt = 1;
            grades = 100;
            colors = null;
            positions = null;
        } else if (currentMode == Mode.VIBRANCE) {
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
        }
        p.setStrokeWidth(realWidth / grades);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentMode == Mode.HUE) {
            p.setShader(gradient);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        } else {
            float[] color1 = color.clone();
            color1[modeInt] = 0;
            float[] color2 = color.clone();
            color2[modeInt] = 1;
            gradient = new LinearGradient(0, 0, canvas.getWidth(), canvas.getHeight(), Color.HSVToColor(color1), Color.HSVToColor(color2), Shader.TileMode.CLAMP);

            p.setShader(gradient);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
        }

        int x = Math.round(((float) canvas.getWidth() / grades) * (modeInt == 0 ? color[modeInt] : color[modeInt] * 100));
        p.setShader(null);
        p.setColor(Utils.invertColor(Color.HSVToColor(color)));

        canvas.drawLine(x, 0, x, canvas.getHeight(), p);
    }
}
