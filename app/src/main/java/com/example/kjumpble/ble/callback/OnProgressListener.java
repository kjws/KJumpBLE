package com.example.kjumpble.ble.callback;

import android.bluetooth.BluetoothGatt;

public interface OnProgressListener {
    void onScanMotionFailed();
    void onStartScan();
    void onStopScan();
    void onConnected(BluetoothGatt gatt);
    void onDisConnected();
}