package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Aefyr on 01.07.2017.
 */

public class Utils {


    static float clamp(float x, float min, float max){
        if(x<min)return min;
        if(x>max) return max;
        return x;
    }

    static float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    public static int invertColor(int color){
        return Color.rgb(255-Color.red(color), 255-Color.green(color), 255 - Color.blue(color));
    }

    static void toaster(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static void setBitmapPixelsFromOtherBitmap(Bitmap bitmap, Bitmap otherBitmap){
        int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
        otherBitmap.getPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
        bitmap.setPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
    }

    static boolean saveBitmap(Bitmap bitmap, File path){
        try(FileOutputStream fileOutputStream = new FileOutputStream(path)){
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            return true;
        }catch (IOException e){
            return false;
        }
    }


}
