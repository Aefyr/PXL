package com.aefyr.pxl.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.R;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 16.02.2018.
 */

public class HSVSeekBar extends View {
    public static final int MODE_H = 0;
    public static final int MODE_S = 1;
    public static final int MODE_V = 2;

    private int mode = 0;
    private float rounding = 8;

    private float[] color = {0, 1, 1};
    int[] colors;
    private Paint paint;

    private RectF gradientRectBorders;

    private float pointerShadowSize = 16;
    private float pointerPadding = 0;
    private float actualRadius = 0;

    private int segmentsCount;
    private float segmentSize;
    private int position;

    private Bitmap hBitmap;

    public HSVSeekBar(Context context) {
        super(context);
        initialize();
    }

    public HSVSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HSVSeekBar);
        mode = attributes.getInt(R.styleable.HSVSeekBar_mode, 0);
        rounding = attributes.getDimension(R.styleable.HSVSeekBar_rounding, 8);
        pointerShadowSize = attributes.getDimension(R.styleable.HSVSeekBar_pointerShadowSize, 8);
        pointerPadding = attributes.getDimension(R.styleable.HSVSeekBar_pointerPadding, 0);
        attributes.recycle();
        initialize();
    }

    private void initialize(){
        setMode(mode);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setMode(int mode){
        if(hBitmap!=null)
            hBitmap.recycle();
        hBitmap = null;

        if(mode == MODE_H){
            colors = new int[361];
            segmentsCount = 360;
        }else if(mode == MODE_S){
            colors = new int[101];
            segmentsCount = 100;
        }else if (mode == MODE_V){
            colors = new int[101];
            segmentsCount = 100;
        }else
            throw new IllegalArgumentException("Invalid mode parameter");
    }

    public void setColor(float h, float s, float v){
        color[0] = h;
        color[1] = s;
        color[2] = v;
        syncColorWithPosition();
    }

    public void setColor(float[] newColorHSV){
        color[0] = newColorHSV[0];
        color[1] = newColorHSV[1];
        color[2] = newColorHSV[2];
        syncColorWithPosition();
    }

    private void syncColorWithPosition(){
        if(mode == MODE_H){
            int pPos = position;
            position = Utils.clamp((int)color[0],0, segmentsCount);

            if(pPos!=position)
                invalidate();
        }else {
            position = Utils.clamp((int) (color[mode] * 100f), 0, segmentsCount);
            invalidate();
        }
    }

    public void setPosition(int position){
        this.position = Utils.clamp(position, 0, segmentsCount);
        invalidate();
    }

    public int getSelectedPosition(){
        return position;
    }

    public interface OnPositionUpdateListener{
        void onPositionChanged(HSVSeekBar seekBar, int newPosition);
    }

    private OnPositionUpdateListener onPositionUpdateListener;

    public void setOnPositionUpdateListener(OnPositionUpdateListener listener){
        onPositionUpdateListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        actualRadius = h/2f - pointerPadding - pointerShadowSize;
        gradientRectBorders = new RectF(getPaddingLeft() + actualRadius + pointerShadowSize, getPaddingTop(), w  - actualRadius - pointerShadowSize, h-getPaddingBottom());
        segmentSize = gradientRectBorders.width()/segmentsCount;
        if(mode == MODE_H)
            hBitmap = generateGradient();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isInEditMode()){
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            canvas.drawRoundRect(gradientRectBorders, rounding, rounding, paint);

            paint.setColor(Color.GRAY);
            float boundPosition = position*segmentSize + gradientRectBorders.left;
            canvas.drawCircle(boundPosition, canvas.getHeight()/2f, actualRadius + pointerShadowSize, paint);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(boundPosition, canvas.getHeight()/2f, actualRadius, paint);
            return;
        }

        drawGradient(canvas);
        drawPointer(canvas);
    }

    private void drawGradient(Canvas c){
        if(mode == MODE_H)
            c.drawBitmap(hBitmap, 0, 0, paint);
        else
            renderGradient(c);
    }

    private void drawPointer(Canvas c){
        float boundPosition = position*segmentSize + gradientRectBorders.left;
        float cy = gradientRectBorders.top + gradientRectBorders.height()/2f;

        RadialGradient pointerShadow = new RadialGradient(boundPosition, cy, actualRadius + pointerShadowSize, Color.GRAY, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        paint.setShader(pointerShadow);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(boundPosition, cy, actualRadius + pointerShadowSize, paint);

        paint.setShader(null);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        c.drawCircle(boundPosition, cy, actualRadius, paint);
    }

    //Used only for MODE_H since it has a static gradient
    private Bitmap generateGradient(){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        renderGradient(canvas);
        return bitmap;
    }

    private void renderGradient(Canvas c){
        if(mode == MODE_H){
            color[1] = color[2] = 1;
            for(int i = 0; i<=360; i++){
                color[0] = i;
                colors[i] = Color.HSVToColor(color);
            }
        }else{
            for(int i = 0; i<=100; i++){
                color[mode] = ((float)i)/100f;
                colors[i] = Color.HSVToColor(color);
            }
        }

        paint.setShader(new LinearGradient(gradientRectBorders.left, gradientRectBorders.top, gradientRectBorders.right, gradientRectBorders.bottom, colors, null, Shader.TileMode.CLAMP));
        c.drawRoundRect(gradientRectBorders, rounding, rounding, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            updatePosition(event.getX(0));
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            updatePosition(event.getX(0));
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            updatePosition(event.getX(0));
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void updatePosition(float rawX){
        rawX = Utils.clamp(rawX, gradientRectBorders.left, gradientRectBorders.right);
        int pPos = position;
        position = (int) ((rawX - gradientRectBorders.left)/segmentSize);

        if(pPos!=position) {
            if(onPositionUpdateListener!=null)
                onPositionUpdateListener.onPositionChanged(this, position);
            invalidate();
        }
    }
}
