package com.af.pxl.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.af.pxl.R;

/**
 * Created by Aefyr on 22.11.2017.
 */

public class PermissionsUtils {
    public static final int CODE_STORAGE_PERMISSIONS_REQUEST = 3232;
    private static boolean permissionsGranted;

    public static boolean checkStoragePermissions(Context c) {
        if (permissionsGranted)
            return true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (c.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && (c.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                permissionsGranted = true;
        } else
            permissionsGranted = true;
        return permissionsGranted;
    }

    public static void requestStoragePermissions(Activity from){
        if (Build.VERSION.SDK_INT >= 23)
            from.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_STORAGE_PERMISSIONS_REQUEST);
    }

    public static void requestStoragePermissions(Fragment from){
        if (Build.VERSION.SDK_INT >= 23)
            from.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_STORAGE_PERMISSIONS_REQUEST);
    }

    public static void showNoStoragePermissionWarning(Context c){
        Utils.easyAlert(c,null , c.getString(R.string.storage_permissions_denied)).show();
    }
}
