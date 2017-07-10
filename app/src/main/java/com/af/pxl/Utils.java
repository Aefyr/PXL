package com.af.pxl;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

/**
 * Created by Aefyr on 01.07.2017.
 */

class Utils {
    static float clamp(float x, float min, float max){
        if(x<min)return min;
        if(x>max) return max;
        return x;
    }

    static float vector2Distance(float x1, float y1, float x2, float y2){
        return (float) (Math.sqrt(Math.pow(x1-x2, 2)+ Math.pow(y1-y2, 2)));
    }

    static int invertColor(int color){
        return Color.rgb(255-Color.red(color), 255-Color.green(color), 255 - Color.blue(color));
    }

    static void toaster(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
