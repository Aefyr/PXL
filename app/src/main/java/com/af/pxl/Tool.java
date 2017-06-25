package com.af.pxl;

import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Created by Aefyr on 25.06.2017.
 */

abstract class Tool {

    PixelSurface2 pixelSurface2;
    boolean inUse;
    boolean wasCanceled;

    abstract void processMotionEvent(MotionEvent motionEvent);

    abstract void cancel(MotionEvent motionEvent);
}
