package com.aefyr.pxl.experimental;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aefyr.pxl.custom.ColorRect;

/**
 * Created by Aefyr on 20.01.2018.
 */

public class SimpleColorPickerH extends View {
    public SimpleColorPickerH(@NonNull Context context) {
        super(context);
        initialize();
    }

    public SimpleColorPickerH(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SimpleColorPickerH(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO do this, lul
        //LinearGradient gradient = new LinearGradient()
    }
}
