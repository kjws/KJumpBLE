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
import com.example.kjumpble.ble.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.CheckBLEScan;
import com.example.kjumpble.ble.CheckLocationStatus;
import com.example.kjumpble.ble.OnProgressListener;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.timeFormat.TemperatureUnitEnum;
import com.example.kjumpble.util.MyPermissions;
import com.example.kjumpble.permission.PermissionRequestCode;

public class MainActivity extends AppCompatActivity {
    private BLEService bleService;

    private Button scanDeviceButton;
    private Button getUserIndexButton;
    private Button getNumberOfDataButton;
    private Button readLatestMemoryButton;
    private Button readAllMemoryButton;
    private Button clearAllDataButton;

    private Button writeClockTimeAndFlagButton;

    private Button writeReminderClockTimeAndFlagButton;

    private Button initDeviceButton;

    private Button writeTemperatureUnitButton;

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

        getUserIndexButton = findViewById(R.id.GetUserIndexButton);
        getUserIndexButton.setOnClickListener(getUserIndexOnClickListener);

        getNumberOfDataButton = findViewById(R.id.GetNumberOfDataButton);
        getNumberOfDataButton.setOnClickListener(getNumberOfDataButtonOnClickListener);

        readLatestMemoryButton = findViewById(R.id.ReadLatestMemoryButton);
        readLatestMemoryButton.setOnClickListener(readLatestMemoryButtonOnClickListener);

        readAllMemoryButton = findViewById(R.id.ReadAllMemoryButton);
        readAllMemoryButton.setOnClickListener(readAllMemoryButtonOnClickListener);

        clearAllDataButton = findViewById(R.id.ClearAllDataButton);
        clearAllDataButton.setOnClickListener(clearAllDataButtonOnClickListener);

        // Clock
        writeClockTimeAndFlagButton = findViewById(R.id.WriteClockTimeAndFlagButton);
        writeClockTimeAndFlagButton.setOnClickListener(writeClockTimeAndFlagButtonOnClickListener);

        // Reminder clock
        writeReminderClockTimeAndFlagButton = findViewById(R.id.WriteReminderClickTimeAndFlagButton);
        writeReminderClockTimeAndFlagButton.setOnClickListener(writeReminderClockTimeAndFlagButtonOnClickListener);

        // init device
        initDeviceButton = findViewById(R.id.InitDeviceButton);
        initDeviceButton.setOnClickListener(initDeviceButtonOnClickListener);

        // Temperature Unit
        writeTemperatureUnitButton = findViewById(R.id.writeTemperatureUnit);
        writeTemperatureUnitButton.setOnClickListener(writeTemperatureUnitOnClickListener);

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


    private final View.OnClickListener getUserIndexOnClickListener = v -> {
        bleService.writeCommand(BLE_CLIENT_CMD.ReadUserAndMemoryCmd);
    };

    private final View.OnClickListener getNumberOfDataButtonOnClickListener = v -> {
        bleService.writeCommand(BLE_CLIENT_CMD.ReadNumberOfDataCmd);
    };

    private final View.OnClickListener readLatestMemoryButtonOnClickListener = v -> {
        bleService.writeCommand(BLE_CLIENT_CMD.ReadLatestMemoryCmd);
    };

    private final View.OnClickListener readAllMemoryButtonOnClickListener = v -> {
        bleService.writeCommand(BLE_CLIENT_CMD.ReadAllMemoryCmd);
    };

    private final View.OnClickListener clearAllDataButtonOnClickListener = v -> {
        bleService.writeCommand(BLE_CLIENT_CMD.ClearAllDataCmd);
    };

    private static final ClockTimeFormat testClockTime = new ClockTimeFormat(2021, 12, 9, 10, 5, 40);
    private static final ReminderTimeFormat testReminderClockTime = new ReminderTimeFormat(10, 6);
    private static final TemperatureUnitEnum testTemperatureUnit = TemperatureUnitEnum.F;
    private static final boolean testEnable = false;
    // Clock
    private final View.OnClickListener writeClockTimeAndFlagButtonOnClickListener = v -> {
        bleService.writeClockTimeAndShowFlag(testClockTime, testEnable);
    };

    // Reminder
    private final View.OnClickListener writeReminderClockTimeAndFlagButtonOnClickListener = v -> {
        bleService.writeReminderClockTimeAndEnabled(testReminderClockTime, testEnable);
    };

    // Init
    private final View.OnClickListener initDeviceButtonOnClickListener = v -> {
        bleService.writeInitDevice();
    };

    // Unit
    private final View.OnClickListener writeTemperatureUnitOnClickListener = v -> {
        bleService.writeTemperatureUnit(testTemperatureUnit);
    };

    private final OnProgressListener onProgressListener = new OnProgressListener() {
        @Override
        public void onScanMotionFailed () {
            CheckBLEScan.checkBLEEnabledToast(activity);
        }

        @Override
        public void onStartScan () {
            activity.runOnUiThread(() -> scanDeviceButton.setText(R.string.stop_scan));
        }

        @Override
        public void onStopScan () {
            activity.runOnUiThread(() -> scanDeviceButton.setText(R.string.start_scan));
        }

        @Override
        public void onConnected () {
            activity.runOnUiThread(() -> bleStatusTextView.setText(R.string.connecting));
        }

        @Override
        public void onDisConnected () {
            activity.runOnUiThread(() -> bleStatusTextView.setText(R.string.do_nothing));
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