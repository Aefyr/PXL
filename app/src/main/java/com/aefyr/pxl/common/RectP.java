package com.aefyr.pxl.common;

/**
 * Created by Aefyr on 06.08.2017.
 */

public class RectP {
    public int left = 0;
    public int top = 0;
    public int right = 1;
    public int bottom = 1;

    public RectP() {
    }

    public RectP(int left, int top, int right, int bottom) {
        set(left, top, right, bottom);
    }

    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean contains(int x, int y) {
        return contains((float) x, (float) y);
    }

    public boolean contains(float x, float y) {
        return (right > left ? (x < right && x >= left) : (x < left && x >= right)) && (bottom > top ? (y < bottom && y >= top) : (y < top && y >= bottom));
    }

    public void offset(int x, int y) {
        right += x;
        left += x;
        top += y;
        bottom += y;
    }

    public int height() {
        return Math.abs(bottom - top);
    }

    public int width() {
        return Math.abs(right - left);
    }

    public boolean overlaps(RectP other) {
        if (other.height() <= height() && other.width() <= width())
            return (contains(other.left, other.top) || contains(other.right, other.top) || contains(other.left, other.bottom) || contains(other.right, other.bottom));
        else {
            int thisMinY = top < bottom ? top : bottom;
            int thisMaxY = thisMinY == bottom ? top : bottom;
            int thisMinX = left < right ? left : right;
            int thisMaxX = thisMinX == right ? left : right;

            int otherMinY = other.top < other.bottom ? other.top : other.bottom;
            int otherMaxY = otherMinY == other.bottom ? other.top : other.bottom;
            int otherMinX = other.left < other.right ? other.left : other.right;
            int otherMaxX = otherMinX == other.right ? other.left : other.right;

            if (other.height() > height())
                return (((otherMinY < thisMinY && otherMaxY > thisMinY) || (otherMaxY > thisMaxY && otherMinY < thisMaxY)) && ((otherMinX < thisMinX && otherMaxX > thisMaxX) || (otherMinX >= thisMinX && otherMinX <= thisMaxX) || (otherMaxX <= thisMaxX && otherMaxX >= thisMinX)));
            else
                return (((otherMinX < thisMinX && otherMaxX > thisMaxX) || otherMaxX > thisMaxX && otherMinX < thisMaxX) && ((otherMinY < thisMinY && otherMaxY > thisMaxY) || (otherMinY >= thisMinY && otherMinY < thisMaxY) || (otherMaxY >= thisMinY && otherMaxY <= thisMaxY)));
        }
    }


}
