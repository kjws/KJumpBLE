package com.example.kjumpble.ble.main.kg;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.kg.KjumpKG517xCallback;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.cmd.kg.KG517xCmd;
import com.example.kjumpble.ble.cmd.ki.KI8186Cmd;
import com.example.kjumpble.ble.cmd.ki.Ki8360Cmd;
import com.example.kjumpble.ble.data.kg.DataFormatOfKG;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.kg.KGGlucoseUnit;
import com.example.kjumpble.ble.format.kg.KGSettings;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;
import com.example.kjumpble.util.KI8186Util;

import java.util.Arrays;

public class KjumpKG517x {
    static final String TAG = KjumpKG517x.class.getSimpleName();

    private final KjumpKG517xCallback callback;

    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    public BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    int numberOfData = 0;
    int indexOfData = 0;
    int indexOfReminder = 0;
    DataFormatOfKG dataFormatOfKG;
    BluetoothGattCharacteristic beWroteCharacteristic;

    byte[] settingsBytes = new byte[24];

    KGSettings settings;

    public KjumpKG517x (BluetoothGatt gatt, KjumpKG517xCallback callback, BluetoothManager bluetoothManager) {
        this.gatt = gatt;
        this.callback = callback;
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

    public void readSetting() {
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
        writeCharacteristic(Ki8360Cmd.refreshDeviceCmd);
    }

    private void readSettingStep2() {
        cmd = BLE_CMD.READ_SETTING_STEP_2;
        writeCharacteristic(Ki8360Cmd.refreshDeviceCmd);
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
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setTimeAndShowFlag(clock_time, enabled);
            writeClockTimeAndFlagPreCmd(clock_time);
        }
    }

    private void writeClockTimeAndFlagPreCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPreCommand(clock_time));
    }

    private void writeClockTimeAndFlagPostCmd (ClockTimeFormat clock_time, boolean enabled) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(Ki8360Cmd.getWriteClockTimeAndEnabledPostCommand(clock_time, enabled));
    }

    /**
     * Set device reminder clock time and enable to alarm.
     * Success or not will show in KjumpKI8360Callback.onWriteClockFinished
     * @param reminder_clock_time : Reminder clock time.
     * @param enabled : True if you want to enable reminder clock.
     */
    public void writeReminderClockTimeAndEnabled (int index, ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (!KI8186Util.checkReminderIndexOutOfRange(index, TAG))
            return;
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteReminderCmd;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            indexOfReminder = index;
            settings.setReminders(index, reminder_clock_time, enabled);
            cmd = BLE_CMD.WRITE_REMINDER_CLOCK;
            writeCharacteristic(KI8186Cmd.getWriteReminderAndFlagCommand(index, reminder_clock_time, enabled));
        }
    }

    public void writeUnit(KGGlucoseUnit unit) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setUnit(unit);
            cmd = BLE_CMD.WRITE_UNIT;
            writeCharacteristic(KG517xCmd.getWriteUnitCommand(settingsBytes[23], unit));
        }
    }

    public void writeLeftOrRightHand(LeftRightHand hand) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteHandCmd;
        if (settings == null) {
            readSettingStep1();
        }
        else {
            settings.setHand(hand);
            cmd = BLE_CMD.WRITE_HAND;
            writeCharacteristic(KG517xCmd.getWriteHandCommand(settingsBytes[23], hand));
        }
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

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    // *****************************************************************************************
    // **                               onCharacteristicChanged                               **
    // *****************************************************************************************
    /**
     *
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
            case WRITE_REMINDER_CLOCK:
            case WRITE_UNIT:
                onGetWaitCmd(characteristic);
                break;
        }
    }

    // *****************************************************************************************
    // **                                     sendCallback                                    **
    // *****************************************************************************************

    private void sendCallback () {
        Log.d(TAG, "sendBroadcast bleClientCmd = " + bleClientCmd + ",cmd = " + cmd);
        switch (bleClientCmd) {
            case ReadSettingsCmd:
                callback.onReadSettings(settings);
                break;
            case WriteSetDeviceCmd:
                callback.onSetDeviceFinished(cmd == BLE_CMD.WRITE_SET);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    callback.onGetNumberOfData(numberOfData);
                break;
            case ReadIndexMemoryCmd:
                callback.onGetIndexMemory(indexOfData, dataFormatOfKG);
                break;
            case ClearAllDataCmd:
                callback.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
            case WriteReminderCmd:
            case WriteUnitCmd:
            case WriteHandCmd:
                callbackForWaitCmd(callback, bleClientCmd);
                break;
        }
    }

    private void callbackForWaitCmd(KjumpKG517xCallback callback, BLE_CLIENT_CMD bleClientCmd) {
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
            case WriteHandCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callback.onWriteHandDisplayFinished(true);
                break;

        }
    }

    private void onGetSettingsStep1 (BluetoothGattCharacteristic characteristic) {
        System.arraycopy(characteristic.getValue(), 1, settingsBytes, 0, 18);
        readSettingStep2();
    }

    private void onGetSettingsStep2 (BluetoothGattCharacteristic characteristic) {
        System.arraycopy(characteristic.getValue(), 1, settingsBytes, 18, 6);
        settings = new KGSettings(settingsBytes);
        switch (bleClientCmd) {
            case ReadSettingsCmd:
                sendCallback();
                break;
            case WriteClockCmd:
                writeClockTimeAndShowFlag(settings.getClockTime(), settings.isClockEnabled());
                break;
            case WriteReminderCmd:
                ReminderFormat reminder = settings.getReminders()[indexOfReminder];
                writeReminderClockTimeAndEnabled(indexOfReminder, reminder.getTime(),
                        reminder.isEnable());
                break;
            case WriteUnitCmd:
                writeUnit(settings.getUnit());
                break;
            case WriteHandCmd:
                writeLeftOrRightHand(settings.getHand());
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
        dataFormatOfKG = new DataFormatOfKG(characteristic.getValue());
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
            writeClockTimeAndFlagPostCmd(settings.getClockTime(), settings.isClockEnabled());
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
                case WriteClockCmd:
                case WriteReminderCmd:
                case WriteUnitCmd:
                case WriteHandCmd:
                    sendCallback();
                    break;
            }
    }
}
