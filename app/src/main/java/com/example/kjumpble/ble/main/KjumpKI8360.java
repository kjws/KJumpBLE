package com.example.kjumpble.ble.main;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.callback.GattCallback;
import com.example.kjumpble.ble.callback.KjumpKI8360Callback;
import com.example.kjumpble.ble.cmd.Ki8360Cmd;
import com.example.kjumpble.ble.data.DataFormatOfKI8360;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;

import java.util.ArrayList;
import java.util.Arrays;

public class KjumpKI8360 implements GattCallback {
    static final String TAG = KjumpKI8360.class.getSimpleName();

    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    public BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    byte dataStartPosition = 0;
    int user = 0;
    int numberOfData = 0;
    int dataIndex = 0;
    ArrayList<DataFormatOfKI8360> dataFormatOfKI8360s;
    BluetoothGattCharacteristic beWroteCharacteristic;

    // clock
    ClockTimeFormat clock_time;
    boolean enabled;

    private final KjumpKI8360Callback kjumpKI8360Callback;

    public KjumpKI8360 (BluetoothGatt gatt, KjumpKI8360Callback kjumpKI8360Callback, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.kjumpKI8360Callback = kjumpKI8360Callback;
        this.bluetoothManager = bluetoothManager;
    }

    private void dataInit () {
        dataFormatOfKI8360s = new ArrayList<>();
        dataIndex = 0;
        numberOfData = 0;
        user = 0;
        dataStartPosition = 0;
        beWroteCharacteristic = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }

    private LeConnectStatus checkConnectStatus() {
        if (bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
            Log.w(TAG, "Device is disconnected. Please check your device status.");
            return LeConnectStatus.DisConnected;
        }
        else return LeConnectStatus.Connected;
    }

    public void setDevice() {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteSetDeviceCmd;
        cmd = BLE_CMD.WRITE_SET;
        writeCharacteristic(Ki8360Cmd.refreshDeviceCmd);
    }

    public void readUserIndex () {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadUserAndMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(Ki8360Cmd.getConfirmUserAndMemoryCmd());
    }

    /**
     * Read number of temperature data.
     * Return value will show in KjumpKI8360Callback.onGetNumberOfData
     */
    public void readNumberOfData () {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(Ki8360Cmd.getConfirmUserAndMemoryCmd());
    }

    /**
     * Read last memory.
     * Return value will show in KjumpKI8360Callback.onGetLastMemory
     */
    public void readLatestMemory () {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadLatestMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(Ki8360Cmd.getConfirmUserAndMemoryCmd());
    }

    /**
     * Read all memory.
     * Return value will show in KjumpKI8360Callback.onGetAllMemory
     */
    public void readAllMemory () {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadAllMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY;
        writeCharacteristic(Ki8360Cmd.getConfirmUserAndMemoryCmd());
    }

//    /**
//     * Init device
//     * It must be done when you notify success so that you can work device normally.
//     */
//    public void writeSetDevice () {
//        if (checkConnectStatus() == LeConnectStatus.DisConnected)
//            return;
//        dataInit();
//        bleClientCmd = BLE_CLIENT_CMD.WriteSetDeviceCmd;
//        cmd = BLE_CMD.WRITE_SET;
//        writeRefreshCommand();
//        final Handler handler = new Handler();
//        handler.postDelayed(this::writeRefreshCommand, 3000);
//    }

    /**
     * Clear all data.
     * Success or not will show in KjumpKI8360Callback.onClearAllDataFinished
     */
    public void clearAllData () {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
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
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        this.clock_time = clock_time;
        this.enabled = enabled;

        writeClockTimePreCmd(clock_time);
    }

    private void writeClockTimePreCmd (ClockTimeFormat clock_time) {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPreCommand(clock_time));
    }

    private void writeClockTimePostCmd (ClockTimeFormat clock_time, boolean enabled) {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPostCommand(clock_time, enabled));
    }

    /**
     * Set device reminder clock time and enable to alarm.
     * Success or not will show in KjumpKI8360Callback.onWriteClockFinished
     * @param reminder_clock_time : Reminder clock time.
     * @param enabled : True if you want to enable reminder clock.
     */
    public void writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteReminderCmd;
        cmd = BLE_CMD.WRITE_REMINDER_CLOCK;
        writeCharacteristic(Ki8360Cmd.getWriteReminderClockTimeAndEnabledCommand(reminder_clock_time, enabled));
    }

    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeTemperatureUnit (TemperatureUnitEnum unit) {
        if (checkConnectStatus() == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.WRITE_UNIT;
        writeCharacteristic(Ki8360Cmd.getWriteTemperatureUnitCmdCommand(unit));
    }

    private void writeRefreshCommand () {
        cmd = BLE_CMD.WRITE_SET;
        beWroteCharacteristic.setValue(Ki8360Cmd.refreshDeviceCmd);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    /**
     * Notify when ble notification is enable and concurrently get onChanged.
     * @param characteristic characteristic
     */
    @Override
    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged - " + cmd);
        switch (cmd) {
            case CONFIRM_USER_AND_MEMORY:
                onGetConfirmUserAndMemory(characteristic);
                break;
            case CONFIRM_NUMBER_OF_DATA:
                onGetConfirmNumberOfData(characteristic);
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
            case WRITE_REMINDER_CLOCK:
            case WRITE_UNIT:
                if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
                    sendCallback();
                break;
            case WRITE_SET:
                if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
                    onGetRefreshCmd(characteristic);
                break;
        }
    }



    private void sendCallback () {
        Log.d(TAG, "sendBroadcast bleClientCmd = " + bleClientCmd + ",cmd = " + cmd);
        switch (bleClientCmd) {
            case ReadUserAndMemoryCmd:
                if (cmd == BLE_CMD.CONFIRM_USER_AND_MEMORY)
                    kjumpKI8360Callback.onGetUserAndMemory(user);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    kjumpKI8360Callback.onGetNumberOfData(numberOfData);
                break;
            case ReadLatestMemoryCmd:
                DataFormatOfKI8360 data;
                if (dataFormatOfKI8360s.size() == 0)
                    data = null;
                else
                    data = dataFormatOfKI8360s.get(0);
                switch (cmd) {
                    case READ_DATA:
                    case NUMBER_OF_DATA_ZERO:
                        kjumpKI8360Callback.onGetLastMemory(data);
                        break;
                }
                break;
            case ReadAllMemoryCmd:
                switch (cmd) {
                    case READ_DATA:
                    case NUMBER_OF_DATA_ZERO:
                        kjumpKI8360Callback.onGetAllMemory(dataFormatOfKI8360s);
                        break;
                }
                break;
            case ClearAllDataCmd:
                kjumpKI8360Callback.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    kjumpKI8360Callback.onWriteClockFinished(true);
                break;
            case WriteReminderCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    kjumpKI8360Callback.onWriteReminderClockFinished(true);
                break;
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    kjumpKI8360Callback.onWriteUnitFinished(true);
                break;
            case WriteSetDeviceCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    kjumpKI8360Callback.onSetDeviceFinished(true);
                break;
        }
    }

    // ***********************
    // ** Write split
    // ***********************

    /**
     * onCharacteristicChanged trigger and command is CONFIRM_USER_AND_MEMORY_COMMAND
     * @param characteristic characteristic
     */
    private void onGetConfirmUserAndMemory (BluetoothGattCharacteristic characteristic) {
        dataStartPosition = characteristic.getValue()[7];
        user = characteristic.getValue()[9];
        sendCallback();

        switch (bleClientCmd) {
            case ReadNumberOfDataCmd:
            case ReadLatestMemoryCmd:
            case ReadAllMemoryCmd:
                cmd = BLE_CMD.CONFIRM_NUMBER_OF_DATA;
                writeCharacteristic(Ki8360Cmd.getConfirmNumberOfDataCmd(user));
                break;
        }
    }

    /**
     * onCharacteristicChanged trigger and command is CONFIRM_NUMBER_OF_DATA
     * @param characteristic characteristic
     */
    private void onGetConfirmNumberOfData (BluetoothGattCharacteristic characteristic) {
        numberOfData = characteristic.getValue()[1];
        dataFormatOfKI8360s = new ArrayList<>();
        sendCallback();

        switch (bleClientCmd) {
            case ReadLatestMemoryCmd:
                if (numberOfData == 0) {
                    cmd = BLE_CMD.NUMBER_OF_DATA_ZERO;
                    sendCallback();
                }
                else {
                    dataIndex = numberOfData - 1;
                    cmd = BLE_CMD.READ_DATA;
                    writeCharacteristic(Ki8360Cmd.getReadDataCmd(dataIndex));
                }
                break;
            case ReadAllMemoryCmd:
                if (numberOfData == 0) {
                    cmd = BLE_CMD.NUMBER_OF_DATA_ZERO;
                    sendCallback();
                }
                else {
                    cmd = BLE_CMD.READ_DATA;
                    writeCharacteristic(Ki8360Cmd.getReadDataCmd(dataIndex));
                }
                break;
        }
    }

    /**
     * onCharacteristicChanged trigger and command is READ_DATA
     * @param characteristic characteristic
     */
    private void onGetReadData (BluetoothGattCharacteristic characteristic) {
        dataFormatOfKI8360s.add(new DataFormatOfKI8360(characteristic.getValue()));
        switch (bleClientCmd) {
            case ReadLatestMemoryCmd:
                if (dataIndex == numberOfData - 1)
                    sendCallback();
                break;
            case ReadAllMemoryCmd:
                dataIndex++;
                if (dataIndex == numberOfData)
                    sendCallback();
                else
                    writeCharacteristic(Ki8360Cmd.getReadDataCmd(dataIndex));
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
            writeClockTimePostCmd(clock_time, enabled);
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_REFRESH_COMMAND
     * @param characteristic characteristic
     */
    private void onGetRefreshCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            switch (bleClientCmd) {
                case WriteSetDeviceCmd:
                    sendCallback();
                    break;
            }
    }
}
