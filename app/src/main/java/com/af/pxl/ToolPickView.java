package com.af.pxl;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.RelativeLayout;

/**
 * Created by Aefyr on 03.07.2017.
 */

public class ToolPickView extends View {
    int width, height;
    int startWidth, startHeight;
    boolean startGet = false;

    int toolsCount = 6;
    int currentTool = 0;

    int circleColor;
    int barColor;
    int circleBorderColor;
    int barBorderColor;

    int toolBitmapSizeReducedBy;
    float offsetBetweenTools;

    ToolSettingsManager manager;

    public ToolPickView(Context context) {
        super(context);
    }

    public ToolPickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void setToolSettingsManager(ToolSettingsManager manager){
        this.manager = manager;
        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.PENCIL);
        manager.hide();
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
        if(Build.VERSION.SDK_INT>=21){
            setOutlineProvider(new ToolPickOutlineProvider());
        }
        System.out.println("W:"+width+", H:"+height);
    }

    @TargetApi(21)
    private class ToolPickOutlineProvider extends ViewOutlineProvider{

        @Override
        public void getOutline(View view, Outline outline) {

            outline.setRoundRect(startWidth/12, -32, startWidth-startWidth/12, startHeight-startWidth/12, 25);
        }
    }

    Bitmap[] tools;
    private void initialize(){
        toolBitmapSizeReducedBy = startWidth/3;
        offsetBetweenTools = startWidth/32f;

        tools = new Bitmap[toolsCount];
        Resources res = getResources();
        tools[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.pencil), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.eraser), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.shapes), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.colorpick), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.fill), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
        tools[5] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.colorswap), startWidth-toolBitmapSizeReducedBy, startWidth-toolBitmapSizeReducedBy, false);
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

        //canvas.drawCircle((float)startWidth/2f, (float)startHeight/2f, (float) startWidth/2f-(float)startWidth/12f, cPaint);
        //canvas.drawCircle(startWidth/4, startHeight/4, startWidth-startWidth/32f-startWidth/4, cPaint);
        if(Build.VERSION.SDK_INT >= 21)
            canvas.drawRoundRect(startWidth/12f, -32, startWidth-startWidth/12f, startHeight-startWidth/12f, 25, 25, cPaint);
        else
            canvas.drawRect(startWidth/12f, -32, startWidth-startWidth/12f, startHeight-startWidth/12f, cPaint);


        cPaint.setColor(circleBorderColor);
        cPaint.setStyle(Paint.Style.STROKE);
        //canvas.drawCircle((float)startWidth/2f, (float)startHeight/2f, (float) startWidth/2f-(float)startWidth/12f, cPaint);
        //canvas.drawCircle(startWidth/4, startHeight/4, startWidth-startWidth/32f-startWidth/4, cPaint);
        if(Build.VERSION.SDK_INT < 21)
            canvas.drawRect(startWidth/12f, -32, startWidth-startWidth/12f, startHeight-startWidth/12f, cPaint);

        canvas.drawBitmap(tools[currentTool],toolBitmapSizeReducedBy/2, toolBitmapSizeReducedBy/2  - startWidth/24f, null);
        //canvas.drawRect((float)startWidth/2f - 30f , (float)startHeight/2f - 30f, (float)startWidth/2f +30f, (float)startHeight/2f + 30f, cPaint);
    }

    void drawTools(Canvas canvas){
        cPaint.setColor(circleColor);
        cPaint.setStrokeWidth(startWidth/32f);
        cPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        if(Build.VERSION.SDK_INT>=21)
            canvas.drawRoundRect(width/8f, startWidth/12f, width-width/8f, height - startWidth/64f, 50, 50, cPaint);
        else
            canvas.drawRect(width/8f, startWidth/12f + startWidth/2f, width-width/8f, height - startWidth/64f, cPaint);


        cPaint.setColor(barColor);
        cPaint.setStyle(Paint.Style.STROKE);

        if(Build.VERSION.SDK_INT>=21)
            canvas.drawRoundRect(width/8f, startWidth/12f, width-width/8f, height - startWidth/64f, 50, 50, cPaint);
        else
            canvas.drawRect(width/8f, startWidth/12f + startWidth/2f, width-width/8f, height - startWidth/64f, cPaint);

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
            if(currentTool == clickedItem)
                return;

            currentTool = clickedItem;
            if(aps!=null){
                switch (currentTool){
                    case 0:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.PENCIL);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.PENCIL);
                        invalidate();
                        break;
                    case 1:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.ERASER);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.ERASER);
                        invalidate();
                        break;
                    case 2:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.MULTISHAPE);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.MULTISHAPE);
                        invalidate();
                        break;
                    case 3:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.COLOR_PICK);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.COLOR_PICK);
                        hideTools();
                        break;
                    case 4:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.FLOOD_FILL);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.FLOOD_FILL);
                        hideTools();
                        break;
                    case 5:
                        aps.setTool(AdaptivePixelSurfaceH.Tool.COLOR_SWAP);
                        manager.notifyToolPicked(AdaptivePixelSurfaceH.Tool.COLOR_SWAP);
                        hideTools();
                        break;
                }
            }

            //hideTools();
            System.out.println("ClickedItem=" + clickedItem);
        }
    }

    boolean toolsShown = false;

    void showTools(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height += offsetBetweenTools*toolsCount+width*toolsCount;
        setLayoutParams(params);
        manager.show();
        toolsShown = true;
    }

    void hideTools(){
        if(!toolsShown)
            return;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.height = startHeight;
        setLayoutParams(params);
        manager.hide();
        toolsShown = false;
    }

    boolean shown(){
        return toolsShown;
    }

    AdaptivePixelSurfaceH aps;

    void setAps(final AdaptivePixelSurfaceH aps){
        this.aps = aps;
    }
}
