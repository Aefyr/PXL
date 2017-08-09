package com.af.pxl;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.os.Build;
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

    static boolean doesRectContains(Rect rect, int x, int y){
        return rect.right>rect.left?(x<=rect.right&&x>=rect.left):(x<=rect.left&&x>=rect.right)&&rect.bottom>rect.top?(y<=rect.bottom&&y>=rect.top):(y<=rect.top&&y>=rect.bottom);
    }

    public static float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    public static float[] signedVector2Distance(float x1, float y1, float x2, float y2){
        float distance = (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));

        float[] d = new float[]{distance, distance};
        if(x2<x1)
            d[0] = -d[0];
        if(y2<y1)
            d[1] = -d[1];

        return d;
    }

    public static float vector2Angle(float x1, float y1, float x2, float y2){
        float angle = (float) Math.toDegrees(Math.acos((x1*x2+y1*y2)/(Math.sqrt((x1*x1+y1*y1))*(Math.sqrt((x2*x2+y2*y2))))));
        if(x2<x1)
            angle=-angle;
        return angle;
    }

    public static float getClosestNumber(float x, float[] ascendingNumbers){
        int i = 0;

        if(x<ascendingNumbers[i])
            return ascendingNumbers[i];

        do{
            i++;
        }while(i<ascendingNumbers.length&&ascendingNumbers[i]<x);

        if(i==ascendingNumbers.length-1)
            return ascendingNumbers[i];

        if(ascendingNumbers[i]-x>x-ascendingNumbers[i-1])
            return ascendingNumbers[i-1];
        else
            return ascendingNumbers[i];
    }

    public static int invertColor(int color){
        return Color.rgb(255-Color.red(color), 255-Color.green(color), 255 - Color.blue(color));
    }

    public static int lerpColor(int color1, int color2, float a){
        return Color.RED;
    }

    public static float colorsDifference(int color1, int color2){
        return (Math.abs(Color.red(color1)-Color.red(color2))/255f+Math.abs(Color.blue(color1)-Color.blue(color2))/255f+Math.abs(Color.blue(color1)-Color.blue(color2))/255f)/3f;
    }

    public static int averageColor(int color1, int color2){
        return Color.rgb((Color.red(color1)+Color.red(color2))/2, (Color.green(color1)+Color.green(color2))/2, (Color.blue(color1)+Color.blue(color2))/2);
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

    public static String colorToHex(int color){
        String r = Integer.toHexString(Color.red(color)).toUpperCase();
        if(r.length()==1)
            r = "0"+r;
        String g = Integer.toHexString(Color.green(color)).toUpperCase();
        if(g.length()==1)
            g = "0"+g;
        String b = Integer.toHexString(Color.blue(color)).toUpperCase();
        if(b.length()==1)
            b = "0"+b;
        return r+g+b;
    }

    public static String correctHexColor(String hexColor){
        if(hexColor.length()<6){
            while(hexColor.length()!=6)
                hexColor+="0";
        }
        return hexColor;
    }

    private static boolean permissionsGranted;
    public static boolean checkPermissions(Context c){
        if(permissionsGranted)
            return true;
        if(Build.VERSION.SDK_INT>=23) {
            if(c.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED&&(c.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED))
                permissionsGranted = true;
        }else
            permissionsGranted = true;
        return permissionsGranted;
    }



}
