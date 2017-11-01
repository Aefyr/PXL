package com.af.pxl.palettes;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Created by Aefyr on 01.11.2017.
 */

public class DynamicPalettesLoader {
    private static DynamicPalettesLoader instance;
    private File palettesDirectory;

    private DynamicPalettesLoader(Context c){
        palettesDirectory = new File(c.getFilesDir() + "/palettes");
        instance = this;
    }

    public static DynamicPalettesLoader getInstance(Context c){
        return instance==null?new DynamicPalettesLoader(c):instance;
    }

    public void loadPalettes(PalettesLoaderCallbackD listener){
        new PalettesLoaderTask().execute(new Params(listener));
    }

    public interface PalettesLoaderCallbackD{
        void onPaletteLoaded(Palette2 palette);
    }

    private class Params{
        PalettesLoaderCallbackD listener;
        Params(PalettesLoaderCallbackD listener){
            this.listener = listener;
        }
    }

    private class PalettesLoaderTask extends AsyncTask<Params, Palette2, Void>{
        private PalettesLoaderCallbackD listener;

        @Override
        protected Void doInBackground(Params... params) {
            this.listener = params[0].listener;

            File[] palettesFiles = palettesDirectory.listFiles();
            Arrays.sort(palettesFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Long.compare(o1.lastModified(), o2.lastModified());
                }
            });

            String name;
            for (File palette : palettesFiles) {
                name = palette.getName();
                name = name.substring(0, name.length() - PaletteUtils.EXTENSION.length());

                publishProgress(PaletteUtils.loadPalette(name));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Palette2... palettes) {
            super.onProgressUpdate(palettes);
            listener.onPaletteLoaded(palettes[0]);
        }
    }

}
