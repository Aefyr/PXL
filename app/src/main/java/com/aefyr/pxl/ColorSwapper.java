package com.aefyr.pxl;

import android.graphics.Bitmap;

import java.util.HashSet;

/**
 * Created by Aefyr on 11.07.2017.
 */

public class ColorSwapper {
    private Bitmap bitmap;
    private int[] pixels;
    private HashSet<Integer> pixelsToSwap;
    private long deltaTime;

    ColorSwapper(Bitmap bitmap, int colorToSwap) {
        this.bitmap = bitmap;
        pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        pixelsToSwap = new HashSet<>();

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == colorToSwap)
                pixelsToSwap.add(i);
        }
    }

    void swapColor(int newColor) {
        long start = System.currentTimeMillis();
        for (int i : pixelsToSwap) {
            pixels[i] = newColor;
        }


        bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        deltaTime = System.currentTimeMillis() - start;
    }

    long getDeltaTime() {
        return deltaTime;
    }
}
