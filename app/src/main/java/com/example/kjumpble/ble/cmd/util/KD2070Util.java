package com.example.kjumpble.ble.cmd.util;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnit;

public class KD2070Util {
    // Temperature unit
    public static byte getUnitData (byte data, TemperatureUnit unit) {
        if ((data & 0x01) == 0x01) {
            data = (byte) (data - 0x01);
        }
        data = (byte) (data | (unit == TemperatureUnit.F ? 0x01 : 0x00));
        return data;
    }

    // Hand
    public static byte getHandData(byte data, LeftRightHand hand) {
        if ((data & 0x02) == 0x02) {
            data = (byte) (data - 0x02);
        }
        data = (byte) (data | (hand == LeftRightHand.Left ? 0x02 : 0x00));
        return data;
    }
}
