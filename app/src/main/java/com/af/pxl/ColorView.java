package com.af.pxl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Peter on 17.06.2017.
 */

public class ColorView extends View {

    float[] color = {0,0,1};

    public ColorView(Context context) {
        super(context);
    }

    public ColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawColor(Color.HSVToColor(color));
    }
}
