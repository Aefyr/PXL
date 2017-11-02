package com.af.pxl.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Aefyr on 02.11.2017.
 */

public class Ruler {
    private static Ruler instance;
    private Context c;
    private SharedPreferences preferences;

    private Ruler(Context c){
        this.c = c;
        preferences = PreferenceManager.getDefaultSharedPreferences(c);
        instance = this;
    }

    public static Ruler getInstance(Context c){
        return instance==null?new Ruler(c):instance;
    }

    public int maxDimensionSize(){
        return preferences.getBoolean("allow_512", false)?512:256;
    }
}
