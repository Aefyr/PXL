package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;

/**
 * Created by Aefyr on 17.06.2017.
 */

public class ColorPickerH {

    private ColorPickerViewH hueView;
    private SeekBar hueBar;
    private ColorPickerViewH saturationView;
    private SeekBar saturationBar;
    private ColorPickerViewH valueView;
    private SeekBar valueBar;

    private EditText editTextHue;
    private EditText editTextVal;
    private EditText editTextSat;
    private TextWatcher HWatcher;
    private TextWatcher SWatcher;
    private TextWatcher VWatcher;

    private ColorRect newColorCircle;
    private ColorRect oldColorCircle;

    float[] color = {0,0,0};

    public ColorPickerH(ColorPickerViewH hueView, SeekBar hueBar, ColorPickerViewH saturationView, SeekBar saturationBar, ColorPickerViewH valueView, SeekBar valueBar, ColorRect newColorCircle, int currentColor){
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

    public ColorPickerH(Window colorPickerView, int startColor){
        hueView = (ColorPickerViewH) colorPickerView.findViewById(R.id.colorPickerHue);
        hueBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarHue);
        saturationView = (ColorPickerViewH) colorPickerView.findViewById(R.id.colorPickerSat);
        saturationBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarSat);
        valueView = (ColorPickerViewH) colorPickerView.findViewById(R.id.colorPickerVal);
        valueBar = (SeekBar) colorPickerView.findViewById(R.id.seekBarVal);
        newColorCircle = (ColorRect) colorPickerView.findViewById(R.id.newColor);
        oldColorCircle = (ColorRect) colorPickerView.findViewById(R.id.oldColor);

        editTextHue = (EditText) colorPickerView.findViewById(R.id.editTextHue);
        editTextSat = (EditText) colorPickerView.findViewById(R.id.editTextSat);
        editTextVal = (EditText) colorPickerView.findViewById(R.id.editTextVal);

        setStartColor(startColor);
        initialize();
    }

    private void setStartColor(int startColor){
        oldColorCircle.setColor(startColor);

        Color.colorToHSV(startColor, color);
        hueBar.setProgress((int) color[0]);
        saturationBar.setProgress((int)(color[1]*100));
        valueBar.setProgress((int)(color[2]*100));

        editTextHue.setText((int) color[0]+"");
        editTextSat.setText((int) (color[1]*100f)+"");
        editTextVal.setText((int) (color[2]*100f)+"");
    }

    private void initialize(){
        newColorCircle.setColor(Color.HSVToColor(color));
        hueView.color = saturationView.color = valueView.color = color;

        hueView.setMode(ColorPickerViewH.Mode.HUE);
        saturationView.setMode(ColorPickerViewH.Mode.SATURATION);
        valueView.setMode(ColorPickerViewH.Mode.VIBRANCE);


        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekBar == hueBar){
                    color[0] = i;
                    editTextHue.setText(i+"");
                    editTextHue.setSelection(editTextHue.length());
                    updateColorViews(true, true, true);
                }else if (seekBar == saturationBar){
                    color[1] = (float)i * 0.01f;
                    editTextSat.setText(i+"");
                    editTextSat.setSelection(editTextSat.length());
                    updateColorViews(false, true, true);
                }else if(seekBar == valueBar){
                    color[2] = (float)i * 0.01f;
                    editTextVal.setText(i+"");
                    editTextVal.setSelection(editTextVal.length());
                    updateColorViews(false, true, true);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        createWatchers();
        editTextHue.addTextChangedListener(HWatcher);
        editTextSat.addTextChangedListener(SWatcher);
        editTextVal.addTextChangedListener(VWatcher);

        hueBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        saturationBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        valueBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        updateColorViews(true, true, true);
    }

    private static final String hexColorPattern = "[0123456789AaBbCcDdEeFf]*";
    private void createWatchers(){

        HWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int newVal = checkHSVEditableValue(editable, 360);


                color[0] = newVal;
                hueBar.setProgress(newVal);
                updateColorViews(true, true, true);
            }
        };

        SWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int newVal = checkHSVEditableValue(editable, 100);

                color[1] = newVal*0.01f;
                saturationBar.setProgress(newVal);
                updateColorViews(false, true, true);
            }
        };

        VWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int newVal = checkHSVEditableValue(editable, 100);


                color[2] = newVal*0.01f;
                valueBar.setProgress(newVal);
                updateColorViews(false, true, true);
            }
        };

    }

    private int checkHSVEditableValue(Editable editable, int maxValue){
        int newVal = 0;

        if(editable.length()==0){
            editable.replace(0, editable.length(), "0");
        }else
            newVal = Integer.parseInt(editable.toString());

        if(editable.length()>0&&newVal>maxValue) {
            editable.replace(0, editable.length(), maxValue+"");
            newVal = 360;
        }else if(editable.length()>1){
            if(editable.charAt(0)=='0')
                editable.replace(0, 1, "");
        }

        return newVal;
    }


    private void updateColorViews(boolean h, boolean s, boolean v){
        if(h)
            hueView.invalidate();
        if(s)
            saturationView.invalidate();
        if(v)
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
