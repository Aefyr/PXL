package com.af.pxl.custom;

/**
 * Created by Aefyr on 14.10.2017.
 */

public class Vector2 {
    public int x;
    public int y;

    public Vector2(int x, int y){
        set(x, y);
    }

    public void set(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static float distance(Vector2 a, Vector2 b){
        return (float) (Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)));
    }
}
