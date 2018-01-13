package com.aefyr.pxl.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.aefyr.pxl.R;
import com.aefyr.pxl.ScriptC_posterizer;
import com.aefyr.pxl.palettes.PaletteMakerH;

import java.util.ArrayList;

/**
 * Created by Aefyr on 10.01.2018.
 */

public class Posterizer {
    private static String TAG = "Posterizer";

    private boolean recycled;
    private Context c;
    private ScriptC_posterizer posterizer;
    private RenderScript rs;

    public Posterizer(Context c){
        this.c = c;
        rs = RenderScript.create(c);
        posterizer = new ScriptC_posterizer(rs);
    }

    public Bitmap posterize(Bitmap image, int colorsCount){
        if(recycled)
            throw new IllegalStateException("You can't use a recycled Posterizer");

        long start = System.currentTimeMillis();

        final int w = image.getWidth();
        final int h = image.getHeight();

        Allocation bitmapAllocation = Allocation.createFromBitmap(rs, image);
        Log.d(TAG, "Created bitmap allocation from the image");

        Log.d(TAG, String.format("Extracting %d-colors palette from image...", colorsCount));
        PaletteMakerH paletteMaker = new PaletteMakerH(c);
        ArrayList<Integer> colorsAL = paletteMaker.extractColorsFromImage(image, colorsCount);

        colorsCount = colorsAL.size();
        posterizer.set_paletteSize(colorsCount);
        Log.d(TAG, String.format("Actually extracted %d colors", colorsCount));

        int[] colors = new int[colorsCount];
        for(int i=0; i<colorsCount; i++){
            colors[i] = colorsAL.get(i);
        }

        Allocation paletteAllocation = Allocation.createTyped(rs, Type.createX(rs, Element.I32(rs), colorsCount));
        paletteAllocation.copyFrom(colors);

        Log.d(TAG, String.format("Palette was extracted and loaded into allocation in %d ms, starting applying it to the image...", System.currentTimeMillis()-start));
        start = System.currentTimeMillis();

        posterizer.set_palette(paletteAllocation);
        Log.d(TAG, "Loaded palette allocation into the RenderScript script");

        posterizer.forEach_posterize(bitmapAllocation, bitmapAllocation);
        Log.d(TAG, "Started posterization...");

        //TODO Work out wtf is happening to the original bitmap
        Bitmap posterizedImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapAllocation.copyTo(posterizedImage);

        bitmapAllocation.destroy();
        paletteAllocation.destroy();
        Log.d(TAG, String.format("Done! Palette was applied to the bitmap in %d ms.", System.currentTimeMillis()-start));

        return posterizedImage;
    }

    public void posterizeAsync(Bitmap image, int colorsCount, PosterizationListener listener){
        new PosterizerTask(colorsCount, listener).execute(image);
    }

    public void recycle(){
        recycled = true;
        posterizer.destroy();
        rs.destroy();
    }

    public interface PosterizationListener{
        void onImagePosterized(Bitmap posterizedImage);
    }

    private class PosterizerTask extends AsyncTask<Bitmap, Void, Bitmap>{
        private PosterizationListener listener;
        private int colorsCount;

        PosterizerTask(int colorsCount, PosterizationListener listener){
            this.listener = listener;
            this.colorsCount = colorsCount;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            return posterize(bitmaps[0], colorsCount);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(listener!=null){
                listener.onImagePosterized(bitmap);
            }
        }
    }

    public static ProgressDialog createGenerationProgressDialog(Context c) {
        ProgressDialog dialog = new ProgressDialog(c, ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(c.getString(R.string.posterizing));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
