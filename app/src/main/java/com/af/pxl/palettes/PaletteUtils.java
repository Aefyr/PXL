package com.af.pxl.palettes;

import android.content.Context;
import android.os.AsyncTask;

import com.af.pxl.R;
import com.af.pxl.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Aefyr on 17.07.2017.
 */

public class PaletteUtils {

    static final String PALETTE_NAME_VALIDITY_PATTERN = "\\w+[A-Za-zА-Яа-я_0-9\\s]*";
    static String paletteDuplicatePostfix;

    static String palettesPath;
    static final String EXTENSION = ".pxlpalette";

    public static void initialize(Context c) {
        palettesPath = c.getFilesDir() + "/palettes";
        paletteDuplicatePostfix = " " + c.getString(R.string.duplicate_prefix);
        File f = new File(palettesPath);

        if (!f.exists()) {
            f.mkdir();
        }
    }

    public static boolean savePalette(Palette2 palette) {
        File path = new File(palettesPath, palette.getName() + EXTENSION);

        if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (IOException e) {
                System.out.println("Failed to create a palette file");
                e.printStackTrace();
                return false;
            }
        }

        long lastModified = path.lastModified();
        try (FileWriter fileWriter = new FileWriter(path, false)) {

            int limit = palette.getColors().size();
            for (int i = 0; i < limit; i++) {
                fileWriter.append(String.valueOf(palette.getColor(i)));
                if (i < limit - 1) {
                    fileWriter.append(",");
                }
            }

            System.out.println("Palette was saved!");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save the palette :(");
            e.printStackTrace();
            return false;
        }finally {
            path.setLastModified(lastModified);
        }
    }

    public static Palette2 loadPalette(String name) {
        File path = new File(palettesPath, name + EXTENSION);

        try (FileReader reader = new FileReader(path)) {
            int c = reader.read();
            StringBuilder builder = new StringBuilder();
            while (c != -1) {
                builder.append((char) c);
                c = reader.read();
            }
            String[] colors = builder.toString().split(",");
            System.out.println("last currentColor: " + colors[colors.length - 1]);

            Palette2 palette = new Palette2(name, true);
            ArrayList<Integer> paletteColors = palette.getColors();
            for (c = 0; c < colors.length; c++) {
                paletteColors.add(Integer.parseInt(colors[c]));
            }

            System.out.println("Palette was successfully loaded!");
            return palette;

        } catch (IOException e) {
            System.out.println("Failed to load the " + name + " palette, loading default one");
            e.printStackTrace();
            return defaultPalette();
        }
    }

    public static void renamePalette(Palette2 palette, String newName) {
        palette.directory().renameTo(new File(palettesPath + "/" + newName + EXTENSION));
        palette.setName(newName);
    }

    public static Palette2 duplicatePalette(Palette2 original) {
        String newPaletteName = original.getName() + paletteDuplicatePostfix;
        if (!isNameAvailable(newPaletteName)) {
            int a = 1;
            System.out.println("Loop");
            while (!isNameAvailable(newPaletteName + " " + a))
                a++;

            newPaletteName = newPaletteName + " " + a;
        }

        Utils.copyFileOrDirectory(new File(palettesPath + "/" + original.getName() + EXTENSION), new File(palettesPath + "/" + newPaletteName + EXTENSION));
        Palette2 duplicatedPalette = loadPalette(newPaletteName);
        duplicatedPalette.setLastModified(System.currentTimeMillis());
        return duplicatedPalette;
    }

    public static void deletePalette(Palette2 palette) {
        File p = new File(palettesPath + "/" + palette.getName() + EXTENSION);
        p.delete();
    }

    public static boolean isNameAvailable(String name) {
        if (!name.matches(PALETTE_NAME_VALIDITY_PATTERN))
            return false;

        File f = new File(palettesPath + "/" + name + EXTENSION);
        return (!f.exists());
    }

    public static Palette2 defaultPalette() {
        File defaultPalette = new File(palettesPath + "/Default" + EXTENSION);
        if (defaultPalette.exists())
            return loadPalette("Default");
        Palette2 p = new Palette2("Default");
        return p;
    }


    public static ArrayList<String> getSavedPalettes() {
        ArrayList<String> names = new ArrayList<>();

        File file = new File(palettesPath);

        for (File child : file.listFiles()) {
            String name = child.getName();
            name = name.substring(0, name.length() - EXTENSION.length());
            names.add(name);
        }

        return names;
    }

    public static int getSavedPalettesCount(){
        return new File(palettesPath).listFiles().length;
    }

    public void loadSavedPalettesAsync(boolean allAtOnce, PalettesLoaderListener listener){
        new PalettesLoaderTask().execute(new PalettesLoaderTaskParams(listener, allAtOnce));
    }

    public interface PalettesLoaderListener{
        void onPalettesLoaded(ArrayList<Palette2> palettes);
        void onPaletteLoaded(Palette2 palette);
    }

    private class PalettesLoaderTaskParams{
        private PalettesLoaderListener listener;
        private boolean allAtOnce;
        private PalettesLoaderTaskParams(PalettesLoaderListener listener, boolean allAtOnce){
            this.listener = listener;
            this.allAtOnce = allAtOnce;
        }
    }
    private class PalettesLoaderTask extends AsyncTask<PalettesLoaderTaskParams, Palette2, ArrayList<Palette2>>{

        private PalettesLoaderListener listener;

        @Override
        protected ArrayList<Palette2> doInBackground(PalettesLoaderTaskParams... params) {
            this.listener = params[0].listener;
            final boolean allAtOnce = params[0].allAtOnce;

            File[] palettesFiles = (new File(palettesPath)).listFiles();
            Arrays.sort(palettesFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Long.compare(o1.lastModified(), o2.lastModified());
                }
            });

            ArrayList<Palette2> palettes = null;
            if(allAtOnce)
                palettes = new ArrayList<>(palettesFiles.length);

            String name;
            for (File palette : palettesFiles) {
                name = palette.getName();
                name = name.substring(0, name.length() - EXTENSION.length());

                if(allAtOnce)
                    palettes.add(loadPalette(name));
                else
                    publishProgress(loadPalette(name));
            }

            return allAtOnce?palettes:null;
        }

        @Override
        protected void onProgressUpdate(Palette2... palettes) {
            super.onProgressUpdate(palettes);
            listener.onPaletteLoaded(palettes[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Palette2> palettes) {
            super.onPostExecute(palettes);
            if(palettes!=null)
                listener.onPalettesLoaded(palettes);
        }
    }
}
