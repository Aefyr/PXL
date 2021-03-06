package com.aefyr.pxl.util;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.Toast;

import com.aefyr.pxl.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Aefyr on 01.07.2017.
 */

public class Utils {


    public static float dpToPx(int dp, Resources r) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float getScreenWidth(Resources r) {
        return r.getDisplayMetrics().widthPixels;
    }

    public static float getScreenHeight(Resources r) {
        return r.getDisplayMetrics().heightPixels;
    }

    public static float clamp(float a, float min, float max) {
        if (a < min) return min;
        if (a > max) return max;
        return a;
    }

    public static int clamp(int a, int min, int max) {
        if (a < min) return min;
        if (a > max) return max;
        return a;
    }

    public static float vector2Distance(float x1, float y1, float x2, float y2) {
        return (float) (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    public static float[] signedVector2Distance(float x1, float y1, float x2, float y2) {
        float distance = vector2Distance(x1, y1, x2, y2);

        float[] d = new float[]{distance, distance};
        if (x2 < x1)
            d[0] = -d[0];
        if (y2 < y1)
            d[1] = -d[1];

        return d;
    }

    public static float vector2Angle(float x1, float y1, float x2, float y2) {
        float angle = (float) Math.toDegrees(Math.acos((x1 * x2 + y1 * y2) / (Math.sqrt((x1 * x1 + y1 * y1)) * (Math.sqrt((x2 * x2 + y2 * y2))))));
        if (x2 < x1)
            angle = -angle;
        return angle;
    }

    public static float getClosestNumber(float x, float[] ascendingNumbers) {
        int i = 0;

        if (x < ascendingNumbers[i])
            return ascendingNumbers[i];

        do {
            i++;
        } while (i < ascendingNumbers.length && ascendingNumbers[i] < x);

        if (i == ascendingNumbers.length - 1)
            return ascendingNumbers[i];

        if (ascendingNumbers[i] - x > x - ascendingNumbers[i - 1])
            return ascendingNumbers[i - 1];
        else
            return ascendingNumbers[i];
    }

    public static float lerp(float a, float b, float t){
        return a+(b-a)*t;
    }

    public static int invertColor(int color) {
        return Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
    }

    public static int lerpColor(int color1, int color2, float a) {
        return Color.rgb((int)lerp(Color.red(color1), Color.red(color2), a), (int) lerp(Color.green(color1), Color.green(color2), a), (int)lerp(Color.blue(color1), Color.blue(color2), a));
    }

    @Deprecated
    public static float colorsDifference(int color1, int color2) {
        return (Math.abs(Color.red(color1) - Color.red(color2)) / 255f + Math.abs(Color.blue(color1) - Color.blue(color2)) / 255f + Math.abs(Color.blue(color1) - Color.blue(color2)) / 255f) / 3f;
    }

    public static int averageColor(int... colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        for(int color: colors){
            r+=Color.red(color);
            g+=Color.green(color);
            b+=Color.blue(color);
        }
        return Color.rgb(r/colors.length, g/colors.length, b/colors.length);
    }

    public static void toaster(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void setBitmapPixelsFromOtherBitmap(Bitmap bitmap, Bitmap otherBitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        otherBitmap.getPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
        bitmap.setPixels(pixels, 0, otherBitmap.getWidth(), 0, 0, otherBitmap.getWidth(), otherBitmap.getHeight());
    }

    public static boolean saveBitmap(Bitmap bitmap, File path) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(path)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            return true;
        } catch (IOException e) {
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

    public static void alternativeAddImageToGallery(Context c, File imagePath) {
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

            try (InputStream in = new FileInputStream(sourceLocation); OutputStream out = new FileOutputStream(targetLocation)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String colorToHex(int color) {
        StringBuilder hexBuilder = new StringBuilder();

        String r = Integer.toHexString(Color.red(color));
        if(r.length()==1)
            hexBuilder.append(0);
        hexBuilder.append(r);

        String g = Integer.toHexString(Color.green(color));
        if(g.length()==1)
            hexBuilder.append(0);
        hexBuilder.append(g);

        String b = Integer.toHexString(Color.blue(color));
        if(b.length()==1)
            hexBuilder.append(0);
        hexBuilder.append(b);

        return hexBuilder.toString().toUpperCase();
    }

    public static int hexToColor(String hex, boolean ascendingOrder){
        int base = 0xFF000000;
        char[] hexChars = hex.toCharArray();

        for(int i = 0; i<hexChars.length; i++)
            base |= hexCharToInt(hexChars[i]) << (ascendingOrder?i:5-i)*4;

        return base;
    }

    private static int hexCharToInt(char hexChar){
        hexChar = Character.toLowerCase(hexChar);
        switch (hexChar){
            case 'a':
                return 10;
            case 'b':
                return 11;
            case 'c':
                return 12;
            case 'd':
                return 13;
            case 'e':
                return 14;
            case 'f':
                return 15;
        }
        return Character.getNumericValue(hexChar);
    }

    public static String correctHexColor(String hexColor) {
        if (hexColor.length() < 6) {
            while (hexColor.length() != 6)
                hexColor += "0";
        }
        return hexColor;
    }

    public static AlertDialog easyAlert(Context c, @Nullable CharSequence title, @Nullable CharSequence message){
        AlertDialog.Builder builder = new AlertDialog.Builder(c).setPositiveButton(c.getString(R.string.ok), null);

        if(title!=null)
            builder.setTitle(title);
        if(message!=null)
            builder.setMessage(message);

        return builder.create();
    }

    public static void highlightET(final EditText editText){
        ValueAnimator colorAnimator = new ValueAnimator();
        colorAnimator.setIntValues(Color.RED, editText.getResources().getColor(R.color.colorEditTextHint));
        colorAnimator.setEvaluator(new ArgbEvaluator());
        colorAnimator.setRepeatCount(6);
        colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimator.setDuration(100);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                editText.setHintTextColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        editText.requestFocus();
        colorAnimator.start();
    }

    public static void deleteRecursive(File file){
        if(file.isDirectory()){
            for(File child: file.listFiles())
                deleteRecursive(child);
        }
        file.delete();
    }

    public static boolean copyFile(File source, File dest){
        try(FileInputStream inputStream = new FileInputStream(source); FileOutputStream outputStream = new FileOutputStream(dest, false); FileChannel inputChannel = inputStream.getChannel()){
            outputStream.getChannel().transferFrom(inputChannel, 0, inputChannel.size());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
