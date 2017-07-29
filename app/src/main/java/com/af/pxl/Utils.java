package com.af.pxl;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Aefyr on 01.07.2017.
 */

public class Utils {


    public static float dpToPx(int dp, Resources r){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float getScreenWidth(Resources r){
        return r.getDisplayMetrics().widthPixels;
    }

    public static float getScreenHeight(Resources r){
        return r.getDisplayMetrics().heightPixels;
    }

    public static float clamp(float x, float min, float max){
        if(x<min)return min;
        if(x>max) return max;
        return x;
    }

    static int clamp(int x, int min, int max){
        if(x<min)return min;
        if(x>max) return max;
        return x;
    }

    public static float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    public static int invertColor(int color){
        return Color.rgb(255-Color.red(color), 255-Color.green(color), 255 - Color.blue(color));
    }

    public static void toaster(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static void setBitmapPixelsFromOtherBitmap(Bitmap bitmap, Bitmap otherBitmap){
        int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
        otherBitmap.getPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
        bitmap.setPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
    }

    public static boolean saveBitmap(Bitmap bitmap, File path){
        try(FileOutputStream fileOutputStream = new FileOutputStream(path)){
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            return true;
        }catch (IOException e){
            return false;
        }
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static void alternativeAddImageToGallery(Context c, File imagePath){
        MediaScannerConnection.scanFile(c, new String[]{imagePath.toString()}, null, null);
    }

    public static void copyFileOrDirectory(File sourceLocation, File targetLocation) {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyFileOrDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            try(InputStream in = new FileInputStream(sourceLocation); OutputStream out = new FileOutputStream(targetLocation)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }


}
