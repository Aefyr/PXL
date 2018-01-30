package com.aefyr.pxl.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.aefyr.pxl.MainActivity;
import com.aefyr.pxl.R;
import com.aefyr.pxl.palettes.PaletteMaker;
import com.aefyr.pxl.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesFragment extends PreferenceFragment {

    public static final String CURSOR_SENSITIVITY = "cursor_sensitivity";
    public static final String CURSOR_OPACITY = "cursor_opacity";
    public static final String GRID_COLOR = "grid_color";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String TRANSPARENT_CANVAS_BACKGROUND_COLOR = "trans_background";
    public static final String AUTO_SWITCH_AFTER_CS = "abacp";
    public static final String FLAWLESS_SYMMETRY = "flawless_symmetry";
    public static final String SYM_AXISES_COLOR = "sym_axises_color";


    public PreferencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        findPreference("restore_palettes").setOnPreferenceChangeListener(new android.support.v7.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.support.v7.preference.Preference preference, Object newValue) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.warn).setMessage(R.string.palettes_override_warn).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PaletteMaker.generateDefaultPalettes(getActivity());
                        ((MainActivity)getActivity()).destroyFragments(MainActivity.FRAGMENT_PALETTES);
                        Utils.toaster(getActivity(), getString(R.string.palettes_restored));
                    }
                }).setNegativeButton(R.string.cancel, null).create().show();
                return true;
            }
        });

        findPreference("hardware_accelerated").setOnPreferenceChangeListener(new android.support.v7.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.support.v7.preference.Preference preference, Object newValue) {
                if(!((boolean)newValue))
                    Utils.easyAlert(getActivity(), getString(R.string.warn), getText(R.string.hw_warn)).show();

                return true;
            }
        });
    }
}
