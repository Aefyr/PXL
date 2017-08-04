package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;

/**
 * Created by Aefyr on 31.07.2017.
 */

public class MultiShapeH extends ToolH{
    enum Shape{
        LINE, RECT, CIRCLE
    }

    private Paint overlayPaint;
    //Path path;
    Shape shape = Shape.LINE;
    boolean locked = false;
    boolean fill = false;
    int rounding = 0;



    private Bitmap backupBitmap;
    private Bitmap shapeBitmap;
    private Canvas shapeCanvas;

    private static final float[] angles = {0, 30, 45, 60, 90, 120, 135, 150, 180};


    MultiShapeH(AdaptivePixelSurfaceH aps){
        this.aps = aps;
        //path = new Path();
        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(false);
        overlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        shapeBitmap = Bitmap.createBitmap(aps.pixelWidth, aps.pixelHeight, Bitmap.Config.ARGB_8888);
        shapeCanvas = new Canvas(shapeBitmap);
    }

    @Override
    void startDrawing(float x, float y){
        if(drawing)
            return;

        moves = 0;
        calculateCanvasXY(x, y);
        startX = sX;
        startY = sY;

        if(fill&&(shape==Shape.CIRCLE||shape==Shape.RECT))
            aps.paint.setStyle(Paint.Style.FILL);

        shapeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        backupBitmap = aps.pixelBitmap.copy(Bitmap.Config.ARGB_8888, false);
        aps.canvasHistory.startHistoricalChange();
        drawing = true;

    }

    float[] d;

    @Override
    void move(float x, float y){
        if(!drawing)
            return;

        calculateCanvasXY(x, y);
        shapeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        switch (shape){
            case LINE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                //path.rewind();
                if(!locked)
                    shapeCanvas.drawLine(startX, startY, sX, sY, aps.paint);
                else {
                    float d = Utils.vector2Distance(startX, startY, sX, sY);

                    int lockedAngle = (int) Utils.getClosestNumber(Math.abs(Utils.vector2Angle(0, -1, sX-startX, sY-startY)), angles);

                    float sinA = (float) Math.abs(d*Math.sin(Math.toRadians(lockedAngle)));
                    float cosA = (float) Math.abs((d*Math.cos(Math.toRadians(lockedAngle))));

                    shapeCanvas.drawLine(startX, startY, sX-startX>0?startX+sinA:startX-sinA, lockedAngle<90?startY-cosA:startY+cosA, aps.paint);


                    float a = Utils.vector2Angle(0, -1, sX-startX, sY-startY);
                    System.out.println("Angle="+a+", closest locked angle="+lockedAngle+String.format(" cosLockedAngle: %f, sinLockedAngle: %f", (d*Math.cos(Math.toRadians(lockedAngle))),(d*Math.cos(Math.toRadians(lockedAngle)))));
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
                        shapeCanvas.drawRoundRect(startX<sX?startX:sX, startY<sY?startY:sY, startX<sX?sX:startX, startY<sY?sY:startY, rounding, rounding, aps.paint);
                        //path.addRoundRect(startX, startY, sX, sY, rounding, rounding, Path.Direction.CW);
                    else
                        shapeCanvas.drawRect(startX, startY, sX, sY, aps.paint);
                        //path.addRect(startX, startY, sX, sY, Path.Direction.CW);
                } else {
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);
                    float aX = (float) (startX+Math.sqrt(Math.pow(d[0], 2)/2f)*Math.signum(d[0]));
                    float aY = (float) (startY+Math.sqrt(Math.pow(d[1], 2)/2f)*Math.signum(d[1]));

                    if (rounding > 0 && Build.VERSION.SDK_INT >= 21) {

                        shapeCanvas.drawRoundRect(startX < aX ? startX : aX, startY < aY ? startY : aY, startX < aX ? aX : startX, startY < aY ? aY : startY, rounding, rounding, aps.paint);
                        //aps.pixelCanvas.drawRoundRect(startX, startY, startX+d[0], startY+d[1], rounding, rounding, aps.paint);
                        //path.addRoundRect(startX, startY, startX+d[0], startY+d[1], rounding, rounding, Path.Direction.CW);
                    }else
                        shapeCanvas.drawRect(startX, startY, aX, aY, aps.paint);
                        //path.addRect(startX, startY, startX+d[0], startY+d[1], Path.Direction.CW);

                }
                //aps.pixelCanvas.drawPath(path, aps.paint);
                break;
            case CIRCLE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                //path.rewind();
                if(!locked&& Build.VERSION.SDK_INT>=21)
                    shapeCanvas.drawOval(startX, startY, sX, sY, aps.paint);
                    //path.addOval(startX, startY, sX, sY, Path.Direction.CW);
                else if(locked&&Build.VERSION.SDK_INT>=21){
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);
                    shapeCanvas.drawOval(startX, startY, startX+d[0], startY+d[1], aps.paint);
                    //path.addOval(startX, startY, startX+d[0], startY+d[1], Path.Direction.CW);
                }else {
                    shapeCanvas.drawCircle(startX, startY, Utils.vector2Distance(startX, startY, sX, sY), aps.paint);
                    //path.addCircle(startX, startY, Utils.vector2Distance(startX, startY, sX, sY), Path.Direction.CW);
                }
                //aps.pixelCanvas.drawPath(path, aps.paint);
                break;
        }
        aps.pixelCanvas.drawBitmap(shapeBitmap,0, 0, aps.noAAPaint);
        if(aps.symmetry)
            aps.pixelCanvas.drawBitmap(shapeBitmap, aps.superPencil.mirrorMatrix, aps.noAAPaint);

        moves++;
        aps.invalidate();
    }

    @Override
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

    @Override
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
}
