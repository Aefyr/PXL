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
import java.util.ArrayList;

/**
 * Created by Peter on 21.06.2017.
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

    ArrayDeque<Bitmap> history;

    float zoom = 1;
    float offsetX = 0;
    float offsetY = 0;

    boolean showGrid = true;

    Tool currentTool = Tool.PEN;

    DrawingThread2 drawingThread2;

    Pen pen;

    public enum Tool{
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

    void initialize(){
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
        pen = new Pen();
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
        drawingThread2.update();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        scaleX = (float) i1/pixelCanvas.getWidth();
        scaleY = (float) i2/pixelCanvas.getHeight();
        scaleMatrix.setScale(scaleX, scaleY);
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

            float halfX = (event.getX(0) + event.getX(1))/2f/zoom;
            float halfY = (event.getY(0) + event.getY(1))/2f/zoom;
            cancelTools(event);

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

            drawingThread2.update();

            prevX = halfX;
            prevY = halfY;
            previousTouchCount = event.getPointerCount();

            return true;
        }else {
            canceled = false;
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

        if(event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_UP)
            canceled =false;

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

        synchronized void update(){
            notify();
            paused = false;
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
                tempC.drawPath(path, paint);

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
                gridC.drawLine(x*pixelSizeX*zoom, 0, x*pixelSizeX*zoom, height, gridPaint);
            }
            for(int y = 0; y <= Q; y++){
                gridC.drawLine(0, pixelSizeY*y*zoom,width, pixelSizeY*y*zoom, gridPaint);
            }
        }
    }

    void test_zoom(float zoomScale){
        zoom = zoomScale;
        scaleMatrix.setScale(scaleX * zoomScale, scaleY * zoomScale);
        drawingThread2.gridUpdateNeeded = true;
        if(drawingThread2.paused)
            drawingThread2.update();
        else {
            drawingThread2.gridUpdateNeeded = true;
            drawingThread2.forcedUpdate = true;
        }
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

        if(drawingThread2.paused)
            drawingThread2.update();
        else
            drawingThread2.forcedUpdate = true;
    }

    //Tools
    boolean canceled = false;
    void cancelTools(MotionEvent event){
        canceled = true;
        if(pen.inUse)
            pen.finishPath(event);
    }

    float nX;
    float nY;

    //TODO Optimize this, cuz too many same calculations
    class Pen{
        float sX;
        float sY;

        int moves = 0;

        boolean inUse = false;

        void processMotionEvent(MotionEvent event){
            sX = (event.getX()/scaleX)/zoom;
            sY = (event.getY()/scaleY)/zoom;

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                path.reset();
                path.moveTo(sX, sY);
                nX = sX;
                nY = sY;
                inUse = true;
                moves = 0;
            }

            if(event.getAction() == MotionEvent.ACTION_MOVE && inUse){

                float a = sX+nX;
                float b = sY+nY;

                path.quadTo(nX, nY, a/2f, b/2f);
                nX = sX;
                nY = sY;

                moves++;
            }

            if(event.getAction() == MotionEvent.ACTION_UP && inUse){
                finishPath(event);
            }


            if(!drawingThread2.paused) {
                drawingThread2.forcedUpdate = true;
                return;
            }

            drawingThread2.update();
        }

        void finishPath(MotionEvent event){
            Path t = path;
            path = new Path();
            inUse = false;
            if(canceled && moves < 10)
                return;

            sX = (event.getX()/scaleX)/zoom;
            sY = (event.getY()/scaleY)/zoom;
            Bitmap b = Bitmap.createBitmap(Q,Q, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);

            if(!canceled)
             c.drawPoint(sX,sY, paint);

            t.lineTo(nX, nY);
            c.drawPath(t, paint);
            commitHistoryChange();
            pixelCanvas.drawBitmap(b, -offsetX, -offsetY, paint);
            //pixelCanvas.drawPath(path, paint);
        }
    }


    boolean alreadyFilling = false;
    void Fill(MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_DOWN || alreadyFilling)
            return;
        int x = (int)((event.getX()/scaleX)/zoom-offsetX);
        int y = (int)((event.getY()/scaleY)/zoom-offsetY);
        if(x<0||x>Q||y<0||y>Q)
            return;

        drawingThread2.requestFill(new Pixel(x,y), new int[]{pixelBitmap.getPixel(x,y), paint.getColor()});
        drawingThread2.update();
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
        public Pixel(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
