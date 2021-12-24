package com.example.kjumpble.ble.data.kd;

import android.util.Log;

import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;

public class KDData {
    DeviceTimeFormat time;
    float Temperature;

    public KDData (byte[] data) {
        int Year = 2000 + data[1];
        int Month = data[2];
        int Day = data[3];
        int Hour = data[4];
        int Minute = data[5];
        int Second = data[6];
        time = new DeviceTimeFormat(Year, Month, Day, Hour, Minute, Second);
        Temperature = (float) (((data[7] & 0xff) + (data[8] & 0xff) * 256) / 100.0);

//        logAll();
    }

    public DeviceTimeFormat getTime () {
        return time;
    }

    public float getTemperature () {
        return Temperature;
    }

    private void logAll() {
        Log.d("test8360", "year = " + time.getYear());
        Log.d("test8360", "Month = " + time.getMonth());
        Log.d("test8360", "Day = " + time.getDay());
        Log.d("test8360", "Hour = " + time.getHour());
        Log.d("test8360", "Minute = " + time.getMinute());
        Log.d("test8360", "Second = " + time.getSecond());
        Log.d("test8360", "Temperature = " + Temperature);
    }
}
