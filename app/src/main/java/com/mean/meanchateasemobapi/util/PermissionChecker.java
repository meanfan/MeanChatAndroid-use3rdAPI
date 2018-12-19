package com.mean.meanchateasemobapi.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionChecker {
    private static final String PERMISSION_NAME_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_NAME_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String PERMISSION_NAME_CAMERA = Manifest.permission.CAMERA;
    public static final int PERMISSION_CHECK_REQUEST_STORAGE = 10;
    public static final int PERMISSION_CHECK_REQUEST_RECORD_AUDIO = 11;
    public static final int PERMISSION_CHECK_REQUEST_CAMERA = 12;



    public static boolean checkPermission(Activity currentActivity, String permissionName, int requestCode){

        if (ContextCompat.checkSelfPermission(currentActivity, permissionName)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{permissionName},
                    requestCode);
            return false;
        }
        return true;
    }

    public static boolean checkStoragePermission(Activity currentActivity){
        return checkPermission(currentActivity,PERMISSION_NAME_EXTERNAL_STORAGE,PERMISSION_CHECK_REQUEST_STORAGE);
    }

    public static boolean checkRecordAudioPermission(Activity currentActivity){
        return checkPermission(currentActivity,PERMISSION_NAME_RECORD_AUDIO,PERMISSION_CHECK_REQUEST_RECORD_AUDIO);
    }

    public static boolean checkCameraPermission(Activity currentActivity){
        return checkPermission(currentActivity,PERMISSION_NAME_CAMERA,PERMISSION_CHECK_REQUEST_CAMERA);
    }
}
