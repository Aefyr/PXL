package com.aefyr.pxl;

/**
 * Created by Aefyr on 08.07.2017.
 */

public class PaletteColor {
    int color = android.graphics.Color.RED;
    boolean isSelected =false;

    PaletteColor(int color){
        this.color = color;
    }

    void setSelected(boolean selected){
        this.isSelected = selected;
    }

}
