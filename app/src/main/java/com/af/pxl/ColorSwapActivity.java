package com.af.pxl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ColorSwapActivity extends AppCompatActivity {

    PixelImageView pixelImageView;
    Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_swap);
        final Intent intent = getIntent();

        String path = intent.getStringExtra("path");
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inMutable = true;
        image = BitmapFactory.decodeFile(path, o);

        pixelImageView = (PixelImageView) findViewById(R.id.pxlImageView);
        pixelImageView.setImageBitmap(image);

        int color = intent.getIntExtra("color", Color.RED);

        final ColorPicker colorPicker = new ColorPicker(getWindow(), color);

        colorPicker.useColorSwap(image, color, new ColorPicker.OnLivePreviewUpdateListener() {
            @Override
            public void onLivePreviewUpdate() {
                pixelImageView.invalidate();
            }
        });

        Switch livePreviewSwitch = (Switch) findViewById(R.id.livePreviewSwitch);

        colorPicker.applyColorSwap();
        if(colorPicker.isLivePreviewAcceptable()) {
            colorPicker.setLivePreviewEnabled(true);
            livePreviewSwitch.setChecked(true);
        }
        else colorPicker.setLivePreviewEnabled(false);


        livePreviewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                colorPicker.setLivePreviewEnabled(b);
                if(b) {
                    colorPicker.applyColorSwap();
                    pixelImageView.invalidate();
                }
            }
        });

        findViewById(R.id.colorPickButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPicker.applyColorSwap();
                File t = new File(getFilesDir(), "r.pxl");
                Utils.saveBitmap(image, t);
                image.recycle();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("path", t.getAbsolutePath());
                setResult(1, resultIntent);
                finish();
            }
        });

        findViewById(R.id.csCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(0);
                finish();
            }
        });
    }



    @Override
    public void onBackPressed() {

        setResult(0);
        finish();
        super.onBackPressed();
    }
}
