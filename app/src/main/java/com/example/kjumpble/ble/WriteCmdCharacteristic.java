package com.example.kjumpble.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.example.kjumpble.ble.cmd.BLE_CMD;

public class WriteCmdCharacteristic {
    private static final byte[] confirmUserAndMemoryCmd = new byte[]{0x02, 0x04, 0x00, (byte) 0x80};
    private static final byte[] readNumberOfDataCmd = new byte[]{0x02, 0x01, 0x00, 0x6c};
    private static final byte[] readDataCmd = new byte[]{0x02, 0x09, 0x00, (byte) 0xa8}; // 02, 09, 00, 0xa8(起始位置)
    private static final byte[] clearDataCmd = new byte[]{0x03, 0x01, 0x00, (byte) 0x6c, 0x00};
    private static byte[] getCommand (BLE_CMD cmd) {
        switch (BLE_CMD.values()[cmd.ordinal()]) {
            case CONFIRM_USER_AND_MEMORY_COMMAND:
                return confirmUserAndMemoryCmd;
            default:
                return new byte[]{};
        }
    }

    public static void write(BluetoothGatt gatt, BLE_CMD cmd) {
        BluetoothGattCharacteristic characteristic = gatt.getService(SampleServiceAttributes.OUcare_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_WRITE_FFF3_UUID);
        characteristic.setValue(getCommand(cmd));
        gatt.writeCharacteristic(characteristic);
    }
}
