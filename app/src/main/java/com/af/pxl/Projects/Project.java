package com.af.pxl.Projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class Project {
    public String projectDirectoryPath;
    public String name;
    Bitmap image;

    //Meta
    public int pixelWidth;
    public int pixelHeight;
    public String palette;


    public Project(String name, String projectDirectoryPath){
        this.name = name;
        this.projectDirectoryPath = projectDirectoryPath;
        loadMeta();
    }

    public Bitmap getBitmap(boolean mutable){
        if(image == null) {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inMutable = mutable;
            return image = BitmapFactory.decodeFile(projectDirectoryPath + "/image.pxl", op);
        }
        return image;
    }



    public String getResolutionString(){
        return pixelWidth+"x"+pixelHeight;
    }

    private void loadMeta(){
        StringBuilder builder = new StringBuilder();

        try(FileReader reader = new FileReader(projectDirectoryPath+"/m.pxlmeta")) {
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
