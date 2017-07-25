package com.af.pxl;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Window;
import android.widget.SeekBar;

/**
 * Created by Aefyr on 17.06.2017.
 */

public class ColorPicker {

    private ColorPickerView hueView;
    private SeekBar hueBar;
    private ColorPickerView saturationView;
    private SeekBar saturationBar;
    private ColorPickerView valueView;
    private SeekBar valueBar;

    private ColorCircle newColorCircle;
    private ColorCircle oldColorCircle;

    float[] color = {0,0,0};

    public ColorPicker(ColorPickerView hueView, SeekBar hueBar, ColorPickerView saturationView, SeekBar saturationBar, ColorPickerView valueView, SeekBar valueBar, ColorCircle newColorCircle, int currentColor){
        this.hueView = hueView;
        this.hueBar = hueBar;
        this.saturationView = saturationView;
        this.saturationBar = saturationBar;
        this.valueView = valueView;
        this.valueBar = valueBar;

        this.newColorCircle = newColorCircle;

        setStartColor(currentColor);
        initialize();
    }

    public ColorPicker(Window colorPickerView, int startColor){
        hueView = (ColorPickerView) colorPickerView.findViewById(R.id.colorPickerHue);
        hueBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarHue);
        saturationView = (ColorPickerView) colorPickerView.findViewById(R.id.colorPickerSat);
        saturationBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarSat);
        valueView = (ColorPickerView) colorPickerView.findViewById(R.id.colorPickerVal);
        valueBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarVal);
        newColorCircle = (ColorCircle) colorPickerView.findViewById(R.id.newColor);
        oldColorCircle = (ColorCircle) colorPickerView.findViewById(R.id.oldColor);

        setStartColor(startColor);
        initialize();
    }

    //Hai, hai, this workflow sucks, but I gonna write a new ColorPicker analogue soon anyway
    private void setStartColor(int startColor){
        oldColorCircle.setColor(startColor);

        Color.colorToHSV(startColor, color);
        hueBar.setProgress((int) color[0]);
        saturationBar.setProgress((int)(color[1]*100));
        valueBar.setProgress((int)(color[2]*100));
    }

    private void initialize(){
        newColorCircle.setColor(Color.HSVToColor(color));
        saturationView.color = valueView.color = color;

        hueView.setMode(ColorPickerView.MODE.HUE);
        saturationView.setMode(ColorPickerView.MODE.SATURATION);
        valueView.setMode(ColorPickerView.MODE.VIBRANCE);

        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekBar == hueBar){
                    color[0] = i;
                }else if (seekBar == saturationBar){
                    color[1] = (float)i * 0.01f;
                }else if(seekBar == valueBar){
                    color[2] = (float)i * 0.01f;
                }

                updateColorViews();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        hueBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        saturationBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        valueBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        updateColorViews();
    }


    private void updateColorViews(){
        saturationView.invalidate();
        valueView.invalidate();
        newColorCircle.setColor(Color.HSVToColor(color));
        if(livePreview){
            applyColorSwap();
            listener.onLivePreviewUpdate();
        }
    }

    public int getColor(){
        return Color.HSVToColor(color);
    }

    //Color Swapping
    private boolean livePreview = false;
    private OnLivePreviewUpdateListener listener;
    private ColorSwapper colorSwapper;

    void useColorSwap(Bitmap bitmap, int colorToSwap, OnLivePreviewUpdateListener listener){
        this.listener = listener;
        colorSwapper = new ColorSwapper(bitmap, colorToSwap);
    }

    void setLivePreviewEnabled(boolean enabled){
        livePreview = enabled;
    }

    void applyColorSwap(){
        colorSwapper.swapColor(Color.HSVToColor(color));
    }

    boolean isLivePreviewAcceptable(){
        applyColorSwap();
        return colorSwapper.getDeltaTime() <= 60;
    }

    interface OnLivePreviewUpdateListener{
        void onLivePreviewUpdate();
    }
}
