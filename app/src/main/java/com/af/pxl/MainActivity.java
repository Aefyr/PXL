package com.af.pxl;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

            if((Runtime.getRuntime().maxMemory()/1024/1024)>=128)
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("allow_512", true).apply();

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
            navigationView.setCheckedItem(R.id.nav_prefs);
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

        switch (id){
            case R.id.nav_gallery:
                setFragment(PXLFragment.GALLERY);
                break;
            case R.id.nav_palettes:
                setFragment(PXLFragment.PALETTES);
                break;
            case R.id.nav_prefs:
                setFragment(PXLFragment.PREFERENCES);
                break;
            case R.id.nav_feedback:
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setType("text/plain");
                mailIntent.setData(Uri.parse("mailto:polychromaticfox+pxl@gmail.com?subject="+getString(R.string.feedback_mail_subject)));
                startActivity(Intent.createChooser(mailIntent, getString(R.string.feedback)));
                break;
            case R.id.nav_about:
                new AlertDialog.Builder(this).setMessage("mem: "+Runtime.getRuntime().maxMemory()/1024/1024).setPositiveButton(R.string.ok, null).create().show();
                break;
            /*case R.id.nav_tutorial:
                Intent tut = new Intent(MainActivity.this, TutorialActivity.class);
                startActivity(tut);
                break;*/
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
    public void notifyProjectOpened(){
        destroyFragments(FragmentTag.PALETTES, FragmentTag.PREFS);
    }

    public void destroyFragments(String... tags){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for(String tag: tags){
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if(fragment!=null)
                transaction.remove(fragment);
        }
        transaction.commit();
    }

    public class FragmentTag {
        public static final String GALLERY = "GALLERY";
        public static final String PALETTES = "PALETTES";
        public static final String PREFS = "PREFS";
    }
}
