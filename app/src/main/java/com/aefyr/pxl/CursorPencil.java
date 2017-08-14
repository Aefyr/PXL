package com.aefyr.pxl;

import android.graphics.Path;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 01.07.2017.
 */

class CursorPencil extends Tool {

    private AdaptivePixelSurface aps;

    float currentX = 0;
    float currentY = 0;

    private Path path, mirroredPath;

    CursorPencil(AdaptivePixelSurface aps){
        this.aps = aps;
        path = new Path();
        mirroredPath = new Path();
    }

    private int moves = 0;

    private float previousX, previousY;
    private float[] p = {0,0};

    private float sX, sY, nX, nY;
    private float aSX, aSY, aNX, aNY;
    @Override
    void processMotionEvent(MotionEvent motionEvent) {

        float eventX = motionEvent.getX();
        float eventY = motionEvent.getY();

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || aps.prevPointerCount >1){
            previousX = eventX;
            previousY = eventY;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
            currentX = Utils.clamp(currentX+(eventX-previousX), 0, aps.realWidth);
            currentY = Utils.clamp(currentY+(eventY-previousY), 0, aps.realHeight);
            if(inUse){
                p[0] = p[1] = 0;
                aps.pixelMatrix.mapPoints(p);

                sX = (currentX - p[0])/aps.pixelScale;
                sY = (currentY - p[1])/aps.pixelScale;

                if(aps.symmetry){
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

                    mirroredPath.quadTo(aNX, aNY, (aSX+aNX)/2,(aSY+aNY)/2);
                    aNX = aSX;
                    aNY = aSY;

                    aps.pixelCanvas.drawPath(mirroredPath, aps.paint);
                }

                path.quadTo(nX, nY, (sX+nX)/2,(sY+nY)/2);
                nX = sX;
                nY = sY;

                aps.pixelCanvas.drawPath(path, aps.paint);
                moves++;

            }
        }

        previousX = eventX;
        previousY = eventY;

        aps.pixelDrawThread.update();
    }

    void startUsing(){
        path.reset();
        mirroredPath.reset();
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        sX = (currentX - p[0])/aps.pixelScale;
        sY = (currentY - p[1])/aps.pixelScale;
        path.moveTo(sX, sY);
        nX = sX;
        nY = sY;

        aps.canvasHistory.startHistoricalChange();

        if(aps.symmetry) {
            aSX = sX;
            aSY = sY;
            if (aps.symmetryType == AdaptivePixelSurface.SymmetryType.HORIZONTAL) {
                aSX = Math.abs(aps.pixelWidth - sX);
                if (sX > aps.pixelWidth)
                    aSX = -aSX;
            }
            if (aps.symmetryType == AdaptivePixelSurface.SymmetryType.VERTICAL) {
                aSY = Math.abs(aps.pixelHeight - sY);
                if (sY > aps.pixelHeight)
                    aSY = -aSY;
            }
            mirroredPath.moveTo(aSX, aSY);
            aNX = aSX;
            aNY = aSY;
            aps.pixelCanvas.drawPoint(aSX, aSY, aps.paint);
        }

        aps.pixelCanvas.drawPoint(sX, sY, aps.paint);
        aps.pixelDrawThread.update();
        moves = 0;
        inUse = true;
    }

    void stopUsing(){
        if(inUse) {
            inUse = false;
            path.reset();
            mirroredPath.reset();
            aps.canvasHistory.completeHistoricalChange();
        }
    }

    @Override
    void cancel(MotionEvent motionEvent) {
        stopUsing();
    }
}
