package com.aefyr.pxl.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aefyr.pxl.R;

import java.text.DecimalFormat;

/**
 * Created by Aefyr on 28.07.2017.
 */

public class CursorOpacityPreference extends Preference {

    private int currentOpacity;

    private SeekBar seekBar;
    private TextView value;
    private ImageView preview;

    private DecimalFormat twoDigitsAfterPoint;

    public CursorOpacityPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        twoDigitsAfterPoint = new DecimalFormat("##.##");
        seekBar = (SeekBar) holder.findViewById(R.id.cursorOpacitySeekbar);
        value = (TextView) holder.findViewById(R.id.cursorOpacityValue);
        preview = (ImageView) holder.findViewById(R.id.cursorOpacityPreview);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentOpacity = i;
                updateValueAndPreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                persistInt(seekBar.getProgress());
            }
        });
        seekBar.setProgress(currentOpacity);
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 9);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if (restorePersistedValue) {
            currentOpacity = getPersistedInt(255);
        } else {
            currentOpacity = (int) defaultValue;
        }
    }

    private void updateValueAndPreview() {
        float alpha = currentOpacity / 255f;
        value.setText(twoDigitsAfterPoint.format(alpha * 100) + "%");
        preview.setAlpha(alpha);
    }
}
