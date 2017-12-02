package com.af.pxl.custom;

import android.support.annotation.DrawableRes;

/**
 * Created by Aefyr on 02.12.2017.
 */

public class ItemWithIcon{
    String text;
    int icon;
    public ItemWithIcon(String text, @DrawableRes int icon){
        this.text = text;
        this.icon = icon;
    }
}
