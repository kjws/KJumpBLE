package com.example.kjumpble.ble;

import android.util.Log;

public class DataFor8360 {
    int Year;
    int Month;
    int Day;
    int Hour;
    int Minute;
    int SensePosition;
    float Temperature;

    DataFor8360(byte[] data) {
        Year = 2000 + data[1];
        Month = data[2];
        Day = data[3];
        Hour = data[4];
        Minute = data[5];
        SensePosition = calSensePosition(data[6]);
        Temperature = (float) ((data[7] * 256 + data[8]) / 100.0);

        logAll();
    }

    private int calSensePosition (byte data) {
        data = (byte) (data & 0x0f);
        switch (data) {
            case 0:
                return SensePositionEnum.House.ordinal();
            case 1:
                return SensePositionEnum.Ear.ordinal();
            default:
                return SensePositionEnum.Head.ordinal();
        }
    }

    private void logAll() {
        Log.d("test8360", "year = " + Year);
        Log.d("test8360", "Month = " + Month);
        Log.d("test8360", "Day = " + Day);
        Log.d("test8360", "Hour = " + Hour);
        Log.d("test8360", "Minute = " + Minute);
        Log.d("test8360", "SensePosition = " + SensePosition);
        Log.d("test8360", "Temperature = " + Temperature);

    }
}
