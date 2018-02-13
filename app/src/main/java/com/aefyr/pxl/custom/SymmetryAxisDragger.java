package com.aefyr.pxl.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.aefyr.pxl.R;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 13.02.2018.
 */

public class SymmetryAxisDragger extends View implements AdaptivePixelSurfaceH.OnCanvasTransformChangeListener, AdaptivePixelSurfaceH.OnSymmetryConfigChangeListener{
    private Paint paint;
    private AdaptivePixelSurfaceH aps;
    private float half;
    private float strokeWidth = 4f;

    public SymmetryAxisDragger(Context context) {
        super(context);
        initialize();
    }

    public SymmetryAxisDragger(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SymmetryAxisDragger, 0, 0);
        strokeWidth = a.getDimension(R.styleable.SymmetryAxisDragger_strokeWidth, 4f);
        a.recycle();

        initialize();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        half = w/2f;

        View parent = (View) getParent();
        setX(parent.getWidth()/2 - half);
        setY(parent.getHeight()/2 - half);

        syncPositionFromAPS();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec>heightMeasureSpec?widthMeasureSpec:heightMeasureSpec, widthMeasureSpec>heightMeasureSpec?widthMeasureSpec:heightMeasureSpec);
    }

    private void initialize(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void bindToAPS(AdaptivePixelSurfaceH aps){
        this.aps = aps;
        aps.setOnCanvasTransformChangeListener(this);
        aps.setOnSymmetryConfigChangeListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(half, half, half  - strokeWidth/2f, paint);

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(strokeWidth/2f);
        canvas.drawCircle(half, half, half  - strokeWidth/2f, paint);
    }


    private boolean beingDragged;
    private float pX, pY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(beingDragged)
                return false;

            aps.setActive(false);
            beingDragged = true;
            pX = event.getX();
            pY = event.getY();
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(!beingDragged)
                return false;

            setX(getX() + (event.getX()-pX));
            setY(getY() + (event.getY()-pY));

            updateSymmetryAxis();
            return  true;
        }

        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
            if(beingDragged) {
                aps.setActive(true);
                beingDragged = false;
                return true;
            }
        }


        pX = event.getX();
        pY = event.getY();
        return false;
    }

    private float[] p = {0, 0};
    private void updateSymmetryAxis(){
        p[0] = p[1] = 0;
        aps.pixelMatrix().mapPoints(p);

        boolean h = aps.getSymmetryType() == AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL;

        float sA = (((h?getX():getY()) + half) - p[h?0:1])/aps.pixelScale();

        sA = (float) Math.floor(sA) + ((sA - (int)sA)>=0.5f?0.5f:0f);

        sA = Utils.clamp(sA, 0, h?aps.pixelWidth():aps.pixelHeight());

        aps.setSymmetryAxises(h?sA:aps.symmetryAxisX(), h?aps.symmetryAxisY():sA);

    }

    @Override
    public void onCanvasTransformChanged() {
        if(getVisibility()==VISIBLE&&!beingDragged)
            syncPositionFromAPS();
    }

    private void syncPositionFromAPS(){
        p[0] = aps.symmetryAxisX();
        p[1] = aps.symmetryAxisY();
        aps.pixelMatrix().mapPoints(p);

        if(aps.getSymmetryType()== AdaptivePixelSurfaceH.SymmetryType.HORIZONTAL)
            setX(p[0] - half);
        else
            setY(p[1] - half);
    }

    @Override
    public void onSymmetryConfigChanged() {
        if(aps.symGuidelinesShown() && aps.isSymmetryEnabled()) {
            setVisibility(VISIBLE);
            syncPositionFromAPS();
        }else
            setVisibility(GONE);

    }
}
