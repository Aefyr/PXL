package com.af.pxl;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;


/**
 * Created by Aefyr on 02.07.2017.
 */

public class SuperPencilH {
    private AdaptivePixelSurfaceH aps;
    Matrix mirrorMatrix;

    SuperPencilH(AdaptivePixelSurfaceH adaptivePixelSurface){
        aps = adaptivePixelSurface;
        path = new Path();
        mirroredPath = new Path();
        mirrorMatrix = new Matrix();
    }

    boolean instaDots = true;

    private boolean drawing = false;
    private Path path;
    private float sX, sY, nX, nY;
    private int moves;

    private boolean hitBounds = false;

    void startDrawing(float x, float y){
        if(drawing)
            return;

        hitBounds = false;

        sX = x;
        sY = y;
        calculateCanvasXY();

        moves = 0;
        path.reset();
        path.moveTo(sX, sY);
        nX = sX;
        nY = sY;

        aps.canvasHistory.startHistoricalChange();

        if(aps.paint.getStrokeWidth()==1||aps.cursorMode)
            aps.pixelCanvas.drawPoint(sX, sY, aps.paint);

        if(aps.symmetry){
            calculateSymmetricalCanvasXY();
            mirroredPath.reset();
            if(aps.paint.getStrokeWidth()==1||aps.cursorMode)
                aps.pixelCanvas.drawPoint(aSX, aSY, aps.paint);
        }

        drawing = true;
    }

    void move(float x, float y){
        if(!drawing)
            return;

        sX = x;
        sY = y;
        calculateCanvasXY();

        if(aps.cursorMode)
            path.lineTo(sX, sY);
        else
            path.quadTo(nX, nY, (sX+nX)/2f, (sY+nY)/2f);

        nX = sX;
        nY = sY;

        if(aps.cursorMode&&instaDots){
            aps.pixelCanvas.drawPoint(sX, sY,aps.paint);
        }

        if(aps.symmetry){
            if(aps.cursorMode&&instaDots) {
                calculateSymmetricalCanvasXY();
                aps.pixelCanvas.drawPoint(aSX, aSY,aps.paint);
            }
            path.transform(mirrorMatrix, mirroredPath);
            aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
        }

        moves++;
        aps.pixelCanvas.drawPath(path, aps.paint);
        aps.invalidate();
    }

    void stopDrawing(float x, float y){
        if(!drawing)
            return;
        sX = x;
        sY = y;
        calculateCanvasXY();

        path.lineTo(sX, sY);
        path.setLastPoint(sX, sY);
        aps.pixelCanvas.drawPath(path, aps.paint);
        if(aps.paint.getStrokeWidth()==1)
            aps.pixelCanvas.drawPoint(sX, sY, aps.paint);

        if(aps.symmetry){
            path.transform(mirrorMatrix, mirroredPath);
            aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
            calculateSymmetricalCanvasXY();
            if(aps.paint.getStrokeWidth()==1)
                aps.pixelCanvas.drawPoint(sX, sY, aps.paint);
            mirroredPath.reset();
        }

        if(hitBounds)
            aps.canvasHistory.completeHistoricalChange();
        else
            aps.canvasHistory.cancelHistoricalChange(false);

        path.reset();
        drawing = false;
        aps.invalidate();
    }

    void cancel(float x, float y){
        if(!drawing)
            return;
        if(aps.cursorMode) {
            stopDrawing(x, y);
            return;
        }
        if(moves>10)
            stopDrawing(x, y);
        else {
            if(aps.symmetry)
                mirroredPath.reset();
            path.reset();
            drawing = false;
            aps.canvasHistory.cancelHistoricalChange(true);
        }
    }

    float[] p = {0,0};
    private void calculateCanvasXY(){
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        sX = (sX-p[0])/aps.pixelScale;
        sY = (sY-p[1])/aps.pixelScale;

        if(!hitBounds&&sX>0&&sX<aps.pixelWidth&&sY>0&&sY<aps.pixelHeight)
            hitBounds = true;

    }

    private Path mirroredPath;
    private float aSX, aSY;
    private void calculateSymmetricalCanvasXY(){
        aSX = sX;
        aSY = sY;
        if(aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL) {
            aSX = Math.abs(aps.pixelWidth - sX);
            if(sX>aps.pixelWidth)
                aSX = -aSX;
        }
        if(aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.VERTICAL) {
            aSY = Math.abs(aps.pixelHeight - sY);
            if(sY>aps.pixelHeight)
                aSY = -aSY;
        }

    }

    void symmetryUpdate(){
        if(!aps.symmetry)
            return;
        if(aps.symmetryType== AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL) {
            mirrorMatrix.setScale(-1f, 1f, (float) aps.pixelWidth/2f,(float)  aps.pixelHeight/2f);
            return;
        }
        if(aps.symmetryType == AdaptivePixelSurfaceH.SymmetryType.VERTICAL){
            mirrorMatrix.setScale(1f, -1f, (float) aps.pixelWidth/2f,(float)  aps.pixelHeight/2f);
        }

    }
}
