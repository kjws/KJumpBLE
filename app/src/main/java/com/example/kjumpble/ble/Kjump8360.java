package com.example.kjumpble.ble;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.kjumpble.Helper;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.timeFormat.TemperatureUnitEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Kjump8360 implements GattCallback {
    private static final byte[] confirmUserAndMemoryCmd = new byte[]{0x02, 0x0a, 0x00, (byte) 0x80};
    private static final byte[] readNumberOfDataCmd = new byte[]{0x02, 0x01, 0x00, 0x6c};
    private static final byte[] readDataCmd = new byte[]{0x02, 0x09, 0x00, (byte) 0xa8}; // 02, 09, 00, 0xa8(起始位置)
    private static final byte[] clearDataCmd = new byte[]{0x03, 0x01, 0x00, (byte) 0x6c, 0x00};

    // clock
    private static final byte[] writeClockTimeAndFlagPreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x01, 0x02, 0x03, 0x04}; // 年月日時
    private static final byte[] writeClockTimeAndFlagPostCmd = new byte[]{0x03, 0x03, 0x00, 0x58,
            0x05, 0x06, 0x01}; // 年月日時分秒enable

    // reminder
    private static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x03, 0x00, 0x5e,
            0x01, 0x02}; // enable時分


    private static final byte[] refreshDeviceCmd = new byte[]{0x03, 0x01, 0x00, 0x64,
            (byte) 0x99};

    private static final byte[] writeReturnCmd = new byte[]{0x03, 0x55, (byte) 0xaa};

    private static final byte[] writeTemperatureUnitCmd = new byte[]{0x03, 0x01, 0x00, 0x6b,
            0x01};

    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    int writeRefreshCmdTime = 0;
    Service service;
    BluetoothGatt gatt;
    byte dataStartPosition = 0;
    int user = 0;
    int numberOfData = 0;
    int dataIndex = 0;
    ArrayList<DataFor8360> dataFor8360;
    BluetoothGattCharacteristic beWroteCharacteristic;

    // clock
    ClockTimeFormat clock_time;
    boolean enabled;

    Kjump8360 (Service service, BluetoothGatt gatt) {
        this.service = service;
        this.gatt = gatt;
    }

    private void dataInit() {
        dataFor8360 = new ArrayList<DataFor8360>();
        dataIndex = 0;
        numberOfData = 0;
        user = 0;
        dataStartPosition = 0;
        writeRefreshCmdTime = 0;
        beWroteCharacteristic = gatt.getService(SampleServiceAttributes.OUcare_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_WRITE_FFF3_UUID);
    }

    public void readUserIndex () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadUserAndMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readNumberOfData() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readLatestMemory() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadLatestMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readAllMemory() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadAllMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void initDevice() {
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteRefreshCmd;
        cmd = BLE_CMD.WRITE_REFRESH_COMMAND;
        writeRefreshCommand();
        final Handler handler = new Handler();
        handler.postDelayed(this::writeRefreshCommand, 3000);
    }

    public void clearAllData() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ClearAllDataCmd;
        cmd = BLE_CMD.CLEAR_DATA;
        writeCharacteristic(getCommand(cmd));
    }

    public void writeClockTimeAndShowFlag(ClockTimeFormat clock_time, boolean enabled) {
        dataInit();
        this.clock_time = clock_time;
        this.enabled = enabled;
        writeClockTimePreCmd(clock_time, enabled);
    }

    private void writeClockTimePreCmd(ClockTimeFormat clock_time, boolean enabled) {
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(getWriteClockTimeAndEnabledPreCommand(clock_time, enabled));
    }

    private void writeClockTimePostCmd(ClockTimeFormat clock_time, boolean enabled) {
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(getWriteClockTimeAndEnabledPostCommand(clock_time, enabled));
    }


    public void writeReminderClockTimeAndEnabled(ReminderTimeFormat reminder_clock_time, boolean enabled) {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_REMINDER_CLOCK;
        writeCharacteristic(getWriteReminderClockTimeAndEnabledCommand(reminder_clock_time, enabled));
    }

    public void writeTemperatureUnit(TemperatureUnitEnum unit) {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.WRITE_UNIT_COMMAND;
        writeCharacteristic(getWriteTemperatureUnitCmdCommand(unit));
    }

    private void writeRefreshCommand() {
        cmd = BLE_CMD.WRITE_REFRESH_COMMAND;
        writeRefreshCmdTime++;
        beWroteCharacteristic.setValue(refreshDeviceCmd);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    private void writeCharacteristic(byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    @Override
    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        switch (cmd) {
            case CONFIRM_USER_AND_MEMORY_COMMAND:
                dataStartPosition = characteristic.getValue()[7];
                user = characteristic.getValue()[9];
                sendBroadcast();
                switch (bleClientCmd) {
                    case ReadNumberOfDataCmd:
                    case ReadLatestMemoryCmd:
                    case ReadAllMemoryCmd:
                        cmd = BLE_CMD.CONFIRM_NUMBER_OF_DATA;
                        writeCharacteristic(getCommand(cmd));
                        break;
                }
                break;
            case CONFIRM_NUMBER_OF_DATA:
                numberOfData = characteristic.getValue()[1];
                Log.d("test8360", "getNumberOfData = " + numberOfData);
                dataFor8360 = new ArrayList<DataFor8360>();

                if (numberOfData == 0) {
                    Log.d("test8360", "Client CMD = " + bleClientCmd);
                    cmd = BLE_CMD.NUMBER_OF_DATA_ZERO;
                    sendBroadcast();
                    break;
                }
                sendBroadcast();

                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        if (numberOfData != 0) {
                            Log.d("test8360", "testReadLatest 4");
                            dataIndex = numberOfData - 1;
                            cmd = BLE_CMD.READ_DATA;
                            writeCharacteristic(getCommand(cmd));
                        }
                        break;
                    case ReadAllMemoryCmd:
                        if (numberOfData != 0) {
                            cmd = BLE_CMD.READ_DATA;
                            writeCharacteristic(getCommand(cmd));
                        }
                        break;
                }
                break;
            case READ_DATA:
                Log.d("test8360", "testReadLatest 3");
                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        dataFor8360.add(new DataFor8360(characteristic.getValue()));
                        Log.d("test8360", "testReadLatest 2");
                        if (dataIndex == numberOfData - 1)
                            Log.d("test8360", "testReadLatest 1");
                            sendBroadcast();
                        break;
                    case ReadAllMemoryCmd:
                        dataFor8360.add(new DataFor8360(characteristic.getValue()));
                        dataIndex++;
                        if (dataIndex == numberOfData)
                            sendBroadcast();
                        else {
                            writeCharacteristic(getCommand(cmd));
                        }
                        break;
                }
                break;
            case CLEAR_DATA:
                if (Arrays.equals(characteristic.getValue(), writeReturnCmd)) {
                    sendBroadcast();
                }
                break;
            case WRITE_PRE_CLOCK:
                if (Arrays.equals(characteristic.getValue(), writeReturnCmd)) {
                    writeClockTimePostCmd(clock_time, enabled);
                }
                break;
            case WRITE_POST_CLOCK:
            case WRITE_REMINDER_CLOCK:
            case WRITE_UNIT_COMMAND:
                if (Arrays.equals(characteristic.getValue(), writeReturnCmd)) {
                    writeRefreshCommand();
                }
                break;
            case WRITE_REFRESH_COMMAND:
                switch (bleClientCmd) {
                    case WriteClockCmd:
                        sendBroadcast();
                        break;
                    case WriteRefreshCmd:
                        if (writeRefreshCmdTime == 2) {
                            sendBroadcast();
                        }
                }
        }
    }

    private byte[] getCommand (BLE_CMD cmd) {
        switch (cmd) {
            case CONFIRM_USER_AND_MEMORY_COMMAND:
                return confirmUserAndMemoryCmd;
            case CONFIRM_NUMBER_OF_DATA:
                return commandForConfirmNumberOfData();
            case READ_DATA:
                return commandForReadData();
            case CLEAR_DATA:
                return clearDataCmd;
            default:
                return new byte[]{};
        }
    }

    private byte[] getWriteClockTimeAndEnabledPreCommand (ClockTimeFormat time, boolean enabled) {
        byte[] command = writeClockTimeAndFlagPreCmd;
        command[4] = (byte) (time.year - 2000);
        command[5] = (byte) time.month;
        command[6] = (byte) time.day;
        command[7] = (byte) time.hour;
        return command;
    }

    private byte[] getWriteClockTimeAndEnabledPostCommand (ClockTimeFormat time, boolean enabled) {
        byte[] command = writeClockTimeAndFlagPostCmd;
        command[4] = (byte) time.minute;
        command[5] = (byte) time.second;
        command[6] = (byte) (enabled ? 0x01 : 0x00);
        return command;
    }

    private byte[] getWriteReminderClockTimeAndEnabledCommand (ReminderTimeFormat time, boolean enabled) {
        byte[] command = writeReminderClockTimeAndFlagCmd;
        command[4] = (byte) (time.hour + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.minute;
        Log.d("test8360", "command = " + Helper.getHexStr(command));
        return command;
    }

    private byte[] getWriteTemperatureUnitCmdCommand (TemperatureUnitEnum unit) {
        byte[] command = writeTemperatureUnitCmd;
        byte unitByte = 0x00;
        switch (unit) {
            case C:
                unitByte = 0x00;
                break;
            case F:
                unitByte = 0x01;
                break;
        }
        command[4] = unitByte;
        Log.d("test8360", "command = " + Helper.getHexStr(command));
        return command;
    }

    private byte[] commandForConfirmNumberOfData() {
        byte[] command = readNumberOfDataCmd;
        command[3] = (byte) (0x6c + (user - 1) * 2);
        return command;
    }

    private byte[] commandForReadData() {
        Log.d("test8360", "commandForReadData.dataIndex = " + dataIndex);
        byte[] command = readDataCmd;
        command[3] = (byte) (0xa8 + dataIndex * 0x08);
        return command;
    }

    private void sendBroadcast () {
        Intent intent = new Intent();
        intent.setAction(ResultReceiveIntent.SEND_RESULT_INTENT);
        intent.putExtra(BroadcastIntentExtraName.DoingAction, bleClientCmd);
        Log.d("test8360", "sendBroadcast bleClientCmd = " + bleClientCmd);
        boolean canSend = false;
        switch (bleClientCmd) {
            case ReadUserAndMemoryCmd:
                if (cmd == BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.UserIndex, user);
                }
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.NumberOfData, numberOfData);
                }
                break;
            case ReadLatestMemoryCmd:
                if (cmd == BLE_CMD.READ_DATA & dataIndex >= numberOfData - 1) {
                    canSend = true;
                    Bundle extra = new Bundle();
                    extra.putSerializable(BroadcastIntentExtraName.Data, dataFor8360);
                    intent.putExtra("extra", extra);
                }
                else if (cmd == BLE_CMD.NUMBER_OF_DATA_ZERO) {
                    Log.d("test8360", "First sendBroadcast bleClientCmd = " + bleClientCmd);
                    canSend = true;
                    Bundle extra = new Bundle();
                    extra.putSerializable(BroadcastIntentExtraName.Data, dataFor8360);
                    intent.putExtra("extra", extra);
                }
                break;
            case ReadAllMemoryCmd:
                if (cmd == BLE_CMD.READ_DATA & dataIndex >= numberOfData - 1) {
                    canSend = true;
                    Log.d("test8360", "first get all memory = " + dataFor8360.get(0).Temperature);
                    Bundle extra = new Bundle();
                    extra.putSerializable(BroadcastIntentExtraName.Data, dataFor8360);
                    intent.putExtra("extra", extra);
                }
                else if (cmd == BLE_CMD.NUMBER_OF_DATA_ZERO) {
                    Log.d("test8360", "Second sendBroadcast bleClientCmd = " + bleClientCmd);
                    canSend = true;
                    Log.d("test8360", "first get all memory = " + dataFor8360.get(0).Temperature);
                    Bundle extra = new Bundle();
                    extra.putSerializable(BroadcastIntentExtraName.Data, dataFor8360);
                    intent.putExtra("extra", extra);
                }
                break;
            case ClearAllDataCmd:
                canSend = true;
                break;
            case WriteClockCmd:
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_REFRESH_COMMAND) {
                    canSend = true;
                }
                break;
        }
        if (canSend) {
            Log.d("test8360", "testSendBroadCast");
            service.sendBroadcast(intent);
        }
    }
}
