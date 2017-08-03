package com.af.pxl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import com.af.pxl.Fragments.PreferencesFragment;
import com.af.pxl.Palettes.Palette2;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.Projects.Project;

import java.util.ArrayDeque;

/**
 * Created by Aefyr on 27.06.2017.
 */

public class AdaptivePixelSurface extends SurfaceView implements SurfaceHolder.Callback, Palette2.OnPaletteChangeListener {

    Project project;

    //Canvas and drawing
    PixelDrawThread pixelDrawThread;
    Paint paint;
    int currentColor;

    int pixelWidth = 128;
    int pixelHeight = 128;

    Bitmap pixelBitmap;
    Canvas pixelCanvas;

    float pixelScale = 1f;
    Matrix pixelMatrix;

    int realWidth, realHeight;

    //Tools and utils
    enum Tool {
        PENCIL, FLOOD_FILL, COLOR_PICK, COLOR_SWAP, ERASER
    }
    Tool currentTool = Tool.PENCIL;


    CanvasHistory canvasHistory;

    Cursor cursor;
    boolean cursorMode = false;
    SuperPencil superPencil;

    //Symmetry
    boolean symmetry = false;
    SymmetryType symmetryType = SymmetryType.HORIZONTAL;

    enum SymmetryType{
        HORIZONTAL, VERTICAL
    }

    //Rest
    ColorCircle colorCircle;
    Palette2 palette;

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
        /*pixelBitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        pixelCanvas = new Canvas(pixelBitmap);*/

        pixelMatrix = new Matrix();
        pixelMatrix.setScale(pixelScale, pixelScale);

        //pixelCanvas.drawColor(Color.WHITE);

        //canvasHistory = new CanvasHistory(this, pixelBitmap, 100);

        initializePaints();

        cursor = new Cursor(this);
        superPencil = new SuperPencil(this);
    }

    void cleanupWeReLeaving(){
        pixelDrawThread.alive = false;
        pixelDrawThread.update();
        try{
            pixelDrawThread.join();
        }catch (InterruptedException e){
            cleanupWeReLeaving();
        }
    }

    //Setters
    void setTool(Tool tool){
        if(project.transparentBackground)
            paint.setXfermode(null);
        paint.setColor(currentColor);
        switch (tool){
            case PENCIL:
                currentTool = Tool.PENCIL;
                break;
            case FLOOD_FILL:
                currentTool = Tool.FLOOD_FILL;
                break;
            case COLOR_PICK:
                currentTool = Tool.COLOR_PICK;
                break;
            case COLOR_SWAP:
                currentTool = Tool.COLOR_SWAP;
                break;
            case ERASER:
                currentTool = Tool.ERASER;
                if(project.transparentBackground){
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }else
                    paint.setColor(Color.WHITE);
                break;
        }
    }

    void setColor(int color){
        currentColor = color;
        if(currentTool!=Tool.ERASER)
            paint.setColor(color);
    }

    void setColorCircle(ColorCircle colorCircle){
        this.colorCircle = colorCircle;
        updateColorCircle(paint.getColor());
    }

    void setPalette(Palette2 palette){
        if(!palette.getName().equals(project.palette))
            project.setPalette(palette);

        this.palette = palette;
        palette.addOnPaletteChangeListener(this);
        setColor(palette.getSelectedColor());
        updateColorCircle(palette.getSelectedColor());
    }

    @Override
    public void onColorSelection(int selectedColor) {
        setColor(selectedColor);
        updateColorCircle(selectedColor);
    }

    @Override
    public void onPaletteChanged() {

    }

    void setSymmetryEnabled(boolean enabled, SymmetryType type){
        symmetry = enabled;
        symmetryType = type;
        superPencil.symmetryUpdate();
    }

    void setProject(Project project){
        this.project = project;
        pixelBitmap = project.getBitmap(true);
        pixelCanvas = new Canvas(pixelBitmap);
        canvasHistory = new CanvasHistory(this, project, 100);
        this.pixelWidth = pixelBitmap.getWidth();
        this.pixelHeight = pixelBitmap.getHeight();
        setPalette(PaletteUtils.loadPalette(project.palette));
        System.out.println("Loaded bitmap config: "+pixelBitmap.getConfig().name());
    }

    //Utility methods
    Bitmap gridB;
    Canvas gridC;
    Paint gridP;
    float[] p = {0, 0};
    void drawGrid(){
        gridC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        gridP.setStrokeWidth((int)Utils.clamp(pixelScale/20, 1, 999));

        p[0] = 0;
        p[1] = 0;
        pixelMatrix.mapPoints(p);

        float gridCellSize = pixelScale * (int) Utils.clamp((8 * (1 - (pixelScale / (realWidth>realHeight?realHeight:realWidth / 64)))), 1, 8);

        float lOX = Utils.clamp(p[0]%gridCellSize, p[0], realWidth);
        float lOY = Utils.clamp(p[1]%gridCellSize, p[1], realHeight);


        p[0] = pixelWidth;
        p[1] = pixelHeight;
        pixelMatrix.mapPoints(p);

        float limitX = Utils.clamp(p[0], 0, realWidth);
        float limitY = Utils.clamp(p[1], 0, realHeight);

        float x = lOX;
        float y = lOY;





        while(x<limitX){
            gridC.drawLine(x, lOY - gridCellSize, x, limitY+gridCellSize, gridP);
            x+=gridCellSize;
        }

        while(y<limitY){
            gridC.drawLine(lOX-gridCellSize, y, limitX+gridCellSize, y, gridP);
            y+=gridCellSize;
        }


    }

    //Updaters

    private void updateColorCircle(int color){
        if(colorCircle!=null){
            colorCircle.setColor(color);
        }
    }

    //Initializers?
    //TODO Add ability to create custom CursorPencil pointers

    private boolean gridEnabled = false;
    boolean toggleGrid(){
        setGridEnabled(!gridEnabled);
        return gridEnabled;
    }
    void setGridEnabled(boolean enabled){

        if(enabled){
            if(gridB == null) {
                gridB = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                if(gridC==null)
                    gridC = new Canvas(gridB);
                else
                    gridC.setBitmap(gridB);
            }
            drawGrid();
        }else {
            gridC.setBitmap(null);
            gridB = null;
        }
        gridEnabled = enabled;
        pixelDrawThread.update();
    }

    Paint noAAPaint;
    private int backgroundColor = Color.GRAY;
    void initializePaints(){
        //Main paint
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setColor(Color.WHITE);
        updateColorCircle(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(24);
        noAAPaint = new Paint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        backgroundColor = preferences.getInt(PreferencesFragment.BACKGROUND_COLOR, Color.GRAY);
        //Grid paint
        gridP = new Paint();
        gridP.setColor(preferences.getInt(PreferencesFragment.GRID_COLOR, Color.BLACK));
        gridP.setStyle(Paint.Style.STROKE);
        gridP.setStrokeWidth(1f);
    }

    //SurfaceView.Callback methods
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(pixelDrawThread==null) {
            cursor.center(getWidth(), getHeight());
            centerCanvas();
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

    void centerCanvas(){
        pixelScale = getWidth()<getHeight()?getWidth()/pixelWidth:getHeight()/pixelHeight;
        matrixOffsetX = (int) (getWidth() / 2 - ((pixelWidth * pixelScale) / 2));
        matrixOffsetY = (int) (getHeight() / 2 - ((pixelHeight * pixelScale) / 2));
    }




    float prevCX, prevCY;
    int prevPointerCount = 0;
    int matrixOffsetX, matrixOffsetY;

    float prevDist = 0;
    float c = 0;
    int th = 32;
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
                pixelScale *= Math.pow(0.95f, Math.abs(c/th));
                c = 0;
            }else if(c<-th){
                pixelScale *= Math.pow(1.05f, Math.abs(c/th));
                c = 0;
            }

            pixelScale = Utils.clamp(pixelScale, 1f, realWidth/4);

            prevDist = dist;

            matrixOffsetX += midX-prevCX;
            matrixOffsetY += midY-prevCY;
            //matrixOffsetX = Utils.clamp(matrixOffsetX, (int)-pixelScale*pixelWidth, realWidth);
            //matrixOffsetY = Utils.clamp(matrixOffsetX, (int)-pixelScale*pixelHeight, realHeight);

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
                case ERASER:
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
                case COLOR_SWAP:
                    if(event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if(event.getAction() == MotionEvent.ACTION_UP) {
                        if (touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            int x = (int) ((event.getX() - p[0]) / pixelScale);
                            int y = (int) ((event.getY() - p[1]) / pixelScale);
                            if(x<pixelWidth&&x>=0&&y<pixelHeight&&y>=0)
                                onSpecialToolUseListener.onColorSwapToolUse(pixelBitmap.getPixel(x, y));
                        }
                    }
                    break;
            }
        }



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
        int pickedColor = pixelBitmap.getPixel(x, y);
        if(pickedColor==0)
            return;
        if(!palette.colorPickToolWasUsed(pickedColor)){
            //Set LiveColor to pickedColor
            /*setCurrentColor(pickedColor);
            updateColorCircle(pickedColor);
            palette.editColor(palette.getSelectedColorIndex(), pickedColor);*/
        }
    }

    void clearCanvas(){
        canvasHistory.startHistoricalChange();
        if(project.transparentBackground)
            pixelCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        else
            pixelCanvas.drawColor(Color.WHITE);
        canvasHistory.completeHistoricalChange();
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
                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setStrokeWidth(2);
                textPaint.setAntiAlias(false);
                textPaint.setFilterBitmap(false);
            }
            while (alive){
                long start = System.currentTimeMillis();
                Canvas canvas = surfaceHolder.lockCanvas();
                if(canvas == null)
                    continue;

                canvas.drawColor(backgroundColor);

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


                canvas.drawBitmap(pixelBitmap, pixelMatrix, noAAPaint);

                if(gridEnabled) {
                    canvas.drawBitmap(gridB, 0, 0, noAAPaint);
                }

                if(project.transparentBackground){
                    p[0] = p[1] = 0;
                    pixelMatrix.mapPoints(p);
                    float x1 = p[0];
                    float y1 = p[1];
                    p[0] = pixelWidth;
                    p[1] = pixelHeight;
                    pixelMatrix.mapPoints(p);
                    canvas.drawRect(x1, y1, p[0], p[1], gridP);
                }

                //Draw fps count
                //TODO Make this optional or remove in release version
                if(showFps){
                    String a = 1000/deltaTime+" fps.";
                    paint.getTextBounds(a, 0, a.length(), bounds);
                    canvas.drawText(a, getWidth()-bounds.width()*2, bounds.height()*2+256, textPaint);
                }

                //Highlight the pixel we'll draw on with cursorPencil
                //TODO Maybe make "less than 1 pixel" offsets global variables
                if(cursorMode) {
                    textPaint.setAlpha(cursor.opacity);
                    canvas.drawBitmap(cursor.cursorPointerImage, cursor.matrix,  textPaint);
                    textPaint.setAlpha(255);
                    //Stole this from drawGrid^^
                    p[0] = 0;
                    p[1] = 0;
                    pixelMatrix.mapPoints(p);

                    //Legacy method, doesn't work with the very first pixels visible
                    /*float lessThan1PixelOffsetX = p[0]%pixelScale>0?p[0]%pixelScale:pixelScale+p[0]%pixelScale;
                    float lessThan1PixelOffsetY = p[1]%pixelScale>0?p[1]%pixelScale:pixelScale+p[1]%pixelScale;

                    float x1 = (cursor.getX() -lessThan1PixelOffsetX - ((cursor.getX()-lessThan1PixelOffsetX)%pixelScale))+lessThan1PixelOffsetX;
                    float y1 = (cursor.getY()-lessThan1PixelOffsetY - ((cursor.getY()-lessThan1PixelOffsetY)%pixelScale))+lessThan1PixelOffsetY;

                    canvas.drawRect(x1, y1, x1+pixelScale, y1+pixelScale, textPaint);*/

                    float loX = p[0]%pixelScale;
                    int bX = (int) Math.floor((cursor.getX()-loX)/pixelScale);
                    float loY = p[1]%pixelScale;
                    int bY = (int) Math.floor((cursor.getY()-loY)/pixelScale);
                    canvas.drawRect(bX*pixelScale+loX, bY*pixelScale+loY, bX*pixelScale+loX+pixelScale, bY*pixelScale+loY+pixelScale, textPaint);

                }

                surfaceHolder.unlockCanvasAndPost(canvas);

                deltaTime = System.currentTimeMillis() - start;
                System.out.println("Canvas drawn in "+deltaTime+" ms");



                if(forcedUpdate) {
                    forcedUpdate = false;
                    continue;
                }
                if(alive)
                    pause();
            }
        }
    }

    OnSpecialToolUseListener onSpecialToolUseListener;
    void setOnSpecialToolUseListener(OnSpecialToolUseListener listener){
        onSpecialToolUseListener = listener;
    }
    interface OnSpecialToolUseListener{
        void onColorSwapToolUse(int color);
    }
}
