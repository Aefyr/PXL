package com.af.pxl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.af.pxl.Fragments.PreferencesFragment;
import com.af.pxl.Palettes.Palette2;
import com.af.pxl.Palettes.PaletteUtils;
import com.af.pxl.Projects.Project;

import java.util.ArrayDeque;

/**
 * Created by Aefyr on 27.06.2017.
 */

public class AdaptivePixelSurfaceH extends View implements Palette2.OnPaletteChangeListener {

    Project project;
    boolean projectReady = false;
    boolean surfaceReady = false;

    //Canvas and drawing
    Paint paint;
    private Paint cursorPaint;
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


    CanvasHistoryH canvasHistory;

    CursorH cursor;
    boolean cursorMode = false;
    SuperPencilH superPencil;

    //Symmetry
    boolean symmetry = false;
    SymmetryType symmetryType = SymmetryType.HORIZONTAL;

    enum SymmetryType{
        HORIZONTAL, VERTICAL
    }

    //Rest
    ColorCircle colorCircle;
    Palette2 palette;

    public AdaptivePixelSurfaceH(Context context) {
        super(context);
        initialize();
    }

    public AdaptivePixelSurfaceH(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize(){
        /*pixelBitmap = Bitmap.createBitmap(pixelWidth, pixelHeight, Bitmap.Config.ARGB_8888);
        pixelCanvas = new Canvas(pixelBitmap);*/

        pixelMatrix = new Matrix();
        pixelMatrix.setScale(pixelScale, pixelScale);

        //pixelCanvas.drawColor(Color.WHITE);

        //canvasHistory = new CanvasHistory(this, pixelBitmap, 100);

        cursor = new CursorH(this);
        superPencil = new SuperPencilH(this);
        initializePaints();
        ter();
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

    public void setColor(int color){
        currentColor = color;
        if(currentTool!= Tool.ERASER)
            paint.setColor(color);
    }

    public void setColorCircle(ColorCircle colorCircle){
        this.colorCircle = colorCircle;
        updateColorCircle(paint.getColor());
    }

    public void setPalette(Palette2 palette){
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

    Paint trans;
    public void setProject(Project project){
        this.project = project;
        pixelBitmap = project.getBitmap(true);
        this.pixelWidth = pixelBitmap.getWidth();
        this.pixelHeight = pixelBitmap.getHeight();
        pixelCanvas = new Canvas(pixelBitmap);
        canvasHistory = new CanvasHistoryH(this, project, CanvasHistoryH.ADAPTIVE_SIZE);
        setPalette(PaletteUtils.loadPalette(project.palette));
        if(project.transparentBackground){
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
            int bg = p.getInt(PreferencesFragment.TRANSPARENT_CANVAS_BACKGROUND_COLOR, 111);
            trans = new Paint();
            if(bg<0){
                trans.setColor(bg);
                trans.setStyle(Paint.Style.FILL);
            }else if(bg == 111) {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inScaled = false;
                op.inMutable = false;
                trans.setShader(new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.tilebglidl), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                trans.setFilterBitmap(false);
                trans.setAntiAlias(false);
                trans.setStyle(Paint.Style.FILL);
            }else if(bg == 322){
                trans.setColor(gridP.getColor());
                trans.setStyle(Paint.Style.STROKE);
                trans.setStrokeWidth((int)Utils.dpToPx(1, getResources()));
            }

        }
        projectReady = true;
    }

    //Utility methods
    Paint gridP;
    float[] p = {0, 0};
    void drawGrid(Canvas c){
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
            c.drawLine(x, lOY - gridCellSize, x, limitY+gridCellSize, gridP);
            x+=gridCellSize;
        }

        while(y<limitY){
            c.drawLine(lOX-gridCellSize, y, limitX+gridCellSize, y, gridP);
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
        gridEnabled = enabled;
        invalidate();
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

        cursorPaint = new Paint();
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setAntiAlias(false);
        cursorPaint.setAlpha(cursor.opacity);
        cursorPaint.setStrokeWidth(Utils.dpToPx(1, getResources()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        realWidth = w;
        realHeight = h;
        cursor.setLimits(realWidth, realHeight);
        if(!surfaceReady){
            th = (int) Utils.dpToPx(1, getResources());
            centerCanvas();
            cursor.center(realWidth, realHeight);
            surfaceReady = true;
        }
        invalidate();
    }



    void centerCanvas(){
        pixelScale = realWidth<realHeight?realWidth/pixelWidth:realHeight/pixelHeight;
        matrixOffsetX = (int) (realWidth / 2 - ((pixelWidth * pixelScale) / 2));
        matrixOffsetY = (int) (realHeight / 2 - ((pixelHeight * pixelScale) / 2));
    }




    float prevCX, prevCY;
    int prevPointerCount = 0;
    int matrixOffsetX, matrixOffsetY;

    float prevDist = 0;
    float c = 0;
    int th = 1;
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

        if(event.getPointerCount() != 1){
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

            if(prevPointerCount !=event.getPointerCount()){
                prevCX = midX;
                prevCY = midY;
                deltaDist = 0;
            }

            c+=deltaDist;
            //System.out.println("distance="+dist);

            if(c>th){
                pixelScale *= Math.pow(0.995f, Math.abs(c/th));
                System.out.println("RESETED C WITH VALUE OF "+c);
                c = 0;
            }else if(c<-th){
                pixelScale *= Math.pow(1.005f, Math.abs(c/th));
                System.out.println("RESETED C WITH VALUE OF "+c);
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
            translateChanged = true;
            invalidate();

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
        invalidate();
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
        invalidate();
    }

    void colorPick(int x, int y){
        if(x<0||x>=pixelWidth||y<0||y>=pixelHeight)
            return;
        int pickedColor = pixelBitmap.getPixel(x, y);
        if(pickedColor==0)
            return;
        if(!palette.colorPickToolWasUsed(pickedColor)){
            //Set LiveColor to pickedColor
            /*setColor(pickedColor);
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
        invalidate();
    }

    class Pixel {
        int x, y;

        Pixel(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    boolean scaleChanged = false;
    boolean translateChanged = true;


    boolean showFps = true;
    Paint textPaint;
    Rect bounds;
    int screenRefreshRate=60;
    long deltaTime = 1;

    void ter(){
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenRefreshRate = (int) display.getRefreshRate();
        System.out.println("refreshRate="+screenRefreshRate);
        bounds = new Rect();
        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(50);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setStrokeWidth(2);
        textPaint.setAntiAlias(false);
        textPaint.setFilterBitmap(false);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        long start = System.currentTimeMillis();
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);


        if(!projectReady)
            return;

        //Recalculate matrix and grid if zoom or/and move were used
        if(scaleChanged||translateChanged) {
            pixelMatrix.reset();
            pixelMatrix.setScale(pixelScale, pixelScale, scaleAnchorX, scaleAnchorY);
            pixelMatrix.postTranslate(matrixOffsetX, matrixOffsetY);
            scaleChanged = false;
            translateChanged = false;

        }

        //Draw transparency tile
        if(project.transparentBackground){
            p[0] = p[1] = 0;
            pixelMatrix.mapPoints(p);
            float x1 = p[0];
            float y1 = p[1];
            p[0] = pixelWidth;
            p[1] = pixelHeight;
            pixelMatrix.mapPoints(p);
            canvas.drawRect(x1, y1, p[0], p[1], trans);
        }

        //Draw the actual drawing
        canvas.drawBitmap(pixelBitmap, pixelMatrix, noAAPaint);

        //Draw grid
        if(gridEnabled) {
            drawGrid(canvas);
        }


        //Highlight the pixel we'll draw on with cursorPencil
        //TODO Maybe make "less than 1 pixel" offsets global variables
        if(cursorMode) {
            canvas.drawBitmap(cursor.cursorPointerImage, cursor.matrix, cursorPaint);
            p[0] = 0;
            p[1] = 0;
            pixelMatrix.mapPoints(p);

            float loX = p[0]%pixelScale;
            int bX = (int) Math.floor((cursor.getX()-loX)/pixelScale);
            float loY = p[1]%pixelScale;
            int bY = (int) Math.floor((cursor.getY()-loY)/pixelScale);

            cursor.updateCanvasXY();
            if(!cursor.offCanvasBounds())
                cursorPaint.setColor(Utils.invertColor(pixelBitmap.getPixel(cursor.getCanvasX(), cursor.getCanvasY())));
            canvas.drawRect(bX*pixelScale+loX, bY*pixelScale+loY, bX*pixelScale+loX+pixelScale, bY*pixelScale+loY+pixelScale, cursorPaint);
        }


        //Draw FPS
        if(showFps){
            deltaTime = (long) Utils.clamp(deltaTime, 1, 1000);
            String a = "???";
            if(1000/deltaTime>screenRefreshRate)
                a = "SYNCHRONIZED fps";
            else
                a = 1000/deltaTime+" fps.";
            paint.getTextBounds(a, 0, a.length(), bounds);
            canvas.drawText(a, getWidth()-bounds.width()*3, bounds.height()*2+256, textPaint);
        }

        deltaTime = System.currentTimeMillis() - start;
        System.out.println("Canvas drawn in "+deltaTime+" ms");
    }

    /*void writeStateToBundle(Bundle outState){
        outState.putString("projectToLoad", project.name);
        outState.putInt("selectedColor", currentColor);
        outState.putBoolean("grid", gridEnabled);
        outState.putBoolean("cursorMode", cursorMode);
        outState.putBoolean("symmetry", symmetry);
        int symmetryT = 0;
        if(symmetryType == SymmetryType.VERTICAL)
            symmetryT = 1;
        outState.putInt("symmetryType", symmetryT);
        int tool = 0;
        switch (currentTool){
            case FLOOD_FILL:
                tool = 1;
                break;
            case COLOR_PICK:
                tool = 2;
                break;
            case COLOR_SWAP:
                tool = 3;
                break;
            case ERASER:
                tool = 4;
                break;
        }
        outState.putInt("tool", tool);
    }*/

    OnSpecialToolUseListener onSpecialToolUseListener;
    public void setOnSpecialToolUseListener(OnSpecialToolUseListener listener){
        onSpecialToolUseListener = listener;
    }
    public interface OnSpecialToolUseListener{
        void onColorSwapToolUse(int color);
    }
}
