package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 25.06.2017.
 */

class Pen extends Tool {

    Pen(PixelSurface2 pixelSurface2){
        this.pixelSurface2 = pixelSurface2;
        tempBitmap = Bitmap.createBitmap(pixelSurface2.Q, pixelSurface2.Q, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
    }
    private int moves = 0;

    private float sX, sY, nX, nY;

    private Canvas tempCanvas;
    private Bitmap tempBitmap;

    @Override
    void processMotionEvent(MotionEvent motionEvent) {
        sX = (motionEvent.getX()/pixelSurface2.scaleX)/ pixelSurface2.zoomScale;
        sY = (motionEvent.getY()/pixelSurface2.scaleY)/ pixelSurface2.zoomScale;

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            pixelSurface2.path.reset();
            pixelSurface2.path.moveTo(sX, sY);
            nX = sX;
            nY = sY;
            inUse = true;
            moves = 0;
            wasCanceled = false;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_MOVE && inUse){

            float a = sX+nX;
            float b = sY+nY;

            pixelSurface2.path.quadTo(nX, nY, a/2f, b/2f);
            nX = sX;
            nY = sY;

            moves++;
        }

        if(motionEvent.getAction() == MotionEvent.ACTION_UP && inUse){
            finishPath();
        }

        pixelSurface2.drawingThread2.update(true);
    }

    private void finishPath(){
        inUse = false;
        if(wasCanceled && moves < 10)
            return;

        tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        tempCanvas.drawPoint(sX,sY, pixelSurface2.paint);

        pixelSurface2.path.lineTo(nX, nY);
        tempCanvas.drawPath(pixelSurface2.path, pixelSurface2.paint);
        pixelSurface2.commitHistoryChange();
        pixelSurface2.pixelCanvas.drawBitmap(tempBitmap, -pixelSurface2.offsetX, -pixelSurface2.offsetY, pixelSurface2.paint);
    }

    @Override
    void cancel(MotionEvent motionEvent) {
        if(!inUse)
            return;
        sX = (motionEvent.getX()/pixelSurface2.scaleX)/ pixelSurface2.zoomScale;
        sY = (motionEvent.getY()/pixelSurface2.scaleY)/ pixelSurface2.zoomScale;
        wasCanceled = true;
        finishPath();
        pixelSurface2.drawingThread2.update(true);
    }
}
