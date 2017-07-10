package com.af.pxl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Aefyr on 10.07.2017.
 */

public class PaletteView2 extends View implements Palette2.OnPaletteChangeListener{
    public PaletteView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PaletteView2, 0, 0);

        try {
            gapBetweenColors = a.getDimensionPixelSize(R.styleable.PaletteView2_gapBetweenColors, 100);
            sizeOfColorX = a.getDimensionPixelSize(R.styleable.PaletteView2_sizeOfColorX, 100);
            sizeOfColorY = a.getDimensionPixelSize(R.styleable.PaletteView2_sizeOfColorY, 100);
        }finally {
            a.recycle();
        }

        p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        reCalculate(w, h);
    }

    private Palette2 palette;
    private Paint p;
    private int gapBetweenColors, sizeOfColorX, sizeOfColorY;

    private int maxColorsPerLine;
    private int linesCount;
    private int linesDisplayed;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int currentX = gapBetweenColors;
        int currentY = gapBetweenColors;
        int colorInLine = 0;

        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setStrokeWidth(0);
        for(int i = 0; i<palette.getSize(); i++){
            int color = palette.getColor(i);

            p.setColor(color);
            canvas.drawRect(currentX, currentY, currentX+sizeOfColorX, currentY+sizeOfColorY, p);
            currentX += sizeOfColorX+gapBetweenColors;
            colorInLine++;

            if(colorInLine >= maxColorsPerLine){
                currentY += gapBetweenColors+sizeOfColorY;
                currentX = gapBetweenColors;
                colorInLine = 0;
            }

        }

        //Outline selected color
        currentX = gapBetweenColors + (palette.getSelectedColorIndex()%maxColorsPerLine)*(sizeOfColorX+gapBetweenColors);
        currentY = gapBetweenColors + (int) (Math.floor(palette.getSelectedColorIndex()/maxColorsPerLine)*(sizeOfColorY+gapBetweenColors));
        p.setColor(Utils.invertColor(palette.getSelectedColor()));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(sizeOfColorX/10);
        canvas.drawRect(currentX, currentY, currentX+sizeOfColorX, currentY+sizeOfColorY, p);
    }

    void reCalculate(int width, int height){
        maxColorsPerLine = (int) Math.floor((width-gapBetweenColors)/(sizeOfColorX+gapBetweenColors));
        linesCount = (int) Math.ceil((float)palette.getSize()/(float) maxColorsPerLine);
        System.out.println("maxColorsPerLine="+maxColorsPerLine+", linesCount="+linesCount);

        if(resizeNeeded())
            resize();
    }

    void resize(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = linesCount*(sizeOfColorY+gapBetweenColors)+gapBetweenColors;
        params.width = maxColorsPerLine*(sizeOfColorX+gapBetweenColors)+gapBetweenColors;
        setLayoutParams(params);
        linesDisplayed = linesCount;
        invalidate();
    }

    boolean resizeNeeded(){
        return linesDisplayed != linesCount;
    }

    void setPalette(Palette2 palette){
        this.palette = palette;
        palette.setOnPaletteChangeListener(this);
        invalidate();
    }

    @Override
    public void paletteChanged() {
        reCalculate(getWidth(), getHeight());
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getClickedColor(event.getX(), event.getY());
        return super.onTouchEvent(event);
    }

    void getClickedColor(float x, float y){
        if(x>gapBetweenColors)
            x-=gapBetweenColors;

        if(y>gapBetweenColors)
            y-=gapBetweenColors;
        int clickedColorId = (int) (x/(sizeOfColorX+gapBetweenColors)) + ((int) (y/(sizeOfColorY+gapBetweenColors)))*maxColorsPerLine;
        palette.setSelectedColor(clickedColorId);
        System.out.println("Clicked color index="+clickedColorId+", btw y="+(int) (y/(sizeOfColorY+gapBetweenColors)));
    }


}
