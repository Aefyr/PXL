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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.aefyr.pxl.ColorPicker;
import com.aefyr.pxl.R;
import com.aefyr.pxl.palettes.HexColorPicker;
import com.aefyr.pxl.util.Utils;

/**
 * Created by Aefyr on 29.07.2017.
 */

public class TransparentColorBackgroundPreference extends Preference {

    private Spinner optionsSpinner;
    private int currentValue = 1;
    private boolean firstSpinnerSelection = true;

    private Button colorPickButton;
    private LinearLayout colorConfig;

    public TransparentColorBackgroundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private ColorPicker colorPicker;

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        colorPickButton = (Button) holder.findViewById(R.id.colorPickButton);
        colorPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorEditDialog();
            }
        });
        colorConfig = (LinearLayout) holder.findViewById(R.id.colorConfig);
        optionsSpinner = (Spinner) holder.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getContext().getResources().getStringArray(R.array.trans_bg_options));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        optionsSpinner.setAdapter(adapter);
        optionsSpinner.setPrompt(getContext().getString(R.string.trans_canvas_bg));
        switch (currentValue) {
            case 1:
                optionsSpinner.setSelection(0);
                break;
            case 2:
                optionsSpinner.setSelection(1);
                break;
            default:
                optionsSpinner.setSelection(2);
                colorConfig.setVisibility(View.VISIBLE);
                updateColor();
                break;
        }

        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (firstSpinnerSelection) {
                    firstSpinnerSelection = false;
                    return;
                }
                switch (i) {
                    case 0:
                        colorConfig.setVisibility(View.GONE);
                        currentValue = 1;
                        persistInt(currentValue);
                        break;
                    case 1:
                        colorConfig.setVisibility(View.GONE);
                        currentValue = 2;
                        persistInt(currentValue);
                        break;
                    case 2:
                        colorConfig.setVisibility(View.VISIBLE);
                        currentValue = Color.WHITE;
                        updateColor();
                        persistInt(currentValue);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void updateColor() {
        colorPickButton.setBackgroundColor(currentValue);
        colorPickButton.setTextColor(Utils.invertColor(currentValue));
    }

    private void openColorEditDialog() {
        final AlertDialog colorEditDialog = new AlertDialog.Builder(getContext()).setView(R.layout.color_picker).setPositiveButton(getContext().getString(R.string.done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentValue = colorPicker.getColor();
                updateColor();
                persistInt(currentValue);
                colorPicker = null;
            }
        }).setNegativeButton(getContext().getString(R.string.cancel), null).setTitle(getContext().getString(R.string.edit_color)).create();
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
        int c = Color.WHITE;
        if (currentValue < 0)
            c = currentValue;
        colorPicker = new ColorPicker(colorEditDialog.getWindow(), c);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if (restorePersistedValue) {
            currentValue = getPersistedInt(currentValue);
        } else {
            currentValue = (int) defaultValue;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, currentValue);
    }
}
