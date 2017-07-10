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

public class PaletteView extends View implements View.OnLongClickListener{

    Palette palette;
    Paint paint;

    @Override
    public boolean onLongClick(View view) {
        g = false;
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ONLONGCLICK");
        final int clickedColor = getClickedColor(lastX, lastY);
        if(clickedColor>=palette.colors.size())
            return true;
        new AlertDialog.Builder(getContext()).setTitle("Delete this color?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(clickedColor == palette.currentColor && clickedColor>0)
                    palette.currentColor -= 1;
                deleteColor(clickedColor);
            }
        }).setNegativeButton("Cancel", null).create().show();
        return true;
    }

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
        setOnLongClickListener(this);
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
    long delayBeforeDeleteWindow = 300;

    float prevY;
    float scrollOffsetY;
    float offsetThisSession = 0;
    boolean s = false;
    float lastX, lastY;
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            g = true;
            h = true;
            s = true;

            offsetThisSession = 0;
            /*postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!h)
                        return;
                    g = false;
                    final int clickedColor = getClickedColor(event.getX(), event.getY());
                    if(clickedColor>=palette.colors.size())
                        return ;
                    new AlertDialog.Builder(getContext()).setTitle("Delete this color?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(clickedColor == palette.currentColor && clickedColor>0)
                                    palette.currentColor -= 1;
                            deleteColor(clickedColor);
                        }
                    }).setNegativeButton("Cancel", null).create().show();
                }
            }, delayBeforeDeleteWindow);*/
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(s){
                prevY = event.getY();
                s = false;
            }
            float a = event.getY()-prevY;
            scrollOffsetY=Utils.clamp(scrollOffsetY-a,0,((r*2+offsetY)*(lines-getHeight()/(r*2+offsetY)))+offsetY);
            offsetThisSession+=Math.abs(a);
            prevY = event.getY();
            System.out.println("OffsetThisSession="+offsetThisSession);
            if(offsetThisSession>=32){
                g = false;
                h = false;
            }
            invalidate();
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(g){
                h = false;
                g = false;
                int clickedColor = getClickedColor(event.getX(), event.getY());
                if(clickedColor>palette.colors.size())
                    return true;
                if(palette.colors.size() <16 && clickedColor==palette.colors.size()) {
                    tempAddColorDialog();
                    return true;
                }
                palette.currentColor = clickedColor;
                invalidate();
                if(onColorChangedListener!=null){
                    onColorChangedListener.onColorChanged(palette.colors.get(clickedColor).color);
                }
            }
        }

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

    void drawColors(Canvas canvas){
        max = (int)(getWidth()/(r*2+offsetX));
        int a = palette.colors.size()<16?palette.colors.size()+1:palette.colors.size();
        lines = (int) Utils.clamp((float) Math.ceil((float)(a)/max), 1, 999);
        resizeToFit();
        System.out.println("lines="+palette.colors.size()+1+"/"+max);
        System.out.println("r="+r+", max="+max+", offsetY="+offsetY+", offsetX="+offsetX);
        float y = offsetY+r-scrollOffsetY;
        float x = offsetX+r;

        int i = 0;
        int id = 0;
        long start = System.currentTimeMillis();

        //TODO Optimize this, cause now this just skipping thing is just retarded
        for(PaletteColor pc: palette.colors){
            //I mean this V
            int c = pc.color;
            if(y<-r){
                x+= r*2+offsetX;
                i++;
                id++;
                if(i==max){
                    i=0;
                    y += offsetY+r*2;
                    x = offsetX+r;
                }
                continue;
            }
            if(y>getHeight()+r){
                x+= r*2+offsetX;
                i++;
                id++;
                if(i==max){
                    i=0;
                    y += offsetY+r*2;
                    x = offsetX+r;
                }
                continue;
            }
            paint.setColor(c);
            canvas.drawCircle(x,y,r, paint);
            if(id == palette.currentColor){
                Paint p = new Paint();
                p.setColor(Utils.invertColor(c));
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
        System.out.println("Palette drawn in "+(System.currentTimeMillis()-start)+" ms!");

        System.out.println("Drawing add sign at "+x+", "+y);
        if(palette.colors.size()<16) {
            drawAddColorSign(canvas, x, y);
        }
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
        int clickedRow = (int) ((y+scrollOffsetY)/(r*2+offsetY));
        System.out.println("Row="+clickedRow+", column="+clickedColumn);
        int clickedColor = clickedRow*max + clickedColumn;
        System.out.println("Clicked color: "+clickedColor);
        return clickedColor;
    }

    void addColor(int color){
        palette.colors.add(new PaletteColor(color));
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
