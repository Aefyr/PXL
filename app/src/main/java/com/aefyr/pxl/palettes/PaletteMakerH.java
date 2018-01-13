package com.aefyr.pxl.palettes;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.aefyr.pxl.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by Aefyr on 19.10.2017.
 */

public class PaletteMakerH {
    private static final String TAG = "PaletteMakerH";

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
        Bitmap image;
        try {
            image = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(bitmapUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return extractPalette3(image);
    }

    private Palette2 extractPalette3(Bitmap image){
        ArrayList<Integer> colors = extractColorsFromImage(image, 16);

        Palette2 palette = new Palette2(getName(), false);

        palette.setColors(colors);
        if(colors.size()<16)
            palette.fillVoidColorsWithDefault(true);

        return palette;
    }

    public ArrayList<Integer> extractColorsFromImage(Bitmap image, int colorsCount){
        long start = System.currentTimeMillis();
        Bitmap lidlBitmap = createSampleSizedImage(image, 128, true);

        /*This lidl-looking system tries to retrieve colorsCount+1 colors from image just to check if we need to run Kmeans on the image,
        since running Kmeans on and image is very expensive and it will actually ruin the color palette of an image that has less than colorsCount colors*/
        ArrayList<Integer> colors = sequentiallyGetUniqueColorsFromImage(lidlBitmap, colorsCount+1);

        if(colors.size()<=colorsCount)
            return colors;

        lidlBitmap = new Kmeans().calculate(lidlBitmap, colorsCount, 1);

        colors = sequentiallyGetUniqueColorsFromImage(lidlBitmap, colorsCount);

        Log.d(TAG, String.format("Extracted %d colors from image in %d ms", colorsCount, System.currentTimeMillis()-start));

        return colors;
    }

    private ArrayList<Integer> sequentiallyGetUniqueColorsFromImage(Bitmap image, int stopWhenUniqueColorsCountReaches){
        long start = System.currentTimeMillis();

        ArrayList<Integer> colors = new ArrayList<>(stopWhenUniqueColorsCountReaches);
        int[] pixels = new int[image.getWidth()*image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        for(int color: pixels){
            if(!colors.contains(color)) {
                colors.add(color);

                if(colors.size()>=stopWhenUniqueColorsCountReaches)
                    break;
            }
        }

        Collections.sort(colors, new ColorBrightnessComparator());

        Log.d(TAG, String.format("Retrieved %d colors from a %dx%d image in %d ms", colors.size(), image.getWidth(), image.getHeight(), System.currentTimeMillis()-start));

        return colors;
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
