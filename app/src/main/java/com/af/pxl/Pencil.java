package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 28.06.2017.
 */

public class Pencil extends Tool{

    private AdaptivePixelSurface adaptivePixelSurface;
    private int moves;

    private float sX, sY, nX, nY;

    private Path path;

    private int[] backupBitmap;

    Pencil(AdaptivePixelSurface adaptivePixelSurface){
        this.adaptivePixelSurface = adaptivePixelSurface;
        path = new Path();
        backupBitmap = new int[adaptivePixelSurface.pixelWidth*adaptivePixelSurface.pixelHeight];
    }

    @Override
    void processMotionEvent(MotionEvent motionEvent) {

        sX = (motionEvent.getX()-adaptivePixelSurface.matrixOffsetX)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorX;
        sY = (motionEvent.getY()-adaptivePixelSurface.matrixOffsetY)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorY;

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            path.reset();
            path.moveTo(sX, sY);
            nX = sX;
            nY = sY;
            inUse = true;
            moves = 0;
            wasCanceled = false;
            adaptivePixelSurface.pixelBitmap.getPixels(backupBitmap, 0, adaptivePixelSurface.pixelWidth, 0, 0, adaptivePixelSurface.pixelWidth, adaptivePixelSurface.pixelHeight);
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_MOVE && inUse){

            float a = sX+nX;
            float b = sY+nY;

            path.quadTo(nX, nY, a/2f, b/2f);
            nX = sX;
            nY = sY;

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

        adaptivePixelSurface.pixelBitmap.setPixels(backupBitmap, 0, adaptivePixelSurface.pixelWidth, 0, 0, adaptivePixelSurface.pixelWidth, adaptivePixelSurface.pixelHeight);

        if(wasCanceled && moves < 10){
            path.reset();
            return;
        }

        adaptivePixelSurface.canvasHistory.commitHistoricalChange();

        adaptivePixelSurface.pixelCanvas.drawPoint(sX,sY, adaptivePixelSurface.paint);
        path.lineTo(nX, nY);
        adaptivePixelSurface.pixelCanvas.drawPath(path, adaptivePixelSurface.paint);
        path.reset();
    }

    @Override
    void cancel(MotionEvent motionEvent) {
        if(!inUse)
            return;
        sX = (motionEvent.getX()-adaptivePixelSurface.matrixOffsetX)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorX;
        sY = (motionEvent.getY()-adaptivePixelSurface.matrixOffsetY)/adaptivePixelSurface.pixelScale + (1-1/adaptivePixelSurface.pixelScale)* adaptivePixelSurface.scaleAnchorY;
        wasCanceled = true;
        finishPath();
        adaptivePixelSurface.pixelDrawThread.update();
    }
}
