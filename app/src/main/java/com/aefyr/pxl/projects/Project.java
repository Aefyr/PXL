package com.aefyr.pxl.projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.aefyr.pxl.palettes.Palette2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Aefyr on 19.07.2017.
 */

public class Project {
    public String id;
    public File directory;
    public File imageFile;
    public String name;

    //Meta
    public int width = 999;
    public int height = 999;
    public String palette = "Default";
    public boolean transparentBackground = false;


    public Project(File directory) {
        id = directory.getName();
        this.directory = directory;
        imageFile = new File(directory, "image.png");
        loadMeta();
    }

    public Bitmap getBitmap(boolean mutable) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inMutable = mutable;
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap loadedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), op);
        if (loadedBitmap!=null&&transparentBackground)
            loadedBitmap.setHasAlpha(true);
        return loadedBitmap;

    }

    public Bitmap getPreviewBitmap(int targetMaxSideSize){
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), op);

        int oW = op.outWidth;
        int oH = op.outHeight;

        int maxSideSize = op.outWidth>op.outHeight?op.outWidth:op.outHeight;

        if(maxSideSize>targetMaxSideSize) {
            op.inScaled = true;
            op.inDensity = maxSideSize;
            op.inTargetDensity = targetMaxSideSize;
        }

        op.inJustDecodeBounds = false;
        op.inMutable = false;

        Bitmap loadedBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), op);
        if (loadedBitmap!=null&&transparentBackground)
            loadedBitmap.setHasAlpha(true);

        Log.d("PROJECTS", String.format("maxside=%d, from %dx%d to %dx%d",targetMaxSideSize, oW, oH, loadedBitmap.getWidth(), loadedBitmap.getHeight()));
        return loadedBitmap;
    }

    public void setPalette(Palette2 palette) {
        if(this.palette.equals(palette.getName()))
            return;

        this.palette = palette.getName();
        ProjectsUtils.writeMeta(this);
    }

    public void setName(String name){
        name = name.replaceAll(ProjectsUtils.META_SEPARATOR_REGEX, " ");
        if(name.equals(this.name))
            return;

        this.name = name;
        ProjectsUtils.writeMeta(this);
    }

    public long lastModified(){
        return directory.lastModified();
    }

    public void notifyProjectModified() {
        directory.setLastModified(System.currentTimeMillis());
        ProjectsUtils.updateVersion();
    }

    public String getResolutionString() {
        return width + "x" + height;
    }

    private void loadMeta() {
        StringBuilder builder = new StringBuilder();

        try (FileReader reader = new FileReader(directory + "/.meta")) {
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
        String[] metaValues = rawMeta.split(ProjectsUtils.META_SEPARATOR_REGEX);
        if (metaValues.length == 0)
            return;
        width = Integer.parseInt(metaValues[0]);
        if (metaValues.length == 1)
            return;
        height = Integer.parseInt(metaValues[1]);
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
