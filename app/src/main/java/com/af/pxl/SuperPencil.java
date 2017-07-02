package com.af.pxl;

import android.graphics.Path;

/**
 * Created by Aefyr on 02.07.2017.
 */

public class SuperPencil {
    private AdaptivePixelSurface aps;

    SuperPencil(AdaptivePixelSurface adaptivePixelSurface){
        aps = adaptivePixelSurface;
        path = new Path();
        mirroredPath = new Path();
    }

    private boolean drawing = false;
    private Path path;
    private float sX, sY, nX, nY;
    private int moves;
    void startDrawing(float x, float y){
        if(drawing)
            return;

        sX = x;
        sY = y;
        calculateCanvasXY();

        moves = 0;
        path.reset();
        path.moveTo(sX, sY);
        nX = sX;
        nY = sY;
        if(aps.symmetry){
            calculateSymmetricalCanvasXY();
            mirroredPath.reset();
            mirroredPath.moveTo(aSX, aSY);
            aNX = aSX;
            aNY = aSY;
        }
        aps.canvasHistory.startHistoricalChange();
        drawing = true;
    }

    void move(float x, float y){
        if(!drawing)
            return;

        sX = x;
        sY = y;
        calculateCanvasXY();

        path.quadTo(nX, nY, (sX+nX)/2f, (sY+nY)/2f);
        nX = sX;
        nY = sY;

        if(aps.symmetry){
            calculateSymmetricalCanvasXY();
            mirroredPath.quadTo(aNX, aNY, (aSX+aNX)/2f, (aSY+aNY)/2f);
            aNX = aSX;
            aNY = aSY;
            aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
        }

        moves++;
        aps.pixelCanvas.drawPath(path, aps.paint);
        aps.pixelDrawThread.update();
    }

    void stopDrawing(float x, float y){
        if(!drawing)
            return;
        sX = x;
        sY = y;
        calculateCanvasXY();
        if(aps.symmetry){
            calculateSymmetricalCanvasXY();
            aps.pixelCanvas.drawPoint(aSX, aSY, aps.paint);
            mirroredPath.reset();
        }
        aps.pixelCanvas.drawPoint(sX, sY, aps.paint);
        aps.canvasHistory.completeHistoricalChange();
        path.reset();
        drawing = false;
        aps.pixelDrawThread.update();
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
            aps.canvasHistory.cancelHistoricalChange();
        }
    }

    float[] p = {0,0};
    private void calculateCanvasXY(){
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        sX = (sX-p[0])/aps.pixelScale;
        sY = (sY-p[1])/aps.pixelScale;
    }

    private float aSX, aSY, aNX, aNY;
    private Path mirroredPath;
    private void calculateSymmetricalCanvasXY(){
        aSX = sX;
        aSY = sY;
        if(aps.symmetryType == AdaptivePixelSurface.SymmetryType.HORIZONTAL) {
            aSX = Math.abs(aps.pixelWidth - sX);
            if(sX>aps.pixelWidth)
                aSX = -aSX;
        }
        if(aps.symmetryType == AdaptivePixelSurface.SymmetryType.VERTICAL) {
            aSY = Math.abs(aps.pixelHeight - sY);
            if(sY>aps.pixelHeight)
                aSY = -aSY;
        }
    }
}
