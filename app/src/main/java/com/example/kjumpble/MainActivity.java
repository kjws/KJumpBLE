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
import com.example.kjumpble.ble.cmd.BLE_CLIENT_CMD;
import com.example.kjumpble.ble.CheckBLEScan;
import com.example.kjumpble.ble.CheckLocationStatus;
import com.example.kjumpble.ble.callback.OnProgressListener;
import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;
import com.example.kjumpble.util.MyPermissions;
import com.example.kjumpble.permission.PermissionRequestCode;

import java.util.ArrayList;

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

    private Button setDeviceButton;

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
        setDeviceButton = findViewById(R.id.SetDeviceButton);
        setDeviceButton.setOnClickListener(setDeviceButtonOnClickListener);

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

    private static final ClockTimeFormat testClockTime = new ClockTimeFormat(2003, 11, 30, 23, 58, 55);
    private static final ReminderTimeFormat testReminderClockTime = new ReminderTimeFormat(21, 17);
    private static final TemperatureUnitEnum testTemperatureUnit = TemperatureUnitEnum.F;
    private static final boolean testEnable = true;
    // Clock
    private final View.OnClickListener writeClockTimeAndFlagButtonOnClickListener = v -> {
        bleService.writeClockTimeAndShowFlag(testClockTime, testEnable);
    };

    // Reminder
    private final View.OnClickListener writeReminderClockTimeAndFlagButtonOnClickListener = v -> {
        bleService.writeReminderClockTimeAndEnabled(testReminderClockTime, testEnable);
    };

    // Init
    private final View.OnClickListener setDeviceButtonOnClickListener = v -> {
        bleService.writeSetDevice();
    };

    // Unit
    private final View.OnClickListener writeTemperatureUnitOnClickListener = v -> {
        bleService.writeTemperatureUnit(testTemperatureUnit);
    };

    // Write reminder time
    private final ArrayList<ReminderFormat> reminder = new ArrayList<> () {{
            add(new ReminderFormat(false, 7, 3));
            add(new ReminderFormat(false, 11, 4));
            add(new ReminderFormat(false, 15, 7));
            add(new ReminderFormat(false, 19, 9));
        }
    };

    private final View.OnClickListener writeKPReminderOnClickListener = v -> {
        bleService.setKPReminder(reminder);
    };

    // Write time
    private HourFormat hourFormat = HourFormat.is12;
    private boolean ambient = true;
    private boolean clockShowFlag = true;
    private final View.OnClickListener writeKPTimeOnClickListener = v -> {
        bleService.setKPTime(reminder, ambient, testTemperatureUnit, hourFormat, clockShowFlag);
    };

    private final View.OnClickListener readNumberOfMemoryOnClickListener = v -> {
        bleService.readKPNumberOfMemory();
    };

    private int dataIndex = 0;
    private final View.OnClickListener readMemoryOnClickListener = v -> {
        bleService.readKPMemory(dataIndex);
    };

    private final View.OnClickListener kpStartSenseOnClickListener = v -> {
        bleService.kpStartSense();
    };

    private final View.OnClickListener kpStopSenseOnClickListener = v -> {
        bleService.kpStopSense();
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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    bleStatusTextView.setText(R.string.connecting);
                    activity.setContentView(R.layout.kplayout);

                    // kp
                    Button readNumberOfMemoryButton;
                    Button readMemoryButton;
                    Button setKPReminderButton;
                    Button setKPTimeButton;
                    Button kpStartSenseButton;
                    Button kpStopSenseButton;
                    // kp

                    readNumberOfMemoryButton = findViewById(R.id.readNumberOfMemoryButton);
                    readNumberOfMemoryButton.setOnClickListener(readNumberOfMemoryOnClickListener);

                    readMemoryButton = findViewById(R.id.readMemoryButton);
                    readMemoryButton.setOnClickListener(readMemoryOnClickListener);

                    setKPReminderButton = findViewById(R.id.writeKPReminderButton);
                    setKPReminderButton.setOnClickListener(writeKPReminderOnClickListener);

                    setKPTimeButton = findViewById(R.id.writeKPTimeButton);
                    setKPTimeButton.setOnClickListener(writeKPTimeOnClickListener);

                    kpStartSenseButton = findViewById(R.id.startSenseButton);
                    kpStartSenseButton.setOnClickListener(kpStartSenseOnClickListener);

                    kpStopSenseButton = findViewById(R.id.stopSenseButton);
                    kpStopSenseButton.setOnClickListener(kpStopSenseOnClickListener);
                }
            });
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