package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import com.af.pxl.Tools.NotRetardedRect;

import java.util.HashSet;

/**
 * Created by Aefyr on 06.08.2017.
 */

public class SelectorH extends ToolH {

    private boolean hasSelection;
    private boolean sessionStarted;
    private NotRetardedRect selection;
    private Paint selectionPaint;
    private Bitmap sB;
    private Canvas sC;

    private Bitmap selectedPart;
    private Canvas selPC;
    private Bitmap backup;

    private int offsetX, offsetY;
    private int pX, pY;

    public SelectorH(AdaptivePixelSurfaceH aps){
        this.aps = aps;
        sB = Bitmap.createBitmap(aps.pixelWidth, aps.pixelHeight, Bitmap.Config.ARGB_8888);
        selectedPart = sB.copy(Bitmap.Config.ARGB_8888, true);
        selPC = new Canvas(selectedPart);
        sC = new Canvas(sB);
        selectionPaint = new Paint();
        selectionPaint.setColor(Color.CYAN);
        selectionPaint.setAlpha(75);
        selection = new NotRetardedRect();
    }

    @Override
    void startDrawing(float x, float y) {

        calculateCanvasXY(x, y);


        if(hasSelection&&(!selection.contains((int)sX, (int) sY)||(sX<0||sY<0||sX>=aps.pixelWidth||sY>=aps.pixelHeight))) {
            hasSelection = false;
            if(offsetX!=0||offsetY!=0) {
                aps.canvasHistory.completeHistoricalChange();
            }else
                aps.canvasHistory.cancelHistoricalChange(false);
            sessionStarted = false;
            drawing =false;
            aps.onSpecialToolUseListener.onSelectionOptionsVisibilityChanged(false);
        }

        moves = 0;

        if(!hasSelection) {
            sessionStarted = false;
            startX = sX;
            startY = sY;
            selection.set((int) startX, (int)startY, (int)sX, (int)sY);
            aps.invalidate();
            drawing = true;
        }else {
            pX = (int) sX;
            pY = (int) sY;

            drawing =true;
        }
    }

    private class Pixel{
        int c;
        int x;
        int y;
        Pixel(int x, int y, int c){
            this.x = x;
            this.y = y;
            this.c = c;
        }
    }

    @Override
    void move(float x, float y) {
        calculateCanvasXY(x, y);
        if(!drawing)
            return;


        if(hasSelection){

            aps.pixelCanvas.drawBitmap(backup, 0, 0, aps.multiShape.overlayPaint);
            offsetX+=(int)sX-pX;
            offsetY+=(int)sY-pY;
            selection.offset((int)sX-pX, (int)sY-pY);

            aps.pixelCanvas.drawBitmap(selectedPart, offsetX, offsetY, aps.noAAPaint);

            pX = (int) sX;
            pY = (int) sY;
            moves++;
        }else {
            selection.set(sX>startX?Math.round(startX):Math.round(sX), sY>startY?Math.round(startY):Math.round(sY), sX>startX?Math.round(sX):Math.round(startX), sY>startY?Math.round(sY):Math.round(startY));
        }
        aps.invalidate();
    }

    @Override
    void stopDrawing(float x, float y) {
        if(!hasSelection&&selection.height()>0&&selection.width()>0) {
            hasSelection = true;
            if(!sessionStarted) {
                aps.canvasHistory.startHistoricalChange();
                offsetX = offsetY = 0;
                HashSet<Pixel> pixels = new HashSet<>();
                for (int i = Utils.clamp(selection.left<selection.right?selection.left:selection.right, 0, aps.pixelWidth - 1); i < aps.pixelWidth && i < (selection.left<selection.right?selection.right:selection.left); i++) {
                    for (int i2 = Utils.clamp(selection.top<selection.bottom?selection.top:selection.bottom, 0, aps.pixelHeight - 1); i2 < aps.pixelHeight && i2 < (selection.top<selection.bottom?selection.bottom:selection.top); i2++) {
                        pixels.add(new Pixel(i, i2, aps.pixelBitmap.getPixel(i, i2)));
                    }
                }

                selPC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                for (Pixel p : pixels) {
                    if (aps.project.transparentBackground)
                        aps.pixelBitmap.setPixel(p.x, p.y, Color.TRANSPARENT);
                    else
                        aps.pixelBitmap.setPixel(p.x, p.y, Color.WHITE);

                    selectedPart.setPixel(p.x, p.y, p.c);
                }

                backup = aps.pixelBitmap.copy(Bitmap.Config.ARGB_8888, true);
                sessionStarted = true;
                aps.pixelCanvas.drawBitmap(selectedPart, offsetX, offsetY, aps.noAAPaint);
                aps.invalidate();
                aps.onSpecialToolUseListener.onSelectionOptionsVisibilityChanged(true);
            }
        }
        drawing = false;
    }

    @Override
    void cancel(float x, float y) {
        if(hasSelection&&(offsetX!=0||offsetY!=0))
            aps.canvasHistory.completeHistoricalChange();
        else if(offsetX==0&&offsetY==0){
            aps.canvasHistory.cancelHistoricalChange(sessionStarted);
        }
        sessionStarted = false;
        hasSelection = false;
        selection.set(0, 0, 0, 0);
        drawing = false;
        aps.invalidate();
        aps.onSpecialToolUseListener.onSelectionOptionsVisibilityChanged(false);
    }

    void copy(){
        Canvas c= new Canvas(backup);
        c.drawBitmap(selectedPart, offsetX, offsetY, aps.noAAPaint);
        aps.pixelCanvas.drawBitmap(backup, 0, 0, aps.multiShape.overlayPaint);
        aps.invalidate();
    }

    void delete(){
        aps.pixelCanvas.drawBitmap(backup, 0, 0, aps.multiShape.overlayPaint);
        hasSelection = false;
        selection.set(0, 0, 0, 0);
        aps.canvasHistory.completeHistoricalChange();
        sessionStarted = false;
        drawing =false;
        aps.invalidate();
        aps.onSpecialToolUseListener.onSelectionOptionsVisibilityChanged(false);
    }

    void drawSelection(Canvas c, Matrix pixelMatrix){

        if(!drawing&&!hasSelection)
            return;

        sC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        sC.drawRect(selection.left, selection.top, selection.right, selection.bottom, selectionPaint);
        c.drawBitmap(sB, pixelMatrix, aps.noAAPaint);
    }
}
