package com.example.kjumpble.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.kjumpble.ble.callback.kd.KjumpKD2070Callback;
import com.example.kjumpble.ble.callback.ki.KjumpKI8360Callback;
import com.example.kjumpble.ble.callback.KjumpKPCallback;
import com.example.kjumpble.ble.data.kd.KDData;
import com.example.kjumpble.ble.format.KP.KPSettings;
import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPUser;
import com.example.kjumpble.ble.format.KP.SenseMode;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.kd.KD2070Settings;
import com.example.kjumpble.ble.main.kd.KjumpKD2070;
import com.example.kjumpble.ble.main.ki.KjumpKI8360;
import com.example.kjumpble.ble.main.KjumpKP;
import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;
import com.example.kjumpble.util.DeviceRegex;
import com.example.kjumpble.util.Helper;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.callback.OnProgressListener;
import com.example.kjumpble.ble.data.ki.KIData;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.uuid.KjumpUUIDList;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class BLEService extends Service {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothManager bluetoothManager;
    private final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private final LeDeviceListAdapter scannedDeviceListAdapter = new LeDeviceListAdapter();
    private final LeDeviceListAdapter connectingDeviceListAdapter = new LeDeviceListAdapter();
    private boolean scanning;
    private final Handler handler = new Handler();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";

    private int connectionState;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private final IBinder msgBinder = new MsgBinder();

    private BLE_CMD cmd;

    KjumpKP kjumpKP;
    KjumpKI8360 kjumpKI8360;
    KjumpKD2070 kjumpKD2070;

    @Override
    public void onCreate () {
        super.onCreate();

        Log.d("test8360", "BLEService onCreate");
    }

    @Override
    public IBinder onBind (Intent intent) {
        Log.d("test8360", "BLEService onBind");

//        registerReceiver(resultReceiver, new IntentFilter(ResultReceiveIntent.SEND_RESULT_INTENT));
        return msgBinder;
    }

    @Override
    public boolean onUnbind (Intent intent) {
//        unregisterReceiver(resultReceiver);
        Log.d("test8360", "BLEService onUnbind");
//        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        stopSelf();
        Log.d("test8360", "BLEService onDestroy");
    }


    public void scanLeDevice () {
        if (!bluetoothAdapter.isEnabled()) {
            onProgressListener.onScanMotionFailed();
            return;
        } else if (scanning) {

        }
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run () {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    onProgressListener.onStopScan();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            onProgressListener.onStartScan();
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            onProgressListener.onStopScan();
        }
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult (int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (scannedDeviceListAdapter.containsDevice(result.getDevice())) {

            } else {
                scannedDeviceListAdapter.addDevice(result.getDevice());
                connectDevice(result.getDevice());
            }
        }
    };

    private void connectDevice (final BluetoothDevice device) {
        if (device.getName() != null) {
            Log.d("test8360", "device: " + device.getName());
            String deviceName = device.getName();
            if (Pattern.matches(DeviceRegex.KPSeries, deviceName)) {
                connect(device.getAddress());
            }
            else if (Pattern.matches(DeviceRegex.KI8180, deviceName)) {

            }
            else if (Pattern.matches(DeviceRegex.KI8186, deviceName)) {

            }
            else if (Pattern.matches(DeviceRegex.KI8360, deviceName)) {
                connect(device.getAddress());
            }
            else if (Pattern.matches(DeviceRegex.KG517x, deviceName)) {

            }
            else if (Pattern.matches(DeviceRegex.KD2070, deviceName)) {
                connect(device.getAddress());
            }
            else if (Pattern.matches(DeviceRegex.KD2161, deviceName)) {

            }
        }
    }

    private void connect (final String address) {
        if (bluetoothAdapter == null || address == null) {
            return;
        }
        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            device.connectGatt(this, true, bluetoothGattCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange (BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                // successfully connected to the GATT Server
                onProgressListener.onConnected(gatt);
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);

                // init kjump8360
                Log.d("testService", "connected");
                String deviceName = gatt.getDevice().getName();
                if (Pattern.matches(DeviceRegex.KPSeries, deviceName)) {
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    kjumpKP = new KjumpKP(gatt, kjumpKPCallback, bluetoothManager);
                }
                else if (Pattern.matches(DeviceRegex.KI8180, deviceName)) {

                }
                else if (Pattern.matches(DeviceRegex.KI8186, deviceName)) {

                }
                else if (Pattern.matches(DeviceRegex.KI8360, deviceName)) {
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    kjumpKI8360 = new KjumpKI8360(gatt, kjumpKI8360Callback, bluetoothManager);
                }
                else if (Pattern.matches(DeviceRegex.KG517x, deviceName)) {

                }
                else if (Pattern.matches(DeviceRegex.KD2070, deviceName)) {
                    Log.d("testkd2070", "connect");
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    kjumpKD2070 = new KjumpKD2070(gatt, kjumpKD2070Callback, bluetoothManager);
                }
                else if (Pattern.matches(DeviceRegex.KD2161, deviceName)) {

                }
                // add device in connecting devices
                connectingDeviceListAdapter.addDevice(gatt.getDevice());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("testService", "disconnected");

                // disconnected from the GATT Server
                onProgressListener.onDisConnected();
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);

                // delete device in connecting devices
                connectingDeviceListAdapter.deleteDevice(gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered (BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(KjumpUUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(KjumpUUIDList.KJUMP_CHARACTERISTIC_READ_UUID);
                setCharacteristicNotification(characteristic, true);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w("test8360", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.w("test8360", "onCharacteristicChanged = " + Helper.getHexStr(characteristic.getValue()));
            if (kjumpKI8360 != null)
                kjumpKI8360.onCharacteristicChanged(characteristic);
            if (kjumpKP != null)
                kjumpKP.onCharacteristicChanged(characteristic);
            if (kjumpKD2070 != null)
                kjumpKD2070.onCharacteristicChanged(characteristic);
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.w("test8360", "onCharacteristicWrite = " + Helper.getHexStr(characteristic.getValue()));
        }
    };

    private void broadcastUpdate (final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private OnProgressListener onProgressListener;

    public void setOnProgressListener (OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public class MsgBinder extends Binder {
        public BLEService getServiceInstance () {
            return BLEService.this;
        }
    }

    public void readCharacteristic (BluetoothGattCharacteristic characteristic) {
        if (kjumpKI8360 == null)
            return;
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjumpKI8360.gatt.readCharacteristic(characteristic);
    }

    /**
     * Notify indicate characteristic to make LE service get onCharacteristicChanged.
     * @param characteristic : BluetoothGattCharacteristic which you want to notify.
     * @param enabled : Enable or disable notification.
     */
    private void setCharacteristicNotification (BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (kjumpKI8360 == null && kjumpKP == null && kjumpKD2070 == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        if (kjumpKI8360 != null) {
            if (kjumpKI8360.gatt.setCharacteristicNotification(characteristic, enabled)) {
                Log.w("test8360", "Notification success");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KjumpUUIDList.KJUMP_CHARACTERISTIC_DESCRIBE_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                kjumpKI8360.gatt.writeDescriptor(descriptor);
            }
        }
        if (kjumpKP != null) {
            if (kjumpKP.gatt.setCharacteristicNotification(characteristic, enabled)) {
                Log.w("test8360", "Notification success");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KjumpUUIDList.KJUMP_CHARACTERISTIC_DESCRIBE_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                kjumpKP.gatt.writeDescriptor(descriptor);
            }
        }
        if (kjumpKD2070 != null) {
            if (kjumpKD2070.gatt.setCharacteristicNotification(characteristic, enabled)) {
                Log.w("test8360", "Notification success");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KjumpUUIDList.KJUMP_CHARACTERISTIC_DESCRIBE_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                kjumpKD2070.gatt.writeDescriptor(descriptor);
            }
        }
    }


    /**
     * Write command for doing what you want to get.
     * @param clientCmd :
     *             ReadNumberOfDataCmd: To get number of temperature data.
     *             ReadLatestMemoryCmd: To get latest memory
     *             ReadAllMemoryCmd: To get all memory
     *             ClearAllDataCmd: To clear all data
     */
    public void writeCommand (BLE_CLIENT_CMD clientCmd) {
        if (kjumpKI8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        switch (clientCmd) {
            case ReadUserAndMemoryCmd:
                kjumpKI8360.readUserIndex();
                break;
            case ReadNumberOfDataCmd:
                kjumpKI8360.readNumberOfData();
                break;
            case ReadLatestMemoryCmd:
                kjumpKI8360.readLatestMemory();
                break;
            case ReadAllMemoryCmd:
                kjumpKI8360.readAllMemory();
                break;
            case ClearAllDataCmd:
                kjumpKI8360.clearAllData();
                break;
        }
    }

    /**
     * Set device clock time and whether you want to show on device screen.
     * @param clock_time : Time you want to write in device.
     * @param enabled : True if you want to show clock in your screen.
     */
    public void writeClockTimeAndShowFlag (DeviceTimeFormat clock_time, boolean enabled) {
        if (kjumpKI8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjumpKI8360.writeClockTimeAndShowFlag(clock_time, enabled);
    }

    /**
     * Set device reminder clock time and enable to alarm.
     * @param reminder_clock_time : Reminder clock time.
     * @param enabled : True if you want to enable reminder clock.
     */
    public void writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (kjumpKI8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjumpKI8360.writeReminderClockTimeAndEnabled(reminder_clock_time, enabled);
    }

    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeTemperatureUnit (TemperatureUnit unit) {
        if (kjumpKI8360 == null)
            return;
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjumpKI8360.writeTemperatureUnit(unit);
    }

    /**
     * Init device
     * It must be done when you notify success so that you can work device normally.
     */
    public void writeSetDevice () {
        if (kjumpKI8360 == null)
            return;
        if (kjumpKI8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjumpKI8360.setDevice();
    }

    public void setDevice (KPSettings deviceSetting) {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.setDevice(deviceSetting);
    }

    public void readKPMemory (int index) {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.readDataAtIndex(index);
    }

    public void readKPNumberOfMemory () {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.readNumberOfData();
    }

    public void kpStartSense () {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.startSense();
    }

    public void kpStopSense () {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.stopSense();
    }

    public void kpClearMemory () {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.clearAllData();
    }

    public void kpChangeMode (SenseMode mode) {
        if (kjumpKP == null)
            return;
        if (kjumpKP.gatt == null) {
            Log.w("testKP", "BluetoothGatt not initialized");
            return;
        }
        kjumpKP.changeMode(mode);
    }

    /**
     * KD-2070
     */
    public void kd2070ReadNumberOfMemory () {
        kjumpKD2070.readNumberOfData();
    }

    public void kd2070ReadDataAtIndex (int index) {
        kjumpKD2070.readDataAtIndex(index);
    }

    public void kd2070WriteHand (LeftRightHand hand) {
        kjumpKD2070.writeHand(hand);
    }

    public void kd2070WriteUnit (TemperatureUnit unit) {
        kjumpKD2070.writeUnit(unit);
    }

    public void kd2070ClearData () {
        kjumpKD2070.clearAllData();
    }

    public void kd2070ReadSettings () {
        kjumpKD2070.readSettings();
    }

    public void kd2070WriteClock (DeviceTimeFormat clock_time) {
        kjumpKD2070.writeClockTime(clock_time);
    }
    /**
     * Callback for ki-8360.
     * Each data will be received here.
     */
    private final KjumpKI8360Callback kjumpKI8360Callback = new KjumpKI8360Callback() {
        // Get number of temperature data.
        @Override
        public void onGetNumberOfData (int number) {
            super.onGetNumberOfData(number);

            Log.d("test8360", "getNumberOfData = " + number);
        }

        // Get user number. But it doesn't matter for ki-8360. So you don't need to use it.
        @Override
        public void onGetUserAndMemory (int startPosition) {
            super.onGetUserAndMemory(startPosition);

            Log.d("test8360", "getMemoryStartPosition = " + startPosition);
        }

        // When you call writeCommand(BLE_CLIENT_CMD.ReadLatestMemoryCmd) you will get it here.
        @Override
        public void onGetLastMemory (KIData data) {
            super.onGetLastMemory(data);

            Log.d("test8360", "GetLastMemory");

            if (data != null) {
                DeviceTimeFormat time = data.getTime();
                int year = time.getYear();
                int month = time.getMonth();
                int day = time.getDay();
                int hour = time.getHour();
                int minute = time.getMinute();
                int sensePosition = data.SensePosition;
                float Temperature = data.Temperature;

                Log.d("test8360", "year = " + year + ", month = " + month + ", day = "
                        + day + ", hour = " + hour + ", minute = " + minute + ", sensePosition = "
                        + sensePosition + ", Temperature = " + Temperature);
            } else {
                Log.d("test8360", "Don't have last memory.");
            }
        }

        // When you call writeCommand(BLE_CLIENT_CMD.ReadAllMemoryCmd) you will get it here.
        @Override
        public void onGetAllMemory (ArrayList<KIData> data) {
            super.onGetAllMemory(data);

            if (data.size() > 0) {
                Log.d("test8360", "read all memory size > 0");
            } else {
                Log.d("test8360", "read all memory size == 0");
            }

            for (int i = 0; i < data.size(); i++) {
                DeviceTimeFormat time = data.get(i).getTime();
                int year = time.getYear();
                int month = time.getMonth();
                int day = time.getDay();
                int hour = time.getHour();
                int minute = time.getMinute();
                int sensePosition = data.get(i).SensePosition;
                float Temperature = data.get(i).Temperature;

                Log.d("test8360", "year = " + year + ", month = " + month + ", day = "
                        + day + ", hour = " + hour + ", minute = " + minute + ", sensePosition = "
                        + sensePosition + ", Temperature = " + Temperature);
            }
        }

        // When you call writeCommand(BLE_CLIENT_CMD.ClearAllDataCmd) you will get response here.
        @Override
        public void onClearAllDataFinished (boolean success) {
            super.onClearAllDataFinished(success);

            Log.d("test8360", "onClearAllDataFinished = " + success);
        }

        // When you call writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time,
        // boolean enabled) and writeClockTimeAndShowFlag (DeviceTimeFormat clock_time,
        // boolean enabled). You will get response here.
        @Override
        public void onWriteClockFinished (boolean success) {
            super.onWriteClockFinished(success);

            Log.d("test8360", "onWriteClockFinished = " + success);
        }

        @Override
        public void onWriteReminderClockFinished (boolean success) {
            super.onWriteReminderClockFinished(success);

            Log.d("test8360", "onWriteReminderClockFinished = " + success);
        }

        //When you call writeTemperatureUnit (TemperatureUnit unit) You will get response here.
        @Override
        public void onWriteUnitFinished (boolean success) {
            super.onWriteUnitFinished(success);

            Log.d("test8360", "onWriteUnitFinished = " + success);
        }

        @Override
        public void onSetDeviceFinished (boolean success) {
            super.onSetDeviceFinished(success);
        }
    };

    private final KjumpKPCallback kjumpKPCallback = new KjumpKPCallback() {
        @Override
        public void onGetDataAtIndex (KPMemory kpMemory) {
            super.onGetDataAtIndex(kpMemory);
        }

        @Override
        public void onGetUser (KPUser kpUser) {
            super.onGetUser(kpUser);
        }

        @Override
        public void onMeasuring (boolean enabled, int systolic) {
            super.onMeasuring(enabled, systolic);
            Log.d("test8360", "onSensing.enabled = " + enabled);
            Log.d("test8360", "onSensing.Systolic = " + systolic);
        }

        @Override
        public void onStartSense () {
            super.onStartSense();

            Log.d("test8360", "onStartSense");
        }

        @Override
        public void onStopSense () {
            super.onStopSense();

            Log.d("test8360", "onStopSense");
        }

        @Override
        public void onMeasurementFinished (KPMemory kpMemory) {
            super.onMeasurementFinished(kpMemory);

            Log.d("test8360", "onFinishedSense = " + kpMemory);
        }
    };

    private final KjumpKD2070Callback kjumpKD2070Callback = new KjumpKD2070Callback() {
        @Override
        public void onGetNumberOfData (int number) {
            super.onGetNumberOfData(number);

            Log.d("testKD2070", "onGetNumberOfData = " + number);
        }

        @Override
        public void onGetDataAtIndex (int index, KDData data) {
            super.onGetDataAtIndex(index, data);

            DeviceTimeFormat time = data.getTime();
            Log.d("testKD2070", "onGetIndexMemory index = " + index);
            Log.d("testKD2070", "onGetIndexMemory Year = " + time.getYear());
            Log.d("testKD2070", "onGetIndexMemory Month = " + time.getMonth());
            Log.d("testKD2070", "onGetIndexMemory Day = " + time.getDay());
            Log.d("testKD2070", "onGetIndexMemory Hour = " + time.getHour());
            Log.d("testKD2070", "onGetIndexMemory Minute = " + time.getMinute());
            Log.d("testKD2070", "onGetIndexMemory Second = " + time.getSecond());
            Log.d("testKD2070", "onGetIndexMemory Temperature = " + data.getTemperature());
        }

        @Override
        public void onClearAllDataFinished (boolean success) {
            super.onClearAllDataFinished(success);

            Log.d("testKD2070", "onClearAllDataFinished = " + success);
        }

        @Override
        public void onWriteClockTimeFinished (boolean success) {
            super.onWriteClockTimeFinished(success);

            Log.d("testKD2070", "onWriteClockFinished = " + success);
        }

        @Override
        public void onWriteUnitFinished (boolean success) {
            super.onWriteUnitFinished(success);

            Log.d("testKD2070", "onWriteUnitFinished = " + success);
        }

        @Override
        public void onWriteHandFinished (boolean success) {
            super.onWriteHandFinished(success);

            Log.d("testKD2070", "onWriteHandFinished = " + success);
        }

        @Override
        public void onGetSettings (KD2070Settings settings) {
            super.onGetSettings(settings);

            DeviceTimeFormat time = settings.getClockTime();
            Log.d("testKD2070", "onReadSettings year = " + time.getYear());
            Log.d("testKD2070", "onReadSettings month = " + time.getMonth());
            Log.d("testKD2070", "onReadSettings day = " + time.getDay());
            Log.d("testKD2070", "onReadSettings hour = " + time.getHour());
            Log.d("testKD2070", "onReadSettings minute = " + time.getMinute());
            Log.d("testKD2070", "onReadSettings second = " + time.getSecond());
            Log.d("testKD2070", "onReadSettings unit = " + settings.getUnit());
            Log.d("testKD2070", "onReadSettings hand = " + settings.getHand());
        }
    };
}
