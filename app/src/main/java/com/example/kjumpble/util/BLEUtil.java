package com.example.kjumpble.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;

public class BLEUtil {
    public LeConnectStatus checkConnectStatus(BluetoothManager bluetoothManager, BluetoothGatt gatt, String TAG) {
        if (bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
            Log.w(TAG, "Device is disconnected. Please check your device status.");
            return LeConnectStatus.DisConnected;
        }
        else return LeConnectStatus.Connected;
    }
}
