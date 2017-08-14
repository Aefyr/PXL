package com.aefyr.pxl;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Aefyr on 05.07.2017.
 */

class Palette {
    ArrayList<PaletteColor> colors;
    int currentColor = 0;

    Palette(){
        colors = new ArrayList<>();
        colors.add(new PaletteColor(Color.RED));
        colors.get(currentColor).setSelected(true);
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));
        colors.add(new PaletteColor(Color.RED));

    }

    void setColorSelected(int colorId){
        if(currentColor == colorId)
            return;
        colors.get(currentColor).setSelected(false);
        colors.get(colorId).setSelected(true);
        currentColor = colorId;
    }


}
