package com.example.kjumpble.ble.format.KP;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SenseTimerStructure {
    Timer Timer;
    public int SenseCount;
    TimerStatus Timer_Status;

    enum TimerStatus {
        running,
        sleeping,
    }

    public SenseTimerStructure() {
        Log.d("testSenseTimer", "SenseTimerStructure");
        Timer = new Timer();
        startSenseTimer();
    }

    private void startSenseTimer() {
        Log.d("testSenseTimer", "startSenseTimer");
        timerSchedule();
        SenseCount = 0;
        Timer_Status = TimerStatus.running;
    }

    private void timerSchedule() {
        Timer.schedule(new TimerTask() {
            @Override
            public void run () {
                Log.d("testSenseTimer", "TimerOutOfTime");
                SenseCount = 0;
            }
        }, (long) 500);
    }

    private void renewSchedule() {
        Timer.cancel();
        timerSchedule();
    }

    private void stopTimer() {
        Timer.cancel();
        Timer_Status = TimerStatus.sleeping;
    }

    public boolean isSensing (byte[] data) {
        int senseByteCount = 0;
        for (byte datum : data)
            if (datum == (byte) 0xFE)
                senseByteCount++;

        if (senseByteCount >= 6) {
            SenseCount++;
            renewSchedule();
        }

        if (SenseCount >= 5) {
            return true;
        }
        return false;
    }
}
