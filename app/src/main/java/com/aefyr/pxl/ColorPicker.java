package com.aefyr.pxl;

import android.graphics.Bitmap;
import android.view.Window;

import com.aefyr.pxl.custom.ColorRect;
import com.aefyr.pxl.experimental.SimpleColorPickerH;

/**
 * Created by Aefyr on 17.06.2017.
 */

public class ColorPicker {

    private SimpleColorPickerH simpleColorPickerH;

    private ColorRect newColor;
    private ColorRect oldColor;

    private int color;

    public ColorPicker(Window colorPickerView, int startColor) {
        simpleColorPickerH = colorPickerView.findViewById(R.id.simpleColorPickerH);
        newColor = colorPickerView.findViewById(R.id.newColor);
        oldColor = colorPickerView.findViewById(R.id.oldColor);

        initialize();
        setColor(startColor, true);
    }

    //Hai, hai, this workflow sucks, but I gonna write a new ColorPicker analogue soon anyway
    void setColor(int color, boolean startColor) {
        this.color = color;

        if(startColor) {
            oldColor.setColor(color);
            simpleColorPickerH.setColor(color);
        }

        newColor.setColor(color);
    }

    private void initialize() {
        simpleColorPickerH.setOnColorPickListener(new SimpleColorPickerH.OnColorPickListener() {
            @Override
            public void onColorPicked(int color) {
                setColor(color, false);
                if (livePreview) {
                    applyColorSwap();
                    listener.onLivePreviewUpdate();
                }
            }
        });
    }


    public int getColor() {
        return color;
    }

    //Color Swapping
    private boolean livePreview = false;
    private OnLivePreviewUpdateListener listener;
    private ColorSwapperH colorSwapperH;

    void useColorSwap(Bitmap bitmap, int colorToSwap, OnLivePreviewUpdateListener listener) {
        this.listener = listener;
        colorSwapperH = new ColorSwapperH(simpleColorPickerH.getContext(), bitmap, colorToSwap, getColor());
    }

    void setLivePreviewEnabled(boolean enabled) {
        livePreview = enabled;
    }

    void applyColorSwap() {
        colorSwapperH.swapTo(getColor());
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
