package com.example.kjumpble.permission;

import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MyPermissions {
    private ArrayList<String> permissions;

    private final ArrayList<String> blePermissionsapi31 = new ArrayList<String>() {{
            add(Manifest.permission.BLUETOOTH);
            add(Manifest.permission.BLUETOOTH_ADMIN);
        }
    };

    private final ArrayList<String> blePermissionsapi29 = new ArrayList<String>() {{
        add(Manifest.permission.ACCESS_FINE_LOCATION);
        add(Manifest.permission.BLUETOOTH);
        add(Manifest.permission.BLUETOOTH_ADMIN);
        }
    };

    private final ArrayList<String> blePermissionsapiOhters = new ArrayList<String>() {{
        add(Manifest.permission.ACCESS_COARSE_LOCATION);
        add(Manifest.permission.BLUETOOTH);
        add(Manifest.permission.BLUETOOTH_ADMIN);
        }
    };

    private ArrayList<String> arrayListMerge(ArrayList<String> arr1, ArrayList<String> arr2) {
        if (arr1 != null & arr2 != null) {
            arr1.addAll(arr2);
            return arr1;
        }
        return null;
    }

    private void initPermissions() {
        ArrayList<String> permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            permissions = blePermissionsapi31;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            permissions = blePermissionsapi29;
        else
            permissions = blePermissionsapiOhters;
        this.permissions = permissions;
    }

    public String[] getPermissions () {
        initPermissions();

        return convertArraylistToArray(permissions);
    }

    public static String[] convertArraylistToArray (ArrayList<String> permissions) {
        String[] requestStr = new String[permissions.size()];
        if (permissions.size() != 0) {
            for (int i = 0; i < permissions.size(); i++)
                requestStr[i] = permissions.get(i);
        }
        return requestStr;
    }
}
