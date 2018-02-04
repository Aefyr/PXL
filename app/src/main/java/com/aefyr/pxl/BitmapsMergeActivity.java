package com.aefyr.pxl;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.aefyr.pxl.common.Ruler;
import com.aefyr.pxl.custom.PixelImageView;
import com.aefyr.pxl.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;

public class BitmapsMergeActivity extends AppCompatActivity {


    static final int MODE_MERGE = 13221;
    static final int MODE_TRANSLATE = 13220;

    private int mode;
    private boolean transparentBackground;
    private PixelImageView piv;
    private Bitmap b;
    private Bitmap o;
    private Bitmap image;
    private Canvas bC;
    private Matrix m;
    private float offsetX;
    private float offsetY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmaps_merge);

        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 999);

        if (mode == MODE_TRANSLATE) {
            transparentBackground = intent.getBooleanExtra("transparentBackground", false);
        } else if (mode == MODE_MERGE) {
            try {
                final int dLimit = Ruler.getInstance(this).maxDimensionSize();
                BitmapFactory.Options sizeCheckOptions = new BitmapFactory.Options();
                sizeCheckOptions.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(intent.getStringExtra("uri"))), new Rect(0, 0, 0, 0), sizeCheckOptions);
                if (sizeCheckOptions.outHeight > dLimit || sizeCheckOptions.outWidth > dLimit) {
                    AlertDialog errorDialog = new AlertDialog.Builder(this).setMessage(String.format(getString(R.string.imported_bitmap_too_big), dLimit, dLimit)).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            cancelled();
                        }
                    }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cancelled();
                        }
                    }).create();
                    errorDialog.show();
                    return;
                }

                o = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(intent.getStringExtra("uri"))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.toaster(this, getString(R.string.error));
                finish();
            }

        }

        String path = intent.getStringExtra("path");
        image = BitmapFactory.decodeFile(path);

        piv = (PixelImageView) findViewById(R.id.pxlImageView);
        piv.setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(this).getInt("background_color", -3343361));


        b = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bC = new Canvas(b);
        m = new Matrix();
        setupButtons();
        setupTouch();

        piv.setImageBitmap(b);

        if(savedInstanceState!=null){
            offsetX = savedInstanceState.getFloat("offsetX", 0);
            offsetY = savedInstanceState.getFloat("offsetY", 0);

        }
        redraw();
    }

    private void setupButtons() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean needRedraw = true;
                switch (view.getId()) {
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
                if (needRedraw)
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

    void redraw() {
        m.setTranslate(offsetX, offsetY);
        if (mode == MODE_TRANSLATE) {
            if (!transparentBackground)
                bC.drawColor(Color.WHITE);
            else
                bC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            bC.drawBitmap(image, m, null);
        } else if (mode == MODE_MERGE) {
            bC.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            bC.drawBitmap(image, 0, 0, null);
            bC.drawBitmap(o, m, null);
        }
        piv.invalidate();
    }


    private void done() {
        File p = new File(getFilesDir(), "td.pxl");
        Utils.saveBitmap(b, p);
        Intent i = new Intent();
        i.putExtra("path", p.getAbsolutePath());
        setResult(1, i);
        finish();
    }

    private void cancelled() {
        setResult(0);
        finish();
    }

    @Override
    public void onBackPressed() {
        cancelled();
        super.onBackPressed();
    }

    private float pX, pY;
    private int prevPointerCount = 0;

    private void setupTouch() {
        piv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = 0;
                float y = 0;
                int pointerCount = motionEvent.getPointerCount();
                if (pointerCount > 1) {
                    for (int p = 0; p < pointerCount; p++) {
                        x += motionEvent.getX(p);
                        y += motionEvent.getY(p);
                    }
                    x /= pointerCount;
                    y /= pointerCount;
                } else {
                    x = motionEvent.getX();
                    y = motionEvent.getY();
                }

                if (prevPointerCount != pointerCount || motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    pX = x;
                    pY = y;
                }


                offsetX+=(x-pX)/piv.getPixelScale();
                offsetY+=(y-pY)/piv.getPixelScale();

                pX = x;
                pY = y;

                prevPointerCount = pointerCount;

                redraw();
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("offsetX", offsetX);
        outState.putFloat("offsetY", offsetY);
    }
}
