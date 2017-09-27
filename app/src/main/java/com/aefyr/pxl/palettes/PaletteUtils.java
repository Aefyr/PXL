package com.aefyr.pxl.palettes;

import android.content.Context;

import com.aefyr.pxl.R;
import com.aefyr.pxl.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
        paletteDuplicatePostfix = " " + c.getString(R.string.project_duplicate_postfix);
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


        try (FileWriter fileWriter = new FileWriter(path, false)) {


            int limit = palette.getColors().size();
            for (int i = 0; i < limit; i++) {
                fileWriter.append(String.valueOf(palette.getColor(i)));
                if (i < limit - 1) {
                    fileWriter.append(",");
                }
            }
            path.setLastModified(System.currentTimeMillis());
            System.out.println("Palette was saved!");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save the palette :(");
            e.printStackTrace();
            return false;
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

            Palette2 palette = new Palette2(name, 16, Integer.parseInt(colors[0]), true);
            ArrayList<Integer> paletteColors = palette.getColors();
            for (c = 1; c < colors.length; c++) {
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

    public static String renamePalette(Palette2 palette, String newName) {
        File old = new File(palettesPath + "/" + palette.getName() + EXTENSION);
        old.renameTo(new File(palettesPath + "/" + newName + EXTENSION));
        palette.setName(newName);
        return newName;
    }

    public static String duplicatePalette(Palette2 original) {
        String newPaletteName = original.getName() + paletteDuplicatePostfix;
        if (!isNameAvailable(newPaletteName)) {
            int a = 1;
            System.out.println("Loop");
            while (!isNameAvailable(newPaletteName + " " + a))
                a++;

            newPaletteName = newPaletteName + " " + a;
        }

        Utils.copyFileOrDirectory(new File(palettesPath + "/" + original.getName() + EXTENSION), new File(palettesPath + "/" + newPaletteName + EXTENSION));
        return newPaletteName;
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
}
