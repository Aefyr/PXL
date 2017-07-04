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
    int currentTool = 0;

    int circleColor;
    int barColor;
    int circleBorderColor;
    int barBorderColor;

    int toolBitmapSizeReducedBy;
    float offsetBetweenTools;

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
            startGet = true;
        }
        System.out.println("W:"+width+", H:"+height);
    }

    Bitmap[] tools;
    private void initialize(){
        toolBitmapSizeReducedBy = startWidth/3;
        offsetBetweenTools = startWidth/32f;

        tools = new Bitmap[3];
        Resources res = getResources();
        tools[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.pencil), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.fill), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.colorpick), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        circleColor = Color.parseColor("#FFFAFAFA");
        barColor = Color.parseColor("#FFE0E0E0");
        circleBorderColor = Color.parseColor("#FFBDBDBD");
        barBorderColor = Color.parseColor("#FF9E9E9E");
        cPaint = new Paint();

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
        //canvas.drawColor(Color.BLUE);

        if(toolsShown)
            drawTools(canvas);

        drawMainCircle(canvas);
        super.onDraw(canvas);
    }

    Paint cPaint;
    void drawMainCircle(Canvas canvas){

        cPaint.setColor(circleColor);
        cPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cPaint.setStrokeWidth(startWidth/32f);

        canvas.drawCircle((float)startWidth/2f, (float)startHeight/2f, (float) startWidth/2f-(float)startWidth/12f, cPaint);

        cPaint.setColor(circleBorderColor);
        cPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle((float)startWidth/2f, (float)startHeight/2f, (float) startWidth/2f-(float)startWidth/12f, cPaint);
        canvas.drawBitmap(tools[currentTool],toolBitmapSizeReducedBy/2, toolBitmapSizeReducedBy/2, null);
        //canvas.drawRect((float)startWidth/2f - 30f , (float)startHeight/2f - 30f, (float)startWidth/2f +30f, (float)startHeight/2f + 30f, cPaint);
    }

    void drawTools(Canvas canvas){
        cPaint.setColor(barColor);
        cPaint.setStrokeWidth(startWidth/32f);
        cPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawRoundRect(width/8f, startWidth/12f, width-width/8f, height - startWidth/64f, 100, 100, cPaint);

        cPaint.setColor(barBorderColor);
        cPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(width/8f, startWidth/12f, width-width/8f, height- startWidth/64f, 100, 100, cPaint);

        float realHeightForTools = height - (startWidth/2f-(float)startWidth/12f)*2f -(float)startWidth/12f;
        float startYForToolsZone = height - realHeightForTools;

        float y = startYForToolsZone;
        y+= offsetBetweenTools;

        cPaint.setColor(Color.BLACK);
        for(int i = 0; i < toolsCount; i++){
            canvas.drawBitmap(tools[i], toolBitmapSizeReducedBy/2,y, null);
            //canvas.drawRect(0, y, startWidth, y+startWidth, rPaint);
            y+= startWidth+offsetBetweenTools;
        }
    }

    void getClickedTool(float x, float y){
        float realHeightForTools = height - (startWidth/2f-(float)startWidth/12f)*2f -(float)startWidth/12f;
        float startYForToolsZone = height - realHeightForTools;

        int clickedItem = (int) ((y - startYForToolsZone)/(startWidth+offsetBetweenTools));
        if(y<startYForToolsZone)
            hideTools();
        else {
            currentTool = clickedItem;
            if(aps!=null){
                switch (currentTool){
                    case 0:
                        aps.currentTool = AdaptivePixelSurface.Tool.PENCIL;
                        break;
                    case 1:
                        aps.currentTool = AdaptivePixelSurface.Tool.FLOOD_FILL;
                        break;
                    case 2:
                        aps.currentTool = AdaptivePixelSurface.Tool.COLOR_PICK;
                        break;
                }
            }
            hideTools();
            System.out.println("ClickedItem=" + clickedItem);
        }
    }

    boolean toolsShown = false;

    void showTools(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height += offsetBetweenTools*3+width*3;
        setLayoutParams(params);
        toolsShown = true;
    }

    void hideTools(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = startHeight;
        setLayoutParams(params);
        toolsShown = false;
    }

    AdaptivePixelSurface aps;

    void setAps(final AdaptivePixelSurface aps){
        this.aps = aps;
        aps.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(toolsShown)
                    hideTools();
                return aps.onTouchEvent(motionEvent);
            }
        });
    }
}
