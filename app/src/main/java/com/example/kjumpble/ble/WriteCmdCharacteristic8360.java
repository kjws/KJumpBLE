package com.example.kjumpble.ble;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.util.Log;

import com.example.kjumpble.ble.cmd.BLE_CMD;

import java.io.Serializable;

public class WriteCmdCharacteristic8360 implements GattCallback {
    private static final byte[] confirmUserAndMemoryCmd = new byte[]{0x02, 0x0a, 0x00, (byte) 0x80};
    private static final byte[] readNumberOfDataCmd = new byte[]{0x02, 0x01, 0x00, 0x6c};
    private static final byte[] readDataCmd = new byte[]{0x02, 0x09, 0x00, (byte) 0xa8}; // 02, 09, 00, 0xa8(起始位置)
    private static final byte[] clearDataCmd = new byte[]{0x03, 0x01, 0x00, (byte) 0x6c, 0x00};

    Service service;
    BluetoothGatt gatt;
    byte dataStartPosition = 0;
    int user = 0;
    int numberOfData = 0;
    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;
    DataFor8360[] dataFor8360;
    BluetoothGattCharacteristic beWroteCharacteristic;
    int dataIndex = 0;

    WriteCmdCharacteristic8360(Service service, BluetoothGatt gatt) {
        this.service = service;
        this.gatt = gatt;
    }

    private void dataInit() {
        beWroteCharacteristic = gatt.getService(SampleServiceAttributes.OUcare_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_WRITE_FFF3_UUID);
    }

    public void getNumberOfData() {

    }

    public void readUserIndex () {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadUserAndMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic();
    }

    public void readNumberOfData() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadNumberOfDataCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic();
    }

    public void readLatestMemory() {
        dataInit();
        if (gatt == null)
            return;
        bleClientCmd = BLE_CLIENT_CMD.ReadLatestMemoryCmd;
        cmd = BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND;
        writeCharacteristic();
    }

    private void writeCharacteristic() {
        beWroteCharacteristic.setValue(getCommand(cmd));
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
                        cmd = BLE_CMD.CONFIRM_NUMBER_OF_DATA;
                        writeCharacteristic();
                        break;
                }
                break;
            case CONFIRM_NUMBER_OF_DATA:
                numberOfData = characteristic.getValue()[1];
                Log.d("test8360", "getNumberOfData = " + numberOfData);
                dataFor8360 = new DataFor8360[numberOfData];
                sendBroadcast();
                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        dataIndex = numberOfData - 1;
                        cmd = BLE_CMD.READ_DATA;
                        writeCharacteristic();
                        break;
                }
                break;
            case READ_DATA:
                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                        dataFor8360[0] = new DataFor8360(characteristic.getValue());
                        break;
                    case ReadAllMemoryCmd:
                        dataFor8360[dataIndex] = new DataFor8360(characteristic.getValue());
                        break;
                }
                switch (bleClientCmd) {
                    case ReadLatestMemoryCmd:
                    case ReadAllMemoryCmd:
                        if (numberOfData == dataIndex)
                            sendBroadcast();
                        break;
                }
                break;
            case CLEAR_DATA:
                break;
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
        Log.d("test8360", "sendBroadcast bleClientCmd = " + bleClientCmd);
        boolean canSend = false;
        switch (bleClientCmd) {
            case ReadUserAndMemoryCmd:
                if (cmd == BLE_CMD.CONFIRM_USER_AND_MEMORY_COMMAND) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.DoingAction, ResultReceiveIntent.SEND_USER_INDEX_INTENT);
                    intent.putExtra(BroadcastIntentExtraName.UserIndex, user);
                }
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.DoingAction, ResultReceiveIntent.SEND_NUMBER_OF_DATA_INTENT);
                    intent.putExtra(BroadcastIntentExtraName.NumberOfData, numberOfData);
                }
                break;
            case ReadLatestMemoryCmd:
                if (cmd == BLE_CMD.READ_DATA & dataIndex == numberOfData - 1) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.DoingAction, ResultReceiveIntent.SEND_LATEST_MEMORY_INTENT);
                    intent.putExtra(BroadcastIntentExtraName.Data, (Serializable) dataFor8360[0]);
                }
                break;
            case ReadAllMemoryCmd:
                if (cmd == BLE_CMD.READ_DATA & dataIndex == numberOfData - 1) {
                    canSend = true;
                    intent.putExtra(BroadcastIntentExtraName.DoingAction, ResultReceiveIntent.SEND_ALL_MEMORY_INTENT);
                    intent.putExtra(BroadcastIntentExtraName.Data, dataFor8360);
                }
                break;
        }
        Log.d("test8360", "testSendBroadCast");
        if (canSend) {
            service.sendBroadcast(intent);
        }
    }
}
