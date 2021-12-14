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

import com.example.kjumpble.util.Helper;
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.callback.Kjump8360Callback;
import com.example.kjumpble.ble.callback.OnProgressListener;
import com.example.kjumpble.ble.data.DataFormatOfKI8360;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.timeFormat.TemperatureUnitEnum;
import com.example.kjumpble.ble.uuid.KI8360UUIDList;

import java.util.ArrayList;

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

    Kjump8360 kjump8360;


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
            if (device.getName().equals("KI-8360")) {
                connect(device.getAddress());
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
                onProgressListener.onConnected();
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);

                // init kjump8360
                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                kjump8360 = new Kjump8360(gatt, kjump8360Callback, bluetoothManager);
                // add device in connecting devices
                connectingDeviceListAdapter.addDevice(gatt.getDevice());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
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
                BluetoothGattService service = gatt.getService(KI8360UUIDList.KJUMP_CHARACTERISTIC_CONFIG_UUID);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(KI8360UUIDList.KJUMP_CHARACTERISTIC_READ_UUID);
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
            kjump8360.onCharacteristicChanged(characteristic);
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
        if (kjump8360 == null)
            return;
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.gatt.readCharacteristic(characteristic);
    }

    /**
     * Notify indicate characteristic to make LE service get onCharacteristicChanged.
     * @param characteristic : BluetoothGattCharacteristic which you want to notify.
     * @param enabled : Enable or disable notification.
     */
    private void setCharacteristicNotification (BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (kjump8360 == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        if (kjump8360.gatt == null) {
            return;
        }
        if (kjump8360.gatt.setCharacteristicNotification(characteristic, enabled)) {
            Log.w("test8360", "Notification success");
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KI8360UUIDList.KJUMP_CHARACTERISTIC_DESCRIBE_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            kjump8360.gatt.writeDescriptor(descriptor);
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
        if (kjump8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        switch (clientCmd) {
            case ReadUserAndMemoryCmd:
                kjump8360.readUserIndex();
                break;
            case ReadNumberOfDataCmd:
                kjump8360.readNumberOfData();
                break;
            case ReadLatestMemoryCmd:
                kjump8360.readLatestMemory();
                break;
            case ReadAllMemoryCmd:
                kjump8360.readAllMemory();
                break;
            case ClearAllDataCmd:
                kjump8360.clearAllData();
                break;
        }
    }

    /**
     * Set device clock time and whether you want to show on device screen.
     * @param clock_time : Time you want to write in device.
     * @param enabled : True if you want to show clock in your screen.
     */
    public void writeClockTimeAndShowFlag (ClockTimeFormat clock_time, boolean enabled) {
        if (kjump8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeClockTimeAndShowFlag(clock_time, enabled);
    }

    /**
     * Set device reminder clock time and enable to alarm.
     * @param reminder_clock_time : Reminder clock time.
     * @param enabled : True if you want to enable reminder clock.
     */
    public void writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (kjump8360 == null) {
            Log.w("test8360", "kjump8360 not initialized");
            return;
        }
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeReminderClockTimeAndEnabled(reminder_clock_time, enabled);
    }

    /**
     * Write temperature unit like C or F to device.
     * @param unit : C for Celsius, F for Fahrenheit.
     */
    public void writeTemperatureUnit (TemperatureUnitEnum unit) {
        if (kjump8360 == null)
            return;
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeTemperatureUnit(unit);
    }

    /**
     * Init device
     * It must be done when you notify success so that you can work device normally.
     */
    public void writeInitDevice () {
        if (kjump8360 == null)
            return;
        if (kjump8360.gatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.initDevice();
    }

    /**
     * Callback for ki-8360.
     * Each data will be received here.
     */
    private final Kjump8360Callback kjump8360Callback = new Kjump8360Callback() {
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
        public void onGetLastMemory (DataFormatOfKI8360 data) {
            super.onGetLastMemory(data);

            Log.d("test8360", "GetLastMemory");

            if (data != null) {
                int year = data.Year;
                int month = data.Month;
                int day = data.Day;
                int hour = data.Hour;
                int minute = data.Minute;
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
        public void onGetAllMemory (ArrayList<DataFormatOfKI8360> data) {
            super.onGetAllMemory(data);

            if (data.size() > 0) {
                Log.d("test8360", "read all memory size > 0");
            } else {
                Log.d("test8360", "read all memory size == 0");
            }

            for (int i = 0; i < data.size(); i++) {
                int year = data.get(i).Year;
                int month = data.get(i).Month;
                int day = data.get(i).Day;
                int hour = data.get(i).Hour;
                int minute = data.get(i).Minute;
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
        // boolean enabled) and writeClockTimeAndShowFlag (ClockTimeFormat clock_time,
        // boolean enabled). You will get response here.
        @Override
        public void onWriteClockFinished (boolean success) {
            super.onWriteClockFinished(success);

            Log.d("test8360", "onWriteClockFinished = " + success);
        }

        //When you call writeTemperatureUnit (TemperatureUnitEnum unit) You will get response here.
        @Override
        public void onWriteUnitFinished (boolean success) {
            super.onWriteUnitFinished(success);

            Log.d("test8360", "onWriteUnitFinished = " + success);
        }

        @Override
        public void onInitDeviceFinished (boolean success) {
            super.onInitDeviceFinished(success);
        }
    };
}
