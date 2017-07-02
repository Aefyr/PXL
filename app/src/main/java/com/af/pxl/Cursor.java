package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 02.07.2017.
 */

public class Cursor {
    private AdaptivePixelSurface aps;

    private float currentX, currentY;
    private float limitX, limitY;

    private float sensitivity = 1f;

    Bitmap cursorPointerImage;
    private Canvas cursorPointerImageCanvas;

    Cursor(AdaptivePixelSurface adaptivePixelSurface){
        aps = adaptivePixelSurface;
        cursorPointerImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        drawDefaultCursorImage();
    }

    private void drawDefaultCursorImage(){
        cursorPointerImageCanvas = new Canvas(cursorPointerImage);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        paint.setStrokeWidth(8);
        cursorPointerImageCanvas.drawLine(0, 0, 0, 100, paint);
        cursorPointerImageCanvas.drawLine(0,100,100,100, paint);
    }

    void setLimits(float limitX, float limitY){
        this.limitX = limitX;
        this.limitY = limitY;
    }

    private float previousX, previousY;

    void processMotionEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN || aps.prevPointerCount > 1) {
            previousX = x;
            previousY = y;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            currentX = Utils.clamp(currentX + (x - previousX)*sensitivity, 0, limitX);
            currentY = Utils.clamp(currentY + (y - previousY)*sensitivity, 0, limitY);
        }

        previousX = x;
        previousY = y;

        aps.pixelDrawThread.update();
    }

    void setSensitivity(float sensitivity){
        this.sensitivity = sensitivity;
    }

    float getX(){
        return currentX;
    }

    float getY(){
        return currentY;
    }
}
