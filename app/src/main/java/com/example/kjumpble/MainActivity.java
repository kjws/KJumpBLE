package com.example.kjumpble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.kjumpble.ble.BLEService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume () {
        super.onResume();

        Log.d("test8360", "onResume");

        Intent bleServiceIntent = new Intent(this, BLEService.class);
        bindService(bleServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause () {
        super.onPause();

        Log.d("test8360", "onPause");
        unbindService(mServiceConnection);
    }

    public ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("test8360", "onServiceDisconnected");
            mServiceConnection = null;
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            Log.d("test8360", "onServiceConnected");
        }
    };
}