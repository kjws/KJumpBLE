package com.example.kjumpble.ble.main;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.KjumpKPCallback;
import com.example.kjumpble.ble.cmd.kp.KPCmd;
import com.example.kjumpble.ble.cmd.kp.KPDesCmd;
import com.example.kjumpble.ble.cmd.kp.KPInnerCmd;
import com.example.kjumpble.ble.data.KP.memory.KPMemoryFilter;
import com.example.kjumpble.ble.data.KP.user.KPUserFilter;
import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPUser;
import com.example.kjumpble.ble.format.KP.SenseTimerStructure;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class KjumpKP {
    static final String TAG = KjumpKP.class.getSimpleName();

    private final KjumpKPCallback kjumpKPCallback;
    public BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    private int memoryIndex;
    private int numberOfMemory;

    BluetoothGattCharacteristic beWroteCharacteristic;

    KPDesCmd destinyCommand = KPDesCmd.Nothing;

    KPInnerCmd innerCmd;
    // Sense Timer
    SenseTimerStructure senseTimer;

    public KjumpKP (BluetoothGatt gatt, KjumpKPCallback kjumpKPCallback, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.kjumpKPCallback = kjumpKPCallback;
        this.bluetoothManager = bluetoothManager;
    }

    private void dataInit () {
//        dataFormatOfKI8360s = new ArrayList<>();
//        dataIndex = 0;
//        numberOfData = 0;
//        user = 0;
//        dataStartPosition = 0;
        senseTimer = new SenseTimerStructure();
        beWroteCharacteristic = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }

    public void setReminder(ArrayList<ReminderFormat> reminders) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
//        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
//        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(KPCmd.getWriteReminderCommand(reminders));
    }

    public void setTime(ArrayList<ReminderFormat> reminders, boolean Ambient, TemperatureUnitEnum unit, HourFormat hourFormat, boolean clockShowFlag) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
//        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
//        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(KPCmd.getWriteTimeCommand(reminders, Ambient, unit, hourFormat, clockShowFlag));
    }

    public void readMemory(int index) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Read_Memory_At_Index;
        writeCharacteristic(KPCmd.getReadMemoryCommand(index));
    }

    public void readAllMemory () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
    }

    public void readNumberOfMemory () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Read_Number_Of_Memory;
        writeCharacteristic(KPCmd.getReadNumberOfMemoryCommand());
    }

    public void kpStartSense () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Start_Sense;
        writeCharacteristic(KPCmd.getStartSenseCommand());
    }

    public void kpStopSense () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Stop_Sense;
        writeCharacteristic(KPCmd.getStopSenseCommand());
    }

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        switch (destinyCommand) {
            case Read_Memory_At_Index:
                KPMemory kpMemory = new KPMemoryFilter().getKpMemory(gatt.getDevice().getName(), data);
                // null
                if (kpMemory == null) {

                }
                // 有資料
                else {

                }
                break;
            case Read_Number_Of_Memory:
                KPUser kpUser = new KPUserFilter().getKPUser(data);
                // null
                if (kpUser == null) {

                }
                // 有資料
                else {

                }
                break;
        }
        if (senseTimer != null) {
            if (senseTimer.isSensing(data)) {
                int Systolic = 0;
                for (int i = data.length - 5; i < data.length - 2; i++) {
                    if ((data[i] == (byte) 0xFE) & data[i + 2] != (byte) 0xFE) {
                        Systolic = data[i + 1] + data[i + 2] * 256;
                        break;
                    }
                }
                kjumpKPCallback.onSensing(Systolic);
            }
        }
    }
}