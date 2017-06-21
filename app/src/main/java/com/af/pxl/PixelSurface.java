package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by Peter on 20.06.2017.
 */

public class PixelSurface extends SurfaceView implements SurfaceHolder.Callback{

    PixelGridSystem pixelGridSystem;
    DrawingThread drawingThread;
    Pixel[] pixels;
    ArrayList<Pixel> pixelsToDrawThisFrame;

    Paint paint;
    Pixel lastPixel;

    boolean gridEnabled = true;

    public PixelSurface(Context context) {
        super(context);
        initialize(128, 128);
    }

    public PixelSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(128, 128);
    }

    private void initialize(int pixelWidth, int pixelHeight){
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        pixelsToDrawThisFrame = new ArrayList<>();
        pixels = new Pixel[pixelWidth*pixelHeight];
        pixelGridSystem = new PixelGridSystem(pixelWidth, pixelHeight);
        drawingThread = new DrawingThread();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread.surfaceHolder = surfaceHolder;
        drawingThread.running = true;
        if(drawingThread.needToInitializeCache) {
            drawingThread.start();
        }else {
            drawingThread.requestUpdate();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        pixelGridSystem.calculatePixelSizes((float) i1, (float) i2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!drawingThread.paused)
            return true;

        int[] gridLocation = pixelGridSystem.getGridLocationFromTouchLocation(event.getX(), event.getY());


        if(!pixelGridSystem.checkCoordinates(gridLocation[0], gridLocation[1]))
            return true;

        Pixel pixel = pixelGridSystem.getPixelAt(gridLocation[0], gridLocation[1]);
        try {
            if (pixel != null) {
                if (pixel.color != paint.getColor()) {
                    pixel.color = paint.getColor();
                    pixelsToDrawThisFrame.add(pixel);
                }
                return true;
            }

            pixel = new Pixel(gridLocation[0], gridLocation[1], Color.RED, true);
        } finally {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                lastPixel = pixel;

            if(lastPixel != null)
                pixelGridSystem.pixelLine(lastPixel, pixel);

            lastPixel = pixel;

            if(event.getAction() == MotionEvent.ACTION_UP)
                lastPixel = null;
        }
        return true;
    }

    class DrawingThread extends Thread{

        SurfaceHolder surfaceHolder;
        boolean running = false;

        Bitmap cachedPixelsB;
        Canvas cachedPixelsC;

        Bitmap cachedGridB;
        Canvas cachedGridC;
        boolean needToInitializeCache = true;

        boolean paused = false;

        synchronized void requestUpdate(){
            notify();
            paused = false;
        }

        synchronized void pause(){
            try {
                paused = true;
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                paused = false;
                pause();
            }
        }


        @Override
        public void run(){
            Canvas canvas;
            while(running){
                canvas = null;
                long frameStart = System.currentTimeMillis();
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if(canvas == null)
                        continue;

                    if(needToInitializeCache){
                        initializeCache();
                    }

                    try {

                        for (Pixel pixel : pixelsToDrawThisFrame) {
                            if (paint.getColor() != pixel.color)
                                paint.setColor(pixel.color);

                            cachedPixelsC.drawRect(pixelGridSystem.getPixelRect(pixel), paint);
                        }
                        pixelsToDrawThisFrame.clear();
                    }catch (ConcurrentModificationException e){
                        System.out.println("CME");
                        canvas = null;
                        continue;
                    }


                    canvas.drawBitmap(cachedPixelsB, 0, 0, paint);

                    if(gridEnabled)
                        canvas.drawBitmap(cachedGridB, 0, 0, paint);


                }finally {
                    if(canvas != null){
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        //System.out.println("Canvas drawn in "+(System.currentTimeMillis() - frameStart)+" ms");
                        pause();
                    }

                }
            }
        }

        void initializeCache(){
            cachedPixelsB = Bitmap.createBitmap((int) pixelGridSystem.realWidth, (int) pixelGridSystem.realHeight, Bitmap.Config.ARGB_8888);
            cachedPixelsC = new Canvas(cachedPixelsB);
            cachedPixelsC.drawColor(Color.WHITE);
            cachedGridB = Bitmap.createBitmap((int) pixelGridSystem.realWidth, (int) pixelGridSystem.realHeight, Bitmap.Config.ARGB_8888);
            cachedGridC = new Canvas(cachedGridB);
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.BLACK);
            gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            gridPaint.setStrokeWidth(2);
            for(int x = 0; x <= pixelGridSystem.pixelWidth; x++){
                cachedGridC.drawLine(x*pixelGridSystem.pixelSizeX, 0, x*pixelGridSystem.pixelSizeX, pixelGridSystem.realHeight, gridPaint);
            }
            for(int y = 0; y <= pixelGridSystem.pixelHeight; y++){
                cachedGridC.drawLine(0, pixelGridSystem.pixelSizeY*y, pixelGridSystem.realWidth, pixelGridSystem.pixelSizeY*y, gridPaint);
            }
            needToInitializeCache = false;
        }
    }

    class PixelGridSystem{
        int pixelWidth;
        int pixelHeight;

        float realWidth;
        float realHeight;

        float pixelSizeX;
        float pixelSizeY;

        RectF getPixelRect(Pixel pixel){
            return new RectF(pixel.x*pixelSizeX, pixel.y*pixelSizeY, (pixel.x+1)*pixelSizeX, (pixel.y+1)*pixelSizeY);
        }

        public PixelGridSystem(int pixelWidth, int pixelHeight){
            this.pixelWidth = pixelWidth;
            this.pixelHeight = pixelHeight;
        }

        void calculatePixelSizes(float realWidth, float realHeight){
            this.realWidth = realWidth;
            this.realHeight = realHeight;
            pixelSizeX = realWidth / (float) pixelWidth;
            pixelSizeY = realHeight / (float) pixelHeight;
        }

        int[] getGridLocationFromTouchLocation(float touchX, float touchY){
            return new int[]{(int) ((touchX/realWidth) * pixelWidth),(int) ((touchY/realHeight) * pixelHeight)};
        }

        Pixel getPixelAt(int x, int y){

            if(!checkCoordinates(x, y))
                return null;

            return pixels[x+y*pixelHeight];
        }

        boolean checkCoordinates(int x, int y){
            int realIndex = x+y*pixelHeight;
            return (realIndex > 0 && realIndex < pixels.length);
        }

        void pixelLine(Pixel start, Pixel end){
            int x = start.x;
            int y = start.y;
            int w = end.x - start.x;
            int h = end.y - start.y;
            int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
            if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1;
            if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1;
            if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1;
            int longest = Math.abs(w);
            int shortest = Math.abs(h);
            if (!(longest>shortest)) {
                longest = Math.abs(h);
                shortest = Math.abs(w);
                if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1;
                dx2 = 0 ;
            }
            int numerator = longest >> 1;
            for (int i=0;i<=longest;i++) {
                Pixel p = new Pixel(x, y, Color.BLUE, false);
                numerator += shortest ;
                if (!(numerator<longest)) {
                    numerator -= longest ;
                    x += dx1 ;
                    y += dy1 ;
                } else {
                    x += dx2 ;
                    y += dy2 ;
                }
            }

        }


    }

    class Pixel{
        int x;
        int y;
        int color;

        public Pixel(int x, int y, int color, boolean autoUpdate){
            this.x = x;
            this.y = y;
            this.color = color;
            pixels[x+y*pixelGridSystem.pixelHeight] = this;
            pixelsToDrawThisFrame.add(this);
            if(autoUpdate)
                drawingThread.requestUpdate();
        }
    }
}
