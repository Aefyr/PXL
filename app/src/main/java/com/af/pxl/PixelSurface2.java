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
 * Created by Peter on 21.06.2017.
 */

public class PixelSurface2 extends SurfaceView implements SurfaceHolder.Callback {

    Canvas pixelCanvas;
    Bitmap pixelBitmap;
    Paint paint;

    final int Q = 128;

    float scaleX;
    float scaleY;

    float pixelSizeX;
    float pixelSizeY;

    Path path;
    Matrix scaleMatrix;

    float zoom = 1;
    int offsetX = 0;
    int offsetY = 0;

    boolean showGrid = true;

    Tool currentTool = Tool.PEN;

    DrawingThread2 drawingThread2;

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
        drawingThread2 = new DrawingThread2();
        drawingThread2.running = true;
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




    @Override
    public boolean onTouchEvent(MotionEvent event) {



        int scaledX = (int)(event.getX()/scaleX);
        int scaledY = (int)(event.getY()/scaleY);

        switch (currentTool){
            case PEN:
                Pen(event);
                break;
            case FILL:
                Fill(event);
                break;
            case COLORPICK:
                break;
        }

        return true;
    }

    float frac(float a){
        return (float) (a-Math.floor(a));
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
                    filler(fillStart, fillColors[0], fillColors[1]);
                    fillRequested = false;
                }

                if(gridUpdateNeeded) {
                    drawGrid();
                    gridUpdateNeeded = false;
                }
                tempC.drawBitmap(pixelBitmap, 0,0, paint);
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
        drawingThread2.update();
    }

    //Tools

    float nX;
    float nY;

    void Pen(MotionEvent event){
        float sX = (event.getX()/scaleX)/zoom;
        float sY = (event.getY()/scaleY)/zoom;

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            pixelCanvas.drawPoint(sX,sY, paint);
            path.reset();
            path.moveTo(sX, sY);
            nX = sX;
            nY = sY;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){

            float a = sX+nX;
            float b = sY+nY;

            path.quadTo(nX, nY, a/2f, b/2f);
            nX = sX;
            nY = sY;
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            path.lineTo(nX, nY);
            pixelCanvas.drawPath(path, paint);
            path = new Path();
        }


        if(!drawingThread2.paused) {
            drawingThread2.forcedUpdate = true;
            return;
        }

        drawingThread2.update();
    }

    boolean alreadyFilling = false;
    void Fill(MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_DOWN || alreadyFilling)
            return;
        int x = (int)((event.getX()/scaleX)/zoom);
        int y = (int)((event.getY()/scaleY)/zoom);

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
