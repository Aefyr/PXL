package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayDeque;

/**
 * Created by Aefyr on 21.06.2017.
 */

public class PixelSurface2 extends SurfaceView implements SurfaceHolder.Callback {

    Canvas pixelCanvas;
    Bitmap pixelBitmap;
    Paint paint;

    final int Q = 128;
    final int historySize = 100;

    float scaleX;
    float scaleY;

    float pixelSizeX;
    float pixelSizeY;

    Path path;
    Matrix scaleMatrix;

    private ArrayDeque<Bitmap> history;

    float zoomScale = 1;
    float offsetX = 0;
    float offsetY = 0;

    boolean showGrid = true;

    Tool currentTool = Tool.PEN;

    DrawingThread2 drawingThread2;

    Pen pen;

    enum Tool{
        PEN, FILL, COLORPICK
    }

    public PixelSurface2(Context context) {
        super(context);
        initialize();
    }

    public PixelSurface2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize(){
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        path = new Path();
        scaleMatrix = new Matrix();
        pixelBitmap = Bitmap.createBitmap(Q,Q, Bitmap.Config.ARGB_8888);
        pixelCanvas = new Canvas(pixelBitmap);
        pixelCanvas.drawColor(Color.WHITE);
        pen = new Pen(this);
        drawingThread2 = new DrawingThread2();
        drawingThread2.running = true;

        history = new ArrayDeque<>();
        historyPixels = new int[Q*Q];
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        drawingThread2.surfaceHolder = surfaceHolder;
        if(drawingThread2.firstStart)
            drawingThread2.start();
        drawingThread2.update(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        scaleX = (float) i1/pixelCanvas.getWidth();
        scaleY = (float) i2/pixelCanvas.getHeight();
        scaleMatrix.setScale(scaleX* zoomScale, scaleY* zoomScale);
        pixelSizeX = (float) i1/(float)Q;
        pixelSizeY = (float) i2/(float)Q;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    int previousTouchCount;
    float prevX = 0;
    float prevY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        //TODO Remove on release
        int scaledX = (int)(event.getX()/scaleX);
        int scaledY = (int)(event.getY()/scaleY);

        //TODO Optimize this
        if(event.getPointerCount() > 1){

            float halfX = (event.getX(0) + event.getX(1))/2f/ zoomScale;
            float halfY = (event.getY(0) + event.getY(1))/2f/ zoomScale;
            pen.cancel(event);

            if(previousTouchCount == 1){
                prevX = halfX;
                prevY = halfY;
            }

            float deltaX = halfX - prevX;
            float deltaY = halfY - prevY;
            System.out.println("Delta X: "+deltaX+", DeltaY:"+deltaY);

            offsetX += deltaX/pixelSizeX;
            offsetY += deltaY/pixelSizeY;
            System.out.println("Offset = "+offsetX + ", "+offsetY);

            drawingThread2.update(false);

            prevX = halfX;
            prevY = halfY;
            previousTouchCount = event.getPointerCount();

            return true;
        }

        if(previousTouchCount == 2){
            previousTouchCount = event.getPointerCount();
            return true;
        }

        switch (currentTool){
            case PEN:
                pen.processMotionEvent(event);
                break;
            case FILL:
                Fill(event);
                break;
            case COLORPICK:
                break;
        }

        previousTouchCount = event.getPointerCount();
        return true;
    }

    class DrawingThread2 extends Thread{
        SurfaceHolder surfaceHolder;
        boolean running = false;

        boolean paused = false;

        boolean firstStart = true;

        boolean forcedUpdate = false;

        boolean fillRequested = false;

        boolean gridUpdateNeeded = true;

        private boolean pathUpdateNeeded = false;

        Pixel fillStart;
        int[] fillColors;

        Canvas tempC;
        Bitmap tempB;

        Bitmap gridB;
        Canvas gridC;

        synchronized void pause(){
            try {
                paused = true;
                wait();
            }catch (InterruptedException e){
                paused = false;
                pause();
            }
        }

        synchronized void update(boolean updatePath){
            pathUpdateNeeded = updatePath;
            if(paused) {
                notify();
                paused = false;
            }else {
                forcedUpdate = true;
            }
        }

        void requestFill(Pixel c, int[] fillColors){
            this.fillColors = fillColors;
            fillStart = c;
            fillRequested = true;
        }

        @Override
        public void run() {
            firstStart = false;
            tempB = Bitmap.createBitmap(Q,Q, Bitmap.Config.ARGB_8888);
            tempC = new Canvas(tempB);
            gridB = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            gridC = new Canvas(gridB);
            drawGrid();
            while (running){
                long startTime = System.currentTimeMillis();
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas==null)
                    continue;


                if(fillRequested) {
                    commitHistoryChange();
                    filler(fillStart, fillColors[0], fillColors[1]);
                    fillRequested = false;
                }

                if(gridUpdateNeeded) {
                    drawGrid();
                    gridUpdateNeeded = false;
                }

                tempC.drawColor(Color.GRAY);
                tempC.drawBitmap(pixelBitmap, offsetX,offsetY, paint);

                if(pathUpdateNeeded) {
                    tempC.drawPath(path, paint);
                    pathUpdateNeeded = false;
                }

                canvas.drawBitmap(tempB, scaleMatrix, paint);

                if(showGrid){
                    canvas.drawBitmap(gridB, 0, 0, paint);
                }

                surfaceHolder.unlockCanvasAndPost(canvas);

                System.out.println("Canvas drawn in "+(System.currentTimeMillis() - startTime)+" ms");

                if(forcedUpdate) {
                    forcedUpdate = false;
                    continue;
                }


                pause();
            }
        }

        void drawGrid(){
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.BLACK);
            gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            gridPaint.setStrokeWidth(2);
            gridC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int height = getHeight();
            int width = getWidth();
            for(int x = 0; x <= Q; x++){
                gridC.drawLine(x*pixelSizeX* zoomScale, 0, x*pixelSizeX* zoomScale, height, gridPaint);
            }
            for(int y = 0; y <= Q; y++){
                gridC.drawLine(0, pixelSizeY*y* zoomScale,width, pixelSizeY*y* zoomScale, gridPaint);
            }
        }
    }

    void setZoomScale(float zoomScale){
        this.zoomScale = zoomScale;
        scaleMatrix.setScale(scaleX * zoomScale, scaleY * zoomScale);
        drawingThread2.gridUpdateNeeded = true;
        drawingThread2.update(false);
    }

    void commitHistoryChange(){
        history.addFirst(pixelBitmap.copy(Bitmap.Config.ARGB_8888, false));
        if(history.size()>historySize)
            history.removeLast();
    }
    int[] historyPixels;
    void rewindHistory(){
        if(history.size()==0)
            return;

        history.removeFirst().getPixels(historyPixels,0,Q, 0, 0,Q,Q);
        pixelBitmap.setPixels(historyPixels,0,Q,0,0,Q,Q);

        drawingThread2.update(false);
    }

    //Tools
    boolean alreadyFilling = false;
    void Fill(MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_DOWN || alreadyFilling)
            return;
        int x = (int)((event.getX()/scaleX)/ zoomScale -offsetX);
        int y = (int)((event.getY()/scaleY)/ zoomScale -offsetY);
        if(x<0||x>Q||y<0||y>Q)
            return;

        drawingThread2.requestFill(new Pixel(x,y), new int[]{pixelBitmap.getPixel(x,y), paint.getColor()});
        drawingThread2.update(false);
    }

    void filler(Pixel c, int oldColor, int newColor){
        if(oldColor == newColor)
            return;

        alreadyFilling = true;
        ArrayDeque<Pixel> s = new ArrayDeque<>();
        s.add(c);

        while(!s.isEmpty()){
            Pixel p = s.removeFirst();
            if(pixelBitmap.getPixel(p.x, p.y) == oldColor){
                pixelBitmap.setPixel(p.x, p.y, newColor);
                if(p.x>0 && p.x<pixelBitmap.getWidth()-1) {
                    s.add(new Pixel(p.x + 1, p.y));
                    s.add(new Pixel(p.x - 1, p.y));
                }
                if(p.y > 0 && p.y < pixelBitmap.getWidth()-1){
                    s.add(new Pixel(p.x, p.y+1));
                    s.add(new Pixel(p.x, p.y-1));
                }
            }
        }
        alreadyFilling = false;
    }

    class Pixel {
        int x;
        int y;
        Pixel(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
