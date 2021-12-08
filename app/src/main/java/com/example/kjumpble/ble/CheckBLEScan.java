package com.example.kjumpble.ble;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.content.PermissionChecker;

import com.example.kjumpble.R;

public class CheckBLEScan {
    public static boolean ability (Context context) {
        return PermissionChecker.checkSelfPermission(context, permission()) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean abilityDenied (Context context) {
        return PermissionChecker.checkSelfPermission(context, permission()) == PackageManager.PERMISSION_DENIED;
    }


    /**
     * 根據 Android 版本回傳所需要檢測的全縣
     * Android 12 : BLUETOOTH_SCAN
     * Android 10, 11 : ACCESS_FINE_LOCATION
     * Android 9 and under : ACCESS_COARSE_LOCATION
     *
     * @return 權限
     */
    public static String permission () {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            return Manifest.permission.BLUETOOTH_SCAN;
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R)
            return Manifest.permission.ACCESS_FINE_LOCATION;
        else
            return Manifest.permission.ACCESS_COARSE_LOCATION;
    }

    public static int permissionStrRes () {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            return R.string.to_get_nearby_devices_permission;
        else
            return R.string.to_get_location_permission;
    }

    public static void checkPermissionToast(Context context) {
        if (!ability(context)) {
            Toast.makeText(context.getApplicationContext(), permissionStrRes(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void checkLocationToast(Context context) {
        if (!CheckLocationStatus.checkLocationForBLE(context)) {
            Toast.makeText(context.getApplicationContext(), R.string.to_turn_on_location, Toast.LENGTH_SHORT).show();
        }
    }

    public static void checkBLEEnabledToast(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.to_turn_on_ble, Toast.LENGTH_SHORT).show();
    }

    public static void checkBLEToast (Context context) {
        checkPermissionToast(context);
        checkLocationToast(context);
    }
}
