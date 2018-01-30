package com.aefyr.pxl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.aefyr.pxl.fragments.PreferencesFragment;

public class SettingsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        PreferencesFragment preferenceFragment = (PreferencesFragment) getFragmentManager().findFragmentByTag("prefs");

        if(preferenceFragment==null){
            getFragmentManager().beginTransaction().add(R.id.container, new PreferencesFragment(), "prefs").commit();
        }

        getSupportActionBar().setTitle(R.string.title_activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
