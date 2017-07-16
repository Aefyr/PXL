package com.af.pxl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;

public class BitmapsMergeActivity extends AppCompatActivity {


    private int mode;
    private PixelImageView piv;
    private Bitmap b;
    private Bitmap image;
    private Canvas bC;
    private Matrix m;
    int offsetX;
    int offsetY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmaps_merge);

        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 999);
        String path = intent.getStringExtra("path");

        piv = (PixelImageView) findViewById(R.id.pxlImageView);

        image = BitmapFactory.decodeFile(path);

        b = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bC = new Canvas(b);
        m = new Matrix();
        setupButtons();
        setupTouch();

        piv.setImageBitmap(b);
        redraw();
    }

    private void setupButtons(){
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean needRedraw = true;
                switch (view.getId()){
                    case R.id.ibu:
                        offsetY--;
                        break;
                    case R.id.ibr:
                        offsetX++;
                        break;
                    case R.id.ibb:
                        offsetY++;
                        break;
                    case R.id.ibl:
                        offsetX--;
                        break;
                    case R.id.done:
                        done();
                        needRedraw = false;
                        break;
                    case R.id.cancel:
                        cancelled();
                        needRedraw = false;
                        break;
                }
                if(needRedraw)
                    redraw();
            }
        };

        findViewById(R.id.ibb).setOnClickListener(onClickListener);
        findViewById(R.id.ibr).setOnClickListener(onClickListener);
        findViewById(R.id.ibl).setOnClickListener(onClickListener);
        findViewById(R.id.ibu).setOnClickListener(onClickListener);

        findViewById(R.id.done).setOnClickListener(onClickListener);
        findViewById(R.id.cancel).setOnClickListener(onClickListener);
    }

    void redraw(){
        m.setTranslate(offsetX, offsetY);
        bC.drawColor(Color.WHITE);
        bC.drawBitmap(image, m, null);
        piv.invalidate();
    }


    private void done(){
        File p = new File(getFilesDir(), "td.pxl");
        Utils.saveBitmap(b, p);
        Intent i = new Intent();
        i.putExtra("path", p.getAbsolutePath());
        setResult(1, i);
        finish();
    }

    private void cancelled(){
        setResult(0);
        finish();
    }

    @Override
    public void onBackPressed() {
        cancelled();
        super.onBackPressed();
    }

    private float cX, cY;
    private float pX, pY;
    private int t = 8;
    private void setupTouch(){
        piv.setOnSizeChangedListener(new PixelImageView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                t = (int) piv.getPixelScale();
            }
        });
        piv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    pX = x;
                    pY = y;
                    return true;
                }

                if(motionEvent.getAction()==MotionEvent.ACTION_MOVE){
                    cX+=x-pX;
                    cY+=y-pY;

                    if(cX>t){
                        offsetX++;
                        redraw();
                        cX = 0;
                    }
                    if(cX<-t){
                        offsetX--;
                        redraw();
                        cX = 0;
                    }
                    if(cY>t){
                        offsetY++;
                        redraw();
                        cY = 0;
                    }
                    if(cY<-t){
                        offsetY--;
                        redraw();
                        cY = 0;
                    }

                    pX = x;
                    pY = y;
                }

                return false;
            }
        });
    }
}
