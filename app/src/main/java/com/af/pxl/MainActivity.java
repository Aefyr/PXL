package com.af.pxl;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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

import com.af.pxl.TCA.TutorialActivity;
import com.af.pxl.fragments.GalleryFragment;
import com.af.pxl.fragments.PalettesFragment;
import com.af.pxl.fragments.PreferencesFragment;
import com.af.pxl.palettes.PaletteMaker;
import com.af.pxl.palettes.PaletteUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private android.app.FragmentManager fragmentManager;

    private enum PXLFragment {
        UNINITIALIZED, GALLERY, PALETTES, PREFERENCES
    }

    private Fragment currentFragment;
    private PXLFragment currentPxlFragment = PXLFragment.UNINITIALIZED;

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
        if (preferences.getBoolean("first_start", true)) {
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

            if(Build.VERSION.SDK_INT<21) {
                new AlertDialog.Builder(this).setTitle(getString(R.string.warn)).setMessage(getString(R.string.comp_mode)).setPositiveButton(getString(R.string.ok), null).setCancelable(false).create().show();
            }
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

        setFragment(PXLFragment.GALLERY);
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
            setFragment(PXLFragment.PREFERENCES);
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
            setFragment(PXLFragment.GALLERY);
        } else if (id == R.id.nav_palettes) {
            setFragment(PXLFragment.PALETTES);
        } else if (id == R.id.nav_community) {
            setFragment(PXLFragment.PREFERENCES);
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_tutorial) {
            Intent tut = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(tut);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setFragment(PXLFragment pxlFragment) {
        if (currentPxlFragment == pxlFragment)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != null)
            transaction.hide(currentFragment);

        switch (pxlFragment) {
            case GALLERY:
                Fragment gallery = fragmentManager.findFragmentByTag(FragmentTag.GALLERY);
                if (gallery == null) {
                    gallery = new GalleryFragment();
                    transaction.add(R.id.container, gallery, FragmentTag.GALLERY);
                    System.out.println("created gallery");
                } else
                    transaction.show(gallery);

                currentFragment = gallery;
                actionBar.setTitle(getString(R.string.projects));
                break;
            case PALETTES:
                Fragment palettes = fragmentManager.findFragmentByTag(FragmentTag.PALETTES);
                if (palettes == null) {
                    palettes = new PalettesFragment();
                    transaction.add(R.id.container, palettes, FragmentTag.PALETTES);
                    System.out.println("created palettes");
                } else
                    transaction.show(palettes);

                currentFragment = palettes;
                actionBar.setTitle(getString(R.string.palettes));
                break;
            case PREFERENCES:
                Fragment prefs = fragmentManager.findFragmentByTag(FragmentTag.PREFS);
                if (prefs == null) {
                    prefs = new PreferencesFragment();
                    transaction.add(R.id.container, prefs, FragmentTag.PREFS);
                    System.out.println("created prefs");
                } else
                    transaction.show(prefs);

                currentFragment = prefs;
                actionBar.setTitle(getString(R.string.title_activity_settings));
                break;
        }

        transaction.commit();

    }

    private class FragmentTag {
        private static final String GALLERY = "GALLERY";
        private static final String PALETTES = "PALETTES";
        private static final String PREFS = "PREFS";
    }
}
