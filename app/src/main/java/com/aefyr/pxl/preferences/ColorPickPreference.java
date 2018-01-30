package com.aefyr.pxl.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.aefyr.pxl.ColorPicker;
import com.aefyr.pxl.R;
import com.aefyr.pxl.custom.ColorCircle;
import com.aefyr.pxl.palettes.HexColorPicker;

/**
 * Created by Aefyr on 29.07.2017.
 */

public class ColorPickPreference extends Preference implements Preference.OnPreferenceClickListener {
    public ColorPickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreferenceClickListener(this);
    }

    private int currentColor = Color.BLACK;
    private ColorPicker colorPicker;
    private ColorCircle colorCircle;

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ((TextView) holder.findViewById(R.id.preferenceName)).setText(getTitle());
        colorCircle = (ColorCircle) holder.findViewById(R.id.colorCircle);
        colorCircle.setColor(currentColor);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final AlertDialog colorEditDialog = new AlertDialog.Builder(getContext()).setView(R.layout.color_picker).setPositiveButton(getContext().getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentColor = colorPicker.getColor();
                persistInt(currentColor);
                colorCircle.setColor(currentColor);
                colorPicker = null;
            }
        }).setNegativeButton(getContext().getString(R.string.cancel), null).setTitle(getContext().getString(R.string.edit_color)).setNeutralButton(R.string.hex, null).create();
        colorEditDialog.show();
        colorEditDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HexColorPicker(getContext(), colorPicker.getColor(), new HexColorPicker.OnColorPickListener() {
                    @Override
                    public void onColorPicked(int color) {
                        colorPicker.setColor(color);
                    }
                }).show();
            }
        });
        colorPicker = new ColorPicker(colorEditDialog.getWindow(), currentColor);
        return true;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if (restorePersistedValue) {
            currentColor = getPersistedInt(currentColor);
        } else {
            currentColor = (int) defaultValue;
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, currentColor);
    }

}
