package com.aefyr.pxl;

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
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.analytics.CanvasAnalyticsHelper;
import com.aefyr.pxl.fragments.PreferencesFragment;
import com.aefyr.pxl.palettes.PaletteManagerH;
import com.aefyr.pxl.projects.Project;
import com.aefyr.pxl.common.RectP;
import com.aefyr.pxl.util.Utils;

import java.util.ArrayDeque;

/**
 * Created by Aefyr on 27.06.2017.
 */

public class AdaptivePixelSurfaceH extends View {

    Project project;
    boolean projectReady = false;
    boolean surfaceReady = false;

    //Canvas and drawing
    Paint paint;
    private Paint cursorPaint;
    int currentColor;
    int strokeWidth = 1;

    int pixelWidth = 128;
    int pixelHeight = 128;

    Bitmap pixelBitmap;
    Canvas pixelCanvas;

    float pixelScale = 1f;
    Matrix pixelMatrix;

    int realWidth, realHeight;

    //Tools and utils
    public enum Tool {
        PENCIL, FLOOD_FILL, COLOR_PICK, COLOR_SWAP, ERASER, MULTISHAPE, SELECTOR
    }

    Tool currentTool = Tool.PENCIL;
    private Tool prevTool = Tool.PENCIL;

    CanvasHistoryH canvasHistory;

    CursorH cursor;
    boolean cursorMode = false;
    SuperPencilH superPencil;
    MultiShapeH multiShape;
    SelectorH selector;

    //Symmetry
    boolean symmetry = false;
    SymmetryType symmetryType = SymmetryType.HORIZONTAL;
    boolean symmetryAxisesShown = false;

    public enum SymmetryType {
        HORIZONTAL, VERTICAL
    }

    //Rest
    PaletteManagerH colorManager;
    private CanvasAnalyticsHelper canvasAnalytics;

    public AdaptivePixelSurfaceH(Context context) {
        super(context);
        initialize();
    }

    public AdaptivePixelSurfaceH(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        /*pixelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pixelCanvas = new Canvas(pixelBitmap);*/

        pixelMatrix = new Matrix();
        pixelMatrix.setScale(pixelScale, pixelScale);

        //pixelCanvas.drawColor(Color.WHITE);

        //canvasHistory = new CanvasHistory(this, pixelBitmap, 100);

        cursor = new CursorH(this);
        initializePaints();
        cursorPreviewRect = new RectF();
        canvasAnalytics = CanvasAnalyticsHelper.getInstance(getContext());
    }

    public interface OnToolChangeListener{
        void onToolChanged(Tool newTool, boolean showToolSettings);
    }
    private OnToolChangeListener onToolChangeListener;
    public void setOnToolChangeListener(OnToolChangeListener listener){
        onToolChangeListener = listener;
    }

    //Setters
    public void setTool(Tool tool, boolean showToolSettings) {
        if (currentTool == tool)
            return;

        prevTool = currentTool;

        if (currentTool == Tool.SELECTOR)
            selector.cancel(0, 0);

        if (project.transparentBackground)
            paint.setXfermode(null);
        paint.setColor(currentColor);
        switch (tool) {
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
                if (project.transparentBackground) {
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                } else
                    paint.setColor(Color.WHITE);
                break;
            case MULTISHAPE:
                currentTool = Tool.MULTISHAPE;
                break;
            case SELECTOR:
                currentTool = Tool.SELECTOR;
                break;
        }
        if(onToolChangeListener!=null)
            onToolChangeListener.onToolChanged(currentTool, showToolSettings);

        canvasAnalytics.logToolPicked(tool);
        invalidate();
    }

    private void setPrevTool(){
        setTool(prevTool, false);
    }

    public void setColor(int color) {
        currentColor = color;
        cursor.setPaintColor(color);
        if (currentTool != Tool.ERASER)
            paint.setColor(color);
    }

    public void setStrokeWidth(int width) {
        if (strokeWidth == width)
            return;

        strokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public void setColorManager(PaletteManagerH manager) {
        this.colorManager = manager;
    }

    public Project getProject() {
        return project;
    }

    public void setSymmetryEnabled(boolean enabled, SymmetryType type) {
        symmetry = enabled;
        symmetryType = type;
        superPencil.symmetryUpdate();
        canvasAnalytics.logSymmetryChange(enabled, type);
    }

    public SymmetryType getSymmetryType() {
        return symmetryType;
    }

    public boolean isSymmetryEnabled() {
        return symmetry;
    }

    public boolean toggleSymmetryGuidelines(){
        symmetryAxisesShown = !symmetryAxisesShown;
        invalidate();
        return symmetryAxisesShown;
    }

    public boolean symGuidelinesShown(){
        return symmetryAxisesShown;
    }

    private Paint sAP;
    private void drawSymmetryAxises(Canvas c){
        sAP.setStrokeWidth((int) Utils.clamp(pixelScale / 12, 1, 999));
        if(symmetryType==SymmetryType.HORIZONTAL){
            p[0] = ((float)pixelWidth)/2f;
            p[1] = 0;
            pixelMatrix.mapPoints(p);
            c.drawLine(p[0], p[1]-pixelScale, p[0], p[1]+(pixelScale*(pixelHeight+1)), sAP);
        }else {
            p[0] = 0;
            p[1] = ((float)pixelHeight)/2f;
            pixelMatrix.mapPoints(p);
            c.drawLine(p[0]-pixelScale, p[1],p[0]+(pixelScale*(pixelWidth+1)), p[1], sAP);
        }
    }

    public RectP getBounds() {
        return new RectP(0, 0, pixelWidth, pixelHeight);
    }

    Paint trans;

    public void setProject(Project project) {
        this.project = project;
        pixelBitmap = project.getBitmap(true);
        this.pixelWidth = pixelBitmap.getWidth();
        this.pixelHeight = pixelBitmap.getHeight();
        pixelCanvas = new Canvas(pixelBitmap);
        canvasHistory = new CanvasHistoryH(this, project, CanvasHistoryH.ADAPTIVE_SIZE);
        superPencil = new SuperPencilH(this);
        multiShape = new MultiShapeH(this);
        selector = new SelectorH(this);
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
        autoSwitchToPrevToolAfterColorPick = p.getBoolean(PreferencesFragment.AUTO_SWITCH_AFTER_CS, true);
        if (project.transparentBackground) {
            int bg = p.getInt(PreferencesFragment.TRANSPARENT_CANVAS_BACKGROUND_COLOR, 1);
            trans = new Paint();
            if (bg <= 0) {
                trans.setColor(bg);
                trans.setStyle(Paint.Style.FILL);
            } else if (bg == 1) {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inScaled = false;
                op.inMutable = false;
                trans.setShader(new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.tilebglidl), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                trans.setFilterBitmap(false);
                trans.setAntiAlias(false);
                trans.setStyle(Paint.Style.FILL);
            } else if (bg == 2) {
                trans.setColor(gridP.getColor());
                trans.setStyle(Paint.Style.STROKE);
                trans.setStrokeWidth((int) Utils.dpToPx(1, getResources()));
            }

        }
        projectReady = true;
    }

    //Utility methods
    Paint gridP;
    float[] p = {0, 0};

    void drawGrid(Canvas c) {
        gridP.setStrokeWidth((int) Utils.clamp(pixelScale / 20, 1, 999));

        p[0] = 0;
        p[1] = 0;
        pixelMatrix.mapPoints(p);

        float gridCellSize = pixelScale * (int) Utils.clamp((8 * (1 - (pixelScale / (realWidth > realHeight ? realHeight : realWidth / 64)))), 1, 8);

        float lOX = Utils.clamp(p[0] % gridCellSize, p[0], realWidth);
        float lOY = Utils.clamp(p[1] % gridCellSize, p[1], realHeight);

        p[0] = pixelWidth;
        p[1] = pixelHeight;
        pixelMatrix.mapPoints(p);

        float limitX = Utils.clamp(p[0], 0, realWidth);
        float limitY = Utils.clamp(p[1], 0, realHeight);

        float x = lOX;
        float y = lOY;

        while (x <= limitX + 1) {
            c.drawLine(x, lOY - gridCellSize, x, limitY + gridCellSize, gridP);
            x += gridCellSize;
        }

        while (y <= limitY + 1) {
            c.drawLine(lOX - gridCellSize, y, limitX + gridCellSize, y, gridP);
            y += gridCellSize;
        }


    }

    //Updaters

    //Initializers?
    //TODO Add ability to create custom CursorPencil pointers

    private boolean gridEnabled = false;

    boolean toggleGrid() {
        setGridEnabled(!gridEnabled);
        return gridEnabled;
    }

    void setGridEnabled(boolean enabled) {
        gridEnabled = enabled;
        canvasAnalytics.logGridVisibilityChange(enabled);
        invalidate();
    }

    Paint noAAPaint;
    private int backgroundColor = Color.GRAY;

    void initializePaints() {
        //Projects paint
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(24);
        noAAPaint = new Paint(paint);
        //noAAPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        backgroundColor = preferences.getInt(PreferencesFragment.BACKGROUND_COLOR, -3343361);
        //Grid paint
        gridP = new Paint();
        gridP.setColor(preferences.getInt(PreferencesFragment.GRID_COLOR, -11183637));
        gridP.setStyle(Paint.Style.STROKE);
        gridP.setStrokeWidth(1f);

        sAP = new Paint(gridP);
        sAP.setColor(preferences.getInt(PreferencesFragment.SYM_AXISES_COLOR, Color.BLACK));

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
        if (!surfaceReady) {
            th = (int) Utils.dpToPx(1, getResources());
            centerCanvas();
            cursor.center(realWidth, realHeight);
            surfaceReady = true;
        }
        invalidate();
    }


    public void centerCanvas() {
        pixelScale = Math.min(realHeight/(float)pixelHeight, realWidth/(float)pixelWidth);
        matrixOffsetX = ((float) realWidth / 2f - (((float) pixelWidth * pixelScale) / 2f));
        matrixOffsetY = ((float) realHeight / 2f - (((float) pixelHeight * pixelScale) / 2f));
        scaleAnchorX = 0;
        scaleAnchorY = 0;
        translateChanged = true;
    }


    float prevCX, prevCY;
    int prevPointerCount = 0;
    float matrixOffsetX, matrixOffsetY;

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

        //We really don't wanna interfere the flood fill algorithm
        if (fillInProgress)
            return true;

        if (event.getPointerCount() != 1) {
            cancelDrawing(cursorMode?cursor.getX():event.getX(), cursorMode?cursor.getY():event.getY());
            touchToolWillBeUsedOnUpEvent = false;


            midX = (event.getX(0) + event.getX(1)) / 2f;
            midY = (event.getY(0) + event.getY(1)) / 2f;

            float dist = Utils.vector2Distance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
            float deltaDist = prevDist - dist;

            if (prevPointerCount != event.getPointerCount()) {
                prevCX = midX;
                prevCY = midY;
                deltaDist = 0;
            }

            c += deltaDist;

            if (c > th) {
                pixelScale *= Math.pow(0.995f, Math.abs(c / th));
                c = 0;
            } else if (c < -th) {
                pixelScale *= Math.pow(1.005f, Math.abs(c / th));
                c = 0;
            }

            pixelScale = Utils.clamp(pixelScale, 1f, realWidth / 4);

            prevDist = dist;

            matrixOffsetX += midX - prevCX;
            matrixOffsetY += midY - prevCY;


            if (!anchorSet) {
                anchorSet = true;
                float a = (midX - matrixOffsetX) / pixelScale + (1 - 1 / pixelScale) * scaleAnchorX;
                float b = (midY - matrixOffsetY) / pixelScale + (1 - 1 / pixelScale) * scaleAnchorY;
                float c = scaleAnchorX;
                float d = scaleAnchorY;
                scaleAnchorX = a;
                scaleAnchorY = b;

                matrixOffsetX += (a - c) * (pixelScale - 1);
                matrixOffsetY += (b - d) * (pixelScale - 1);
            }

            prevCX = midX;
            prevCY = midY;
            translateChanged = true;
            invalidate();

            prevPointerCount = event.getPointerCount();
            return true;
        }
        anchorSet = false;

        if (cursorMode) {
            cursor.processMotionEvent(event);
        } else {
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
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            colorPick((int) ((event.getX() - p[0]) / pixelScale), (int) ((event.getY() - p[1]) / pixelScale));
                        }
                    }
                    break;
                case FLOOD_FILL:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            floodFill((int) ((event.getX() - p[0]) / pixelScale), (int) ((event.getY() - p[1]) / pixelScale));
                        }
                    }
                    break;
                case COLOR_SWAP:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        touchToolWillBeUsedOnUpEvent = true;
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (touchToolWillBeUsedOnUpEvent) {
                            p[0] = p[1] = 0;
                            pixelMatrix.mapPoints(p);
                            int x = (int) ((event.getX() - p[0]) / pixelScale);
                            int y = (int) ((event.getY() - p[1]) / pixelScale);
                            if (x < pixelWidth && x >= 0 && y < pixelHeight && y >= 0)
                                onSpecialToolUseListener.onColorSwapToolUse(pixelBitmap.getPixel(x, y));
                        }
                    }
                    break;
                case MULTISHAPE:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        multiShape.startDrawing(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        multiShape.move(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        multiShape.stopDrawing(event.getX(), event.getY());
                    }
                    break;
                case SELECTOR:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        selector.startDrawing(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        selector.move(event.getX(), event.getY());
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        selector.stopDrawing(event.getX(), event.getY());
                    }
                    break;
            }
        }


        lastX = event.getX();
        lastY = event.getY();
        prevPointerCount = event.getPointerCount();
        return true;
    }

    public void cancelDrawing(float x, float y){
        superPencil.cancel(x, y);
        multiShape.cancel(x, y);
        selector.cancel(x, y);
    }

    public void cancelDrawing(){
        superPencil.cancel();
        multiShape.cancel();
        selector.cancel();
    }

    void setCursorModeEnabled(boolean enabled) {
        if (enabled == cursorMode)
            return;

        cursorMode = enabled;
        canvasAnalytics.logCursorModeChange(enabled);
        invalidate();
    }

    boolean fillInProgress = false;

    void floodFill(int x, int y) {
        int newC = paint.getColor();
        if (x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight || pixelBitmap.getPixel(x, y) == newC)
            return;

        canvasHistory.startHistoricalChange();
        fillInProgress = true;

        int oldC = pixelBitmap.getPixel(x, y);

        long s = System.currentTimeMillis();
        ArrayDeque<Pixel> p = new ArrayDeque<>();

        p.add(new Pixel(x, y));

        Pixel p1;
        int x1, x2;
        while (!p.isEmpty()) {
            p1 = p.remove();
            if (p1.x < 0 || p1.x >= pixelWidth || p1.y < 0 || p1.y >= pixelHeight || pixelBitmap.getPixel(p1.x, p1.y) != oldC)
                continue;
            x1 = p1.x;
            x2 = p1.x;

            while (x1 < pixelWidth && pixelBitmap.getPixel(x1, p1.y) == oldC)
                x1++;

            while (x2 >= 0 && pixelBitmap.getPixel(x2, p1.y) == oldC)
                x2--;

            x2++;


            for (int i2 = x2; i2 < x1; i2++) {
                pixelBitmap.setPixel(i2, p1.y, newC);
            }

            if (p1.y > 0 && p1.y < pixelHeight) {
                for (int i = x2; i < x1; i++) {
                    if (pixelBitmap.getPixel(i, p1.y - 1) == oldC)
                        p.add(new Pixel(i, p1.y - 1));
                }
            }
            if (p1.y >= 0 && p1.y < pixelHeight - 1) {
                for (int i = x2; i < x1; i++) {
                    if (pixelBitmap.getPixel(i, p1.y + 1) == oldC)
                        p.add(new Pixel(i, p1.y + 1));
                }
            }

        }

        canvasHistory.completeHistoricalChange();
        fillInProgress = false;
        System.out.println("Filled in " + (System.currentTimeMillis() - s) + " ms");
        invalidate();
    }

    private boolean autoSwitchToPrevToolAfterColorPick;
    void colorPick(int x, int y) {
        if (x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight)
            return;
        int pickedColor = pixelBitmap.getPixel(x, y);

        if(pickedColor!=Color.TRANSPARENT) {
            colorManager.setCurrentColor(pickedColor);
            if(autoSwitchToPrevToolAfterColorPick)
                setPrevTool();
        }
    }

    void clearCanvas() {
        canvasHistory.startHistoricalChange();
        if (project.transparentBackground)
            pixelCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        else
            pixelCanvas.drawColor(Color.WHITE);
        canvasHistory.completeHistoricalChange();
        invalidate();
    }

    class Pixel {
        int x, y;

        Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    boolean scaleChanged = false;
    boolean translateChanged = true;

    long deltaTime = 1;

    RectF cursorPreviewRect;

    @Override
    protected void onDraw(Canvas canvas) {
        long start = System.currentTimeMillis();
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);


        if (!projectReady)
            return;

        //Recalculate matrix and grid if zoom or/and move were used
        if (scaleChanged || translateChanged) {
            pixelMatrix.reset();
            pixelMatrix.setScale(pixelScale, pixelScale, scaleAnchorX, scaleAnchorY);
            pixelMatrix.postTranslate(matrixOffsetX, matrixOffsetY);
            scaleChanged = false;
            translateChanged = false;

        }

        //Draw transparency tile
        if (project.transparentBackground) {
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
        if (gridEnabled)
            drawGrid(canvas);

        //Draw symmetry guidelines
        if(symmetryAxisesShown&&symmetry)
            drawSymmetryAxises(canvas);

        if (currentTool == Tool.SELECTOR)
            selector.drawSelection(canvas, pixelMatrix);


        //Highlight the pixel we'll draw on with cursorPencil
        //TODO Maybe make "less than 1 pixel" offsets global variables
        if (cursorMode)
            cursor.drawCursor(canvas);


        deltaTime = System.currentTimeMillis() - start;
        System.out.println("Canvas drawn in " + deltaTime + " ms");
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

    public void setOnSpecialToolUseListener(OnSpecialToolUseListener listener) {
        onSpecialToolUseListener = listener;
    }

    public interface OnSpecialToolUseListener {
        void onColorSwapToolUse(int color);

        void onSelectionOptionsVisibilityChanged(boolean visible);
    }
}
