package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

/**
 * Created by Aefyr on 27.06.2017.
 */

public class AdaptivePixelSurface extends SurfaceView implements SurfaceHolder.Callback {

    PixelDrawThread pixelDrawThread;
    Paint paint;

    int pixelWidth = 128;
    int pixelHeight = 128;

    Bitmap pixelBitmap;
    Canvas pixelCanvas;

    Bitmap phantomBitmap;
    Canvas phantomCanvas;

    float pixelScale = 8f;
    Matrix pixelMatrix;

    public AdaptivePixelSurface(Context context) {
        super(context);
        initialize();
    }

    public AdaptivePixelSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize(){
        getHolder().addCallback(this);
        pixelBitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        pixelCanvas = new Canvas(pixelBitmap);
        phantomBitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        phantomCanvas = new Canvas(phantomBitmap);
        pixelMatrix = new Matrix();
        pixelMatrix.setScale(pixelScale, pixelScale);
        paint = new Paint();
        paint.setColor(Color.RED);
        pixelCanvas.drawColor(Color.WHITE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(pixelDrawThread==null) {
            pixelDrawThread = new PixelDrawThread(surfaceHolder);
            pixelDrawThread.start();
        }else {
            pixelDrawThread.setSurfaceHolder(surfaceHolder);
            pixelDrawThread.update();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        pixelDrawThread.setSurfaceHolder(surfaceHolder);
        pixelDrawThread.update();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    float prevCX, prevCY;
    int prevPointerCount = 0;
    float matrixOffsetX = 0, matrixOffsetY = 0;

    float prevDist = 0;
    float c = 0;
    float th = 32;
    float midX, midY;

    float additionalOffsetX = 0;
    float additionalOffsetY = 0;

    float t1 = 128,t2 = 64;
    boolean t3 = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getPointerCount() > 1){
            midX = (event.getX(0)+event.getX(1))/2f;
            midY = (event.getY(0)+event.getY(1))/2f;

            float dist = vector2Distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
            float deltaDist = prevDist - dist;

            if(prevPointerCount <= 1){
                prevCX = midX;
                prevCY = midY;
                deltaDist = 0;
            }

            c+=deltaDist;
            //System.out.println("distance="+dist);

            if(c>th){
                pixelScale *= 0.95f;
                c = 0;
            }else if(c<-th){
                pixelScale  *= 1.05f;
                c = 0;
            }

            additionalOffsetX = 64;
            additionalOffsetY = 64;

            prevDist = dist;

            matrixOffsetX += midX-prevCX;
            matrixOffsetY += midY-prevCY;

            if(!t3) {
                t3 = true;
                float a = (midX - matrixOffsetX) / pixelScale + (pixelScale - 1f) * (t1 * (1f / pixelScale));
                float b = (midY - matrixOffsetY) / pixelScale + (pixelScale - 1f) * (t2 * (1f / pixelScale));
                float c = t1;
                float d = t2;
                t1 = a;
                t2 = b;
                matrixOffsetX += (a - c)*(pixelScale-1);
                matrixOffsetY += (b - d)*(pixelScale-1);
            }
            //t1 = 128;
            //t2 = 64;

            System.out.println("t1="+t1+", t2="+t2);

            //System.out.println("OffsetX="+matrixOffsetX +", OffsetY="+matrixOffsetY);
            System.out.println("Scale="+pixelScale);

            prevCX = midX;
            prevCY = midY;

            pixelDrawThread.translateChanged = true;
            pixelDrawThread.update();

            prevPointerCount = event.getPointerCount();
            return true;
        }

        float x = event.getX(0);
        float y = event.getY(0);
        t3 = false;

        if(true){
            pixelCanvas.drawPoint((x-matrixOffsetX)/pixelScale + (pixelScale-1f)*(t1*(1f/pixelScale)), (y-matrixOffsetY)/pixelScale+ (pixelScale-1f)*(t2*(1f/pixelScale)), paint);
            System.out.println("Boop: "+((x-matrixOffsetX)/pixelScale + (pixelScale-1f)*(t1*(1f/pixelScale)))+", "+((y-matrixOffsetY)/pixelScale+ (pixelScale-1f)*(t2*(1f/pixelScale))));
            pixelDrawThread.update();
        }

        prevPointerCount = event.getPointerCount();
        return true;
    }

    float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    class PixelDrawThread extends Thread{

        SurfaceHolder surfaceHolder;

        PixelDrawThread(SurfaceHolder surfaceHolder){
            this.surfaceHolder = surfaceHolder;
        }

        boolean paused = false;
        boolean forcedUpdate = false;
        boolean alive = true;
        boolean scaleChanged = false;
        boolean translateChanged = false;

        void setSurfaceHolder(SurfaceHolder surfaceHolder){
            this.surfaceHolder = surfaceHolder;
        }

        private synchronized void pause(){
            try {
                paused = true;
                wait();
            }catch (InterruptedException e){
                paused = false;
                pause();
            }
        }

        synchronized void update(){
            if(paused)
                notify();
            else
                forcedUpdate = true;
            paused = false;
        }

        @Override
        public void run() {
            while (alive){
                long start = System.currentTimeMillis();
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas == null)
                    continue;
                canvas.drawColor(Color.GRAY);

                if(scaleChanged||translateChanged) {
                    pixelMatrix.reset();
                    pixelMatrix.setScale(pixelScale, pixelScale, t1, t2);
                    pixelMatrix.postTranslate(matrixOffsetX, matrixOffsetY);
                    scaleChanged = false;
                    translateChanged = false;
                }


                canvas.drawBitmap(pixelBitmap, pixelMatrix, paint);
                //canvas.drawBitmap(phantomBitmap, pixelMatrix, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);

                //System.out.println("Canvas drawn in "+(System.currentTimeMillis() - start)+" ms");
                if(forcedUpdate) {
                    forcedUpdate = false;
                    continue;
                }
                pause();
            }
        }
    }
}
