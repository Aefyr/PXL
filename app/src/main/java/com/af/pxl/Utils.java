package com.af.pxl;

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
}
