package com.aefyr.pxl;

import android.graphics.Path;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 28.06.2017.
 */

public class Pencil extends Tool{

    private AdaptivePixelSurface adaptivePixelSurface;
    private int moves;

    private float sX, sY, nX, nY;
    private float aSX, aSY, aNX, aNY;

    private Path path;
    private Path mirroredPath;
    private float[] p = {0, 0};

    Pencil(AdaptivePixelSurface adaptivePixelSurface){
        this.adaptivePixelSurface = adaptivePixelSurface;
        path = new Path();
        mirroredPath = new Path();
    }


    @Override
    void processMotionEvent(MotionEvent motionEvent) {

        //Legacy formula, my brain almost exploded when I was deriving it and I have no idea how it even works
        //sX = (motionEvent.getX()-adaptivePixelSurface.matrixOffsetX)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorX;
        //sY = (motionEvent.getY()-adaptivePixelSurface.matrixOffsetY)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorY;

        //New formula, this one is so easy, ffs why didn't I know about Matrix.mapPoints before
        p[0] = p[1] = 0;
        adaptivePixelSurface.pixelMatrix.mapPoints(p);

        sX=(motionEvent.getX()-p[0])/adaptivePixelSurface.pixelScale;
        sY=(motionEvent.getY()-p[1])/adaptivePixelSurface.pixelScale;



        if(adaptivePixelSurface.symmetry){
            aSX = sX;
            aSY = sY;
            if(adaptivePixelSurface.symmetryType == AdaptivePixelSurface.SymmetryType.HORIZONTAL) {
                aSX = Math.abs(adaptivePixelSurface.pixelWidth - sX);
                if(sX>adaptivePixelSurface.pixelWidth)
                    aSX = -aSX;
            }
            if(adaptivePixelSurface.symmetryType == AdaptivePixelSurface.SymmetryType.VERTICAL) {
                aSY = Math.abs(adaptivePixelSurface.pixelHeight - sY);
                if(sY>adaptivePixelSurface.pixelHeight)
                    aSY = -aSY;
            }

            System.out.println("sX="+sX+", aSX="+aSX);
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            adaptivePixelSurface.canvasHistory.startHistoricalChange();

            path.reset();
            path.moveTo(sX, sY);
            nX = sX;
            nY = sY;

            if(adaptivePixelSurface.symmetry){
                mirroredPath.reset();
                mirroredPath.moveTo(aSX, aSY);
                aNX = aSX;
                aNY = aSY;
            }

            inUse = true;
            moves = 0;
            wasCanceled = false;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_MOVE && inUse){

            float a = sX+nX;
            float b = sY+nY;

            path.quadTo(nX, nY, a/2f, b/2f);
            nX = sX;
            nY = sY;

            if(adaptivePixelSurface.symmetry){
                a = aSX+aNX;
                b = aSY+aNY;

                mirroredPath.quadTo(aNX, aNY, a/2f, b/2f);
                aNX = aSX;
                aNY = aSY;

                adaptivePixelSurface.pixelCanvas.drawPath(mirroredPath, adaptivePixelSurface.paint);
            }

            adaptivePixelSurface.pixelCanvas.drawPath(path, adaptivePixelSurface.paint);

            moves++;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_UP && inUse){
            finishPath();
        }

        adaptivePixelSurface.pixelDrawThread.update();
    }

    private void finishPath(){
        inUse = false;

        //adaptivePixelSurface.pixelBitmap.setPixels(backupBitmap, 0, adaptivePixelSurface.pixelWidth, 0, 0, adaptivePixelSurface.pixelWidth, adaptivePixelSurface.pixelHeight);

        if(wasCanceled && moves < 10){
            adaptivePixelSurface.canvasHistory.cancelHistoricalChange(true);
            path.reset();
            if(adaptivePixelSurface.symmetry)
                mirroredPath.reset();
            return;
        }


        adaptivePixelSurface.canvasHistory.completeHistoricalChange();
        if(adaptivePixelSurface.symmetry){
            if(moves<10)
                adaptivePixelSurface.pixelCanvas.drawPoint(aSX, aSY, adaptivePixelSurface.paint);
            //mirroredPath.lineTo(aNX, aNY);
            //adaptivePixelSurface.pixelCanvas.drawPath(mirroredPath, adaptivePixelSurface.paint);
            mirroredPath.reset();
        }

        if(moves<10)
           adaptivePixelSurface.pixelCanvas.drawPoint(sX,sY, adaptivePixelSurface.paint);
        //path.lineTo(nX, nY);
        //adaptivePixelSurface.pixelCanvas.drawPath(path, adaptivePixelSurface.paint);
        path.reset();
    }

    @Override
    void cancel(MotionEvent motionEvent) {
        if(!inUse)
            return;

        if(motionEvent != null) {
            sX = (motionEvent.getX() - adaptivePixelSurface.matrixOffsetX) / adaptivePixelSurface.pixelScale + (1 - 1 / adaptivePixelSurface.pixelScale) * adaptivePixelSurface.scaleAnchorX;
            sY = (motionEvent.getY() - adaptivePixelSurface.matrixOffsetY) / adaptivePixelSurface.pixelScale + (1 - 1 / adaptivePixelSurface.pixelScale) * adaptivePixelSurface.scaleAnchorY;
        }

        if(adaptivePixelSurface.symmetry){
            aSX = sX;
            aSY = sY;
            if(adaptivePixelSurface.symmetryType == AdaptivePixelSurface.SymmetryType.HORIZONTAL)
                aSX = Math.abs(adaptivePixelSurface.pixelWidth-sX);
            if(adaptivePixelSurface.symmetryType == AdaptivePixelSurface.SymmetryType.VERTICAL)
                aSY = Math.abs(adaptivePixelSurface.pixelHeight-sY);
        }

        wasCanceled = true;
        finishPath();
        adaptivePixelSurface.pixelDrawThread.update();
    }
}
