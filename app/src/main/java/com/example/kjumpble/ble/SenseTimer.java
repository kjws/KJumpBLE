package com.example.kjumpble.ble;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.kjumpble.ble.callback.kp.KPTimerCallBack;

public class SenseTimer {
    HandlerThread thread;
    Handler handler;
    private static final String TAG = SenseTimer.class.getSimpleName();
    public int SenseCount;
    private boolean isSensing = false;
    private KPTimerCallBack timerCallBack;
    private TimerStatus timerStatus;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run () {
            SenseCount = 0;
            if (isSensing) {
                isSensing = false;
                timerCallBack.onStopSense();
            }

            if (timerStatus == TimerStatus.running)
                handler.postDelayed(this, 300);
        }
    };

    enum TimerStatus {
        running,
        sleeping,
    }

    public SenseTimer (KPTimerCallBack timerCallBack) {
        Log.d(TAG, "SenseTimer");
        this.timerCallBack = timerCallBack;
        startRunnable();
    }

    // Runnable
    private void startRunnable() {
        Log.d(TAG, "startSenseTimer");
        thread = new HandlerThread("MyHandlerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
        timerStatus = TimerStatus.running;
        handler.postDelayed(runnable, 300);
    }

    private void stopRunnable() {
        Log.d(TAG, "stopSenseTimer");
        timerStatus = TimerStatus.sleeping;
        handler.removeCallbacks(runnable);
        thread = null;
    }

    private void renewSchedule() {
        stopRunnable();
        startRunnable();
    }

    public boolean isSensing (byte[] data) {
        int senseByteCount = 0;
        for (byte datum : data)
            if (datum == (byte) 0xFE)
                senseByteCount++;

        // 當有六個FE時視為這筆資料為量測中
        if (senseByteCount >= 6) {
            SenseCount++;
            renewSchedule();
        }

        // 當有五筆量測中的資料則視為目前正在量測
        if (SenseCount >= 5) {
            if (!isSensing) {
                isSensing = true;
                timerCallBack.onStartSense();
            }
        }
        return isSensing;
    }

    public boolean getIsSensing() {
        return isSensing;
    }

}
