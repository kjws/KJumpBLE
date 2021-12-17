package com.example.kjumpble.ble.callback;

import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPUser;

public abstract class KjumpKPCallback {
    public void onWriteTimeFinished (boolean success) {

    }

    public void onWriteReminderFinished (boolean success) {

    }

    public void onGetMemory(KPMemory kpMemory) {

    }

    public void onGetUser(KPUser kpUser) {

    }

    public void onSensing(boolean enabled, int systolic) {

    }

    public void onStartSense() {

    }

    public void onStopSense() {

    }

    public void onFinishedSense(KPMemory kpMemory) {

    }
}
