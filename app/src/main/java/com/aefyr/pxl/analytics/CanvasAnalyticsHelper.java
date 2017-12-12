package com.aefyr.pxl.analytics;

import android.content.Context;
import android.os.Bundle;

import com.aefyr.pxl.AdaptivePixelSurfaceH;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Aefyr on 10.12.2017.
 */

public class CanvasAnalyticsHelper {
    private static CanvasAnalyticsHelper instance;
    private FirebaseAnalytics firebaseAnalytics;

    public static CanvasAnalyticsHelper getInstance(Context c){
        return instance==null?new CanvasAnalyticsHelper(c):instance;
    }

    private CanvasAnalyticsHelper(Context c){
        firebaseAnalytics = FirebaseAnalytics.getInstance(c);
        instance = this;
    }

    public void logToolPicked(AdaptivePixelSurfaceH.Tool tool){
        String toolValue = "undefined";
        switch (tool){
            case PENCIL:
                toolValue = FirebaseConstants.Tools.TOOL_PENCIL;
                break;
            case FLOOD_FILL:
                toolValue = FirebaseConstants.Tools.TOOL_FILL;
                break;
            case COLOR_PICK:
                toolValue = FirebaseConstants.Tools.TOOL_COLOR_PICKER;
                break;
            case COLOR_SWAP:
                toolValue = FirebaseConstants.Tools.TOOL_COLOR_SWAPPER;
                break;
            case ERASER:
                toolValue = FirebaseConstants.Tools.TOOL_ERASER;
                break;
            case MULTISHAPE:
                toolValue = FirebaseConstants.Tools.TOOL_MULTISHAPE;
                break;
            case SELECTOR:
                toolValue = FirebaseConstants.Tools.TOOL_SELECTOR;
                break;
        }
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Tools.EVENT_TOOL_PICKED_ARG_TOOL, toolValue);
        firebaseAnalytics.logEvent(FirebaseConstants.Tools.EVENT_TOOL_PICKED, bundle);
    }

    public void logSymmetryChange(boolean symmetryEnabled, AdaptivePixelSurfaceH.SymmetryType mode){
        String modeValue = "undefined";
        switch (mode){
            case HORIZONTAL:
                modeValue = FirebaseConstants.Canvas.MODE_HORIZONTAL;
                break;
            case VERTICAL:
                modeValue = FirebaseConstants.Canvas.MODE_VERTICAL;
                break;
        }
        Bundle bundle = new Bundle(2);
        bundle.putString(FirebaseConstants.Generic.GENERIC_ARG_ENABLED, String.valueOf(symmetryEnabled));
        bundle.putString(FirebaseConstants.Canvas.EVENT_SYMMETRY_CHANGED_ARG_MODE, modeValue);
        firebaseAnalytics.logEvent(FirebaseConstants.Canvas.EVENT_SYMMETRY_CHANGED, bundle);
    }

    public void logCursorModeChange(boolean cursorModeEnabled){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Generic.GENERIC_ARG_ENABLED, String.valueOf(cursorModeEnabled));
        firebaseAnalytics.logEvent(FirebaseConstants.Canvas.EVENT_CURSOR_MODE_CHANGED, bundle);
    }

    public void logGridVisibilityChange(boolean gridShown){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Generic.GENERIC_ARG_ENABLED, String.valueOf(gridShown));
        firebaseAnalytics.logEvent(FirebaseConstants.Canvas.EVENT_GRID_VISIBILITY_CHANGED, bundle);
    }

    public void logCanvasSpecialAction(String action){
        Bundle bundle = new Bundle(1);
        bundle.putString(FirebaseConstants.Canvas.EVENT_CANVAS_SPECIAL_ACTION_ARG_ACTION, action);
        firebaseAnalytics.logEvent(FirebaseConstants.Canvas.EVENT_CANVAS_SPECIAL_ACTION, bundle);
    }
}
