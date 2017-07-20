package com.af.pxl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by Aefyr on 12.07.2017.
 */

public class PixelImageView extends View{

    Matrix scaleMatrix;
    private Paint p;
    private float pixelScale;
    private boolean autoResize;
    private int backgroundColor;

    private boolean r = false;
    private OnSizeChangedListener onSizeChangedListener;

    interface OnSizeChangedListener{
        void onSizeChanged(int width, int height);
    }

    public PixelImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PixelImageView, 0, 0);

        try {
            bitmap = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.PixelImageView_image, R.drawable.pencil));
            autoResize = a.getBoolean(R.styleable.PixelImageView_autoResizeToMax, false);
            backgroundColor = a.getColor(R.styleable.PixelImageView_imageBackground, Color.GRAY);
        }finally {
            a.recycle();
        }

        initialize();
    }

    private void initialize(){
        scaleMatrix = new Matrix();
        p = new Paint();
        p.setAntiAlias(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(autoResize&&!r){
            resize();
            r = true;
        }
        calculateMatrix();
        if(onSizeChangedListener!=null)
            onSizeChangedListener.onSizeChanged(w, h);
    }


    private Bitmap bitmap;
    public void setImageBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
        calculateMatrix();
    }

    private void calculateMatrix(){
        scaleMatrix.reset();
        float scaleX = (float) getWidth()/(float)bitmap.getWidth();
        float scaleY = (float)getHeight()/(float)bitmap.getHeight();
        pixelScale = scaleX>scaleY?scaleY:scaleX;
        scaleMatrix.setScale(pixelScale, pixelScale);
        float offsetX = (getWidth()-(bitmap.getWidth()*pixelScale))*0.5f;
        float offsetY = (getHeight()-(bitmap.getHeight()*pixelScale))*0.5f;
        scaleMatrix.postTranslate(offsetX, offsetY);
    }

    private void resize(){
        WindowManager wm = (WindowManager)  getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        int max = p.y>p.x?p.x:p.y;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = max;
        params.width = max;
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(bitmap, scaleMatrix, p);
    }

    float getPixelScale(){
        return pixelScale;
    }

    void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener){
        this.onSizeChangedListener = onSizeChangedListener;
    }
}
