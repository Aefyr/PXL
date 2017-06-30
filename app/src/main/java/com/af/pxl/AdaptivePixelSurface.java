package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayDeque;

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


    float pixelScale = 8f;
    Matrix pixelMatrix;

    Pencil pencil;
    CanvasHistory canvasHistory;

    boolean symmetry = false;
    SymmetryType symmetryType = SymmetryType.HORIZONTAL;

    enum SymmetryType{
        HORIZONTAL, VERTICAL
    }

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

        pixelMatrix = new Matrix();
        pixelMatrix.setScale(pixelScale, pixelScale);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(24);
        pixelCanvas.drawColor(Color.WHITE);
        pencil = new Pencil(this);
        canvasHistory = new CanvasHistory(this, pixelBitmap, 100);

        System.out.println(getWidth());
        System.out.println("Canvas height="+pixelHeight*pixelScale+", Canvas width="+pixelWidth*pixelScale);
        System.out.println("X="+matrixOffsetX+", Y="+matrixOffsetY);

        gridP = new Paint();
        gridP.setColor(Color.BLUE);
        gridP.setStyle(Paint.Style.FILL_AND_STROKE);
        gridP.setStrokeWidth(1f);



    }

    Bitmap gridB;
    Canvas gridC;
    Paint gridP;
    float[] p = {0, 0};
    void drawGrid(){
        gridC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        gridP.setStrokeWidth((int)(pixelScale/10));

        p[0] = 0;
        p[1] = 0;
        pixelMatrix.mapPoints(p);

        float lessThan1PixelOffsetX = p[0]%pixelScale>0?p[0]%pixelScale:pixelScale+p[0]%pixelScale;
        float lessThan1PixelOffsetY = p[1]%pixelScale>0?p[1]%pixelScale:pixelScale+p[1]%pixelScale;


        float oX = clamp(p[0], lessThan1PixelOffsetX, getWidth());
        float oY = clamp(p[1], lessThan1PixelOffsetY , getHeight());

        p[0] = pixelWidth;
        p[1] = pixelHeight;
        pixelMatrix.mapPoints(p);

        float limitX = clamp(p[0], 0, getWidth());
        float limitY = clamp(p[1], 0, getHeight());

        float x = oX;
        float y = oY;

        while(x<limitX){
            gridC.drawLine(x, oY - pixelScale, x, limitY+pixelScale, gridP);
            x+=pixelScale;
        }

        while(y<limitY){
            gridC.drawLine(oX-pixelScale, y, limitX+pixelScale, y, gridP);
            y+=pixelScale;
        }

    }

    float clamp(float x, float min, float max){
        if(x<min)return min;
        if(x>max) return max;
        return x;
    }

    private boolean gridEnabled = false;
    void setGridEnabled(boolean enabled){
        gridEnabled = enabled;

        if(gridEnabled){
            gridB = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            gridC = new Canvas(gridB);
            drawGrid();
        }else {
            if(gridB!=null){
                gridB.recycle();
                gridB = null;
                gridC = null;
            }
        }

        pixelDrawThread.update();
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

    boolean centered = false;
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(!centered) {
            matrixOffsetX = i1 / 2 - ((pixelWidth * pixelScale) / 2);
            matrixOffsetY = i2 / 2 - ((pixelHeight * pixelScale) / 2);
            centered = true;
        }
        pixelDrawThread.setSurfaceHolder(surfaceHolder);
        pixelDrawThread.update();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    float prevCX, prevCY;
    int prevPointerCount = 0;
    float matrixOffsetX, matrixOffsetY;

    float prevDist = 0;
    float c = 0;
    float th = 32;
    float midX, midY;

    float scaleAnchorX = 0, scaleAnchorY = 0;
    boolean anchorSet = false;

    //TODO Make method processEvent(float x, float y, int pointerId) in Tool class, and use a ArrayList of Paths or whatever that tool uses, so symmetry can be implemented on this level, just by sending mirrored coords with currentTool.pointers+1 as pointerId
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getPointerCount() > 1){
            pencil.cancel(event);

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

            prevDist = dist;

            matrixOffsetX += midX-prevCX;
            matrixOffsetY += midY-prevCY;

            if(!anchorSet) {
                anchorSet = true;
                float a = (midX - matrixOffsetX) / pixelScale + (1-1/pixelScale)* scaleAnchorX;
                float b = (midY - matrixOffsetY) / pixelScale + (1-1/pixelScale)* scaleAnchorY;
                float c = scaleAnchorX;
                float d = scaleAnchorY;
                scaleAnchorX = a;
                scaleAnchorY = b;
                matrixOffsetX += (a - c)*(pixelScale-1);
                matrixOffsetY += (b - d)*(pixelScale-1);
            }
            //scaleAnchorX = 128;
            //scaleAnchorY = 64;

            //System.out.println("scaleAnchorX="+ scaleAnchorX +", scaleAnchorY="+ scaleAnchorY);

            //System.out.println("PixelSize="+pixelScale+", OffsetX="+matrixOffsetX +", OffsetY="+matrixOffsetY);

            //System.out.println("Scale="+pixelScale);

            prevCX = midX;
            prevCY = midY;

            pixelDrawThread.translateChanged = true;
            pixelDrawThread.update();

            prevPointerCount = event.getPointerCount();
            return true;
        }

        if(fillMode){
            float[] p = {0, 0};
            pixelMatrix.mapPoints(p);

            floodFill((int)((event.getX()-p[0])/pixelScale), (int)((event.getY()-p[1])/pixelScale), paint.getColor());
            return true;
        }

        float x = event.getX(0);
        float y = event.getY(0);
        anchorSet = false;

        //legacy (pixelScale-1f)*(scaleAnchorY *(1f/pixelScale))

        pencil.processMotionEvent(event);

        prevPointerCount = event.getPointerCount();
        return true;
    }

    float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    boolean fillMode = false;
    void floodFill(int x, int y, int newC){
        if(x<0||x>=pixelWidth||y<0||y>=pixelHeight||pixelBitmap.getPixel(x,y)==newC)
            return;

        canvasHistory.startHistoricalChange();

        int oldC = pixelBitmap.getPixel(x,y);

        long s = System.currentTimeMillis();
        ArrayDeque<Pixel> p= new ArrayDeque<>();

        p.add(new Pixel(x,y));

        Pixel p1;
        int x1, x2;
        while(!p.isEmpty()){
            p1 = p.remove();
            if(p1.x <0||p1.x>=pixelWidth||p1.y<0||p1.y>=pixelHeight||pixelBitmap.getPixel(p1.x, p1.y)!=oldC)
                continue;
            x1 = p1.x;
            x2 = p1.x;

            while(x1<pixelWidth&&pixelBitmap.getPixel(x1, p1.y)==oldC)
                x1++;

            while(x2>=0&&pixelBitmap.getPixel(x2, p1.y)==oldC)
                x2--;

            x2++;


            for(int i2 = x2; i2<x1; i2++){
                pixelBitmap.setPixel(i2, p1.y, newC);
            }

            if(p1.y>0&&p1.y<pixelHeight) {
                for (int i = x2; i < x1; i++) {
                    if(pixelBitmap.getPixel(i, p1.y-1)==oldC)
                        p.add(new Pixel(i, p1.y-1));
                }
            }
            if(p1.y>=0&&p1.y<pixelHeight-1) {
                for (int i = x2; i < x1; i++) {
                    if(pixelBitmap.getPixel(i, p1.y+1)==oldC)
                        p.add(new Pixel(i, p1.y+1));
                }
            }

        }

        canvasHistory.completeHistoricalChange();
        System.out.println("Filled in "+(System.currentTimeMillis()-s)+" ms");
        pixelDrawThread.update();
    }

    class Pixel {
        int x, y;

        Pixel(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    boolean showFps = true;
    Rect bounds;
    Paint textPaint;

    class PixelDrawThread extends Thread{

        SurfaceHolder surfaceHolder;

        PixelDrawThread(SurfaceHolder surfaceHolder){
            this.surfaceHolder = surfaceHolder;
        }

        boolean paused = false;
        boolean forcedUpdate = false;
        boolean alive = true;
        boolean scaleChanged = false;
        boolean translateChanged = true;

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

        long deltaTime = 1;

        @Override
        public void run() {
            if(showFps) {
                bounds = new Rect();
                textPaint = new Paint();
                textPaint.setColor(Color.GREEN);
                textPaint.setTextAlign(Paint.Align.LEFT);
                textPaint.setTextSize(50);
            }
            while (alive){
                long start = System.currentTimeMillis();
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas == null)
                    continue;
                canvas.drawColor(Color.GRAY);

                if(scaleChanged||translateChanged) {
                    pixelMatrix.reset();
                    pixelMatrix.setScale(pixelScale, pixelScale, scaleAnchorX, scaleAnchorY);
                    pixelMatrix.postTranslate(matrixOffsetX, matrixOffsetY);
                    scaleChanged = false;
                    translateChanged = false;

                    if(gridEnabled)
                        drawGrid();
                }


                canvas.drawBitmap(pixelBitmap, pixelMatrix, paint);

                if(gridEnabled) {
                    canvas.drawBitmap(gridB, 0, 0, null);
                }

                if(showFps){
                    String a = 1000/deltaTime+" fps.";
                    paint.getTextBounds(a, 0, a.length(), bounds);
                    canvas.drawText(a, getWidth()-bounds.width()*2, bounds.height()*2, textPaint);
                }

                surfaceHolder.unlockCanvasAndPost(canvas);

                deltaTime = System.currentTimeMillis() - start;
                System.out.println("Canvas drawn in "+deltaTime+" ms");



                if(forcedUpdate) {
                    forcedUpdate = false;
                    continue;
                }
                pause();
            }
        }
    }
}
