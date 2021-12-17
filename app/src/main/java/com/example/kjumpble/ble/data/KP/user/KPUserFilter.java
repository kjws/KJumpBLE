package com.example.kjumpble.ble.data.KP.user;

import com.example.kjumpble.ble.format.KP.KPUser;

public class KPUserFilter {
    public KPUser getKPUser(byte[] data) {
        if (!(data[0] == 0x2d && data[6] == 0x41)) {
            return null;
        }

        return new KPUser(data[4], data[2], data[1]);
    }
}
