package com.example.kjumpble.util;

import android.util.Log;

import com.example.kjumpble.ble.format.TemperatureUnitEnum;

public class KI8186Util {
    public static boolean checkReminderIndexOutOfRange(int index, String TAG) {
        if (index < 0 | index > 3) {
            Log.d(TAG, "Index is out of size. The index range is between 0 to 3 so that match reminder 1 to 4.");
            return false;
        }
        else {
            return true;
        }
    }

    public static boolean checkOffsetOutOfRange(int value, String TAG) {
        int highEnd = 10;
        int lowEnd = -10;

        if (value > highEnd | value < lowEnd) {
            Log.d(TAG, "Value is out of range. Offset value range is between -10 to 10 so that" +
                    " modify -1.0c to 1.0c or -2.0f to 2.0f");
            return false;
        }
        else {
            return true;
        }
    }
}
