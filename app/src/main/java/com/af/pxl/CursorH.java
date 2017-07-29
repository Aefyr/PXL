package com.af.pxl;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import com.af.pxl.Fragments.PreferencesFragment;

/**
 * Created by Aefyr on 02.07.2017.
 */

class CursorH {
    private AdaptivePixelSurfaceH aps;

    private float currentX, currentY;
    private int limitX, limitY;
    private int pixelSize = 128;
    int opacity = 255;
    Matrix matrix;

    private float sensitivity = 1f;

    Bitmap cursorPointerImage;
    private Canvas cursorPointerImageCanvas;

    interface OnCursorChangeListener{
        void onCursorEnabled(boolean enabled);
    }

    OnCursorChangeListener listener;

    CursorH(AdaptivePixelSurfaceH adaptivePixelSurface){
        aps = adaptivePixelSurface;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(aps.getContext());
        opacity = preferences.getInt(PreferencesFragment.CURSOR_OPACITY, 255);
        sensitivity = preferences.getInt(PreferencesFragment.CURSOR_SENSITIVITY, 9)/10f + 0.1f;

        matrix = new Matrix();
        drawDefaultCursorImage();
    }

    private void drawDefaultCursorImage(){
        cursorPointerImage = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888);
        cursorPointerImageCanvas = new Canvas(cursorPointerImage);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        paint.setStrokeWidth(8);
        cursorPointerImageCanvas.drawLine(0, 0, 0, pixelSize, paint);
        cursorPointerImageCanvas.drawLine(0, pixelSize, pixelSize, pixelSize, paint);
        calculateMatrix();
    }

    void setCursorPointerImage(Bitmap bitmap){
        cursorPointerImage = bitmap;
        System.out.println("SIZE="+cursorPointerImage.getWidth());
        calculateMatrix();
        if(aps.cursorMode)
            aps.invalidate();
    }

    void setLimits(int limitX, int limitY){
        this.limitX = limitX;
        this.limitY = limitY;
    }

    private float scale;
    private void calculateMatrix(){
        scale = (float) pixelSize/(float) cursorPointerImage.getWidth();
        updatePosition();
    }

    private void updatePosition(){
        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(currentX, currentY-pixelSize);
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
            currentX = Utils.clamp(currentX + ((x - previousX)*sensitivity), 0, limitX);
            currentY = Utils.clamp(currentY + ((y - previousY)*sensitivity), 0, limitY);
            updatePosition();
        }

        previousX = x;
        previousY = y;

        switch (aps.currentTool){
            case PENCIL:
            case ERASER:
                aps.superPencil.move(currentX, currentY);
                break;
            case FLOOD_FILL:
                break;
            case COLOR_PICK:
                break;
        }

        aps.invalidate();
    }


    void cursorDown(){
        switch (aps.currentTool){
            case PENCIL:
            case ERASER:
                aps.superPencil.startDrawing(currentX, currentY);
                break;
            case FLOOD_FILL:
                updateCanvasXY();
                aps.floodFill(canvasX, canvasY);
                break;
            case COLOR_PICK:
                updateCanvasXY();
                aps.colorPick(canvasX, canvasY);
                break;
            case COLOR_SWAP:
                updateCanvasXY();
                if(canvasX<aps.pixelWidth&&canvasX>=0&&canvasY<aps.pixelHeight&&canvasY>=0)
                    aps.onSpecialToolUseListener.onColorSwapToolUse(aps.pixelBitmap.getPixel((int)canvasX, (int)canvasY));
                break;
        }
    }

    void cursorUp(){
        switch (aps.currentTool){
            case PENCIL:
            case ERASER:
                aps.superPencil.stopDrawing(currentX, currentY);
                break;
            case FLOOD_FILL:
                break;
            case COLOR_PICK:
                break;
            case COLOR_SWAP:
                break;
        }
    }

    void setOnCursorChangeListener(OnCursorChangeListener listener){
        this.listener = listener;
    }

    void setEnabled(boolean enabled){
        if(enabled == aps.cursorMode)
            return;

        if(listener!=null){
            listener.onCursorEnabled(enabled);
        }
        aps.cursorMode = enabled;
        aps.invalidate();
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

    void center(int width, int height){
        currentX = width/2;
        currentY = height/2;
        updatePosition();
    }

    private float[] p = {0, 0};
    private int canvasX, canvasY;
    void updateCanvasXY(){
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        canvasX = (int) ((currentX-p[0])/aps.pixelScale);
        canvasY = (int) ((currentY-p[1])/aps.pixelScale);
    }

    boolean offCanvasBounds(){
        return !(canvasX>=0&&canvasY>=0&&canvasX<aps.pixelWidth&&canvasY<aps.pixelHeight);
    }

    int getCanvasX(){
        return canvasX;
    }

    int getCanvasY(){
        return canvasY;
    }

}
