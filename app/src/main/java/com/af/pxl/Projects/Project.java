package com.af.pxl.Projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.af.pxl.Palettes.Palette2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class Project {
    public File projectDirectory;
    public String name;
    Bitmap image;

    //Meta
    public int pixelWidth;
    public int pixelHeight;
    public String palette;


    public Project(File projectDirectory){
        name = projectDirectory.getName();
        this.projectDirectory = projectDirectory;
        loadMeta();
    }

    public Bitmap getBitmap(boolean mutable){
        if(image == null) {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inMutable = mutable;
            return image = BitmapFactory.decodeFile(projectDirectory + "/image.pxl", op);
        }
        return image;
    }

    public void setPalette(Palette2 palette){
        File meta = new File(projectDirectory ,".pxlmeta");
        try(FileWriter writer = new FileWriter(meta, false)) {
            writer.write(pixelWidth+","+pixelHeight+","+palette.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResolutionString(){
        return pixelWidth+"x"+pixelHeight;
    }

    private void loadMeta(){
        StringBuilder builder = new StringBuilder();

        try(FileReader reader = new FileReader(projectDirectory +"/.pxlmeta")) {
            int c = reader.read();
            while (c!=-1){
                builder.append((char)c);
                c = reader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        parseMeta(builder.toString());
    }

    private void parseMeta(String rawMeta){
        String[] metaValues = rawMeta.split(",");
        pixelWidth = Integer.parseInt(metaValues[0]);
        pixelHeight = Integer.parseInt(metaValues[1]);
        palette = metaValues[2];
    }
}
