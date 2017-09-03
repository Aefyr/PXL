package com.aefyr.pxl;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aefyr.pxl.fragments.GalleryFragment;
import com.aefyr.pxl.fragments.PalettesFragment;
import com.aefyr.pxl.fragments.PreferencesFragment;
import com.aefyr.pxl.palettes.PaletteMaker;
import com.aefyr.pxl.palettes.PaletteUtils;
import com.aefyr.pxl.TCA.TutorialActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private android.app.FragmentManager fragmentManager;

    private enum FRAGMENT{
        GALLERY, TEST, PREFERENCES
    }

    private NavigationView navigationView;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("first_start", true)){
            preferences.edit().putBoolean("first_start", false).apply();
            PaletteUtils.initialize(this);
            PaletteUtils.defaultPalette();
            PaletteMaker.generateDefaultPalettes(this);
            new AlertDialog.Builder(this).setMessage(getString(R.string.tutorial_prompt)).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Start tutorial
                    Intent tut = new Intent(MainActivity.this, TutorialActivity.class);
                    startActivity(tut);
                }
            }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new AlertDialog.Builder(MainActivity.this).setMessage(getString(R.string.tutorial_hint)).setPositiveButton(getString(R.string.ok), null).create().show();
                }
            }).setCancelable(false).create().show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        actionBar = getSupportActionBar();

        //Fragments
        fragmentManager = getFragmentManager();
        PaletteUtils.initialize(this);

        loadFragment(FRAGMENT.GALLERY);
        navigationView.setCheckedItem(R.id.nav_gallery);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            navigationView.setCheckedItem(R.id.nav_community);
            loadFragment(FRAGMENT.PREFERENCES);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            loadFragment(FRAGMENT.GALLERY);
        } else if (id == R.id.nav_palettes) {
            loadFragment(FRAGMENT.TEST);
        } else if (id == R.id.nav_community) {
            loadFragment(FRAGMENT.PREFERENCES);
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_tutorial) {
            Intent tut = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(tut);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // FIXME: 17.07.2017 This is retarded
    void loadFragment(FRAGMENT fragment){
        switch (fragment){
            case GALLERY:
                GalleryFragment gallery = (GalleryFragment) fragmentManager.findFragmentByTag("Gallery");
                if(gallery==null){
                    gallery = new GalleryFragment();
                    //fragmentManager.beginTransaction().add(R.id.container, gallery, "Gallery").commit();
                    System.out.println("created gallery");
                }
                fragmentManager.beginTransaction().replace(R.id.container, gallery, "Gallery").commit();
                actionBar.setTitle(getString(R.string.projects));
                break;
            case TEST:
                PalettesFragment palettes = (PalettesFragment) fragmentManager.findFragmentByTag("Palettes");
                if(palettes==null){
                    palettes = new PalettesFragment();
                    //fragmentManager.beginTransaction().add(R.id.container, test, "Test").commit();
                    System.out.println("created test");
                }
                fragmentManager.beginTransaction().replace(R.id.container, palettes, "Palettes").commit();
                actionBar.setTitle(getString(R.string.palettes));
                break;
            case PREFERENCES:
                PreferencesFragment preferences = (PreferencesFragment) fragmentManager.findFragmentByTag("Preferences");
                if(preferences==null){
                    preferences = new PreferencesFragment();
                    //fragmentManager.beginTransaction().add(R.id.container, test, "Test").commit();
                    System.out.println("created test");
                }
                fragmentManager.beginTransaction().replace(R.id.container, preferences, "Preferences").commit();
                actionBar.setTitle(getString(R.string.title_activity_settings));
                break;
        }
    }
}
