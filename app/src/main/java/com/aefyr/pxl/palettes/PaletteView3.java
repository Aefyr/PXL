package com.aefyr.pxl.palettes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.Utils;

/**
 * Created by Aefyr on 22.07.2017.
 */

public class PaletteView3 extends View implements Palette2.OnPaletteChangeListener {

    Palette2 palette;
    Bitmap bitmap;
    Matrix scaleMatrix;
    Paint noAAPaint;
    float colorSizeX;
    float colorSizeY;
    LinearGradient gradient;


    public PaletteView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void setPalette(Palette2 palette) {
        if (this.palette != null)
            this.palette.removeOnPaletteChangedListener(this);

        this.palette = palette;
        loadColorsFromPalette();
        palette.addOnPaletteChangeListener(this);
        invalidate();
    }

    private void loadColorsFromPalette() {
        int[] colors = new int[16];
        for (int i = 0; i < 16; i++) {
            colors[i] = palette.getColor(i);
        }
        bitmap.setPixels(colors, 0, 4, 0, 0, 4, 4);

        float[] positions = new float[16];
        for (int i = 0; i < 16; i++) {
            positions[i] = ((float) i / 16f);
        }
        float px = Utils.dpToPx(256, getResources());
        gradient = new LinearGradient(0, 0, px, px, colors, positions, Shader.TileMode.REPEAT);
    }

    void initialize() {
        bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
        scaleMatrix = new Matrix();
        noAAPaint = new Paint();
        noAAPaint.setAntiAlias(false);
        noAAPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        colorSizeX = (float) w / 4f;
        colorSizeY = (float) h / 4f;
        scaleMatrix.setScale(colorSizeX, colorSizeY);
        noAAPaint.setStrokeWidth((int) colorSizeX / 10);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw palette
        noAAPaint.setShader(null);
        canvas.drawBitmap(bitmap, scaleMatrix, noAAPaint);

        //Draw whole palette outline
        noAAPaint.setColor(Color.BLACK);
        noAAPaint.setShader(gradient);

        canvas.drawRect(0, 0, getWidth(), getHeight(), noAAPaint);


        //Draw selected currentColor outline
        /*if(palette==null)
            return;
        int selectedColorId = palette.getSelectedColorIndex();
        if(selectedColorId == -1)
            return;


        int widthMultiplier = selectedColorId%4;
        int heightMultiplier = (selectedColorId - widthMultiplier)/4;

        noAAPaint.setColor(Utils.invertColor(palette.getSelectedColor()));
        canvas.drawRect(widthMultiplier*colorSizeX+colorSizeX/4, heightMultiplier*colorSizeY+colorSizeY/4, (widthMultiplier+1)*colorSizeX-colorSizeX/4, (heightMultiplier+1)*colorSizeY-colorSizeY/4, noAAPaint);
        */

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Utils.inBounds(event.getX(), event.getY(), 0, 0, getWidth(), getHeight()))
                calculateClickedColor(event.getX(), event.getY());
            return true;
        }
        return super.onTouchEvent(event);
    }

    public interface OnColorClickListener {
        void onColorClick(int positionInPalette);
    }

    private OnColorClickListener listener;

    public void setOnColorClickListener(OnColorClickListener listener) {
        this.listener = listener;
    }

    void calculateClickedColor(float x, float y) {
        int column = (int) (x / colorSizeX);
        int row = (int) (y / colorSizeY);
        int id = row * 4 + column;
        System.out.println("PV3: Clicked currentColor with id " + id);
        if (id < palette.getSize()) {
            if (listener != null)
                listener.onColorClick(id);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (palette != null)
            palette.removeOnPaletteChangedListener(this);
    }

    @Override
    public void onColorSelection(int selectedColor) {

    }

    @Override
    public void onPaletteChanged() {
        loadColorsFromPalette();
        invalidate();
    }
}
