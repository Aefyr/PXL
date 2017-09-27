package com.aefyr.pxl;

/**
 * Created by Aefyr on 03.08.2017.
 */

abstract class ToolH {

    protected boolean drawing = false;
    protected float sX, sY;
    protected float startX, startY;
    protected int moves;

    protected boolean hitBounds = false;

    protected AdaptivePixelSurfaceH aps;

    abstract void startDrawing(float x, float y);


    abstract void move(float x, float y);


    abstract void stopDrawing(float x, float y);


    abstract void cancel(float x, float y);


    protected float[] p = {0, 0};

    protected void calculateCanvasXY(float x, float y) {
        p[0] = p[1] = 0;
        aps.pixelMatrix.mapPoints(p);
        sX = (x - p[0]) / aps.pixelScale;
        sY = (y - p[1]) / aps.pixelScale;

        if (!hitBounds && sX > 0 && sX < aps.pixelWidth && sY > 0 && sY < aps.pixelHeight)
            hitBounds = true;

    }
}
