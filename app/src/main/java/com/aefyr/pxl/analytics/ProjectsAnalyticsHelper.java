package com.aefyr.pxl.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Aefyr on 11.12.2017.
 */

public class ProjectsAnalyticsHelper {
    private static ProjectsAnalyticsHelper instance;
    private FirebaseAnalytics firebaseAnalytics;

    public static ProjectsAnalyticsHelper getInstance(Context c){
        return instance==null?new ProjectsAnalyticsHelper(c):instance;
    }

    private ProjectsAnalyticsHelper(Context c){
        firebaseAnalytics = FirebaseAnalytics.getInstance(c);
        instance = this;
    }

    public void logProjectCreated(String via){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Projects.EVENT_PROJECT_CREATED_ARG_VIA, via);
        firebaseAnalytics.logEvent(FirebaseConstants.Projects.EVENT_PROJECT_CREATED, bundle);
    }

    public void logProjectOpened(){
        firebaseAnalytics.logEvent(FirebaseConstants.Projects.EVENT_PROJECT_OPENED, null);
    }

    public void logProjectExported(boolean shared){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Projects.EVENT_PROJECT_EXPORTED_ARG_SHARED, String.valueOf(shared));
        firebaseAnalytics.logEvent(FirebaseConstants.Projects.EVENT_PROJECT_EXPORTED, bundle);
    }

}
