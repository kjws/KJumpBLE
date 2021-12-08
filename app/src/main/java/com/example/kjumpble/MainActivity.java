package com.example.kjumpble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kjumpble.ble.BLEService;
import com.example.kjumpble.ble.CheckBLEScan;
import com.example.kjumpble.ble.CheckLocationStatus;
import com.example.kjumpble.ble.OnProgressListener;
import com.example.kjumpble.permission.MyPermissions;
import com.example.kjumpble.permission.PermissionRequestCode;

public class MainActivity extends AppCompatActivity {
    private BLEService bleService;

    private Button scanDeviceButton;
    private Button getNumberOfDataButton;
    private TextView bleStatusTextView;

    private Activity activity;

    private Intent bleServiceIntent;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        scanDeviceButton = findViewById(R.id.ScanDeviceButton);
        scanDeviceButton.setOnClickListener(scanDeviceButtonOnClickListener);

        getNumberOfDataButton = findViewById(R.id.GetNumberOfDataButton);
        getNumberOfDataButton.setOnClickListener(getNumberOfDataButtonOnClickListener);

        bleStatusTextView = findViewById(R.id.bleStatusTextView);

        MyPermissions myPermissions = new MyPermissions();
        String[] permissions = myPermissions.getPermissions();

        if (permissions.length != 0)
            ActivityCompat.requestPermissions(this, permissions, PermissionRequestCode.ON_CREATE_REQUEST_PERMISSIONS);
    }

    @Override
    protected void onResume () {
        super.onResume();

        Log.d("test8360", "onResume");

        bleServiceIntent = new Intent(this, BLEService.class);
        bindService(bleServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE );
    }

    @Override
    protected void onPause () {
        super.onPause();

        Log.d("test8360", "onPause");
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        Log.d("test8360", "onDestroy");
    }

    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            Log.d("test8360", "onServiceDisconnected");
            mServiceConnection = null;
        }

        @Override
        public void onServiceConnected (ComponentName name, IBinder service) {
            Log.d("test8360", "onServiceConnected");
            bleService = ((BLEService.MsgBinder)service).getServiceInstance();
            bleService.setOnProgressListener(onProgressListener);
        }
    };

    private final View.OnClickListener scanDeviceButtonOnClickListener = v -> {
        if (CheckBLEScan.ability(this.getApplicationContext()) && CheckLocationStatus.checkLocationForBLE(this.getApplicationContext())) {
            bleService.scanLeDevice();
        }
        else {
            CheckBLEScan.checkBLEToast(this.getApplicationContext());
        }
    };

    private final View.OnClickListener getNumberOfDataButtonOnClickListener = v -> {
        bleService.writeCharacteristic();
    };

    private final OnProgressListener onProgressListener = new OnProgressListener() {
        @Override
        public void onScanMotionFailed () {
            CheckBLEScan.checkBLEEnabledToast(activity);
        }

        @Override
        public void onStartScan () {
            scanDeviceButton.setText(R.string.stop_scan);
        }

        @Override
        public void onStopScan () {
            scanDeviceButton.setText(R.string.start_scan);
        }

        @Override
        public void onConnected () {
            bleStatusTextView.setText(R.string.connecting);
        }

        @Override
        public void onDisConnected () {
            bleStatusTextView.setText(R.string.do_nothing);
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
            }
            else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
            }
            else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            }
        }
    };
}