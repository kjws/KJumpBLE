package com.example.kjumpble.ble.data.kg;

import android.util.Log;

import com.example.kjumpble.ble.format.kg.KGGlucoseACPC;
import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;

import java.util.Calendar;
import java.util.Date;

public class KGData {
    private boolean isValid = false;
    private KGGlucoseACPC ACPC;
    private int Glucose;
    DeviceTimeFormat time;

    public KGData (byte[] data) {
        if (data[7] == data[8] && (data[8] == 0 || data[8] == (byte) 0xff) || data[0] != 2)
            setValid(false);
        else
            setValid(true);

        setTime(data);
        setACPC(data[6]);
        setGlucose(data);

//        logAll();
    }

    public void setValid (boolean isValid) {
        this.isValid = isValid;
    }

    // Time
    public void setTime (byte[] data) {
        int year = data[1] + 100;
        int month =  data[2] - 1;
        int day = data[3];
        int hour = data[4];
        int minute =  data[5];
        int second = 0;

        time = new DeviceTimeFormat(year, month, day, hour, minute, second);
    }
    public DeviceTimeFormat getTime () {
        return time;
    }

    //ACPC
    public void setACPC(int aCPC) {
        ACPC = aCPC == 0 ? KGGlucoseACPC.PC : KGGlucoseACPC.AC;
        if (aCPC > 1)
            setValid(false);
    }
    public KGGlucoseACPC getACPC() {
        return ACPC;
    }

    // Glucose
    private void setGlucose(byte[] buf) {
        this.Glucose = buf[7] * 256 + buf[8];
    }
    public int getGlucose() {
        return this.Glucose;
    }

    private void logAll() {
        Log.d("test8360", "year = " + time.getYear());
        Log.d("test8360", "Month = " + time.getMonth());
        Log.d("test8360", "Day = " + time.getDay());
        Log.d("test8360", "Hour = " + time.getHour());
        Log.d("test8360", "Minute = " + time.getMinute());
        Log.d("test8360", "Glucose = " + this.Glucose);
        Log.d("test8360", "ACPC = " + this.ACPC);
    }
}
