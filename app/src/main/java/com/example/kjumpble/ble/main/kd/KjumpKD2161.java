package com.example.kjumpble.ble.main.kd;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.kd.KjumpKD2161Callback;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.cmd.SharedCmd;
import com.example.kjumpble.ble.cmd.kd.KD2161Cmd;
import com.example.kjumpble.ble.cmd.ki.KI8360Cmd;
import com.example.kjumpble.ble.data.kd.KDData;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.format.kd.KD2161Settings;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.Arrays;

public class KjumpKD2161 {
    static final String TAG = KjumpKD2161.class.getSimpleName();
    private final KjumpKD2161Callback callback;
    public final BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    private BluetoothGattCharacteristic beWroteCharacteristic;

    private BLE_CMD cmd;
    private BLE_CLIENT_CMD bleClientCmd;

    // DATA
    private KDData KDData;
    private int indexOfData = 0;
    private int numberOfData = 0;

    // Settings
    private byte[] settingsBytes = new byte[24];
    private KD2161Settings settings;

    public KjumpKD2161 (BluetoothGatt gatt, KjumpKD2161Callback callback, BluetoothManager bluetoothManager) {
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
    public void readDataAtIndex (int indexOfData) {
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
            writeCharacteristic(KD2161Cmd.getWriteClockShowFlagCommand(clockFlag));
        }
    }

    /**
     * Set device clock time and enable to alarm.
     * Success or not will show in KjumpKI8360Callback.onWriteClockFinished
     * @param reminder_clock : Reminder.
     */
    public void writeReminderTimeAndEnabled (ReminderFormat reminder_clock) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteReminderCmd;
        cmd = BLE_CMD.WRITE_REMINDER_CLOCK;

        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setReminders(reminder_clock);
            writeCharacteristic(KD2161Cmd.getWriteReminderAndFlagCommand(reminder_clock));
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

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

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
                    onGetRefreshCmd(characteristic);
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
            case WRITE_REMINDER_CLOCK:
            case WRITE_UNIT:
            case WRITE_CLOCK_FLAG:
                if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
                    onGetWaitCmd(characteristic);
                break;
        }
    }

    private void sendCallback () {
        Log.d(TAG, "sendBroadcast bleClientCmd = " + bleClientCmd + ",cmd = " + cmd);
        switch (bleClientCmd) {
            case ReadSettingsCmd:
                callback.onGetSettings(settings);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    callback.onGetNumberOfData(numberOfData);
                break;
            case ReadIndexMemoryCmd:
                callback.onGetDataAtIndex(indexOfData, KDData);
                break;
            case ClearAllDataCmd:
                callback.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
            case WriteReminderCmd:
            case WriteUnitCmd:
            case WriteClockFlagCmd:
                callbackForWaitCmd(callback, bleClientCmd);
                break;
        }
    }

    private void callbackForWaitCmd(KjumpKD2161Callback callback, BLE_CLIENT_CMD bleClientCmd) {
        switch (bleClientCmd) {
            case WriteClockCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteClockTimeFinished(true);
                break;
            case WriteReminderCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteReminderFinished(true);
                break;
            case WriteClockFlagCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteClockFlagFinished(true);
                break;
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteUnitFinished(true);
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
        settings = new KD2161Settings(settingsBytes);
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
            case WriteReminderCmd:
                writeReminderTimeAndEnabled(settings.getReminder());
                break;
            case WriteClockFlagCmd:
                writeClockShowFlag(settings.isClockEnabled());
                break;
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
        KDData = new KDData(characteristic.getValue());
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
            writeClockTimePostCmd(settings.getClockTime());
    }

    private void onGetWaitCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            setDevice();
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_REFRESH_COMMAND
     * @param characteristic characteristic
     */
    private void onGetRefreshCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), KI8360Cmd.writeReturnCmd))
            switch (bleClientCmd) {
                case WriteClockCmd:
                case WriteReminderCmd:
                case WriteUnitCmd:
                    sendCallback();
                    break;
            }
    }
}
