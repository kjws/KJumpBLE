package com.example.kjumpble.ble.main.ki;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.ki.KjumpKI8180Callback;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.cmd.kd.KD2070Cmd;
import com.example.kjumpble.ble.cmd.ki.KI8180Cmd;
import com.example.kjumpble.ble.cmd.ki.Ki8360Cmd;
import com.example.kjumpble.ble.data.ki.DataFormatOfKI;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.Arrays;

public class KjumpKI8180 {
    static final String TAG = KjumpKI8180.class.getSimpleName();

    private final KjumpKI8180Callback callBack;

    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    public BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    int numberOfData = 0;
    int indexOfData = 0;
    DataFormatOfKI dataFormatOfKI;
    BluetoothGattCharacteristic beWroteCharacteristic;

    // clock
    ClockTimeFormat clock_time;
    boolean enabled;

    public KjumpKI8180 (BluetoothGatt gatt, KjumpKI8180Callback callBack, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.callBack = callBack;
        this.bluetoothManager = bluetoothManager;
    }

    private void dataInit () {
        indexOfData = 0;
        numberOfData = 0;
        beWroteCharacteristic = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }


    private void setDevice() {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        cmd = BLE_CMD.WRITE_SET;
        writeCharacteristic(Ki8360Cmd.refreshDeviceCmd);
    }

    /**
     * Read number of temperature data.
     * Return value will show in KjumpKI8360Callback.onGetNumberOfData
     */
    public void readNumberOfData () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
        cmd = BLE_CMD.CONFIRM_NUMBER_OF_DATA;
        writeCharacteristic(Ki8360Cmd.getConfirmUserAndMemoryCmd());
    }

    /**
     * Read last memory.
     * Return value will show in KjumpKI8360Callback.onGetLastMemory
     */
    public void readIndexMemory (int indexOfData) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        this.indexOfData = indexOfData;
        bleClientCmd = BLE_CLIENT_CMD.ReadIndexMemoryCmd;
        cmd = BLE_CMD.READ_DATA;
        writeCharacteristic(Ki8360Cmd.getReadDataCmd(indexOfData));
    }

    /**
     * Clear all data.
     * Success or not will show in KjumpKI8360Callback.onClearAllDataFinished
     */
    public void clearAllData () {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ClearAllDataCmd;
        cmd = BLE_CMD.CLEAR_DATA;
        writeCharacteristic(Ki8360Cmd.getClearDataCmd());
    }

    /**
     * Set device clock time and whether you want to show on device screen.
     * Success or not will show in KjumpKI8360Callback.onWriteClockFinished
     * @param clock_time : Time you want to write in device.
     * @param enabled : True if you want to show clock in your screen.
     */
    public void writeClockTimeAndShowFlag (ClockTimeFormat clock_time, boolean enabled) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        this.clock_time = clock_time;
        this.enabled = enabled;

        writeClockTimeAndFlagPreCmd(clock_time);
    }

    private void writeClockTimeAndFlagPreCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPreCommand(clock_time));
    }

    private void writeClockTimeAndFlagPostCmd (ClockTimeFormat clock_time, boolean enabled) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPostCommand(clock_time, enabled));
    }

    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeTemperatureUnit (TemperatureUnitEnum unit) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.READ_TEMPERATURE_UNIT_AND_HAND;
        writeCharacteristic(KD2070Cmd.readTempUnitAndHandCmd);
    }

    public void writeTemperatureUnit (boolean ambientEnable) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteAmbientCmd;
        cmd = BLE_CMD.WRITE_AMBIENT;
        writeCharacteristic(KI8180Cmd.getWriteAmbientFlagCmd(ambientEnable));
    }


    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    // *****************************************************************************************
    // **                               onCharacteristicChanged                               **
    // *****************************************************************************************
    /**
     * Notify when ble notification is enable and concurrently get onChanged.
     * @param characteristic characteristic
     */
    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged - " + cmd);
        switch (cmd) {
            case WRITE_SET:
                if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
                    onGetSetDeviceCmd(characteristic);
                break;
            case CONFIRM_NUMBER_OF_DATA:
                onGetNumberOfData(characteristic);
                break;
            case READ_DATA:
                onGetReadData(characteristic);
                break;
            case CLEAR_DATA:
                onGetClearDataCmd(characteristic);
                break;
            case WRITE_PRE_CLOCK:
                onGetPreClockCmd(characteristic);
                break;
            case WRITE_POST_CLOCK:
            case WRITE_UNIT:
            case WRITE_AMBIENT:
                onGetWaitCmd(characteristic);
                break;
        }
    }

    private void sendCallback () {
        Log.d(TAG, "sendBroadcast bleClientCmd = " + bleClientCmd + ",cmd = " + cmd);
        switch (bleClientCmd) {
            case WriteSetDeviceCmd:
                callBack.onSetDeviceFinished(cmd == BLE_CMD.WRITE_SET);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    callBack.onGetNumberOfData(numberOfData);
                break;
            case ReadIndexMemoryCmd:
                callBack.onGetIndexMemory(indexOfData, dataFormatOfKI);
                break;
            case ClearAllDataCmd:
                callBack.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callBack.onWriteClockFinished(true);
                break;
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callBack.onWriteUnitFinished(true);
                break;
            case WriteAmbientCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callBack.onWriteAmbientFinished(true);
                break;
        }
    }

    // ***********************
    // **    Write split    **
    // ***********************

    /**
     * onCharacteristicChanged trigger and command is CONFIRM_NUMBER_OF_DATA
     * @param characteristic characteristic
     */
    private void onGetNumberOfData (BluetoothGattCharacteristic characteristic) {
        numberOfData = characteristic.getValue()[1];
        sendCallback();
    }

    /**
     * onCharacteristicChanged trigger and command is READ_DATA
     * @param characteristic characteristic
     */
    private void onGetReadData (BluetoothGattCharacteristic characteristic) {
        dataFormatOfKI = new DataFormatOfKI(characteristic.getValue());
        switch (bleClientCmd) {
            case ReadIndexMemoryCmd:
                sendCallback();
                break;
        }
    }

    /**
     * onCharacteristicChanged trigger and command is READ_DATA
     * @param characteristic characteristic
     */
    private void onGetClearDataCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            sendCallback();
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_PRE_CLOCK
     * @param characteristic characteristic
     */
    private void onGetPreClockCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            writeClockTimeAndFlagPostCmd(clock_time, enabled);
    }

    private void onGetWaitCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            setDevice();
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_REFRESH_COMMAND
     * @param characteristic characteristic
     */
    private void onGetSetDeviceCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            switch (bleClientCmd) {
                case WriteSetDeviceCmd:
                case WriteClockCmd:
                case WriteUnitCmd:
                case WriteAmbientCmd:
                    sendCallback();
                    break;
            }
    }
}
