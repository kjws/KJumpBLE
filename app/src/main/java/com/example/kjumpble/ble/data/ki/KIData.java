package com.example.kjumpble.ble.data.ki;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.kjumpble.ble.SensePositionEnum;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;

import java.io.Serializable;

public class KIData {
    ClockTimeFormat time;
    public int SensePosition;
    public float Temperature;

    public KIData (byte[] data) {
        int Year = 2000 + data[1];
        int Month = data[2];
        int Day = data[3];
        int Hour = data[4];
        int Minute = data[5];
        time = new ClockTimeFormat(Year, Month, Day, Hour, Minute, 0);
        SensePosition = calSensePosition(data[6]);
        Temperature = (float) (((data[7] & 0xff) * 256 + (data[8] & 0xff)) / 100.0);

//        logAll();
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
        Log.d("test8360", "year = " + time.getYear());
        Log.d("test8360", "Month = " + time.getMonth());
        Log.d("test8360", "Day = " + time.getDay());
        Log.d("test8360", "Hour = " + time.getHour());
        Log.d("test8360", "Minute = " + time.getMinute());
        Log.d("test8360", "SensePosition = " + SensePosition);
        Log.d("test8360", "Temperature = " + Temperature);
    }

    public ClockTimeFormat getTime () {
        return time;
    }

    public int getSensePosition () {
        return SensePosition;
    }

    public float getTemperature () {
        return Temperature;
    }
}
