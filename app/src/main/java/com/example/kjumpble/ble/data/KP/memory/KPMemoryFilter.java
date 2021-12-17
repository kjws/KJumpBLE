package com.example.kjumpble.ble.data.KP.memory;

import com.example.kjumpble.ble.format.KP.BloodPressure;
import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPTemperature;

import java.util.Calendar;

public class KPMemoryFilter {
    public KPMemory getKpMemory (String name, byte[] data) {
        if (!(data[0] == 0x2d || data[0] == 0x2a) && data[6] == 0x41) {
            return null;
        }

        BloodPressure bloodPressure = new BloodPressure(data[1] & 0xff, data[2] & 0xff, data[4] & 0xff);
        KPTemperature kpTemperature = setTemperature(data);
        boolean ipd = (data[8] & 0x01) == 0;
        boolean afib = (data[8] & 0x80) == 0x80;
        Calendar time = setTime(name, data);

        return new KPMemory(bloodPressure, kpTemperature, ipd, afib, time);
    }

    private KPTemperature setTemperature(byte[] data) {
        int Temperature = data[8] >> 1 & 0x7f;
        return new KPTemperature(Temperature > 38, Temperature);
    }

    private Calendar setTime(String name, byte[] data) {
        Calendar time = Calendar.getInstance();;
        time.set(Calendar.YEAR, name.equals("KP-6525") ? data[11] + 100 : 0);
        time.set(Calendar.MONTH, data[9] - 1);
        time.set(Calendar.DAY_OF_MONTH, data[3]);
        time.set(Calendar.HOUR, data[5]);
        time.set(Calendar.MINUTE, data[7]);
        return time;
    }
}
