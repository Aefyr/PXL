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

    //Canvas and drawing
    PixelDrawThread pixelDrawThread;
    Paint paint;

    int pixelWidth = 128;
    int pixelHeight = 128;

    Bitmap pixelBitmap;
    Canvas pixelCanvas;

    float pixelScale = 1f;
    Matrix pixelMatrix;

    int realWidth, realHeight;

    //Tools and utils
    enum Tool {
        PENCIL, FLOOD_FILL, COLOR_PICK
    }
    Tool currentTool = Tool.PENCIL;


    Pencil pencil;
    CursorPencil cursorPencil;
    CanvasHistory canvasHistory;

    //TEST
    Cursor cursor;
    boolean cursorMode = true;
    boolean fillMode = false;
    SuperPencil superPencil;

    //Symmetry
    boolean symmetry = false;
    SymmetryType symmetryType = SymmetryType.HORIZONTAL;

    enum SymmetryType{
        HORIZONTAL, VERTICAL
    }

    //Rest
    ColorCircle colorCircle;

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

        pixelCanvas.drawColor(Color.WHITE);

        canvasHistory = new CanvasHistory(this, pixelBitmap, 100);

        initializeTools();
        initializePaints();

        cursor = new Cursor(this);
        superPencil = new SuperPencil(this);
    }

    void setColorCircle(ColorCircle colorCircle){
        this.colorCircle = colorCircle;
    }

    private void updateColorCircle(){
        if(colorCircle!=null){
            colorCircle.setColor(paint.getColor());
            colorCircle.invalidate();
        }
    }

    //Utility methods
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


        float oX = Utils.clamp(p[0], lessThan1PixelOffsetX, getWidth());
        float oY = Utils.clamp(p[1], lessThan1PixelOffsetY , getHeight());

        p[0] = pixelWidth;
        p[1] = pixelHeight;
        pixelMatrix.mapPoints(p);

        float limitX = Utils.clamp(p[0], 0, getWidth());
        float limitY = Utils.clamp(p[1], 0, getHeight());

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

    //TODO Add ability to create custom CursorPencil pointers
    Bitmap cursorPencilPointerBitmap;
    Canvas cursorPencilPointerCanvas;
    void initializeCursorPencil(){
        cursorPencilPointerBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        cursorPencilPointerCanvas = new Canvas(cursorPencilPointerBitmap);
        Paint testCursorP = new Paint();
        testCursorP.setStrokeWidth(8);
        testCursorP.setColor(Color.MAGENTA);
        testCursorP.setStyle(Paint.Style.FILL_AND_STROKE);
        cursorPencilPointerCanvas.drawLine(0, 0, 0, 100, testCursorP);
        cursorPencilPointerCanvas.drawLine(0, 100, 100, 100, testCursorP);
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

    void initializeTools(){
        pencil = new Pencil(this);
        cursorPencil = new CursorPencil(this);
        initializeCursorPencil();
    }

    void initializePaints(){
        //Main paint
        paint = new Paint();
        paint.setColor(Color.RED);
        updateColorCircle();
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(24);

        //Grid paint
        gridP = new Paint();
        gridP.setColor(Color.BLUE);
        gridP.setStyle(Paint.Style.FILL_AND_STROKE);
        gridP.setStrokeWidth(1f);
    }

    //SurfaceView.Callback methods
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(pixelDrawThread==null) {
            pixelScale = getWidth()<getHeight()?getWidth()/pixelWidth:getHeight()/pixelHeight;
            matrixOffsetX = getWidth() / 2 - ((pixelWidth * pixelScale) / 2);
            matrixOffsetY = getHeight() / 2 - ((pixelHeight * pixelScale) / 2);
            pixelDrawThread = new PixelDrawThread(surfaceHolder);
            pixelDrawThread.start();
        }else {
            pixelDrawThread.setSurfaceHolder(surfaceHolder);
            pixelDrawThread.update();
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        realWidth = i1;
        realHeight = i2;
        cursor.setLimits(realWidth, realHeight);
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

    float lastX, lastY;
    boolean touchToolWillBeUsedOnUpEvent = false;
    //TODO Make method processEvent(float x, float y, int pointerId) in Tool class, and use a ArrayList of Paths or whatever that tool uses, so symmetry can be implemented on this level, just by sending mirrored coords with toolButton.pointers+1 as pointerId
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Believe me, we really don't wanna interfere the flood fill algorithm
        if(fillInProgress)
            return true;

        if(event.getPointerCount() > 1){
            if(cursorMode){
                superPencil.cancel(cursor.getX(), cursor.getY());
            }else {
                superPencil.cancel(event.getX(), event.getY());
            }
            touchToolWillBeUsedOnUpEvent = false;


            midX = (event.getX(0)+event.getX(1))/2f;
            midY = (event.getY(0)+event.getY(1))/2f;

            float dist = Utils.vector2Distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
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
        anchorSet = false;

        if(cursorMode) {
            cursor.processMotionEvent(event);
        }else {
            switch (currentTool) {
                case PENCIL:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        superPencil.startDrawing(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        superPencil.move(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        superPencil.stopDrawing(event.getX(), event.getY());
                    }
                    break;
                case COLOR_PICK:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        if(touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            colorPick((int) ((event.getX() - p[0]) / pixelScale), (int) ((event.getY() - p[1]) / pixelScale));
                        }
                    }
                    break;
                case FLOOD_FILL:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        if(touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            floodFill((int) ((event.getX() - p[0]) / pixelScale), (int) ((event.getY() - p[1]) / pixelScale));
                        }
                    }
                    break;
            }
        }
        //TEST



        lastX = event.getX();
        lastY = event.getY();
        prevPointerCount = event.getPointerCount();
        return true;
    }

    void setCursorModeEnabled(boolean enabled){
        if(enabled == cursorMode)
            return;

        if(cursorMode){
            superPencil.cancel(cursor.getX(), cursor.getY());
        }else {
            superPencil.cancel(lastX, lastY);
        }
        cursorMode = enabled;
        pixelDrawThread.update();
    }

    boolean fillInProgress = false;
    void floodFill(int x, int y){
        int newC = paint.getColor();
        if(x<0||x>=pixelWidth||y<0||y>=pixelHeight||pixelBitmap.getPixel(x,y)==newC)
            return;

        canvasHistory.startHistoricalChange();
        fillInProgress = true;

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
        fillInProgress = false;
        System.out.println("Filled in "+(System.currentTimeMillis()-s)+" ms");
        pixelDrawThread.update();
    }

    void colorPick(int x, int y){
        if(x<0||x>=pixelWidth||y<0||y>=pixelHeight)
            return;
        paint.setColor(pixelBitmap.getPixel(x, y));
        updateColorCircle();
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
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setStrokeWidth(2);
            }
            while (alive){
                long start = System.currentTimeMillis();
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas == null)
                    continue;

                canvas.drawColor(Color.GRAY);

                //Recalculate matrix and grid if zoom or/and move were used
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

                //Draw fps count
                //TODO Make this optional or remove in release version
                if(showFps){
                    String a = 1000/deltaTime+" fps.";
                    paint.getTextBounds(a, 0, a.length(), bounds);
                    canvas.drawText(a, getWidth()-bounds.width()*2, bounds.height()*2, textPaint);
                }

                //Highlight the pixel we'll draw on with cursorPencil
                //TODO Maybe make "less than 1 pixel" offsets global variables
                if(cursorMode) {
                    canvas.drawBitmap(cursor.cursorPointerImage, cursor.getX(), cursor.getY()-100, null);

                    //Stole this from drawGrid^^
                    p[0] = 0;
                    p[1] = 0;
                    pixelMatrix.mapPoints(p);

                    float lessThan1PixelOffsetX = p[0]%pixelScale>0?p[0]%pixelScale:pixelScale+p[0]%pixelScale;
                    float lessThan1PixelOffsetY = p[1]%pixelScale>0?p[1]%pixelScale:pixelScale+p[1]%pixelScale;

                    float x1 = (cursor.getX() -lessThan1PixelOffsetX - ((cursor.getX()-lessThan1PixelOffsetX)%pixelScale))+lessThan1PixelOffsetX;
                    float y1 = (cursor.getY()-lessThan1PixelOffsetY - ((cursor.getY()-lessThan1PixelOffsetY)%pixelScale))+lessThan1PixelOffsetY;

                    canvas.drawRect(x1, y1, x1+pixelScale, y1+pixelScale, textPaint);

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
