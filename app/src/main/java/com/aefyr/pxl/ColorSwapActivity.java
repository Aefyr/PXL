package com.aefyr.pxl;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.aefyr.pxl.util.Utils;
import com.aefyr.pxl.custom.PixelImageView;

import java.io.File;

public class ColorSwapActivity extends AppCompatActivity {

    private PixelImageView pixelImageView;
    private Bitmap image;
    private ColorPicker colorPicker;

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

        colorPicker = new ColorPicker(getWindow(), color);

        colorPicker.useColorSwap(image, color, new ColorPicker.OnLivePreviewUpdateListener() {
            @Override
            public void onLivePreviewUpdate() {
                pixelImageView.invalidate();
            }
        });

        Switch livePreviewSwitch = (Switch) findViewById(R.id.livePreviewSwitch);

        colorPicker.applyColorSwap();
        if (colorPicker.isLivePreviewAcceptable()) {
            colorPicker.setLivePreviewEnabled(true);
            livePreviewSwitch.setChecked(true);
        } else colorPicker.setLivePreviewEnabled(false);


        livePreviewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                colorPicker.setLivePreviewEnabled(b);
                if (b) {
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

        findViewById(R.id.preciseSelectorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreciseColorPicker(colorPicker.getColor());
            }
        });

    }

    private ColorPickerH colorPickerH;
    private void openPreciseColorPicker(int currentColor){
        final AlertDialog colorEditDialog = new AlertDialog.Builder(this).setView(R.layout.color_picker_2).setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                colorPicker.setColor(colorPickerH.getColor(), false);
                colorPicker.applyColorSwap();
                pixelImageView.invalidate();
                colorPickerH = null;
            }
        }).setNegativeButton(R.string.cancel, null).setTitle(R.string.edit_color).create();
        colorEditDialog.show();
        colorPickerH = new ColorPickerH(colorEditDialog.getWindow(), currentColor);
    }


    @Override
    public void onBackPressed() {

        setResult(0);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.d("Destroyed!", "Ai ");
        colorPicker.destroySwapperIfNeeded();
        super.onDestroy();
    }
}
