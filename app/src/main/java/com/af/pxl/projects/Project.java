package com.af.pxl.projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.af.pxl.palettes.Palette2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class Project {
    public String id;
    public File projectDirectory;
    public String name;
    long lastModified;

    //Meta
    public int pixelWidth = 999;
    public int pixelHeight = 999;
    public String palette = "Default";
    public boolean transparentBackground = false;


    public Project(File projectDirectory) {
        id = projectDirectory.getName();
        this.projectDirectory = projectDirectory;
        lastModified = projectDirectory.lastModified();
        loadMeta();
    }

    public Bitmap getBitmap(boolean mutable) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inMutable = mutable;
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap loadedBitmap = BitmapFactory.decodeFile(projectDirectory + "/image.pxl", op);
        if (transparentBackground)
            loadedBitmap.setHasAlpha(true);
        return loadedBitmap;

    }

    public void setPalette(Palette2 palette) {
        File meta = new File(projectDirectory, ".pxlmeta");
        try (FileWriter writer = new FileWriter(meta, false)) {
            writer.write(pixelWidth + "," + pixelHeight + "," + palette.getName() + "," + transparentBackground);
            notifyProjectModified();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyProjectModified() {
        System.out.println("LastModifiedWas " + projectDirectory.lastModified());
        System.out.println("Modified Time=" + projectDirectory.setLastModified(System.currentTimeMillis()));

    }

    public String getResolutionString() {
        return pixelWidth + "x" + pixelHeight;
    }

    private void loadMeta() {
        StringBuilder builder = new StringBuilder();

        try (FileReader reader = new FileReader(projectDirectory + "/.pxlmeta")) {
            int c = reader.read();
            while (c != -1) {
                builder.append((char) c);
                c = reader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        parseMeta(builder.toString());
    }

    private void parseMeta(String rawMeta) {
        String[] metaValues = rawMeta.split("]\\|\\[");
        if (metaValues.length == 0)
            return;
        pixelWidth = Integer.parseInt(metaValues[0]);
        if (metaValues.length == 1)
            return;
        pixelHeight = Integer.parseInt(metaValues[1]);
        if (metaValues.length == 2)
            return;
        palette = metaValues[2];
        if (metaValues.length == 3)
            return;
        transparentBackground = Boolean.parseBoolean(metaValues[3]);
        if(metaValues.length == 4)
            return;
        name = metaValues[4];
    }
}
