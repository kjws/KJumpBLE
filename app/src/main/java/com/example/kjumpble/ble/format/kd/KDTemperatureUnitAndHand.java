package com.example.kjumpble.ble.format.kd;

import android.bluetooth.BluetoothGattCharacteristic;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnit;


public class KDTemperatureUnitAndHand {
    TemperatureUnit unit;
    LeftRightHand hand;
    public KDTemperatureUnitAndHand(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        this.hand = (data[1] & 0x02) == 0x02 ? LeftRightHand.Left : LeftRightHand.Right;
        this.unit = (data[1] & 0x01) == 0x01 ? TemperatureUnit.F : TemperatureUnit.C;
    }

    public TemperatureUnit getUnit () {
        return unit;
    }

    public LeftRightHand getHand () {
        return hand;
    }
}
