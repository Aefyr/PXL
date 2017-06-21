package com.af.pxl;

import android.widget.SeekBar;

/**
 * Created by Peter on 17.06.2017.
 */

public class ColorPicker {

    ColorPickerView hueView;
    SeekBar hueBar;
    ColorPickerView saturationView;
    SeekBar saturationBar;
    ColorPickerView valueView;
    SeekBar valueBar;

    ColorView colorView;

    float[] color = {180,0.5f,0.5f};

    public ColorPicker(ColorPickerView hueView, SeekBar hueBar, ColorPickerView saturationView, SeekBar saturationBar, ColorPickerView valueView, SeekBar valueBar, ColorView colorView){
        this.hueView = hueView;
        this.hueBar = hueBar;
        this.saturationView = saturationView;
        this.saturationBar = saturationBar;
        this.valueView = valueView;
        this.valueBar = valueBar;

        this.colorView = colorView;

        initialize();
    }

    void initialize(){
        colorView.color = saturationView.color = valueView.color = color;

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

                //System.out.println("NEW COLOR HSV = "+color[0]+", "+color[1]+", "+color[2]);

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
    }

    void updateColorViews(){
        saturationView.invalidate();
        valueView.invalidate();
        colorView.invalidate();
    }
}
