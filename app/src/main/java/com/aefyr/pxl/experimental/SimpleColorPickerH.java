package com.aefyr.pxl.experimental;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.R;
import com.aefyr.pxl.util.Utils;

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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SimpleColorPickerH, 0, 0);
        circleRadius = a.getDimension(R.styleable.SimpleColorPickerH_circleSize, Utils.dpToPx(32, getResources()));
        circleStrokeThickness = a.getDimension(R.styleable.SimpleColorPickerH_circleStrokeWidth, Utils.dpToPx(2, getResources()));
        a.recycle();

        initialize();
    }

    private void initialize(){
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStrokeThickness);
        circlePaint.setColor(Color.WHITE);
    }

    private Bitmap gradient;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(gradient!=null)
            gradient.recycle();

        gradient = drawGradient(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isInEditMode()){
            canvas.drawColor(Color.RED);
            moveCircleTo(getWidth()/2, getHeight()/2);
            drawCircle(canvas);
            return;
        }
        canvas.drawBitmap(gradient, 0, 0, null);
        drawCircle(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            return true;



        if(listener!=null)
            listener.onColorPicked(resolveColorAt(event.getX(), event.getY()));
        moveCircleTo(event.getX(), event.getY());

        if(event.getAction()==MotionEvent.ACTION_UP)
            return true;

        return false;
    }

    private float circleRadius = 80;
    private float circleStrokeThickness = 16;
    private Paint circlePaint;
    private float currentCircleX, currentCircleY;
    private void moveCircleTo(float x, float y){
        float a = circleRadius+circleStrokeThickness/2f;
        invalidate((int)(currentCircleX - a), (int)(currentCircleY-a), (int)(currentCircleX+a), (int)(currentCircleY+a));
        currentCircleX = Utils.clamp(x, 0, getWidth());
        currentCircleY = Utils.clamp(y, 0, getHeight());
        invalidate((int)(currentCircleX - a), (int)(currentCircleY-a), (int)(currentCircleX+a), (int)(currentCircleY+a));
    }

    private void drawCircle(Canvas c){
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAlpha(75);
        c.drawCircle(currentCircleX+circleStrokeThickness/3f, currentCircleY+circleStrokeThickness/3f, circleRadius, circlePaint);

        circlePaint.setAlpha(255);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(resolveColorAt(currentCircleX, currentCircleY));
        c.drawCircle(currentCircleX, currentCircleY, circleRadius, circlePaint);

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.WHITE);
        c.drawCircle(currentCircleX, currentCircleY, circleRadius, circlePaint);
    }

    private int resolveColorAt(float x, float y){
        return Color.HSVToColor(new float[]{x/((float)getWidth()/361f), (y/((float)getHeight()/101f))/100f, 1});
    }

    private Bitmap drawGradient(float value){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        long start = System.currentTimeMillis();

        Paint p = new Paint();

        float fractionY = (float)getHeight()/100f;

        int[] colors = new int[361];
        float[] hsv = {0, 0, value};


        for(int s = 0; s<=100; s++){
            hsv[1] = (float)s/100f;

            for(int h = 0; h<=360; h++){
                hsv[0] = (float)h;
                colors[h] = Color.HSVToColor(hsv);
            }

            LinearGradient gradient = new LinearGradient(0, 0, getWidth(), fractionY, colors, null, Shader.TileMode.CLAMP);
            p.setShader(gradient);
            canvas.drawRect(0, fractionY*(float)s, getWidth(), fractionY*(float)s+fractionY*((float)s+1f), p);
        }

        Log.d("SimpleColorPickerH", String.format("Generated gradient in %d ms.", System.currentTimeMillis()-start));

        return bitmap;
    }

    public interface OnColorPickListener{
        void onColorPicked(int color);
    }
    private OnColorPickListener listener;

    public void setOnColorPickListener(OnColorPickListener listener){
        this.listener = listener;
    }
}
