package com.aefyr.pxl.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aefyr.pxl.R;

import java.text.DecimalFormat;

/**
 * Created by Aefyr on 28.07.2017.
 */

public class CursorSensitivityPreference extends Preference {
    public CursorSensitivityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private SeekBar seekBar;
    private int currentSensitivity;
    private DecimalFormat oneDigitAfterPoint;

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        oneDigitAfterPoint = new DecimalFormat("##.#");
        seekBar = ((SeekBar) holder.findViewById(R.id.cursorSensSeekbar));
        sensitivity = (TextView) holder.findViewById(R.id.cursorSensValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentSensitivity = i;
                updateText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                persistInt(seekBar.getProgress());
            }
        });
        seekBar.setProgress(currentSensitivity);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 9);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if (restorePersistedValue) {
            currentSensitivity = getPersistedInt(9);
        } else {
            currentSensitivity = (int) defaultValue;
        }

    }

    private TextView sensitivity;

    private void updateText() {
        sensitivity.setText(oneDigitAfterPoint.format(0.1f + currentSensitivity / 10f) + "x");
    }
}
