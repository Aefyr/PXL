package com.aefyr.pxl.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
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
    private Bitmap gradient;
    private RectF mainGradientBorders;
    private RectF mainGradientHitBox;
    private RectF valueGradientBorders;
    private RectF valueGradientHitBox;
    private float valuePickerWidth = 80;
    private float cornersRounding = 20;

    private boolean ready;
    private int toColor;

    public SimpleColorPickerH(@NonNull Context context) {
        super(context);
        initialize();
    }

    public SimpleColorPickerH(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SimpleColorPickerH, 0, 0);
        mainCircleRadius = a.getDimension(R.styleable.SimpleColorPickerH_circleSize, Utils.dpToPx(32, getResources()));
        circleStrokeThickness = a.getDimension(R.styleable.SimpleColorPickerH_circleStrokeWidth, Utils.dpToPx(2, getResources()));
        valuePickerWidth = a.getDimension(R.styleable.SimpleColorPickerH_valueGradientWidth, Utils.dpToPx(32, getResources()));
        cornersRounding = a.getDimension(R.styleable.SimpleColorPickerH_cornersRounding, Utils.dpToPx(8, getResources()));
        a.recycle();

        initialize();
    }

    public void setColor(int color){
        if(!ready){
            toColor=color;
            return;
        }
        float[] colorHSV = new float[3];
        Color.colorToHSV(color, colorHSV);

        mainCircleX = mainGradientBorders.left + colorHSV[0] * (mainGradientBorders.width()/361f);
        mainCircleY = mainGradientBorders.top + colorHSV[1] * 100 *(mainGradientBorders.height()/101f);
        valueCircleY = valueGradientBorders.top + (1-colorHSV[2]) * 100 * (valueGradientBorders.height()/101f);
        invalidate();
    }

    private void initialize(){
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStrokeThickness);
        circlePaint.setColor(Color.WHITE);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(gradient!=null)
            gradient.recycle();

        mainGradientBorders = new RectF(getPaddingLeft(), getPaddingTop(), getWidth()-getPaddingRight()*2- valuePickerWidth, getHeight()-getPaddingBottom());
        if(mainGradientBorders.width()<1||mainGradientBorders.height()<1)
            return;

        mainGradientHitBox = new RectF(mainGradientBorders);
        float d = mainCircleRadius + circleStrokeThickness/2f;
        mainGradientHitBox.top -= d;
        mainGradientHitBox.bottom += d;
        mainGradientHitBox.left -= d;
        mainGradientHitBox.right += d;


        valueGradientBorders = new RectF(mainGradientBorders.right+getPaddingRight(), mainGradientBorders.top, getWidth()-getPaddingRight(), mainGradientBorders.bottom);

        valueCircleX = valueGradientBorders.left+valueGradientBorders.width()/2f;
        valueCircleRadius = valueGradientBorders.width()/2f;

        valueGradientHitBox = new RectF(valueGradientBorders);
        valueGradientHitBox.top -= valueCircleRadius + circleStrokeThickness/2f;
        valueGradientHitBox.bottom += valueCircleRadius + circleStrokeThickness/2f;
        valueGradientHitBox.left -= circleStrokeThickness/2f;
        valueGradientHitBox.right += circleStrokeThickness/2f;

        mainCircleX = mainGradientBorders.left;
        mainCircleY = mainGradientBorders.top;
        valueCircleY = valueGradientBorders.top;

        gradient = drawGradient(1);

        ready=true;
        setColor(toColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mainGradientBorders.width()<1||mainGradientBorders.height()<1)
            return;
        super.onDraw(canvas);
        if(isInEditMode()){
            circlePaint.setColor(Color.RED);
            canvas.drawRect(mainGradientBorders, circlePaint);
            canvas.drawRect(valueGradientBorders, circlePaint);
            drawMainCircle(canvas);
            drawValueCircle(canvas);
            return;
        }

        drawMainGradient(canvas);
        drawMainCircle(canvas);

        drawValueGradient(canvas);
        drawValueCircle(canvas);
    }

    private int currentlyMoving = -1;
    private static final int MAIN_CIRCLE = 0;
    private static final int VALUE_CIRCLE = 1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            if(mainGradientHitBox.contains(event.getX(), event.getY())){
                currentlyMoving = MAIN_CIRCLE;
                return true;
            }

            if(valueGradientHitBox.contains(event.getX(), event.getY())){
                currentlyMoving = VALUE_CIRCLE;
                return true;
            }

            currentlyMoving = -1;
            return false;
        }

        if(event.getAction()==MotionEvent.ACTION_MOVE){
            switch (currentlyMoving){
                case MAIN_CIRCLE:
                    moveMainCircleTo(event.getX(), event.getY());
                    break;
                case VALUE_CIRCLE:
                    moveValueCircle(event.getY());
                    break;
            }

            if(listener!=null)
                listener.onColorPicked(resolveCurrentColor());

            return true;
        }


        if(event.getAction()==MotionEvent.ACTION_UP)
            return true;

        return false;
    }

    //Circles
    private float mainCircleRadius = 80;
    private float circleStrokeThickness = 16;
    private Paint circlePaint;
    private float mainCircleX, mainCircleY;
    private void moveMainCircleTo(float x, float y){
        mainCircleX = Utils.clamp(x, mainGradientBorders.left, mainGradientBorders.right);
        mainCircleY = Utils.clamp(y, mainGradientBorders.top, mainGradientBorders.bottom);

        invalidate();
    }

    private void drawMainCircle(Canvas c){
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAlpha(75);
        c.drawCircle(mainCircleX, mainCircleY +circleStrokeThickness/5f, mainCircleRadius +circleStrokeThickness/4f, circlePaint);

        circlePaint.setAlpha(255);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(resolveCurrentColor());
        c.drawCircle(mainCircleX, mainCircleY, mainCircleRadius, circlePaint);

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.WHITE);
        c.drawCircle(mainCircleX, mainCircleY, mainCircleRadius, circlePaint);
    }

    private float valueCircleRadius;
    private float valueCircleX, valueCircleY;
    private void moveValueCircle(float y){
        valueCircleY = Utils.clamp(y, valueGradientBorders.top, valueGradientBorders.bottom);

        invalidate();
    }

    private void drawValueCircle(Canvas canvas){
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAlpha(75);
        canvas.drawCircle(valueCircleX, valueCircleY +circleStrokeThickness/5f, valueCircleRadius +circleStrokeThickness/4f, circlePaint);

        circlePaint.setAlpha(255);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(resolveCurrentColor());
        canvas.drawCircle(valueCircleX, valueCircleY, valueCircleRadius, circlePaint);

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.WHITE);
        canvas.drawCircle(valueCircleX, valueCircleY, valueCircleRadius, circlePaint);
    }

    //Gradients
    private void drawMainGradient(Canvas canvas){
        canvas.drawBitmap(gradient, mainGradientBorders.left, mainGradientBorders.top, null);
    }

    private int[] values = new int[101];
    private Paint valuePaint;
    private void drawValueGradient(Canvas canvas){
        float[] color = resolveCurrentColorRaw();

        for(int v = 0; v<=100; v++){
            color[2] = 1f-((float)v/100f);
            values[v] = Color.HSVToColor(color);
        }
        LinearGradient valueGradient = new LinearGradient(valueGradientBorders.centerX(), 0, valueGradientBorders.centerX(), valueGradientBorders.height(), values, null, Shader.TileMode.CLAMP);
        valuePaint.setShader(valueGradient);

        canvas.drawRoundRect(valueGradientBorders, cornersRounding, cornersRounding, valuePaint);
    }

    //Misc
    private int resolveCurrentColor(){
        return Color.HSVToColor(resolveCurrentColorRaw());
    }

    private float[] resolveCurrentColorRaw(){
        return new float[]{(mainCircleX-mainGradientBorders.left)/(mainGradientBorders.width()/361f), ((mainCircleY-mainGradientBorders.top)/(mainGradientBorders.height()/101f))/100f, 1f-(((valueCircleY-valueGradientBorders.top)/(valueGradientBorders.height()/101f))/100f)};
    }

    private Bitmap drawGradient(float value){
        Bitmap bitmap = Bitmap.createBitmap((int) mainGradientBorders.width(), (int) mainGradientBorders.height(), Bitmap.Config.ARGB_8888);
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

            LinearGradient gradient = new LinearGradient(0, fractionY/2f, mainGradientBorders.width(), fractionY/2f, colors, null, Shader.TileMode.CLAMP);
            p.setShader(gradient);
            canvas.drawRect(0, fractionY*(float)s, getWidth(), fractionY*(float)s+fractionY*((float)s+1f), p);
        }


        Bitmap mask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(mask);

        Paint temp = new Paint(Paint.ANTI_ALIAS_FLAG);
        temp.setStyle(Paint.Style.FILL);
        temp.setColor(Color.BLACK);

        maskCanvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), cornersRounding, cornersRounding, temp);

        temp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mask, 0,0, temp);

        mask.recycle();

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
