package com.example.kjumpble.ble.main.kd;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.util.Log;

import com.example.kjumpble.ble.LeConnectStatus;
import com.example.kjumpble.ble.callback.kd.KjumpKD2070Callback;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.cmd.kd.KD2070Cmd;
import com.example.kjumpble.ble.cmd.ki.Ki8360Cmd;
import com.example.kjumpble.ble.data.kd.DataFormatOfKD;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.format.kd.KDTemperatureUnitAndHand;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;
import com.example.kjumpble.util.BLEUtil;

import java.util.Arrays;

public class KjumpKD2070 {
    static final String TAG = KjumpKD2070.class.getSimpleName();
    private final KjumpKD2070Callback callBack;
    public final BluetoothGatt gatt;
    private final BluetoothManager bluetoothManager;
    private BluetoothGattCharacteristic beWroteCharacteristic;

    int indexOfData;
    BLE_CMD cmd;
    BLE_CLIENT_CMD bleClientCmd;

    DataFormatOfKD dataFormatOfKD;
    private int numberOfData = 0;
    // clock
    ClockTimeFormat clock_time;

    TemperatureUnit temperatureUnit;
    LeftRightHand hand;

    public KjumpKD2070 (BluetoothGatt gatt, KjumpKD2070Callback callBack, BluetoothManager bluetoothManager) {
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
     */
    public void writeClockTime (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        this.clock_time = clock_time;

        writeClockTimePreCmd(clock_time);
    }

    private void writeClockTimePreCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_PRE_CLOCK;

        writeCharacteristic(KD2070Cmd.getWriteClockTimePreCommand(clock_time));
    }

    private void writeClockTimePostCmd (ClockTimeFormat clock_time) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        bleClientCmd = BLE_CLIENT_CMD.WriteClockCmd;
        cmd = BLE_CMD.WRITE_POST_CLOCK;

        writeCharacteristic(KD2070Cmd.getWriteClockTimePostCommand(clock_time));
    }


    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeTemperatureUnit (TemperatureUnit unit) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteUnitCmd;
        cmd = BLE_CMD.READ_TEMPERATURE_UNIT_AND_HAND;
        writeCharacteristic(KD2070Cmd.readTempUnitAndHandCmd);
    }

    /**
     * Write which hand do you want to take the device, it will effect on device screen orientation.
     * @param hand : Which hand do you take the device.
     */
    public void writeHand (LeftRightHand hand) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        bleClientCmd = BLE_CLIENT_CMD.WriteHandCmd;
        cmd = BLE_CMD.READ_TEMPERATURE_UNIT_AND_HAND;
        writeCharacteristic(KD2070Cmd.readTempUnitAndHandCmd);
    }

    private void writeHandAndUnit (TemperatureUnit unit, LeftRightHand hand) {
        if (new BLEUtil().checkConnectStatus(bluetoothManager, gatt, TAG) == LeConnectStatus.DisConnected)
            return;
        dataInit();
        cmd = BLE_CMD.READ_TEMPERATURE_UNIT_AND_HAND;
        writeCharacteristic(KD2070Cmd.getWriteTempUnitAndHandCmd(unit, hand));
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
            case WRITE_SET:
                if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
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
                onGetPostClockCmd(characteristic);
                break;
            case READ_TEMPERATURE_UNIT_AND_HAND:
                onGetUnitAndHand(characteristic);
                break;
            case WRITE_TEMPERATURE_UNIT_AND_HAND:
                onGetWriteTemperatureUnitAndHandCmd(characteristic);
                break;
        }
    }

    private void sendCallback () {
        Log.d(TAG, "sendBroadcast bleClientCmd = " + bleClientCmd + ",cmd = " + cmd);
        switch (bleClientCmd) {
            case WriteSetDeviceCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callBack.onSetDeviceFinished(true);
                break;
            case ReadNumberOfDataCmd:
                if (cmd == BLE_CMD.CONFIRM_NUMBER_OF_DATA)
                    callBack.onGetNumberOfData(numberOfData);
                break;
            case ReadIndexMemoryCmd:
                callBack.onGetIndexMemory(indexOfData, dataFormatOfKD);
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
            case WriteHandCmd:
                if (cmd == BLE_CMD.WRITE_SET)
                    callBack.onWriteHandFinished(true);
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
        dataFormatOfKD = new DataFormatOfKD(characteristic.getValue());
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
     * onCharacteristicChanged trigger and command is WRITE_REFRESH_COMMAND
     * @param characteristic characteristic
     */
    private void onGetRefreshCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            switch (bleClientCmd) {
                case WriteSetDeviceCmd:
                case WriteUnitCmd:
                case WriteHandCmd:
                case WriteClockCmd:
                    sendCallback();
                    break;
            }
    }

    /**
     * onCharacteristicChanged trigger and command is WRITE_PRE_CLOCK
     * @param characteristic characteristic
     */
    private void onGetPreClockCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            writeClockTimePostCmd(clock_time);
    }

    private void onGetPostClockCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            setDevice();
    }

    private void onGetUnitAndHand (BluetoothGattCharacteristic characteristic) {
        KDTemperatureUnitAndHand kdTemperatureUnitAndHand = new KDTemperatureUnitAndHand(characteristic);
        this.temperatureUnit = kdTemperatureUnitAndHand.getUnit();
        this.hand = kdTemperatureUnitAndHand.getHand();

        writeHandAndUnit(temperatureUnit, hand);
    }

    private void onGetWriteTemperatureUnitAndHandCmd (BluetoothGattCharacteristic characteristic) {
        if (Arrays.equals(characteristic.getValue(), Ki8360Cmd.writeReturnCmd))
            setDevice();
    }
}
