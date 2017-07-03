package com.af.pxl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Aefyr on 03.07.2017.
 */

public class ToolPickView extends View {
    int width, height;
    int startWidth, startHeight;
    boolean startGet = false;

    int toolsCount = 3;

    public ToolPickView(Context context) {
        super(context);
    }

    public ToolPickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();
        height = getHeight();
        if(!startGet){
            startWidth = width;
            startHeight = height;
            initialize();
            showTools();
            startGet = true;
        }
        System.out.println("W:"+width+", H:"+height);
    }

    Bitmap[] tools;
    private void initialize(){
        tools = new Bitmap[3];
        Resources res = getResources();
        tools[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.pencil), startWidth, startWidth, false);
        tools[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.fill), startWidth, startWidth, false);
        tools[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.colorpick), startWidth, startWidth, false);
    }

    boolean actionWillBePerformed = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            actionWillBePerformed = true;
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            System.out.println("Cords: "+event.getX()+", "+event.getY()+", limits: "+getWidth()+", "+getHeight());
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(!actionWillBePerformed)
                return true;
            if(toolsShown)
                getClickedTool(event.getX(), event.getY());
            else showTools();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);

        if(toolsShown)
            drawTools(canvas);

        drawMainCircle(canvas);
        super.onDraw(canvas);
    }

    void drawMainCircle(Canvas canvas){
        Paint cPaint = new Paint();
        cPaint.setColor(Color.GRAY);
        cPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cPaint.setStrokeWidth(1);

        canvas.drawCircle((float)startWidth/2f, (float)startHeight/2f, (float) startWidth/2f-(float)startWidth/12f, cPaint);
        cPaint.setColor(Color.WHITE);
        canvas.drawRect((float)startWidth/2f - 30f , (float)startHeight/2f - 30f, (float)startWidth/2f +30f, (float)startHeight/2f + 30f, cPaint);
    }

    void drawTools(Canvas canvas){
        Paint rPaint = new Paint();
        rPaint.setColor(Color.MAGENTA);
        rPaint.setStrokeWidth(1);
        rPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawRoundRect(width/8f, startWidth/12f, width-width/8f, height, 100, 100, rPaint);

        float realHeightForTools = height - (startWidth/2f-(float)startWidth/12f)*2f -(float)startWidth/12f;
        float startYForToolsZone = height - realHeightForTools;

        float y = startYForToolsZone;
        float paddingY = startWidth/12f;
        y+= paddingY;

        rPaint.setColor(Color.BLACK);
        for(int i = 0; i < toolsCount; i++){
            canvas.drawBitmap(tools[i], 0,y, null);
            //canvas.drawRect(0, y, startWidth, y+startWidth, rPaint);
            y+= startWidth+paddingY;
        }
    }

    void getClickedTool(float x, float y){
        float realHeightForTools = height - (startWidth/2f-(float)startWidth/12f)*2f -(float)startWidth/12f;
        float startYForToolsZone = height - realHeightForTools;
        float paddingY = startWidth/12f;

        int clickedItem = (int) ((y - startYForToolsZone)/(startWidth+paddingY));
        if(y<startYForToolsZone)
            hideTools();

        System.out.println("ClickedItem="+clickedItem);
    }

    boolean toolsShown = false;

    void showTools(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        float paddingY = startWidth/12f;
        params.height += paddingY*3+width*3;
        setLayoutParams(params);
        toolsShown = true;
    }

    void hideTools(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = startHeight;
        setLayoutParams(params);
        toolsShown = false;
    }
}
