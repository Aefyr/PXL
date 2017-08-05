package com.af.pxl.TCA;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;

/**
 * Created by Aefyr on 05.08.2017.
 */

public class TutorialFrame {
    String title;
    String text;
    int image;

    public TutorialFrame(String title, String text, @DrawableRes int image){
        this.title = title;
        this.text = text;
        this.image = image;
    }
}
