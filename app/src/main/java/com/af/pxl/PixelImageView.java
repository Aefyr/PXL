package com.af.pxl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Aefyr on 12.07.2017.
 */

public class PixelImageView extends View {

    Matrix scaleMatrix;
    private Paint p;

    public PixelImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PixelImageView, 0, 0);

        try {
            bitmap = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.PixelImageView_image, R.drawable.pencil));
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
        calculateMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, scaleMatrix, p);
    }

    private Bitmap bitmap;
    void setImageBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
        calculateMatrix();
    }

    void calculateMatrix(){
        float scaleX = getWidth()/bitmap.getWidth();
        float scaleY = getHeight()/bitmap.getHeight();
        float scale = scaleX>scaleY?scaleY:scaleX;
        scaleMatrix.setScale(scale, scale);
    }

}
