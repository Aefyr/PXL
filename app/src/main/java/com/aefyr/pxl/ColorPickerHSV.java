package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;

import com.aefyr.pxl.custom.ColorRect;
import com.aefyr.pxl.custom.HSVSeekBar;

/**
 * Created by Aefyr on 17.06.2017.
 */

public class ColorPickerHSV {

    private HSVSeekBar hue;
    private HSVSeekBar sat;
    private HSVSeekBar val;

    private EditText editTextHue;
    private EditText editTextVal;
    private EditText editTextSat;
    private TextWatcher HWatcher;
    private TextWatcher SWatcher;
    private TextWatcher VWatcher;

    private ColorRect newColorPreview;
    private ColorRect oldColorPreview;

    float[] color = {0, 0, 0};

    public ColorPickerHSV(Window colorPickerView, int startColor) {
        hue = colorPickerView.findViewById(R.id.hue);
        sat = colorPickerView.findViewById(R.id.sat);
        val = colorPickerView.findViewById(R.id.val);

        newColorPreview = colorPickerView.findViewById(R.id.newColor);
        oldColorPreview = colorPickerView.findViewById(R.id.oldColor);

        editTextHue = colorPickerView.findViewById(R.id.editTextHue);
        editTextSat = colorPickerView.findViewById(R.id.editTextSat);
        editTextVal = colorPickerView.findViewById(R.id.editTextVal);

        setColorI(startColor, true);
        initialize();
    }

    public void setColor(int color){
        setColorI(color, false);
    }

    private void setColorI(int color, boolean initial) {
        if(initial)
            oldColorPreview.setColor(color);

        Color.colorToHSV(color, this.color);
        sync();

        editTextHue.setText(String.valueOf((int) this.color[0]));
        editTextSat.setText(String.valueOf((int) (this.color[1] * 100f)));
        editTextVal.setText(String.valueOf((int) (this.color[2] * 100f)));
    }

    private void initialize() {
        newColorPreview.setColor(Color.HSVToColor(color));

        HSVSeekBar.OnPositionUpdateListener listener = new HSVSeekBar.OnPositionUpdateListener() {
            @Override
            public void onPositionChanged(HSVSeekBar seekBar, int newPosition) {
                if(seekBar == hue) {
                    color[0] = newPosition;

                    editTextHue.setText(String.valueOf(newPosition));
                    editTextHue.setSelection(editTextHue.length());
                }else if(seekBar == sat){
                    color[1] = (float) newPosition * 0.01f;

                    editTextSat.setText(String.valueOf(newPosition));
                    editTextSat.setSelection(editTextSat.length());
                }else if(seekBar == val){
                    color[2] = (float) newPosition * 0.01f;

                    editTextVal.setText(String.valueOf(newPosition));
                    editTextVal.setSelection(editTextVal.length());
                }

                sync();
            }
        };


        createWatchers();
        editTextHue.addTextChangedListener(HWatcher);
        editTextSat.addTextChangedListener(SWatcher);
        editTextVal.addTextChangedListener(VWatcher);

        hue.setOnPositionUpdateListener(listener);
        sat.setOnPositionUpdateListener(listener);
        val.setOnPositionUpdateListener(listener);
    }

    private void createWatchers() {

        HWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int newVal = checkValue(editable, 360);
                color[0] = newVal;

                sync();
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
                int newVal = checkValue(editable, 100);
                color[1] = newVal * 0.01f;

                sync();
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
                int newVal = checkValue(editable, 100);
                color[2] = newVal * 0.01f;

                sync();
            }
        };

    }

    private int checkValue(Editable editable, int maxValue) {
        int newVal = 0;

        if (editable.length() == 0) {
            editable.replace(0, editable.length(), "0");
        } else
            newVal = Integer.parseInt(editable.toString());

        if (editable.length() > 0 && newVal > maxValue) {
            editable.replace(0, editable.length(), maxValue + "");
            newVal = 360;
        } else if (editable.length() > 1) {
            if (editable.charAt(0) == '0')
                editable.replace(0, 1, "");
        }

        return newVal;
    }


    private void sync() {
        hue.setColor(color);
        sat.setColor(color);
        val.setColor(color);

        newColorPreview.setColor(Color.HSVToColor(color));
        if (livePreview) {
            applyColorSwap();
            listener.onLivePreviewUpdate();
        }
    }

    public int getColor() {
        return Color.HSVToColor(color);
    }

    //Color Swapping
    private boolean livePreview = false;
    private ColorPicker.OnLivePreviewUpdateListener listener;
    private ColorSwapperH colorSwapperH;

    void useColorSwap(Bitmap bitmap, int colorToSwap, ColorPicker.OnLivePreviewUpdateListener listener) {
        this.listener = listener;
        colorSwapperH = new ColorSwapperH(hue.getContext(), bitmap, colorToSwap, Color.HSVToColor(color));
    }

    void setLivePreviewEnabled(boolean enabled) {
        livePreview = enabled;
    }

    void applyColorSwap() {
        colorSwapperH.swapTo(Color.HSVToColor(color));
    }

    boolean isLivePreviewAcceptable() {
        return colorSwapperH.getDeltaTime() <= 80;
    }

    interface OnLivePreviewUpdateListener {
        void onLivePreviewUpdate();
    }

    void destroySwapperIfNeeded(){
        colorSwapperH.destroy();
    }
}
