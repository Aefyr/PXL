package com.aefyr.pxl.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.aefyr.pxl.R;

/**
 * Created by Aefyr on 12.07.2017.
 */

public class PixelImageView extends View {

    Matrix scaleMatrix;
    private Paint p;
    private float pixelScale;
    private boolean autoResize;
    private Drawable tBackground;

    private boolean r = false;
    private OnSizeChangedListener onSizeChangedListener;

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height);
    }

    public PixelImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PixelImageView, 0, 0);

        try {
            BitmapFactory.Options noInterpolationOptions = new BitmapFactory.Options();
            noInterpolationOptions.inScaled = false;

            bitmap = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.PixelImageView_image, R.drawable.pencil), noInterpolationOptions);
            autoResize = a.getBoolean(R.styleable.PixelImageView_autoResizeToMax, false);
            tBackground = a.getDrawable(R.styleable.PixelImageView_imageBackground);

        } finally {
            a.recycle();
        }

        initialize();
    }

    private void initialize() {
        scaleMatrix = new Matrix();
        p = new Paint();
        p.setAntiAlias(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (autoResize && !r) {
            resize();
            r = true;
        }
        calculateMatrix();
        if (onSizeChangedListener != null)
            onSizeChangedListener.onSizeChanged(w, h);
    }


    private Bitmap bitmap;

    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        calculateMatrix();
    }

    private void calculateMatrix() {
        scaleMatrix.reset();
        float scaleX = (float) getWidth() / (float) bitmap.getWidth();
        float scaleY = (float) getHeight() / (float) bitmap.getHeight();
        pixelScale = scaleX > scaleY ? scaleY : scaleX;
        scaleMatrix.setScale(pixelScale, pixelScale);
        float offsetX = (getWidth() - (bitmap.getWidth() * pixelScale)) * 0.5f;
        float offsetY = (getHeight() - (bitmap.getHeight() * pixelScale)) * 0.5f;
        scaleMatrix.postTranslate(offsetX, offsetY);

        if(tBackground!=null)
            tBackground.setBounds((int) offsetX, (int) offsetY, (int) (offsetX+bitmap.getWidth() * pixelScale), (int)(offsetY+bitmap.getHeight() * pixelScale));
    }

    private void resize() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        int max = p.y > p.x ? p.x : p.y;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = max;
        params.width = max;
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tBackground != null)
            tBackground.draw(canvas);
        canvas.drawBitmap(bitmap, scaleMatrix, p);
    }

    public float getPixelScale() {
        return pixelScale;
    }

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.onSizeChangedListener = onSizeChangedListener;
    }
}
