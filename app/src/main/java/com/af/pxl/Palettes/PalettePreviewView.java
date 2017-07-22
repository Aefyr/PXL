package com.af.pxl.Palettes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Aefyr on 22.07.2017.
 */

public class PalettePreviewView extends View {

    private Bitmap palettePreviewBitmap;
    private Matrix scaleMatrix;
    private Paint noAAPaint;

    public PalettePreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize(){
        palettePreviewBitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
        scaleMatrix = new Matrix();
        noAAPaint = new Paint();
        noAAPaint.setAntiAlias(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scaleMatrix.setScale((float) w/4f, (float) h/4f);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(palettePreviewBitmap, scaleMatrix, noAAPaint);
    }

    public void setPalette(Palette2 palette){
        int[] colors = new int[16];
        for(int i = 0;i<16;i++){
            colors[i] = palette.getColor(i);
        }
        palettePreviewBitmap.setPixels(colors, 0, 4, 0, 0, 4, 4);
        invalidate();
    }
}
