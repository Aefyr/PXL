package com.aefyr.pxl.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 16.02.2018.
 */

public class HSVSeekBar extends View {
    public static final int MODE_H = 0;
    public static final int MODE_S = 1;
    public static final int MODE_V = 2;

    private int mode = 0;
    private int rounding = 8;

    private float[] color = {0, 1, 1};
    int[] colors;
    private Paint paint;

    private RectF gradientRectBorders;
    private float gradientPadding = 8;

    private float pointerAdditionalSize = 16;
    private float rawPointerPosition = 0;
    private float pointerStrokeWidth = 8;
    private float segmentsCount;
    private float segmentSize;

    private Bitmap hBitmap;

    public HSVSeekBar(Context context) {
        super(context);
        initialize();
    }

    public HSVSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize(){
        setMode(MODE_H);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(pointerStrokeWidth);
        gradientPadding += pointerAdditionalSize;
    }

    public void setMode(int mode){
        if(hBitmap!=null)
            hBitmap.recycle();
        hBitmap = null;

        if(mode == MODE_H){
            colors = new int[361];
            segmentsCount = 361;
        }else if(mode == MODE_S){
            colors = new int[101];
            segmentsCount = 101;
        }else if (mode == MODE_V){
            colors = new int[101];
            segmentsCount = 101;
        }else
            throw new IllegalArgumentException("Invalid mode parameter");
    }

    public int getSelectedPosition(){
        return (int) ((rawPointerPosition - gradientRectBorders.left)/segmentSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gradientRectBorders = new RectF(0+gradientPadding + (h - gradientPadding*2f)/2f, 0+gradientPadding, w-gradientPadding - (h - gradientPadding*2f)/2f, h-gradientPadding);
        segmentSize = gradientRectBorders.width()/segmentsCount;

        if(mode == MODE_H)
            hBitmap = generateGradient();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGradient(canvas);
        drawPointer(canvas);
    }

    private void drawGradient(Canvas c){
        if(mode == MODE_H)
            c.drawBitmap(hBitmap, 0, 0, paint);
        else
            renderGradient(c);
    }

    private void drawPointer(Canvas c){
        int segment = getSelectedPosition();
        Log.d("HSVSeekBar", "pos="+segment);
        float boundPosition = segment*segmentSize + gradientRectBorders.left;
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(boundPosition, c.getHeight()/2f, gradientRectBorders.height()/2f + pointerAdditionalSize, paint);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        c.drawCircle(boundPosition, c.getHeight()/2f, gradientRectBorders.height()/2f + pointerAdditionalSize, paint);
    }

    //Used only for MODE_H since it has a static gradient
    private Bitmap generateGradient(){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        renderGradient(canvas);
        return bitmap;
    }

    private void renderGradient(Canvas c){
        if(mode == MODE_H){
            color[1] = color[2] = 1;
            for(int i = 0; i<=360; i++){
                color[0] = i;
                colors[i] = Color.HSVToColor(color);
            }
        }else{
            for(int i = 0; i<=100; i++){
                color[mode] = ((float)i)/100f;
                colors[i] = Color.HSVToColor(color);
            }
        }

        paint.setShader(new LinearGradient(0, 0, gradientRectBorders.width(), gradientRectBorders.height(), colors, null, Shader.TileMode.CLAMP));
        c.drawRoundRect(gradientRectBorders, rounding, rounding, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            rawPointerPosition = Utils.clamp(event.getX(), gradientRectBorders.left, gradientRectBorders.right);
            invalidate();
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            rawPointerPosition = Utils.clamp(event.getX(), gradientRectBorders.left, gradientRectBorders.right);
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            rawPointerPosition = Utils.clamp(event.getX(), gradientRectBorders.left, gradientRectBorders.right);
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }
}
