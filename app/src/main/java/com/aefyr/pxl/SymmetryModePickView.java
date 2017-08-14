package com.aefyr.pxl;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Aefyr on 14.07.2017.
 */

public class SymmetryModePickView extends View {
    private Bitmap[] bitmaps;
    private int gap;
    private int bitmapSize;
    private Paint paint;
    private int cellSize;
    int selected = 0;

    private int BORDER_COLOR;
    private int FILL_COLOR;
    private int BORDER_COLOR_S;
    private int FILL_COLOR_S;

    private boolean ready = false;

    private OnItemClickedListener listener;

    interface OnItemClickedListener{
        void onItemClicked(int id);
    }

    public SymmetryModePickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SymmetryModePickView, 0, 0);

        try {
            gap = a.getDimensionPixelSize(R.styleable.SymmetryModePickView_gapSize, 16);
            bitmapSize = a.getDimensionPixelSize(R.styleable.SymmetryModePickView_bitmapSize, 128);
        }finally {
            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(!ready)
            initialize();
        else
            adaptSize();
    }


    private void initialize(){
        prepareBitmaps();
        adaptSize();
        BORDER_COLOR = Color.parseColor("#ffb0bec5");
        FILL_COLOR = Color.parseColor("#fffafafa");
        BORDER_COLOR_S = Color.parseColor("#ff29b6f6");
        FILL_COLOR_S = Color.parseColor("#ffcfd8dc");
        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(gap/2);
        setVisibility(GONE);
        ready = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long start = System.currentTimeMillis();
        super.onDraw(canvas);
        //canvas.drawColor(Color.RED);
        int x = gap;

        for(int i = 0; i<bitmaps.length; i++){

            if(i==selected)
                paint.setColor(FILL_COLOR_S);
            else
                paint.setColor(FILL_COLOR);

            paint.setStyle(Paint.Style.FILL);

            if(Build.VERSION.SDK_INT>=21)
                canvas.drawRoundRect(x, gap, x+bitmapSize, gap+bitmapSize, 16, 16, paint);
            else
                canvas.drawRect(x, gap, x+bitmapSize, gap+bitmapSize, paint);

            canvas.drawBitmap(bitmaps[i], x, gap, paint);

            if(i==selected)
                paint.setColor(BORDER_COLOR_S);
            else
                paint.setColor(BORDER_COLOR);

            paint.setStyle(Paint.Style.STROKE);

            if(Build.VERSION.SDK_INT>=21)
                canvas.drawRoundRect(x, gap, x+bitmapSize, gap+bitmapSize, 16, 16, paint);
            else
                canvas.drawRect(x, gap, x+bitmapSize, gap+bitmapSize, paint);

            x+= bitmapSize+gap;
        }

        System.out.println("EMVE in "+(System.currentTimeMillis()-start)+"ms");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            return true;

        if(event.getAction() == MotionEvent.ACTION_UP){

            if(event.getX()>getWidth()||event.getX()<0)
                return true;
            int clickedPosition = (int)event.getX()/cellSize;

            if(listener!=null)
                listener.onItemClicked((int) (event.getX()/(getWidth()/3)));
            System.out.println("Clicked: "+clickedPosition);

            if(clickedPosition==selected)
                return true;

            selected = clickedPosition;

            return true;
        }

        return super.onTouchEvent(event);
    }

    void setOnItemClickedListener(OnItemClickedListener listener){
        this.listener = listener;
    }

    private void adaptSize(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = bitmapSize+gap*2;
        params.width = ((bitmapSize+gap)*bitmaps.length)+gap;
        setLayoutParams(params);
        if(!ready)
            setX(getX()-getWidth());
        cellSize = (getWidth()/bitmaps.length);
    }

    private void prepareBitmaps(){
        bitmaps = new Bitmap[3];
        Resources res = getResources();
        bitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.symmetryoff), bitmapSize, bitmapSize, false);
        bitmaps[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.symmetryh), bitmapSize, bitmapSize, false);
        bitmaps[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.symmetryv), bitmapSize, bitmapSize, false);
    }
}
