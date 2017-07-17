package com.af.pxl;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Aefyr on 17.07.2017.
 */

public class PaletteUtils {

    private static String palettesPath;
    private static final String EXTENSION = ".pxlpalette";

    static void initialize(Context c){
        palettesPath = c.getFilesDir()+"/palettes";
        File f = new File(palettesPath);

        if(!f.exists()){
            f.mkdir();
        }
    }

    static boolean savePalette(Palette2 palette){
        File path = new File(palettesPath, palette.getName()+EXTENSION);

        if(!path.exists()){
            try {
                path.createNewFile();
            } catch (IOException e){
                System.out.println("Failed to create a palette file");
                e.printStackTrace();
                return false;
            }
        }


        try(FileWriter fileWriter = new FileWriter(path, false)) {


            int limit = palette.getColors().size();
            for(int i = 0; i<limit ; i++){
                fileWriter.append(String.valueOf(palette.getColor(i)));
                if(i<limit-1){
                    fileWriter.append(",");
                }
            }
            System.out.println("Palette was saved!");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to save the palette :(");
            e.printStackTrace();
            return false;
        }
    }

    static Palette2 loadPalette(String name){
        File path = new File(palettesPath, name+EXTENSION);

        try(FileReader reader = new FileReader(path)){
            int c = reader.read();
            StringBuilder builder = new StringBuilder();
            while (c!=-1){
                builder.append((char)c);
                c = reader.read();
            }
            String[] colors = builder.toString().split(",");
            System.out.println("last color: "+colors[colors.length-1]);

            Palette2 palette = new Palette2(name, 16, Integer.parseInt(colors[0]));
            for(c=1; c<colors.length; c++){
                palette.addColor(Integer.parseInt(colors[c]));
            }

            System.out.println("Palette was successfully loaded!");
            return palette;

        } catch (IOException e) {
            System.out.println("Failed to load the palette :(");
            e.printStackTrace();
            return null;
        }
    }
}
