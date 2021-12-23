package com.example.kjumpble.ble.main;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.KjumpKPCallback;
import com.example.kjumpble.ble.callback.kp.KPTimerCallBack;
import com.example.kjumpble.ble.cmd.kp.KPCmd;
import com.example.kjumpble.ble.cmd.kp.KPDesCmd;
import com.example.kjumpble.ble.cmd.kp.KPInnerCmd;
import com.example.kjumpble.ble.data.KP.memory.KPMemoryFilter;
import com.example.kjumpble.ble.data.KP.user.KPUserFilter;
import com.example.kjumpble.ble.format.KP.KPDeviceSetting;
import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPUser;
import com.example.kjumpble.ble.SenseTimer;
import com.example.kjumpble.ble.format.KP.SenseMode;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.ArrayList;

public class KjumpKP {
    static final String TAG = KjumpKP.class.getSimpleName();
    private final KjumpKPCallback kjumpKPCallback;
    public BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    private int memoryIndex;
    private int numberOfMemory;

    BluetoothGattCharacteristic beWroteCharacteristic;

    KPDesCmd destinyCommand = KPDesCmd.Nothing;

    KPInnerCmd innerCmd = KPInnerCmd.Nothing;
    // Sense Timer
    SenseTimer senseTimer;

    // For write kp setting
    KPDeviceSetting deviceSetting;

    public KjumpKP (BluetoothGatt gatt, KjumpKPCallback kjumpKPCallback, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.kjumpKPCallback = kjumpKPCallback;
        this.bluetoothManager = bluetoothManager;
        senseTimer = new SenseTimer(timerCallBack);
    }

    private void dataInit () {
//        dataFormatOfKI8360s = new ArrayList<>();
//        dataIndex = 0;
//        numberOfData = 0;
//        user = 0;
//        dataStartPosition = 0;
        beWroteCharacteristic = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }

    // 先寫下reminder，在收到回覆之後再寫入time
    public void setDevice(KPDeviceSetting deviceSetting) {
        this.deviceSetting = deviceSetting;
        setReminder(deviceSetting.getReminders());
    }

    private void setReminder(ArrayList<ReminderFormat> reminders) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Set_Device;
        innerCmd = KPInnerCmd.Write_Reminder;
        writeCharacteristic(KPCmd.getWriteReminderCommand(reminders));
    }

    private void setTime(KPDeviceSetting deviceSetting) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Set_Device;
        innerCmd = KPInnerCmd.Write_Time;
        writeCharacteristic(KPCmd.getWriteTimeCommand(deviceSetting));
    }

    public void readMemory(int index) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Read_Memory_At_Index;
        writeCharacteristic(KPCmd.getReadMemoryCommand(index));
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

    public void kpChangeMode (SenseMode mode) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Change_Mode;
        writeCharacteristic(KPCmd.getChangeModeCommand(mode));
    }

    public void clearMemory () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        destinyCommand = KPDesCmd.Clear_Memory;
        writeCharacteristic(KPCmd.getClearMemoryCommand());
    }

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        switch (destinyCommand) {
            case Set_Device:
                switch (innerCmd) {
                    case Write_Time:
                        if (data[0] == (byte) 0x2d & data[1] == (byte) 0x88) {
                            kjumpKPCallback.onSetDeviceFinished(true);
                        }
                        break;
                    case Write_Reminder:
                        if (data[0] == (byte) 0x2d & data[1] == (byte) 0x99) {
                            setTime(deviceSetting);
                        }
                        break;
                }
                break;
            case Read_Memory_At_Index: {
                KPMemory kpMemory = new KPMemoryFilter().getKpMemory(gatt.getDevice().getName(), data);
                if (kpMemory != null) {
                    kjumpKPCallback.onGetMemory(kpMemory);
                }
                break;
            }
            case Read_Number_Of_Memory:
                KPUser kpUser = new KPUserFilter().getKPUser(data);
                if (kpUser != null) {
                    kjumpKPCallback.onGetUser(kpUser);
                }
                break;
            case Start_Sense:
                break;
            case Stop_Sense: {
                KPMemory kpMemory = new KPMemoryFilter().getKpMemory(gatt.getDevice().getName(), data);
                if (kpMemory != null) {
                    destinyCommand = KPDesCmd.Nothing;
                    kjumpKPCallback.onFinishedSense(kpMemory);
                }
                break;
            }
        }
        // 讓 timer 知道每次收到的characteristicChanged，藉此判斷是不是正在量測
        if (senseTimer != null) {
            if (senseTimer.isSensing(data)) {
                kjumpKPCallback.onSensing(true, getSensingSystolic(data));
            }
        }
    }

    private int getSensingSystolic(byte[] data) {
        int Systolic = 0;
        for (int i = data.length - 5; i < data.length - 2; i++) {
            if ((data[i] == (byte) 0xFE) & data[i + 2] != (byte) 0xFE) {
                Systolic = data[i + 1] + data[i + 2] * 256;
                break;
            }
        }
        return Systolic;
    }

    // 由Timer判斷現在量測狀態
    private final KPTimerCallBack timerCallBack = new KPTimerCallBack() {
        @Override
        public void onStartSense () {
            super.onStartSense();

            destinyCommand = KPDesCmd.Start_Sense;
            kjumpKPCallback.onStartSense();
        }

        @Override
        public void onStopSense () {
            super.onStopSense();

            destinyCommand = KPDesCmd.Stop_Sense;
            kjumpKPCallback.onStopSense();
        }
    };
}