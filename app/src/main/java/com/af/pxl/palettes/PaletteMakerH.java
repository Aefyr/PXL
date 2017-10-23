package com.af.pxl.palettes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.graphics.Palette;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

import com.af.pxl.R;
import com.af.pxl.ScriptC_palette_extractor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


/**
 * Created by Aefyr on 19.10.2017.
 */

public class PaletteMakerH {
    private Context c;
    private RenderScript rs;
    private ScriptC_palette_extractor paletteExtractor;
    private String TEXT_EXTRACTED = "Extracted";

    public PaletteMakerH(Context c){
        this.c = c;
        rs = RenderScript.create(c);
        paletteExtractor = new ScriptC_palette_extractor(rs);
        TEXT_EXTRACTED = c.getString(R.string.extracted);

    }

    public Palette2 extractPalette(Uri bitmapUri){
        long startTime = System.currentTimeMillis();

        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }



        Bitmap sampleSizedImage = createSampleSizedImage(image, 64, true);

        Allocation imageAlloc = Allocation.createFromBitmap(rs, sampleSizedImage);
        Allocation paletteAlloc = Allocation.createSized(rs, Element.I32(rs), 16);
        paletteExtractor.set_palette(paletteAlloc);

        paletteExtractor.forEach_extract(imageAlloc);
        rs.finish();
        paletteExtractor.invoke_packPalette();


        int[] colors = new int[16];
        paletteAlloc.copyTo(colors);

        Palette2 palette = new Palette2("Generated");

        StringBuilder ca = new StringBuilder();
        for (int i = 0; i<16; i++){
            ca.append(colors[i]).append(", ");
            palette.editColor(i, colors[i]);
        }

        System.out.println("COLORS!\n"+ca.toString());

        System.out.println("Generated palette in "+(System.currentTimeMillis()-startTime));

        return palette;
    }

    private static final int size = 64;
    public Palette2 extractPalette2(Uri bitmapUri) {
        long startTime = System.currentTimeMillis();

        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }


        Bitmap sampleSizedImage = createSampleSizedImage(image, size, true);
        int finalW = sampleSizedImage.getWidth();
        int finalH = sampleSizedImage.getHeight();

        HashMap<Integer, Integer> colors = new HashMap<>();
        int[] pixels = new int[finalW*finalH];
        sampleSizedImage.getPixels(pixels, 0, finalW, 0, 0, finalW, finalH);

        for(int pixel: pixels){
            colors.put(pixel, colors.containsKey(pixel)?colors.get(pixel)+1:1);
        }

        ArrayList<ColorCompetitor> competitors = new ArrayList<>(colors.size());
        for(int key: colors.keySet()){
            competitors.add(new ColorCompetitor(key, colors.get(key)));
        }

        Collections.sort(competitors, new Comparator<ColorCompetitor>() {
            @Override
            public int compare(ColorCompetitor o1, ColorCompetitor o2) {
                if(o1.referred == o2.referred)
                    return 0;

                return o1.referred>o2.referred?-1:1;
            }
        });

        Palette2 palette = new Palette2("Generated via comp");
        for (int i = 0; i<(competitors.size()>=16?16:competitors.size()); i++){
            palette.editColor(i, competitors.get(i).color);
        }

        return palette;
    }

    public Palette2 extractPalette3(Uri bitmapUri){
        long startTime = System.currentTimeMillis();

        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        System.out.println("Decoded bitmap in "+(System.currentTimeMillis()-startTime));
        startTime = System.currentTimeMillis();

        Bitmap lidlBitmap = Kmeans.test(createSampleSizedImage(image, 128, true));

        startTime = System.currentTimeMillis();

        int[] pixels = new int[lidlBitmap.getWidth()*lidlBitmap.getHeight()];
        lidlBitmap.getPixels(pixels, 0, lidlBitmap.getWidth(), 0, 0, lidlBitmap.getWidth(), lidlBitmap.getHeight());

        Palette2 palette = new Palette2(getName(), false);

        ArrayList<Integer> colors = palette.getColors();
        for(int color: pixels){
            if(!colors.contains(color))
                colors.add(color);
        }

        Collections.sort(colors, new ColorBrightnessComparator());

        palette.setColors(colors);
        if(colors.size()<16)
            palette.fillVoidColorsWithDefault(true);

        System.out.println("Parsed image in: "+(System.currentTimeMillis()-startTime));
        return palette;
    }

    private class ColorBrightnessComparator implements Comparator<Integer>{

        @Override
        public int compare(Integer o1, Integer o2) {
            return -o1.compareTo(o2);
        }
    }

    private String getName(){
        if (!PaletteUtils.isNameAvailable(TEXT_EXTRACTED)) {
            int i = 1;
            while (!PaletteUtils.isNameAvailable(TEXT_EXTRACTED + " " + i))
                i++;

            return TEXT_EXTRACTED + " " + i;
        }

        return TEXT_EXTRACTED;
    }

    private class ColorCompetitor{
        int color;
        int referred;
        private ColorCompetitor(int c, int r){
            color = c;
            referred = r;
        }
    }

    private Bitmap createSampleSizedImage(Bitmap image, int maxSideSize, boolean recycleOriginal){
        System.out.println(String.format("Got as %dx%d", image.getWidth(), image.getHeight()));
        if(image.getWidth()<maxSideSize&&image.getHeight()<maxSideSize)
            return image;

        float aspectRatio = (float)image.getWidth()/(float)image.getHeight();

        boolean widthBigger = aspectRatio>1;
        int targetWidth = widthBigger? maxSideSize:(int)(maxSideSize*aspectRatio);
        int targetHeight = widthBigger? (int)(maxSideSize/aspectRatio):maxSideSize;

        System.out.println(String.format("Returned as %dx%d", targetWidth, targetHeight));

        Bitmap sampleSizedImage = Bitmap.createScaledBitmap(image, targetWidth, targetHeight, false);
        if(recycleOriginal&&image!=sampleSizedImage)
            image.recycle();

        return sampleSizedImage;
    }
}
