package com.af.pxl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Aefyr on 13.06.2017.
 */

public class ColorPickerView extends View {

    float pixelsPerGrade;
    float [] color = {1,1,1};
    Paint p;
    float realWidth;
    Paint paint;

    float[] drawingColor;

    LinearGradient gradient;

    MODE currentMode;
    float grades;
    int modeInt;

    int[] colors;
    float[] positions;



    public enum MODE{
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

    void initialize(){
        setMode(MODE.HUE);
        p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(Color.HSVToColor(color));

    }

    public void setMode(MODE mode){
        currentMode = mode;
        if(currentMode == MODE.HUE){
            modeInt = 0;
            grades = 360;
            colors = new int[361];
            positions = new float[361];
        }else if(currentMode == MODE.SATURATION){
            modeInt = 1;
            grades = 100;
            colors = null;
            positions = null;
        }else if(currentMode == MODE.VIBRANCE){
            modeInt = 2;
            grades = 100;
            colors = null;
            positions = null;
        }
        updatePixelsPerGrade();

    }

    void updatePixelsPerGrade(){
        pixelsPerGrade = realWidth / grades;
        invalidate();
    }


    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        realWidth = w;
        updatePixelsPerGrade();
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
    public void onDraw(Canvas canvas){

        //long timeStart = System.currentTimeMillis();

        super.onDraw(canvas);

        //canvas.drawColor(Color.BLUE);

        //System.out.println("DRAW, modeInt="+modeInt);


        if(currentMode == MODE.HUE) {

            //Old Method, ~20 ms
            /*float currentGrade = 0;
            drawingColor = color.clone();
            for (float x = 0; x <= grades; x++) {

                drawingColor[modeInt] = x;

                p.setCurrentColor(Color.HSVToColor(drawingColor));
                canvas.drawRect(currentGrade, 0, currentGrade += pixelsPerGrade, canvas.getHeight(), p);
                //System.out.println("x=" + x);
            }*/

            //New Method, <3 ms
            float[] d = color.clone();



            for(int x = 0; x<=360; x++){
                d[0] = (float) x;
                colors[x] = Color.HSVToColor(d);
                positions[x] = (float)x/360f;
            }

            gradient = new LinearGradient(0, 0 , canvas.getWidth(), canvas.getHeight(),colors, positions, Shader.TileMode.CLAMP);
            p.setShader(gradient);

            canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(), p);

        }else {

            //Old Method, ~2 ms
            /*for (float y = 0; y <= 1; y += 0.01f) {
                drawingColor[modeInt] = y;
                p.setCurrentColor(Color.HSVToColor(drawingColor));
                canvas.drawRect(currentGrade, 0, currentGrade += pixelsPerGrade, canvas.getHeight(), p);
            }*/

            //New Method <1 ms
            float[] color1 = color.clone();
            color1[modeInt] = 0;
            float[] color2 = color.clone();
            color2[modeInt] = 1;
            gradient = new LinearGradient(0,0, canvas.getWidth(), canvas.getHeight(), Color.HSVToColor(color1), Color.HSVToColor(color2), Shader.TileMode.CLAMP);

            p.setShader(gradient);

            canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(), p);
        }

        //System.out.println("Drawn in: " + (System.currentTimeMillis() - timeStart));
        //canvas.drawRect(canvas.getWidth()-100,0,canvas.getWidth(), canvas.getHeight(), p);
        //System.out.println("Pixels per grade: "+pixelsPerGrade);



        //canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 16, p);

        //canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), p);
    }
}
