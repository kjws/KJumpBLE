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
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.kjumpble.Helper;

import java.util.List;
import java.util.UUID;

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

    @Override
    public void onCreate () {
        super.onCreate();

        Log.d("test8360", "BLEService onCreate");
    }

    @Override
    public IBinder onBind (Intent intent) {
        Log.d("test8360", "BLEService onBind");

        return msgBinder;
    }

    @Override
    public boolean onUnbind (Intent intent) {
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

            Log.w("test8360", "onCharacteristicRead");
            Log.w("test8360", "onCharacteristicRead = " + characteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.w("test8360", "onCharacteristicChanged = " + Helper.getHexStr(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.w("test8360", "onCharacteristicWrite = " + Helper.getHexStr(characteristic.getValue()));
        }
    };

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

    private void broadcastUpdate (final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private OnProgressListener onProgressListener;
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public boolean getScanning() {
        return scanning;
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
        if (bluetoothGatt.setCharacteristicNotification(characteristic, enabled)) {
            Log.w("test8360", "Notification success");
            bluetoothGatt.readCharacteristic(characteristic);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
        else {
            Log.w("test8360", "Notification failed");
        }
    }

    public void writeCharacteristic () {
        if (bluetoothGatt == null) {
            Log.w("test8360", "BluetoothGatt not initialized");
            return;
        }
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(SampleServiceAttributes.OUcare_CHARACTERISTIC_CONFIG_UUID).getCharacteristic(SampleGattAttributes.OUcare_CHARACTERISTIC_WRITE_FFF3_UUID);
        byte[] bytes = new byte[]{0x02, 0x04, 0x00, 0x6c};
        Log.d("test8360", "writeCharacteristic = " + characteristic.getUuid().toString());
        characteristic.setValue(bytes);
        Log.d("test8360", "writeCharacteristic_bytes = " + Helper.getHexStr(characteristic.getValue()));
        bluetoothGatt.writeCharacteristic(characteristic);
    }
}