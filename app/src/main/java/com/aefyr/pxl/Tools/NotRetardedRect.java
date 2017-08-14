package com.aefyr.pxl.Tools;

/**
 * Created by Aefyr on 06.08.2017.
 */

public class NotRetardedRect {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public void set(int left, int top, int right, int bottom){
        this.left =left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean contains(int x, int y){
        return (right>left?(x<right&&x>=left):(x<left&&x>=right))&&(bottom>top?(y<bottom&&y>=top):(y<top&&y>=bottom));
    }

    public void offset(int x, int y){
        right+=x;
        left+=x;
        top+=y;
        bottom+=y;
    }

    public int height(){
        return Math.abs(right-left);
    }

    public int width(){
        return Math.abs(bottom-top);
    }


}
