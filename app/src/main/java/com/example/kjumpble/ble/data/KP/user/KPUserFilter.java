package com.example.kjumpble.ble.data.KP.user;

import android.util.Log;

import com.example.kjumpble.ble.format.KP.KPUser;

public class KPUserFilter {
    public KPUser getKPUser(byte[] data) {
        if (data[0] == 0x21 & data[1] == 0x55 & data[2] == (byte) 0xAA) {
            return null;
        }
        else if (!(data[0] == 0x2d && data[6] == 0x41)) {
            Log.d("KP", "Data is invalid");
            return null;
        }
        return new KPUser(data[4], data[2], data[1]);
    }
}
