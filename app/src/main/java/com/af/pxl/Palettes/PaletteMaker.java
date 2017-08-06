package com.af.pxl.Palettes;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.af.pxl.R;
import com.af.pxl.Utils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Aefyr on 06.08.2017.
 */

public class PaletteMaker {

    public static Palette2 heyPaletteMaker(Bitmap bitmap, Resources r){
        String name = r.getString(R.string.extracted);
        if(!PaletteUtils.isNameAvailable(name)){
            int i = 1;
            while(!PaletteUtils.isNameAvailable(name+" "+i))
                i++;
            name += " "+i;
        }
        Palette2 palette = new Palette2(name);
        ArrayList<Integer> colors = palette.getColors();

        Bitmap actualBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, bitmap.getWidth()>48&&bitmap.getHeight()>48);
        int[] pixels = new int[48*48];
        actualBitmap.getPixels(pixels, 0, 48, 0, 0, 48, 48);

        int[] realColors = new int[16];


        HashSet<Integer> uC = new HashSet<>();
        for(int c: pixels){
            if(!uC.contains(c)&&uC.size()<16)
                uC.add(c);
        }



        int i = 0;
        for(int c: uC){
            realColors[i++] =c;
        }

        if(uC.size()<16){
            for(i = uC.size()-1; i<16; i++)
                realColors[i]=Color.WHITE;
        }

        for(int color: pixels){
            tryAddingColor(realColors, color);
        }

        colors.clear();
        for(int c: realColors){
            colors.add(c);
        }

        PaletteUtils.savePalette(palette);
        return palette;
    }

    private static void tryAddingColor(int[] colors, int newColor){
        float lowestDifference = 1f;
        int lowestColor = -1;

        for(int c: colors){
            float difference = Utils.colorsDifference(c, newColor);
            if(difference<lowestDifference)
                lowestDifference = difference;
        }

        for(int i = 0; i<colors.length; i++){
            int c = colors[i];
            for(int a = 0; a<colors.length; a++){
                if(a==i)
                    continue;
                float difference = Utils.colorsDifference(c, colors[a]);
                if(difference<lowestDifference){
                    lowestDifference = difference;
                    lowestColor = i;
                }
            }
        }

        if(lowestColor!=-1){
            colors[lowestColor] = newColor;
        }
    }
}
