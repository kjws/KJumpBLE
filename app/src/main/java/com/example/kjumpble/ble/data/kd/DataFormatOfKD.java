package com.example.kjumpble.ble.data.kd;

import android.util.Log;

import com.example.kjumpble.ble.SensePositionEnum;

public class DataFormatOfKD {
    public int Year;
    public int Month;
    public int Day;
    public int Hour;
    public int Minute;
    public float Temperature;

    public DataFormatOfKD (byte[] data) {
        Year = 2000 + data[1];
        Month = data[2];
        Day = data[3];
        Hour = data[4];
        Minute = data[5];
        Temperature = (float) (((data[7] & 0xff) * 256 + (data[8] & 0xff)) / 100.0);

//        logAll();
    }

    private void logAll() {
        Log.d("test8360", "year = " + Year);
        Log.d("test8360", "Month = " + Month);
        Log.d("test8360", "Day = " + Day);
        Log.d("test8360", "Hour = " + Hour);
        Log.d("test8360", "Minute = " + Minute);
        Log.d("test8360", "Temperature = " + Temperature);
    }
}
