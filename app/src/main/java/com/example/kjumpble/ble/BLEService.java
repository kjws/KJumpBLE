package com.example.kjumpble.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.kjumpble.Helper;
import com.example.kjumpble.ble.cmd.BLE_CMD;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.timeFormat.TemperatureUnitEnum;

import java.util.ArrayList;

public class BLEService extends Service {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private final LeDeviceListAdapter scannedDeviceListAdapter = new LeDeviceListAdapter();
    private final LeDeviceListAdapter connectingDeviceListAdapter = new LeDeviceListAdapter();
    private BluetoothGatt bluetoothGatt;
    private boolean scanning;
    private Handler handler = new Handler();

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

        registerReceiver(resultReceiver, new IntentFilter(ResultReceiveIntent.SEND_RESULT_INTENT));
        return msgBinder;
    }

    @Override
    public boolean onUnbind (Intent intent) {
        unregisterReceiver(resultReceiver);
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
        }
        else if (scanning) {

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

            }
            else {
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

    private boolean connect (final String address) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }
        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange (BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                // successfully connected to the GATT Server
                Log.d("test8360", gatt.getDevice().getName() + " STATE_CONNECTED");
                onProgressListener.onConnected();
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);

                kjump8360 = new Kjump8360(BLEService.this, gatt);
                Log.d("test8360", "Kjump8360 init");
                // add device in connecting devices
                connectingDeviceListAdapter.addDevice(gatt.getDevice());
//                getSuppor
//                gatt.writeCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_WRITE_FFF3)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.d("test8360", gatt.getDevice().getName() + " STATE_DISCONNECTED");
                onProgressListener.onDisConnected();
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);

                // delete device in connecting devices
                connectingDeviceListAdapter.deleteDevice(gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.w("test8360", "onServicesDiscovered: GATT_SUCCESS = " + service.getUuid().toString());
                    if (service.getUuid().equals(SampleServiceAttributes.OUcare_CHARACTERISTIC_CONFIG_UUID)) {
                        Log.w("test8360", "onServicesDiscovered: GATT_SUCCESS_CORRECT = " + service.getUuid().toString());

                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_READ_UUID);
                        setCharacteristicNotification(characteristic, true);
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else {
                Log.w("test8360", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorRead (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.w("test8360", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.w("test8360", "onDescriptorWrite");
        }

        @Override
        public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.w("test8360", "onCharacteristicRead = " + characteristic.getUuid().toString());
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
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public class MsgBinder extends Binder {
        public BLEService getServiceInstance () {
            return BLEService.this;
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, enabled)) {
            return;
        }

        Log.w("test8360", "Notification success");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    public void writeCommand (BLE_CLIENT_CMD clientCmd) {
        if (bluetoothGatt == null) {
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

    public void writeClockTimeAndShowFlag (ClockTimeFormat clock_time, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeClockTimeAndShowFlag(clock_time, enabled);
    }

    public void writeReminderClockTimeAndEnabled (ReminderTimeFormat reminder_clock_time, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeReminderClockTimeAndEnabled(reminder_clock_time, enabled);
    }

    public void writeTemperatureUnit (TemperatureUnitEnum unit) {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.writeTemperatureUnit(unit);
    }

    public void writeInitDevice () {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        kjump8360.initDevice();
    }

    BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("test8360", "receive action = " + action);
            BLE_CLIENT_CMD bleClientCmd = (BLE_CLIENT_CMD) intent.getExtras().get(BroadcastIntentExtraName.DoingAction);

            switch (bleClientCmd) {
                case ReadNumberOfDataCmd:
                    Log.d("test8360", "get number of data = " + intent.getExtras().getInt(BroadcastIntentExtraName.NumberOfData));
                    break;
                case ReadUserAndMemoryCmd:
                    Log.d("test8360", "get user index = " + intent.getExtras().getInt(BroadcastIntentExtraName.UserIndex));
                    break;
                case ReadLatestMemoryCmd: {
                    Bundle extra = intent.getBundleExtra("extra");
                    ArrayList<DataFor8360> dataFor8360s = (ArrayList<DataFor8360>) extra.getSerializable(BroadcastIntentExtraName.Data);
                    Log.d("test8360", "get latest memory = " + dataFor8360s.get(0).Temperature);
                    break;
                }
                case ReadAllMemoryCmd: {
                    Bundle extra = intent.getBundleExtra("extra");
                    ArrayList<DataFor8360> dataFor8360s = (ArrayList<DataFor8360>) extra.getSerializable(BroadcastIntentExtraName.Data);
                    Log.d("test8360", "get all memory = " + dataFor8360s.get(0).Temperature);
                    break;
                }
                case ClearAllDataCmd:
                    Log.d("test8360", "clear all data.");
                    break;
                case WriteClockCmd:
                    Log.d("test8360", "write successes.");
                    break;
            }
        }
    };
}