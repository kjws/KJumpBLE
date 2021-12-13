package com.example.kjumpble.ble.callback;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattCallback {
    void onCharacteristicChanged (BluetoothGattCharacteristic gatt);
}
