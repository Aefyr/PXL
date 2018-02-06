package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aefyr.pxl.fragments.PreferencesFragment;
import com.aefyr.pxl.util.Utils;


/**
 * Created by Aefyr on 02.07.2017.
 */

public class SuperPencilH extends ToolH {
    Matrix mirrorMatrix;
    Style style = Style.SQUARE;
    private float circleRadius = 1;
    private float highVelocityThreshold;

    //Flawless symmetry
    boolean flawlessSymmetry;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    Paint flawlessPaint;

    enum Style {
        SQUARE, ROUND
    }

    SuperPencilH(AdaptivePixelSurfaceH adaptivePixelSurface) {
        roundCanvasXY = true;
        aps = adaptivePixelSurface;
        path = new Path();
        mirroredPath = new Path();
        mirrorMatrix = new Matrix();
        flawlessSymmetry = PreferenceManager.getDefaultSharedPreferences(adaptivePixelSurface.getContext()).getBoolean(PreferencesFragment.FLAWLESS_SYMMETRY, true);
        if(flawlessSymmetry) {
            mBitmap = Bitmap.createBitmap(aps.pixelWidth, aps.pixelHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            flawlessPaint = new Paint();
        }
        notifyScaleChanged();
    }

    void setStyle(Style style) {
        if (this.style == style)
            return;

        this.style = style;
        if (style == Style.SQUARE) {
            aps.paint.setStrokeCap(Paint.Cap.SQUARE);
        } else
            aps.paint.setStrokeCap(Paint.Cap.ROUND);
    }

    void updatePaints(){
        if(aps.currentTool== AdaptivePixelSurfaceH.Tool.ERASER){
            if(aps.project.transparentBackground){
                if(aps.symmetry&&flawlessSymmetry){
                    aps.paint.setXfermode(null);
                    flawlessPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                }else {
                    aps.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
            }else {
                aps.paint.setColor(Color.WHITE);
            }
        }else {
            if(aps.project.transparentBackground) {
                aps.paint.setXfermode(null);
                if(flawlessSymmetry)
                    flawlessPaint.setXfermode(null);
            }else
                aps.paint.setColor(aps.currentColor);
        }
    }

    void notifyScaleChanged(){
        highVelocityThreshold = aps.pixelScale*2;
    }


    boolean instaDots = true;

    private Path path;
    private float nX, nY;

    private float lX, lY;

    @Override
    void startDrawing(float x, float y) {
        if (drawing)
            return;

        lX = x;
        lY = y;

        hitBounds = false;

        calculateCanvasXY(x, y);

        moves = 0;
        path.reset();
        path.moveTo(sX, sY);
        nX = sX;
        nY = sY;

        aps.canvasHistory.startHistoricalChange();
        if(aps.symmetry&&flawlessSymmetry)
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (aps.strokeWidth == 1) {
            if(aps.symmetry&&flawlessSymmetry)
                mCanvas.drawPoint(sX, sY, aps.paint);
            else
                aps.pixelCanvas.drawPoint(sX, sY, aps.paint);

        }

        //STYLE
        if (aps.cursorMode && style == Style.SQUARE) {
            if(aps.symmetry&&flawlessSymmetry)
                mCanvas.drawPoint(sX, sY, aps.paint);
            else
                aps.pixelCanvas.drawPoint(sX, sY, aps.paint);

        }


        if (aps.symmetry) {
            if(flawlessSymmetry){
                aps.pixelCanvas.drawBitmap(mBitmap, 0, 0, flawlessPaint);
                aps.pixelCanvas.drawBitmap(mBitmap, mirrorMatrix, flawlessPaint);
            }else {
                calculateSymmetricalCanvasXY();
                mirroredPath.reset();
                if (aps.paint.getStrokeWidth() == 1)
                    aps.pixelCanvas.drawPoint(aSX, aSY, aps.paint);

                //STYLE
                if (aps.cursorMode && style == Style.SQUARE) {
                    aps.pixelCanvas.drawPoint(aSX, aSY, aps.paint);
                }
            }
        }

        drawing = true;
    }

    @Override
    void move(float x, float y) {
        if (!drawing)
            return;

        boolean highVelocity = Utils.vector2Distance(x, y, lX, lY) >= highVelocityThreshold;

        calculateCanvasXY(x, y);
        Log.d("SPH", "highVelocity="+highVelocity);

        if (aps.cursorMode && !highVelocity && aps.paint.getStrokeWidth() <= 4)
            path.lineTo(sX, sY);
        else
            path.quadTo(nX, nY, (sX + nX) / 2f, (sY + nY) / 2f);

        nX = sX;
        nY = sY;

        lX = x;
        lY = y;

        //Drawing main path, 2 options for 2 symmetry profiles
        if(aps.symmetry&&flawlessSymmetry) {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawPath(path, aps.paint);
        }


        if (aps.cursorMode && !highVelocity && instaDots && aps.strokeWidth == 1)
            (aps.symmetry&&flawlessSymmetry?mCanvas:aps.pixelCanvas).drawPoint(sX, sY, aps.paint);


        if (aps.symmetry) {
            if (aps.cursorMode && instaDots) {
                calculateSymmetricalCanvasXY();

                if (aps.cursorMode && instaDots && aps.strokeWidth == 1)
                    (flawlessSymmetry?mCanvas:aps.pixelCanvas).drawPoint(aSX, aSY, aps.paint);

            }

            //Drawing mirrored path, 2 options for 2 symmetry profiles
            if(flawlessSymmetry)
                aps.pixelCanvas.drawBitmap(mBitmap, mirrorMatrix, flawlessPaint);
            else {
                path.transform(mirrorMatrix, mirroredPath);
                aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
            }
        }

        if(aps.symmetry&&flawlessSymmetry)
            aps.pixelCanvas.drawBitmap(mBitmap, 0, 0, flawlessPaint);
        else
            aps.pixelCanvas.drawPath(path, aps.paint);

        moves++;
        aps.invalidate();
    }

    @Override
    void stopDrawing(float x, float y) {
        if (!drawing)
            return;

        calculateCanvasXY(x, y);

        path.setLastPoint(sX, sY);

        //Drawing main path, 2 options for 2 symmetry profiles
        if(aps.symmetry&&flawlessSymmetry) {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawPath(path, aps.paint);
        }

        if (moves < 10) {
            if (style == Style.SQUARE || aps.paint.getStrokeWidth() == 1)
                (aps.symmetry&&flawlessSymmetry?mCanvas:aps.pixelCanvas).drawPoint(sX, sY, aps.paint);
            else if (style == Style.ROUND)
                (aps.symmetry&&flawlessSymmetry?mCanvas:aps.pixelCanvas).drawCircle(sX, sY, circleRadius, aps.paint);
        }

        if (aps.symmetry) {
            //Drawing a point at the point where user released the draw button
            if (moves < 10) {
                calculateSymmetricalCanvasXY();
                if (style == Style.SQUARE || aps.paint.getStrokeWidth() == 1)
                    (flawlessSymmetry?mCanvas:aps.pixelCanvas).drawPoint(aSX, aSY, aps.paint);
                else if (style == Style.ROUND)
                    (flawlessSymmetry?mCanvas:aps.pixelCanvas).drawCircle(aSX, aSY, circleRadius, aps.paint);
            }

            //Drawing mirrored path, 2 options for 2 symmetry profiles
            if(flawlessSymmetry)
                aps.pixelCanvas.drawBitmap(mBitmap, mirrorMatrix, flawlessPaint);
            else {
                path.transform(mirrorMatrix, mirroredPath);
                aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
                mirroredPath.rewind();
            }
        }

        if(aps.symmetry&&flawlessSymmetry)
            aps.pixelCanvas.drawBitmap(mBitmap, 0, 0, flawlessPaint);
        else
            aps.pixelCanvas.drawPath(path, aps.paint);

        if (hitBounds)
            aps.canvasHistory.completeHistoricalChange();
        else
            aps.canvasHistory.cancelHistoricalChange(false);

        path.rewind();
        drawing = false;
        aps.invalidate();
    }

    @Override
    void cancel(float x, float y) {
        if (!drawing)
            return;
        if (aps.cursorMode) {
            stopDrawing(x, y);
            return;
        }
        if (moves > 10)
            stopDrawing(x, y);
        else {
            if (aps.symmetry)
                mirroredPath.reset();
            path.reset();
            drawing = false;
            aps.canvasHistory.cancelHistoricalChange(hitBounds);
        }
    }

    @Override
    void cancel() {
        cancel(rX, rY);
    }

    private Path mirroredPath;
    private float aSX, aSY;
    private float symmetryAxisX = 12.5f;
    private float symmetryAxisY = 64.5f;

    private void calculateSymmetricalCanvasXY() {
        aSX = sX;
        aSY = sY;
        if (aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL) {
            float d = Math.abs(symmetryAxisX-sX);
            aSX = symmetryAxisX+(sX<symmetryAxisX?d:-d);

        }

        if (aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.VERTICAL) {
            float d = Math.abs(symmetryAxisY-sY);
            aSY = symmetryAxisY+(sY<symmetryAxisY?d:+d);
        }
    }

    void symmetryUpdate() {
        if (!aps.symmetry)
            return;
        if (aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL) {
            mirrorMatrix.setScale(-1f, 1f, ((float) aps.pixelWidth) / 2f, ((float) aps.pixelHeight )/ 2f);
            return;
        }
        if (aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.VERTICAL) {
            mirrorMatrix.setScale(1f, -1f, ((float) aps.pixelWidth) / 2f, ((float) aps.pixelHeight )/ 2f);
        }

    }

    @Override
    protected void checkHitBounds() {
        if (aps.paint.getStrokeWidth() == 1)
            super.checkHitBounds();
        else
            hitBounds = true;
    }

    @Override
    public void writeStateToBundle(Bundle outState) {
        outState.putInt("superPencil_capStyle", style == Style.SQUARE?0:1);
    }

    @Override
    public void restoreState(Bundle savedInstanceState) {
        setStyle(savedInstanceState.getInt("superPencil_capStyle", 0)==0?Style.SQUARE:Style.ROUND);
    }

}
