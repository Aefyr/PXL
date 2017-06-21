package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Peter on 12.06.2017.
 */


public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {
    DrawThread drawThread;
    Paint pixelPaint;
    Paint gridPaint;
    GridSystem gridSystem;
    vPixel[] vPixels;
    boolean symmetry = false;
    boolean showGrid = false;

    int[] palette;

    public CanvasView(Context context) {
        super(context);
        initialize();

    }

    public CanvasView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    void initialize() {
        getHolder().addCallback(this);
        pixelPaint = new Paint();
        pixelPaint.setTextSize(100);
        pixelPaint.setTextAlign(Paint.Align.RIGHT);
        pixelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pixelPaint.setColor(Color.MAGENTA);

        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        palette = new int[16];
        palette[0] = Color.RED;
        palette[1] = Color.GREEN;
        palette[2] = Color.YELLOW;

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawThread = new DrawThread(surfaceHolder, 30);
        System.out.println("Surface created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        gridSystem = new GridSystem(this.getContext(), 33, 33, i1, i2);
        drawThread.setGridSystem(gridSystem);
        if (vPixels == null) {
            vPixels = gridSystem.generateVPixelsArray();

        }
        System.out.println("Surface changed");
        drawThread.vPixels = vPixels;
        drawThread.newPixelsThisFrame = new ArrayList<>(Arrays.asList(vPixels));
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        drawThread.requestUpdate();
        drawThread.setRunning(false);

        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    public void setGridEnabled(boolean enabled) {
        showGrid = enabled;
        drawThread.requestUpdate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eX = event.getX();
        float eY = event.getY();

        if (!showGrid) {

            vPixel p = gridSystem.vPixelFromTouchLocation(eX, eY, 1);
            drawThread.drawVPixel(p);
            if (symmetry) {
                if (eX >= gridSystem.width / 2) {
                    drawThread.drawVPixel(new vPixel(gridSystem.width - p.x - 1, p.y, 0));
                } else {
                    drawThread.drawVPixel(new vPixel(gridSystem.width / 2 + p.x - 1, p.y, 1));
                }
            }
            System.out.println("Event X: " + eX + "; Event Y: " + eY);

        }else {
            drawThread.fill(gridSystem.vPixelFromTouchLocation(eX, eY, 1), 1, 2);
        }

        return true;
    }

    class DrawThread extends Thread {

        SurfaceHolder surfaceHolder;
        boolean running = false;
        int framesPerSecond;
        int frameDelay;
        long nextFrameTime;
        float deltaTime;
        boolean updateRequested = true;

        Bitmap mBitmap;
        Canvas mCanvas;

        Bitmap gBitmap;
        Canvas gCanvas;

        vPixel[] vPixels;

        ArrayList<vPixel> newPixelsThisFrame;


        public GridSystem gridSystem;

        DrawThread(SurfaceHolder surfaceHolder, int fps) {
            this.surfaceHolder = surfaceHolder;

            framesPerSecond = fps;
            frameDelay = 1000 / fps;
            nextFrameTime = System.currentTimeMillis() + frameDelay;

            newPixelsThisFrame = new ArrayList<>();

        }

        public synchronized void requestUpdate() {
            System.out.println("Changes have been made to the picture, updating canvas...");
            notify();
        }

        public synchronized void fill(vPixel startingPixel, int oldColorFromPalette, int newColorFromPalette){
            startingPixel.colorFromPalette = newColorFromPalette;
            newPixelsThisFrame.add(startingPixel);

            int pixelsFound = 0;

            vPixel pixel1 = gridSystem.getVPixelAt(startingPixel.x -1, startingPixel.y);
            if(pixel1 != null && pixel1.colorFromPalette == oldColorFromPalette){
                fill(pixel1, oldColorFromPalette, newColorFromPalette);
                pixelsFound++;
            }

            vPixel pixel2 = gridSystem.getVPixelAt(startingPixel.x +1, startingPixel.y);
            if(pixel2 != null && pixel2.colorFromPalette == oldColorFromPalette){
                fill(pixel2, oldColorFromPalette, newColorFromPalette);
                pixelsFound++;
            }

            vPixel pixel3 = gridSystem.getVPixelAt(startingPixel.x, startingPixel.y -1);
            if(pixel3 != null && pixel3.colorFromPalette == oldColorFromPalette){
                fill(pixel3, oldColorFromPalette, newColorFromPalette);
                pixelsFound++;
            }

            vPixel pixel4 = gridSystem.getVPixelAt(startingPixel.x, startingPixel.y + 1);
            if(pixel4 != null && pixel4.colorFromPalette == oldColorFromPalette){
                fill(pixel4, oldColorFromPalette, newColorFromPalette);
                pixelsFound++;
            }

            if(pixelsFound==0){
                requestUpdate();
            }

        }


        public void setGridSystem(GridSystem gridSystem) {
            this.gridSystem = gridSystem;
            //vPixels = new vPixel[gridSystem.width*gridSystem.height];
        }

        public void drawVPixel(vPixel pixel) {
            if (pixel.x < gridSystem.width && pixel.y < gridSystem.height && pixel.x >= 0 && pixel.y >= 0) {
                vPixels[pixel.x + gridSystem.width * pixel.y] = pixel;
                newPixelsThisFrame.add(pixel);
            }
            requestUpdate();
        }

        void setRunning(boolean r) {
            running = r;
        }

        float test = 0;


        public synchronized void pause() {
            System.out.println("Attempting to pause drawing thread, if no fail message follows this one, pausing was successful.");
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("Failed to pause drawing thread, retrying...");
                pause();
            }

        }

        @Override
        public void run() {
            Canvas canvas;
            while (running) {
                //System.out.println("RUN");
                long frameStartTime = System.currentTimeMillis();
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas == null)
                        continue;



                    if(test < 1) {
                        mBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                        mCanvas = new Canvas();
                        mCanvas.setBitmap(mBitmap);
                        mCanvas.drawColor(Color.WHITE);

                        gBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                        gCanvas = new Canvas();
                        gCanvas.setBitmap(gBitmap);

                        float lineX = gridSystem.vPixelSizeW;
                        for (int x = 0; x < gridSystem.width - 1; x++) {
                            gCanvas.drawLine(lineX, 0, lineX, gridSystem.realHeight, gridPaint);
                            lineX += gridSystem.vPixelSizeW;
                        }
                        float lineY = gridSystem.vPixelSizeH;
                        for (int y = 0; y < gridSystem.height - 1; y++) {
                            gCanvas.drawLine(0, lineY, gridSystem.realWidth, lineY, gridPaint);
                            lineY += gridSystem.vPixelSizeH;
                        }

                        test++;
                    }


                        /*canvas.drawText("time: " + System.currentTimeMillis(), 1000, 100, textPaint);
                        canvas.drawText("delta time: " + deltaTime, 1000, 1000, textPaint);
                        canvas.drawCircle(test, 500, 200, textPaint);
                        test += 50*deltaTime;*/
                    //canvas.drawRect(gridSystem.getVPixelRect(0,5), textPaint);

                    //Old Drawing Method
                    for (vPixel vPixel : newPixelsThisFrame) {
                        if (vPixel != null) {

                            if(pixelPaint.getColor() != palette[vPixel.colorFromPalette])
                                pixelPaint.setColor(palette[vPixel.colorFromPalette]);

                            mCanvas.drawRect(gridSystem.getVPixelRect(vPixel), pixelPaint);
                        }
                    }


                    /*
                    for(RectF rectF: gridSystem.calculateCompleteRects2(vPixels)){
                        if(rectF!=null)
                            canvas.drawRect(rectF, pixelPaint);
                    }

                    for(vPixel vPixel: gridSystem.getRemainingPixels()){
                        if(vPixel!=null)
                            canvas.drawRect(gridSystem.getVPixelRect(vPixel), pixelPaint);
                    }*/

                    canvas.drawBitmap(mBitmap, 0, 0, pixelPaint);

                    if (showGrid) {
                        canvas.drawBitmap(gBitmap, 0, 0, pixelPaint);
                    }
                    updateRequested = false;

                    //canvas.drawLine(0,0, 48, 48, textPaint);
                } finally {

                    nextFrameTime = System.currentTimeMillis() + frameDelay;
                    deltaTime = (int) (System.currentTimeMillis() - frameStartTime);
                    if (deltaTime < frameDelay) {
                        deltaTime = frameDelay;
                    }
                    deltaTime *= 0.001;

                    if (canvas != null) {
                        newPixelsThisFrame.clear();
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        System.out.println("Canvas drawn in " + (System.currentTimeMillis() - frameStartTime) + "ms");
                        System.out.println("Pausing drawing thread, since all changes have been drawn");
                        pause();
                    }
                }
            }
        }
    }

    class GridSystem {
        int width;
        int height;
        float realWidth;
        float realHeight;
        float vPixelSizeH;
        float vPixelSizeW;


        Context c;

        public GridSystem(Context context, int height, int width, int realWidth, int realHeight) {
            c = context;
            this.width = width;
            this.height = height;
            this.realWidth = realWidth;
            this.realHeight = realHeight;
            vPixelSizeH = realHeight / (float) height;
            vPixelSizeW = realWidth / (float) width;
        }

        public vPixel getVPixelAt(int x, int y){
            int actualIndex = y*width + x;
            if(actualIndex < 0 || actualIndex > vPixels.length - 1)
                return null;

            return vPixels[actualIndex];
        }


        public vPixel[] generateVPixelsArray() {
            return new vPixel[height * width];
        }

        public RectF getVPixelRect(vPixel vPixel) {
            return new RectF((vPixel.x * vPixelSizeW), (vPixel.y * vPixelSizeH), ((vPixel.x + 1) * vPixelSizeW), ((vPixel.y + 1) * vPixelSizeH));
        }

        public vPixel vPixelFromTouchLocation(float touchX, float touchY, int color) {
            int vX = (int) (width * (touchX / realWidth));
            int vY = (int) (height * (touchY / realHeight));
            return new vPixel(vX, vY, color);
        }

        float pxToDp(int px) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, c.getResources().getDisplayMetrics());
        }

        float DpToPx(int dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, c.getResources().getDisplayMetrics());
        }
    }

    class vPixel {
        public int x;
        public int y;

        public int colorFromPalette;

        public vPixel(int x, int y, int colorFromPalette) {
            this.x = x;
            this.y = y;
            this.colorFromPalette = colorFromPalette;
        }
    }
}



