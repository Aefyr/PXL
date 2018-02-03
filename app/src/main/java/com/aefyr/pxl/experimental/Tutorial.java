package com.aefyr.pxl.experimental;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.aefyr.pxl.R;

/**
 * Created by Aefyr on 03.02.2018.
 */

public class Tutorial {
    private Context c;
    private SharedPreferences tutorialFlags;

    public Tutorial(Context c){
        this.c = c;
        tutorialFlags = c.getSharedPreferences("passed_tutorials", Context.MODE_PRIVATE);
    }

    public void cursorMode(){
        if(!tutorialFlags.getBoolean("cursorMode", false)){
            createTutorialDialog(R.string.cursor, R.string.t2_cursor).show();
            tutorialFlags.edit().putBoolean("cursorMode", true).apply();
        }
    }

    private AlertDialog createTutorialDialog(@StringRes int toolName, @StringRes int text){
        return new AlertDialog.Builder(c).setTitle(String.format(c.getString(R.string.tutorial_for_tool), c.getString(toolName))).setMessage(text).setPositiveButton(R.string.ok, null).create();
    }
}
