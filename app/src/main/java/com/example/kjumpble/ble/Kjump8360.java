package com.example.kjumpble.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;

import com.example.kjumpble.util.Helper;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.callback.GattCallback;
import com.example.kjumpble.ble.callback.Kjump8360Callback;
import com.example.kjumpble.ble.cmd.Ki8360WriteCmd;
import com.example.kjumpble.ble.data.DataFormatOfKI8360;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.timeFormat.TemperatureUnitEnum;
import com.example.kjumpble.ble.uuid.KI8360UUIDList;

import java.util.ArrayList;
import java.util.Arrays;

public class Kjump8360 implements GattCallback {
    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    int writeRefreshCmdTime = 0;
    public BluetoothGatt gatt;
    byte dataStartPosition = 0;
    int user = 0;
    int numberOfData = 0;
    int dataIndex = 0;
    ArrayList<DataFormatOfKI8360> dataFormatOfKI8360;
    BluetoothGattCharacteristic beWroteCharacteristic;

    // clock
    ClockTimeFormat clock_time;
    boolean enabled;

    private final Kjump8360Callback kjump8360Callback;

    Kjump8360 (BluetoothGatt gatt, Kjump8360Callback kjump8360Callback) {
        this.gatt = gatt;
        this.kjump8360Callback = kjump8360Callback;
    }

    private void dataInit () {
        dataFormatOfKI8360 = new ArrayList<>();
        dataIndex = 0;
        numberOfData = 0;
        user = 0;
        dataStartPosition = 0;
        writeRefreshCmdTime = 0;
        beWroteCharacteristic = gatt.getService(KI8360UUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(KI8360UUIDList.KJUMP_CHARACTERISTIC_WRITE_UUID);
    }

    public void readUserIndex () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadUserAndMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readNumberOfData () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readLatestMemory () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadLatestMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void readAllMemory () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadAllMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic(getCommand(cmd));
    }

    public void initDevice () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteRefreshCmd;
        cmd = BLE_CMD.WRITE_REFRESH_COMMAND;
        writeRefreshCommand();
        final Handler handler = new Handler();
        handler.postDelayed(this::writeRefreshCommand, 3000);
    }

    public void clearAllData () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ClearAllDataCmd;
        cmd = BLE_CMD.CLEAR_DATA;
        writeCharacteristic(getCommand(cmd));
    }

    public void writeClockTimeAndShowFlag (ClockTimeFormat clock_time, boolean enabled) {
        dataInit();
        if (gatt == null)
            return;
        this.clock_time = clock_time;
        this.enabled = enabled;

        writeClockTimePreCmd(clock_time, enabled);
    }

    private void writeClockTimePreCmd (ClockTimeFormat clock_time, boolean enabled) {
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(getWriteClockTimeAndEnabledPreCommand(clock_time, enabled));
    }

    private void writeClockTimePostCmd (ClockTimeFormat clock_time, boolean enabled) {
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_POST_CLOCK;
        writeCharacteristic(getWriteClockTimeAndEnabledPostCommand(clock_time, enabled));
    }

    public void writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (gatt == null)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_REMINDER_CLOCK;
        writeCharacteristic(getWriteReminderClockTimeAndEnabledCommand(reminder_clock_time, enabled));
    }

    public void writeTemperatureUnit (TemperatureUnitEnum unit) {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.WRITE_UNIT_COMMAND;
        writeCharacteristic(getWriteTemperatureUnitCmdCommand(unit));
    }

    private void writeRefreshCommand () {
        cmd = BLE_CMD.WRITE_REFRESH_COMMAND;
        writeRefreshCmdTime++;
        beWroteCharacteristic.setValue(Ki8360WriteCmd.refreshDeviceCmd);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    private void writeCharacteristic (byte[] command) {
        beWroteCharacteristic.setValue(command);
        gatt.writeCharacteristic(beWroteCharacteristic);
    }

    @Override
    public void onCharacteristicChanged (BluetoothGattCharacteristic characteristic) {
        switch (cmd) {
            case CONFIRM_USER_AND_MEMORY_COMMAND:
                Log.d("test8360", "onCharacteristicChanged - CONFIRM_USER_AND_MEMORY_COMMAND");
                dataStartPosition = characteristic.getValue()[7];
                user = characteristic.getValue()[9];
                sendBroadcast();
                switch (bleClientCmd) {
                    case ReadNumberOfDataCmd:
                    case ReadLatestMemoryCmd:
                    case ReadAllMemoryCmd:
                        Log.d("test8360", "write CONFIRM_NUMBER_OF_DATA");
                        cmd = BLE_CMD.CONFIRM_NUMBER_OF_DATA;
                        writeCharacteristic(getCommand(cmd));
                        break;
                }
                break;
            case CONFIRM_NUMBER_OF_DATA:
                Log.d("test8360", "onCharacteristicChanged - CONFIRM_NUMBER_OF_DATA");
                numberOfData = characteristic.getValue()[1];
                Log.d("test8360", "getNumberOfData = " + numberOfData);
                dataFormatOfKI8360 = new ArrayList<>();

                sendBroadcast();

                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        if (numberOfData == 0) {
                            Log.d("test8360", "test 1");
                            cmd = BLE_CMD.NUMBER_OF_DATA_ZERO;
                            sendBroadcast();
                        }
                        else {
                            Log.d("test8360", "test 2");
                            dataIndex = numberOfData - 1;
                            cmd = BLE_CMD.READ_DATA;
                            writeCharacteristic(getCommand(cmd));
                        }
                        break;
                    case ReadAllMemoryCmd:
                        if (numberOfData == 0) {
                            Log.d("test8360", "test 3");
                            cmd = BLE_CMD.NUMBER_OF_DATA_ZERO;
                            sendBroadcast();
                        }
                        else {
                            Log.d("test8360", "test 4");
                            cmd = BLE_CMD.READ_DATA;
                            writeCharacteristic(getCommand(cmd));
                        }
                        break;
                }
                break;
            case READ_DATA:
                Log.d("test8360", "onCharacteristicChanged - READ_DATA");
                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        Log.d("test8360", "add data for 8360 - last memory");
                        dataFormatOfKI8360.add(new DataFormatOfKI8360(characteristic.getValue()));
                        if (dataIndex == numberOfData - 1)
                            sendBroadcast();
                        break;
                    case ReadAllMemoryCmd:
                        dataFormatOfKI8360.add(new DataFormatOfKI8360(characteristic.getValue()));
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
                Log.d("test8360", "onCharacteristicChanged - CLEAR_DATA");
                if (Arrays.equals(characteristic.getValue(), Ki8360WriteCmd.writeReturnCmd)) {
                    sendBroadcast();
                }
                break;
            case WRITE_PRE_CLOCK:
                Log.d("test8360", "onCharacteristicChanged - WRITE_PRE_CLOCK");
                if (Arrays.equals(characteristic.getValue(), Ki8360WriteCmd.writeReturnCmd)) {
                    writeClockTimePostCmd(clock_time, enabled);
                }
                break;
            case WRITE_POST_CLOCK:
                Log.d("test8360", "onCharacteristicChanged - WRITE_POST_CLOCK");
                if (Arrays.equals(characteristic.getValue(), Ki8360WriteCmd.writeReturnCmd)) {
                    writeRefreshCommand();
                }
                break;
            case WRITE_REMINDER_CLOCK:
                Log.d("test8360", "onCharacteristicChanged - WRITE_REMINDER_CLOCK");
                if (Arrays.equals(characteristic.getValue(), Ki8360WriteCmd.writeReturnCmd)) {
                    writeRefreshCommand();
                }
                break;
            case WRITE_UNIT_COMMAND:
                Log.d("test8360", "onCharacteristicChanged - WRITE_UNIT_COMMAND");
                if (Arrays.equals(characteristic.getValue(), Ki8360WriteCmd.writeReturnCmd)) {
                    writeRefreshCommand();
                }
                break;
            case WRITE_REFRESH_COMMAND:
                Log.d("test8360", "onCharacteristicChanged - WRITE_REFRESH_COMMAND");
                switch (bleClientCmd) {
                    case WriteClockCmd:
                        sendBroadcast();
                        break;
                    case WriteRefreshCmd:
                        if (writeRefreshCmdTime == 2) {
                            sendBroadcast();
                        }
                }
                break;
        }
    }

    private byte[] getCommand (BLE_CMD cmd) {
        switch (cmd) {
            case CONFIRM_USER_AND_MEMORY_COMMAND:
                return Ki8360WriteCmd.confirmUserAndMemoryCmd;
            case CONFIRM_NUMBER_OF_DATA:
                return commandForConfirmNumberOfData();
            case READ_DATA:
                return commandForReadData();
            case CLEAR_DATA:
                return Ki8360WriteCmd.clearDataCmd;
            default:
                return new byte[]{};
        }
    }

    private byte[] getWriteClockTimeAndEnabledPreCommand (ClockTimeFormat time, boolean enabled) {
        byte[] command = Ki8360WriteCmd.writeClockTimeAndFlagPreCmd;
        command[4] = (byte) (time.year - 2000);
        command[5] = (byte) time.month;
        command[6] = (byte) time.day;
        command[7] = (byte) time.hour;
        return command;
    }

    private byte[] getWriteClockTimeAndEnabledPostCommand (ClockTimeFormat time, boolean enabled) {
        byte[] command = Ki8360WriteCmd.writeClockTimeAndFlagPostCmd;
        command[4] = (byte) time.minute;
        command[5] = (byte) time.second;
        command[6] = (byte) (enabled ? 0x01 : 0x00);
        return command;
    }

    private byte[] getWriteReminderClockTimeAndEnabledCommand (ReminderTimeFormat time, boolean enabled) {
        byte[] command = Ki8360WriteCmd.writeReminderClockTimeAndFlagCmd;
        command[6] = (byte) (time.hour + (enabled ? 0x80 : 0x00));
        command[7] = (byte) time.minute;
        Log.d("test8360", "command = " + Helper.getHexStr(command));
        return command;
    }

    private byte[] getWriteTemperatureUnitCmdCommand (TemperatureUnitEnum unit) {
        byte[] command = Ki8360WriteCmd.writeTemperatureUnitCmd;
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

    private byte[] commandForConfirmNumberOfData () {
        byte[] command = Ki8360WriteCmd.readNumberOfDataCmd;
        command[3] = (byte) (0x6c + (user - 1) * 2);
        return command;
    }

    private byte[] commandForReadData () {
        Log.d("test8360", "commandForReadData.dataIndex = " + dataIndex);
        byte[] command = Ki8360WriteCmd.readDataCmd;
        command[3] = (byte) (0xa8 + dataIndex * 0x08);
        return command;
    }

    private void sendBroadcast () {
        Log.d("test8360", "sendBroadcast bleClientCmd = " + bleClientCmd);
        Log.d("test8360", "sendBroadcast bleCmd = " + cmd);
        switch (bleClientCmd) {
            case ReadUserAndMemoryCmd:
                if (cmd == BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND) {
                    kjump8360Callback.onGetUserAndMemory(user);
                }
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA) {
                    kjump8360Callback.onGetNumberOfData(numberOfData);
                }
                break;
            case ReadLatestMemoryCmd:
                switch (cmd) {
                    case READ_DATA:
                        Log.d("test8360", "onReadLatestMemory nonnull");
                        kjump8360Callback.onGetLastMemory(dataFormatOfKI8360.get(0));
                        break;
                    case NUMBER_OF_DATA_ZERO:
                        Log.d("test8360", "onReadLatestMemory null");
                        kjump8360Callback.onGetLastMemory(null);
                        break;
                }
                break;
            case ReadAllMemoryCmd:
                switch (cmd) {
                    case READ_DATA:
                        Log.d("test8360", "onReadLatestMemory nonnull");
                        kjump8360Callback.onGetAllMemory(dataFormatOfKI8360);
                        break;
                    case NUMBER_OF_DATA_ZERO:
                        Log.d("test8360", "onReadLatestMemory null");
                        kjump8360Callback.onGetAllMemory(dataFormatOfKI8360);
                        break;
                }
                break;
            case ClearAllDataCmd:
                kjump8360Callback.onClearAllDataFinished(true);
                break;
            case WriteClockCmd:
                if (cmd == BLE_CMD.WRITE_REFRESH_COMMAND) {
                    kjump8360Callback.onWriteClockFinished(true);
                }
                break;
            case WriteUnitCmd:
                if (cmd == BLE_CMD.WRITE_REFRESH_COMMAND) {
                    kjump8360Callback.onWriteUnitFinished(true);
                }
                break;
            case WriteRefreshCmd:
                if (cmd == BLE_CMD.WRITE_REFRESH_COMMAND) {
                    kjump8360Callback.onInitDeviceFinished(true);
                }
        }
    }
}
