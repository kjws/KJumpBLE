package com.example.kjumpble.ble.format.KP;

import android.util.Log;

import java.util.Calendar;

public class KPMemory {
    BloodPressure BloodPressure;
    KPTemperature KpTemperature;
    boolean IPD;
    boolean Afib;
    Calendar time;

    public KPMemory(BloodPressure bloodPressure, KPTemperature kpTemperature,
                    boolean ipd, boolean afib, Calendar time) {
        this.BloodPressure = bloodPressure;
        this.KpTemperature = kpTemperature;
        this.IPD = ipd;
        this.Afib = afib;
        this.time = time;

        logAll();
    }

    private void logAll() {
        Log.d("KP", "BloodPressure = " + BloodPressure.getSystolic() + ", " + BloodPressure.getDiastolic() + ", " + BloodPressure. getPressure());
        Log.d("KP", "KpTemperature = " + KpTemperature.isHaveTemperature() + ", " + KpTemperature.getTemperature());
        Log.d("KP", "IPD = " + IPD + ", AFIB = " + Afib);
        Log.d("KP", "Year = " + time.get(Calendar.YEAR));
        Log.d("KP", "Month = " + time.get(Calendar.MONTH));
        Log.d("KP", "Day = " + time.get(Calendar.DAY_OF_MONTH));
        Log.d("KP", "Hour = " + time.get(Calendar.HOUR));
        Log.d("KP", "Minute = " + time.get(Calendar.MINUTE));
    }

    public com.example.kjumpble.ble.format.KP.BloodPressure getBloodPressure () {
        return BloodPressure;
    }

    public KPTemperature getKpTemperature () {
        return KpTemperature;
    }

    public Calendar getTime () {
        return time;
    }

    public boolean isIPD () {
        return IPD;
    }

    public boolean isAfib () {
        return Afib;
    }
}
