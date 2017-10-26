package com.af.pxl.palettes;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.af.pxl.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by Aefyr on 19.10.2017.
 */

public class PaletteMakerH {
    private Context c;
    private String TEXT_EXTRACTED = "Extracted";

    public PaletteMakerH(Context c){
        this.c = c;
        TEXT_EXTRACTED = c.getString(R.string.extracted);

    }

    public void createPaletteFromImage(Uri bitmapUri, PaletteGeneratonListener listener){
        new PaletteGeneratorTask(listener).execute(bitmapUri);
    }

    private Palette2 extractPalette3(Uri bitmapUri){
        long startTime = System.currentTimeMillis();

        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Bitmap lidlBitmap = Kmeans.test(createSampleSizedImage(image, 128, true));

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

        Log.d("PaletteMaker", "Generated palette from image in "+(System.currentTimeMillis()-startTime)+"ms.");
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


    private Bitmap createSampleSizedImage(Bitmap image, int maxSideSize, boolean recycleOriginal){
        if(image.getWidth()<maxSideSize&&image.getHeight()<maxSideSize)
            return image;

        float aspectRatio = (float)image.getWidth()/(float)image.getHeight();

        boolean widthBigger = aspectRatio>1;
        int targetWidth = widthBigger? maxSideSize:(int)(maxSideSize*aspectRatio);
        int targetHeight = widthBigger? (int)(maxSideSize/aspectRatio):maxSideSize;

        Bitmap sampleSizedImage = Bitmap.createScaledBitmap(image, targetWidth, targetHeight, false);
        if(recycleOriginal&&image!=sampleSizedImage)
            image.recycle();

        return sampleSizedImage;
    }

    public interface PaletteGeneratonListener {
        void onPaletteGenerated(Palette2 palette);
    }
    private class PaletteGeneratorTask extends AsyncTask<Uri, Void, Palette2>{
        private PaletteGeneratonListener listener;
        private PaletteGeneratorTask(PaletteGeneratonListener listener){
            this.listener = listener;
        }

        @Override
        protected Palette2 doInBackground(Uri... bitmapUri) {
            return extractPalette3(bitmapUri[0]);
        }

        @Override
        protected void onPostExecute(Palette2 palette) {
            super.onPostExecute(palette);
            listener.onPaletteGenerated(palette);
        }
    }

    public static ProgressDialog createGenerationProgressDialog(Context c) {
        ProgressDialog dialog = new ProgressDialog(c, ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(c.getString(R.string.extracting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
