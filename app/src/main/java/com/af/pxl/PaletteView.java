package com.af.pxl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Peter on 18.06.2017.
 */

public class PaletteView extends View {

    int[] colors = {Color.RED, Color.CYAN, Color.GREEN, Color.RED, Color.CYAN, Color.GREEN, Color.RED};
    Paint paint;

    final int COLORS_PER_LINE = 6;

    public PaletteView(Context context) {
        super(context);
        initialize();
    }

    public PaletteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    void initialize(){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP){

        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float gap = (((float) canvas.getWidth()) * 0.2f)/ (COLORS_PER_LINE+1);

        float px = ((float) canvas.getWidth() - ((float) canvas.getWidth())*0.2f)/COLORS_PER_LINE;

        float currentPos = gap;

        for(int i = 0; i<colors.length; i++){
            paint.setColor(colors[i]);
            canvas.drawRect(currentPos, 0, currentPos+px, canvas.getWidth(), paint);
            currentPos += px;
            currentPos += gap;
        }
    }
}
