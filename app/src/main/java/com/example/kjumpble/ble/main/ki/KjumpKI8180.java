package com.example.kjumpble.ble.main.ki;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.ki.KjumpKI8180Callback;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.cmd.SharedCmd;
import com.example.kjumpble.ble.cmd.ki.KI8180Cmd;
import com.example.kjumpble.ble.cmd.ki.KI8186Cmd;
import com.example.kjumpble.ble.cmd.ki.KI8360Cmd;
import com.example.kjumpble.ble.data.ki.DataFormatOfKI;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.format.ki.KI8180Settings;
import com.example.kjumpble.ble.format.ki.KI8186Settings;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.Arrays;

public class KjumpKI8180 {
    static final String TAG = KjumpKI8180.class.getSimpleName();

    private final KjumpKI8180Callback callback;

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

    // Settings
    byte[] settingsBytes = new byte[24];
    KI8180Settings settings;

    public KjumpKI8180 (BluetoothGatt gatt, KjumpKI8180Callback callback, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.callback = callback;
        this.bluetoothManager = bluetoothManager;
    }

    private void dataInit () {
        beWroteCharacteristic = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }


    private void setDevice() {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        cmd = BLE_CMD.WRITE_SET;
        writeCharacteristic(KI8360Cmd.setDeviceCmd);
    }

    public void readSettings() {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.ReadSettingsCmd;
        readSettingStep1();
    }

    private void readSettingStep1() {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        cmd = BLE_CMD.READ_SETTING_STEP_1;
        writeCharacteristic(SharedCmd.readSettingPreCmd);
    }

    private void readSettingStep2() {
        cmd = BLE_CMD.READ_SETTING_STEP_2;
        writeCharacteristic(SharedCmd.readSettingPostCmd);
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
        writeCharacteristic(KI8360Cmd.getConfirmUserAndMemoryCmd());
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
        writeCharacteristic(KI8360Cmd.getReadDataCmd(indexOfData));
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
        writeCharacteristic(KI8360Cmd.getClearDataCmd());
    }

    /**
     * Set device clock time.
     * Success or not will show in KjumpKI8360Callback.onWriteClockFinished
     * @param clock_time : Time you want to write in device.
     */
    public void writeClockTime (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();

        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;

        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setClockTime(clock_time);
            writeClockTimePreCmd(clock_time);
        }
    }

    private void writeClockTimePreCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(SharedCmd.getWriteClockTimePreCommand(clock_time));
    }

    private void writeClockTimePostCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(SharedCmd.getWriteClockTimePostCommand(clock_time));
    }

    /**
     * Set clock show or not show.
     * @param clockFlag : True is enabled, false is disabled.
     */
    public void writeClockShowFlag (boolean clockFlag) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteClockFlagCmd;
        cmd = BLE_CMD.WRITE_CLOCK_FLAG;

        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setClockEnabled(clockFlag);
            writeCharacteristic(KI8186Cmd.getWriteClockShowFlagCommand(settingsBytes[23], clockFlag));
        }
    }

    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeUnit (TemperatureUnit unit) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.WRITE_UNIT;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setUnit(unit);
            writeCharacteristic(KI8360Cmd.getWriteTemperatureUnitCommand(unit));
        }
    }

    public void writeAmbient (boolean ambient) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteAmbientCmd;
        cmd = BLE_CMD.WRITE_AMBIENT;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setAmbient(ambient);
            writeCharacteristic(KI8180Cmd.getWriteAmbientFlagCmd(ambient));
        }
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
            case READ_SETTING_STEP_1:
                onGetSettingsStep1(characteristic);
                break;
            case READ_SETTING_STEP_2:
                onGetSettingsStep2(characteristic);
                break;
            case WRITE_SET:
                if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
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
            case WRITE_CLOCK_FLAG:
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
                callback.onSetDeviceFinished(cmd == BLE_CMD.WRITE_SET);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    callback.onGetNumberOfData(numberOfData);
                break;
            case ReadIndexMemoryCmd:
                callback.onGetIndexMemory(indexOfData, dataFormatOfKI);
                break;
            case ClearAllDataCmd:
                callback.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
            case WriteReminderCmd:
            case WriteUnitCmd:
            case WriteClockFlagCmd:
            case WriteAmbientCmd:
                callbackForWaitCmd(callback, bleClientCmd);
                break;
        }
    }

    private void callbackForWaitCmd(KjumpKI8180Callback callback, BLE_CLIENT_CMD bleClientCmd) {
        switch (bleClientCmd) {
            case WriteClockCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteClockFinished(true);
                break;
            case WriteReminderCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteReminderFinished(indexOfData, true);
                break;
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteUnitFinished(true);
                break;
            case WriteClockFlagCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteClockFlagFinished(true);
                break;
            case WriteAmbientCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteAmbientFinished(true);
                break;
        }
    }
    // ***********************
    // **    Write split    **
    // ***********************

    private void onGetSettingsStep1 (BluetoothGattCharacteristic characteristic) {
        System.arraycopy(characteristic.getValue(), 1, settingsBytes, 0, 18);
        readSettingStep2();
    }

    private void onGetSettingsStep2 (BluetoothGattCharacteristic characteristic) {
        System.arraycopy(characteristic.getValue(), 1, settingsBytes, 18, 6);
        settings = new KI8180Settings(settingsBytes);
        switch (bleClientCmd) {
            case ReadSettingsCmd:
                sendCallback();
                break;
            case WriteClockCmd:
                writeClockTime(settings.getClockTime());
                break;
            case WriteUnitCmd:
                writeUnit(settings.getUnit());
                break;
            case WriteClockFlagCmd:
                writeClockShowFlag(settings.isClockEnabled());
                break;
            case WriteAmbientCmd:
                writeAmbient(settings.isAmbient());
        }
    }

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
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            sendCallback();
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_PRE_CLOCK
     * @param characteristic characteristic
     */
    private void onGetPreClockCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            writeClockTimePostCmd(clock_time);
    }

    private void onGetWaitCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            setDevice();
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_REFRESH_COMMAND
     * @param characteristic characteristic
     */
    private void onGetSetDeviceCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            switch (bleClientCmd) {
                case WriteSetDeviceCmd:
                case WriteClockCmd:
                case WriteClockFlagCmd:
                case WriteUnitCmd:
                case WriteAmbientCmd:
                    sendCallback();
                    break;
            }
    }
}
