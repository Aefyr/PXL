package com.aefyr.pxl.Fragments;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aefyr.pxl.Palettes.PaletteMaker;
import com.aefyr.pxl.R;
import com.aefyr.pxl.Utils;

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
                PaletteMaker.generateDefaultPalettes(getActivity());
                Utils.toaster(getActivity(), getString(R.string.palettes_restored));
                return true;
            }
        });

    }
}
