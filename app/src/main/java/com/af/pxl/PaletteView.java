package com.af.pxl;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.util.ArrayList;

/**
 * Created by Aefyr on 18.06.2017.
 */

public class PaletteView extends View {

    Palette palette;
    Paint paint;

    interface OnColorChangedListener{
        void onColorChanged(int newColor);
    }

    OnColorChangedListener onColorChangedListener;

    public PaletteView(Context context) {
        super(context);
        initialize();
    }

    public PaletteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    void initialize(){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    void setOnColorChangedListener(OnColorChangedListener onColorChangedListener){
        this.onColorChangedListener = onColorChangedListener;
    }

    void setPalette(Palette palette){
        this.palette = palette;
        invalidate();
    }

    boolean g = false;
    boolean h = false;
    long start = 0;
    long delayBeforeDeleteWindow = 500;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            g = true;
            h = false;
            start = System.currentTimeMillis();
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(System.currentTimeMillis() - start > delayBeforeDeleteWindow && !h){
                h = true;
                g = false;
                final int clickedColor = getClickedColor(event.getX(), event.getY());
                if(clickedColor>=palette.colors.size()||clickedColor==palette.currentColor)
                    return true;
                new AlertDialog.Builder(getContext()).setTitle("Delete this color?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteColor(clickedColor);
                    }
                }).setNegativeButton("Cancel", null).create().show();
            }
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(g){
                g =false;
                int clickedColor = getClickedColor(event.getX(), event.getY());
                if(clickedColor>palette.colors.size())
                    return true;
                if(clickedColor==palette.colors.size()) {
                    tempAddColorDialog();
                    return true;
                }
                palette.currentColor = clickedColor;
                invalidate();
                if(onColorChangedListener!=null){
                    onColorChangedListener.onColorChanged(palette.colors.get(clickedColor));
                }
            }
        }

        getClickedColor(event.getX(), event.getY());
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(palette == null)
            return;
        canvas.drawColor(Color.BLACK);
        drawColors(canvas);

    }

    boolean a = false;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if(!a) {
            offsetX = h / 12;
            offsetY = h / 12;
            r = h / 2 - h / 12;
            a = true;
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    void resizeToFit(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = (int) ((r*2+offsetY)*lines) + (int) offsetY;
        params.width = (int) ((r*2+offsetX)*max + offsetX);
        System.out.println("New height="+(int) ((r*2+offsetY)*lines));
        setLayoutParams(params);
    }

    float r;
    float offsetY;
    float offsetX;
    int max;
    int lines;
    //TODO Center circles
    void drawColors(Canvas canvas){
        max = (int)(getWidth()/(r*2+offsetX));
        lines = (int) Utils.clamp((float) Math.ceil((float)(palette.colors.size()+1)/max), 1, 999);
        resizeToFit();
        System.out.println("lines="+palette.colors.size()+1+"/"+max);
        System.out.println("r="+r+", max="+max+", offsetY="+offsetY+", offsetX="+offsetX);
        float y = offsetY+r;
        float x = offsetX+r;

        int i = 0;
        int id = 0;
        for(int c: palette.colors){
            paint.setColor(c);
            canvas.drawCircle(x,y,r, paint);
            if(id == palette.currentColor){
                Paint p = new Paint();
                p.setColor(Color.RED);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(offsetY/2);
                canvas.drawCircle(x,y,r, p);
            }
            x+= r*2+offsetX;
            i++;
            id++;
            if(i==max){
                i=0;
                y += offsetY+r*2;
                x = offsetX+r;
            }
        }

        System.out.println("Drawing add sign at "+x+", "+y);
        drawAddColorSign(canvas, x, y);
    }

    void drawAddColorSign(Canvas canvas, float x, float y){
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(offsetY/2);
        canvas.drawCircle(x, y, r, p);
        p.setColor(Color.GRAY);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x,y,r,p);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(x-r+offsetX,y-offsetY, x+r-offsetX, y+offsetY, offsetY, offsetY, p );
        canvas.drawRoundRect(x-offsetX, y-r+offsetY, x+offsetX, y+r-offsetY,offsetY, offsetY, p);
    }

    int getClickedColor(float x, float y){
        int clickedColumn = (int) (x/(r*2+offsetX));
        int clickedRow = (int) (y/(r*2+offsetY));
        System.out.println("Row="+clickedRow+", column="+clickedColumn);
        int clickedColor = clickedRow*max + clickedColumn;
        System.out.println("Clicked color: "+clickedColor);
        return clickedColor;
    }

    void addColor(int color){
        palette.colors.add(color);
        invalidate();
    }

    void deleteColor(int id){
        if(id >= palette.colors.size())
            return;
        palette.colors.remove(id);
        invalidate();
    }

    ColorPicker colorPicker;
    void tempAddColorDialog(){
        final AlertDialog d = new AlertDialog.Builder(getContext()).setView(R.layout.color_picker).create();
        d.show();
        colorPicker = new ColorPicker((ColorPickerView) d.findViewById(R.id.colorPickerHue),(SeekBar) d.findViewById(R.id.seekBarHue),
                (ColorPickerView) d.findViewById(R.id.colorPickerSat), (SeekBar) d.findViewById(R.id.seekBarSat), (ColorPickerView) d.findViewById(R.id.colorPickerVal),
                (SeekBar) d.findViewById(R.id.seekBarVal), (ColorCircle) d.findViewById(R.id.colorView), Color.RED);
        (d.findViewById(R.id.colorPickButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newColor = Color.HSVToColor(colorPicker.color);
                addColor(newColor);
                d.cancel();
                colorPicker = null;
            }
        });

    }
}
