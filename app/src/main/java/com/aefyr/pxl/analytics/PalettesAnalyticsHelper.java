package com.aefyr.pxl.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Aefyr on 12.12.2017.
 */

public class PalettesAnalyticsHelper {
    private static PalettesAnalyticsHelper instance;
    private FirebaseAnalytics firebaseAnalytics;

    public static PalettesAnalyticsHelper getInstance(Context c){
        return instance==null?new PalettesAnalyticsHelper(c):instance;
    }

    private PalettesAnalyticsHelper(Context c){
        firebaseAnalytics = FirebaseAnalytics.getInstance(c);
        instance = this;
    }

    public void logPaletteCreated(String via){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Projects.EVENT_PROJECT_CREATED_ARG_VIA, via);
        firebaseAnalytics.logEvent(FirebaseConstants.Palettes.EVENT_PALETTE_CREATED, bundle);
    }
}
