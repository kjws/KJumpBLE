package com.example.kjumpble.ble;

import android.Manifest;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

public class CheckLocationStatus {
    public static boolean checkLocationEnabled (Context context) {
        boolean gps_enabled = false;
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) {
            return false;
        }

        return gps_enabled;
    }

    public static boolean checkLocationForBLE (Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            return true;
        else
            return checkLocationEnabled(context);
    }
}
