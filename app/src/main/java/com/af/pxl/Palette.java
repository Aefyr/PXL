package com.af.pxl;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Aefyr on 05.07.2017.
 */

class Palette {
    ArrayList<Integer> colors;
    int currentColor = 0;

    Palette(){
        colors = new ArrayList<>();
        colors.add(Color.RED);
    }

}
