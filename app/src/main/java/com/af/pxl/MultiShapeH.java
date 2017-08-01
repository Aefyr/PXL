package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;

/**
 * Created by Aefyr on 31.07.2017.
 */

public class MultiShapeH {
    enum Shape{
        LINE, RECT, CIRCLE
    }

    private Paint overlayPaint;
    //Path path;
    Shape shape = Shape.LINE;
    boolean locked = false;
    boolean fill = false;
    int rounding = 0;

    private boolean drawing = false;
    private float sX, sY;
    private float startX, startY;
    private int moves;

    private boolean hitBounds = false;

    private AdaptivePixelSurfaceH aps;

    Bitmap backupBitmap;


    MultiShapeH(AdaptivePixelSurfaceH aps){
        this.aps = aps;
        //path = new Path();
        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(false);
        overlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    void startDrawing(float x, float y){
        if(drawing)
            return;

        moves = 0;
        calculateCanvasXY(x, y);
        startX = sX;
        startY = sY;

        if(fill&&(shape==Shape.CIRCLE||shape==Shape.RECT))
            aps.paint.setStyle(Paint.Style.FILL);

        backupBitmap = aps.pixelBitmap.copy(Bitmap.Config.ARGB_8888, false);
        aps.canvasHistory.startHistoricalChange();
        drawing = true;

    }

    float[] d;

    void move(float x, float y){
        if(!drawing)
            return;

        calculateCanvasXY(x, y);
        switch (shape){
            case LINE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                //path.rewind();
                if(!locked)
                    aps.pixelCanvas.drawLine(startX, startY, sX, sY, aps.paint);
                else {
                    float d = Utils.vector2Distance(startX, startY, sX, sY);

                    //tan30deg = 1:1.73
                    float aA = d;
                    float aX = d*0.865f;
                    float aY = d/2;
                    aps.pixelCanvas.drawLine(startX, startY, startX+aX,startY+aY, aps.paint);
                    float a = +Utils.vector2Angle(0, -1, sX-startX, sY-startY);

                }
                    //path.moveTo(startX, startY);
                    //path.lineTo(sX, sY);
                //aps.pixelCanvas.drawPath(path, aps.paint);
                break;
            case RECT:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                //path.rewind();
                if(!locked) {
                    if (rounding > 0 && Build.VERSION.SDK_INT >= 21)
                        aps.pixelCanvas.drawRoundRect(startX<sX?startX:sX, startY<sY?startY:sY, startX<sX?sX:startX, startY<sY?sY:startY, rounding, rounding, aps.paint);
                        //path.addRoundRect(startX, startY, sX, sY, rounding, rounding, Path.Direction.CW);
                    else
                        aps.pixelCanvas.drawRect(startX, startY, sX, sY, aps.paint);
                        //path.addRect(startX, startY, sX, sY, Path.Direction.CW);
                } else {
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);

                    if (rounding > 0 && Build.VERSION.SDK_INT >= 21) {
                        float aX = startX+d[0];
                        float aY = startY+d[1];
                        aps.pixelCanvas.drawRoundRect(startX < aX ? startX : aX, startY < aY ? startY : aY, startX < aX ? aX : startX, startY < aY ? aY : startY, rounding, rounding, aps.paint);
                        //aps.pixelCanvas.drawRoundRect(startX, startY, startX+d[0], startY+d[1], rounding, rounding, aps.paint);
                        //path.addRoundRect(startX, startY, startX+d[0], startY+d[1], rounding, rounding, Path.Direction.CW);
                    }else
                        aps.pixelCanvas.drawRect(startX, startY, startX+d[0], startY+d[1], aps.paint);
                        //path.addRect(startX, startY, startX+d[0], startY+d[1], Path.Direction.CW);

                }
                //aps.pixelCanvas.drawPath(path, aps.paint);
                break;
            case CIRCLE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                //path.rewind();
                if(!locked&& Build.VERSION.SDK_INT>=21)
                    aps.pixelCanvas.drawOval(startX, startY, sX, sY, aps.paint);
                    //path.addOval(startX, startY, sX, sY, Path.Direction.CW);
                else if(locked&&Build.VERSION.SDK_INT>=21){
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);
                    aps.pixelCanvas.drawOval(startX, startY, startX+d[0], startY+d[1], aps.paint);
                    //path.addOval(startX, startY, startX+d[0], startY+d[1], Path.Direction.CW);
                }else {
                    aps.pixelCanvas.drawCircle(startX, startY, Utils.vector2Distance(startX, startY, sX, sY), aps.paint);
                    //path.addCircle(startX, startY, Utils.vector2Distance(startX, startY, sX, sY), Path.Direction.CW);
                }
                //aps.pixelCanvas.drawPath(path, aps.paint);
                break;
        }
        moves++;
        aps.invalidate();
    }

    void stopDrawing(float x, float y){
        if(!drawing)
            return;

        //path.rewind();

        if(hitBounds&&moves>=2)
            aps.canvasHistory.completeHistoricalChange();
        else
            aps.canvasHistory.cancelHistoricalChange(false);

        aps.paint.setStyle(Paint.Style.STROKE);
        drawing = false;
    }

    void cancel(float x, float y){
        if(!drawing)
            return;

        if(aps.cursorMode) {
            stopDrawing(x, y);
            return;
        }

        if(moves<10)
            aps.canvasHistory.cancelHistoricalChange(hitBounds);
        else
            stopDrawing(x, y);

        aps.paint.setStyle(Paint.Style.STROKE);

        drawing = false;
    }

    float[] p = {0,0};
    private void calculateCanvasXY(float x, float y){
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        sX = (x-p[0])/aps.pixelScale;
        sY = (y-p[1])/aps.pixelScale;

        if(!hitBounds&&sX>0&&sX<aps.pixelWidth&&sY>0&&sY<aps.pixelHeight)
            hitBounds = true;

    }
}
