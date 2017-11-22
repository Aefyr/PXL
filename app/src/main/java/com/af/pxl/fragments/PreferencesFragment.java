package com.af.pxl.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.af.pxl.MainActivity;
import com.af.pxl.R;
import com.af.pxl.util.Utils;
import com.af.pxl.palettes.PaletteMaker;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreferencesFragment extends PreferenceFragment {

    public static final String CURSOR_SENSITIVITY = "cursor_sensitivity";
    public static final String CURSOR_OPACITY = "cursor_opacity";
    public static final String GRID_COLOR = "grid_color";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String TRANSPARENT_CANVAS_BACKGROUND_COLOR = "trans_background";


    public PreferencesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        findPreference("restore_palettes").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.warn).setMessage(R.string.palettes_override_warn).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PaletteMaker.generateDefaultPalettes(getActivity());
                        ((MainActivity)getActivity()).destroyFragments(MainActivity.FragmentTag.PALETTES);
                        Utils.toaster(getActivity(), getString(R.string.palettes_restored));
                    }
                }).setNegativeButton(R.string.cancel, null).create().show();
                return true;
            }
        });

        findPreference("hardware_accelerated").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(!((boolean)newValue))
                    Utils.easyAlert(getActivity(), getString(R.string.warn), getText(R.string.hw_warn)).show();

                return true;
            }
        });

    }
}
