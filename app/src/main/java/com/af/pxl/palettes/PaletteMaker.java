package com.af.pxl.palettes;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import com.af.pxl.R;
import com.af.pxl.Utils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Aefyr on 06.08.2017.
 */

public class PaletteMaker {

    public interface OnPaletteGenerationListener {
        void onPaletteGenerated(Palette2 palette);

        void onGenerationFailed();
    }

    private static PaletteMaker instance;

    private String TEXT_EXTRACTED = "Extracted";

    private PaletteMaker(Context c) {
        TEXT_EXTRACTED = c.getString(R.string.extracted);
        instance = this;
    }

    public static PaletteMaker getInstance(Context c) {
        return instance == null ? new PaletteMaker(c) : instance;
    }

    public void extractPalette(Context c, Uri bitmapUri, OnPaletteGenerationListener listener) {
        new PaletteGenerationTask().execute(new PaletteGeneratorParams(c, bitmapUri, listener));
    }

    //I forgot how and why this works...
    private void tryAddingColor(int[] colors, int newColor) {
        float lowestDifference = 1f;
        int lowestColor = -1;

        //Okay, here we get the lowest difference between new color and any palette color
        for (int c : colors) {
            float difference = Utils.colorsDifference(c, newColor);
            if (difference < lowestDifference)
                lowestDifference = difference;
        }

        //And here we find two colors in palette with lowest difference and if their difference is lower than our new color's with any other color, we replace one of them with new color
        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            for (int a = 0; a < colors.length; a++) {
                if (a == i)
                    continue;
                float difference = Utils.colorsDifference(c, colors[a]);
                if (difference < lowestDifference) {
                    lowestDifference = difference;
                    lowestColor = i;
                }
            }
        }

        if (lowestColor != -1) {
            colors[lowestColor] = newColor;
        }
    }

    public static void generateDefaultPalettes(Context c) {
        String[] defaultPalettes = c.getResources().getStringArray(R.array.default_palettes);

        for (String palette : defaultPalettes) {
            String[] paletteMeta = palette.split(",");
            Palette2 palette2 = new Palette2(paletteMeta[0]);

            for (int i = 0; i < 16; i++) {
                palette2.editColor(i, Integer.parseInt(paletteMeta[i + 1]));
            }

            PaletteUtils.savePalette(palette2);
        }
    }

    private class PaletteGeneratorParams {
        private OnPaletteGenerationListener listener;
        private Uri bitmapUri;
        private Context c;

        private PaletteGeneratorParams(Context context, Uri bitmapUri, OnPaletteGenerationListener listener) {
            this.c = context;
            this.listener = listener;
            this.bitmapUri = bitmapUri;
        }
    }

    private class PaletteGenerationTask extends AsyncTask<PaletteGeneratorParams, Integer, Palette2> {
        private OnPaletteGenerationListener listener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Palette2 doInBackground(PaletteGeneratorParams... params) {
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(params[0].c.getContentResolver().openInputStream(params[0].bitmapUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            listener = params[0].listener;

            if (!PaletteUtils.isNameAvailable(TEXT_EXTRACTED)) {
                int i = 1;
                while (!PaletteUtils.isNameAvailable(TEXT_EXTRACTED + " " + i))
                    i++;
                TEXT_EXTRACTED += " " + i;
            }
            Palette2 palette = new Palette2(TEXT_EXTRACTED);
            ArrayList<Integer> colors = palette.getColors();

            Bitmap actualBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, bitmap.getWidth() > 48 && bitmap.getHeight() > 48);
            int[] pixels = new int[48 * 48];
            actualBitmap.getPixels(pixels, 0, 48, 0, 0, 48, 48);

            int[] realColors = new int[16];


            HashSet<Integer> uC = new HashSet<>();
            for (int c : pixels) {
                if (!uC.contains(c) && uC.size() < 16)
                    uC.add(c);
            }


            int i = 0;
            for (int c : uC) {
                realColors[i++] = c;
            }

            if (uC.size() < 16) {
                for (i = uC.size() - 1; i < 16; i++)
                    realColors[i] = Color.WHITE;
            }

            for (int color : pixels) {
                tryAddingColor(realColors, color);
            }

            colors.clear();
            for (int c : realColors) {
                colors.add(c);
            }

            PaletteUtils.savePalette(palette);
            return palette;
        }

        @Override
        protected void onPostExecute(Palette2 palette) {
            super.onPostExecute(palette);
            if (palette != null)
                listener.onPaletteGenerated(palette);
            else
                listener.onGenerationFailed();

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
