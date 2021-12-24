package com.example.kjumpble.ble.data.kg;

import android.util.Log;

import com.example.kjumpble.ble.format.kg.KGGlucoseACPC;

import java.util.Calendar;
import java.util.Date;

public class KGData {
    private boolean isValid = false;
    private KGGlucoseACPC ACPC;
    private int Glucose;
    private Calendar time = Calendar.getInstance();

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

    public Calendar getTime () {
        return time;
    }

    public void setTime (byte[] data) {
        this.time.set(Calendar.YEAR, data[1] + 100);
        this.time.set(Calendar.MONTH, data[2] - 1);
        this.time.set(Calendar.DAY_OF_MONTH, data[3]);
        this.time.set(Calendar.HOUR, data[4]);
        this.time.set(Calendar.MINUTE, data[5] & 0x7f);
    }

    public KGGlucoseACPC getACPC() {
        return ACPC;
    }


    public void setACPC(int aCPC) {
        ACPC = aCPC == 0 ? KGGlucoseACPC.PC : KGGlucoseACPC.AC;
        if (aCPC > 1)
            setValid(false);
    }

    public int getGlucose() {
        return this.Glucose;
    }

    private void setGlucose(byte[] buf) {
        this.Glucose = buf[7] * 256 + buf[8];
    }

    private void logAll() {
        Log.d("test8360", "year = " + time.get(Calendar.YEAR));
        Log.d("test8360", "Month = " + time.get(Calendar.MONTH));
        Log.d("test8360", "Day = " + time.get(Calendar.DAY_OF_MONTH));
        Log.d("test8360", "Hour = " + time.get(Calendar.HOUR));
        Log.d("test8360", "Minute = " + time.get(Calendar.MINUTE));
        Log.d("test8360", "Glucose = " + this.Glucose);
        Log.d("test8360", "ACPC = " + this.ACPC);
    }
}
