package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;

import com.aefyr.pxl.common.RectP;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 31.07.2017.
 */

public class MultiShapeH extends ToolH {

    enum Shape {
        LINE, RECT, CIRCLE
    }

    Paint overlayPaint;
    Shape shape = Shape.LINE;
    boolean locked = false;
    boolean fill = false;
    int rounding = 0;
    private RectF rect = new RectF();


    private Bitmap backupBitmap;
    private Bitmap shapeBitmap;
    private Canvas shapeCanvas;

    private static final float[] angles = {0, 30, 45, 60, 90, 120, 135, 150, 180};


    MultiShapeH(AdaptivePixelSurfaceH aps) {
        autoCheckHitBounds = false;

        this.aps = aps;
        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(false);
        overlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        shapeBitmap = Bitmap.createBitmap(aps.pixelWidth, aps.pixelHeight, Bitmap.Config.ARGB_8888);
        shapeCanvas = new Canvas(shapeBitmap);

        canvasBounds = aps.getBounds();
        shapeBounds = new RectP();
    }

    @Override
    void startDrawing(float x, float y) {
        if (drawing)
            return;

        moves = 0;
        hitBounds = false;
        calculateCanvasXY(x, y);
        startX = sX;
        startY = sY;
        cX = sX;
        cY = sY;

        if (fill && (shape == Shape.CIRCLE || shape == Shape.RECT))
            aps.paint.setStyle(Paint.Style.FILL);

        shapeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        backupBitmap = aps.pixelBitmap.copy(Bitmap.Config.ARGB_8888, false);
        aps.canvasHistory.startHistoricalChange();
        drawing = true;

    }

    private float[] d;

    @Override
    void move(float x, float y) {
        if (!drawing)
            return;

        calculateCanvasXY(x, y);
        shapeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        switch (shape) {
            case LINE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                if (!locked)
                    shapeCanvas.drawLine(startX, startY, sX, sY, aps.paint);
                else {
                    float d = Utils.vector2Distance(startX, startY, sX, sY);

                    int lockedAngle = (int) Utils.getClosestNumber(Math.abs(Utils.vector2Angle(0, -1, sX - startX, sY - startY)), angles);

                    float sinA = (float) Math.abs(d * Math.sin(Math.toRadians(lockedAngle)));
                    float cosA = (float) Math.abs((d * Math.cos(Math.toRadians(lockedAngle))));

                    shapeCanvas.drawLine(startX, startY, sX - startX > 0 ? startX + sinA : startX - sinA, lockedAngle < 90 ? startY - cosA : startY + cosA, aps.paint);
                }
                cX = sX;
                cY = sY;
                break;
            case RECT:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                if (!locked) {
                    if (rounding > 0) {
                        rect.set(startX < sX ? startX : sX, startY < sY ? startY : sY, startX < sX ? sX : startX, startY < sY ? sY : startY);
                        shapeCanvas.drawRoundRect(rect, rounding, rounding, aps.paint);
                    }else
                        shapeCanvas.drawRect(startX, startY, sX, sY, aps.paint);

                    cX = sX;
                    cY = sY;
                } else {
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);
                    float aX = (float) (startX + Math.sqrt(Math.pow(d[0], 2) / 2f) * Math.signum(d[0]));
                    float aY = (float) (startY + Math.sqrt(Math.pow(d[1], 2) / 2f) * Math.signum(d[1]));

                    if (rounding > 0) {
                        rect.set(startX < aX ? startX : aX, startY < aY ? startY : aY, startX < aX ? aX : startX, startY < aY ? aY : startY);
                        shapeCanvas.drawRoundRect(rect, rounding, rounding, aps.paint);
                    } else
                        shapeCanvas.drawRect(startX, startY, aX, aY, aps.paint);

                    cX = aX;
                    cY = aY;
                }
                break;
            case CIRCLE:
                aps.pixelCanvas.drawBitmap(backupBitmap, 0, 0, overlayPaint);
                if (!locked) {
                    float x1 = sX >= 0 ? startX : sX;
                    float x2 = sX >= 0 ? sX : startX;
                    float y1 = sY >= 0 ? startY : sY;
                    float y2 = sY >= 0 ? sY : startY;
                    if (startX > aps.pixelWidth) {
                        float t = x1;
                        x1 = x2;
                        x2 = t;
                    }
                    if (startY > aps.pixelHeight) {
                        float t = y1;
                        y1 = y2;
                        y2 = t;
                    }
                    rect.set(x1, y1, x2, y2);
                    shapeCanvas.drawOval(rect, aps.paint);

                    cX = sX;
                    cY = sY;
                } else{
                    d = Utils.signedVector2Distance(startX, startY, sX, sY);
                    float actualD = (float) Math.sqrt(Math.pow(d[0], 2f)/2f);
                    d[0] = actualD * Math.signum(d[0]);
                    d[1] = actualD * Math.signum(d[1]);

                    float x1 = startX + d[0] >= 0 ? startX : startX + d[0];
                    float x2 = startX + d[0] >= 0 ? startX + d[0] : startX;
                    float y1 = startY + d[1] >= 0 ? startY : startY + d[1];
                    float y2 = startY + d[1] >= 0 ? startY + d[1] : startY;
                    if (startX > aps.pixelWidth) {
                        float t = x1;
                        x1 = x2;
                        x2 = t;
                    }
                    if (startY > aps.pixelHeight) {
                        float t = y1;
                        y1 = y2;
                        y2 = t;
                    }

                    rect.set(x1, y1, x2, y2);
                    shapeCanvas.drawOval(rect, aps.paint);

                    cX = x1 == startX ? x2 : x1;
                    cY = y1 == startY ? y2 : y1;
                }
                break;
        }
        aps.pixelCanvas.drawBitmap(shapeBitmap, 0, 0, aps.noAAPaint);
        if (aps.symmetry)
            aps.pixelCanvas.drawBitmap(shapeBitmap, aps.superPencil.mirrorMatrix, aps.noAAPaint);

        moves++;
        aps.invalidate();
    }

    @Override
    void stopDrawing(float x, float y) {
        if (!drawing)
            return;

        checkHitBounds();

        if (hitBounds && moves >= 1)
            aps.canvasHistory.completeHistoricalChange();
        else
            aps.canvasHistory.cancelHistoricalChange(false);

        aps.paint.setStyle(Paint.Style.STROKE);
        drawing = false;
    }

    @Override
    void cancel(float x, float y) {
        if (!drawing)
            return;

        if (aps.cursorMode) {
            stopDrawing(x, y);
            return;
        }

        if (moves < 10) {
            checkHitBounds();
            aps.canvasHistory.cancelHistoricalChange(hitBounds);
        } else
            stopDrawing(x, y);

        aps.paint.setStyle(Paint.Style.STROKE);

        drawing = false;
    }

    @Override
    void cancel() {
        cancel(rX, rY);
    }

    private RectP canvasBounds;
    private RectP shapeBounds;
    private float cX, cY;

    @Override
    protected void checkHitBounds() {
        if (aps.paint.getStrokeWidth() == 1) {
            shapeBounds.set((int) startX, (int) startY, (int) cX, (int) cY);
            hitBounds = shapeBounds.overlaps(canvasBounds);
        } else
            hitBounds = true;
    }


    @Override
    public void writeStateToBundle(Bundle outState) {
        int shapeInt = 0;
        if(shape==Shape.RECT)
            shapeInt = 1;
        else if(shape==Shape.CIRCLE)
            shapeInt = 2;

        outState.putInt("multishape_shape", shapeInt);
        outState.putBoolean("multishape_locked", locked);
        outState.putBoolean("multishape_fill", fill);
        outState.putInt("multishape_rounding", rounding);

    }

    @Override
    public void restoreState(Bundle savedInstanceState) {
        int shapeInt = savedInstanceState.getInt("multishape_shape", 0);
        if(shapeInt == 1)
            shape = Shape.RECT;
        else if(shapeInt == 2)
            shape = Shape.CIRCLE;
        else
            shape = Shape.LINE;

        locked = savedInstanceState.getBoolean("multishape_locked", false);
        fill = savedInstanceState.getBoolean("multishape_fill", false);
        rounding = savedInstanceState.getInt("multishape_rounding", 0);
    }
}
