package com.example.kjumpble.ble;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattCallback {
    void onCharacteristicChanged (BluetoothGattCharacteristic gatt);
}
