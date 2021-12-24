package com.example.kjumpble.ble.callback;

import com.example.kjumpble.ble.format.KP.KPMemory;
import com.example.kjumpble.ble.format.KP.KPUser;

public abstract class KjumpKPCallback {
    public void onSetDeviceFinished (boolean success) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onChangeModeFinished (boolean success) {

    }

    public void onGetDataAtIndex (KPMemory kpMemory) {

    }

    public void onGetUser(KPUser kpUser) {

    }

    public void onMeasuring (boolean enabled, int systolic) {

    }

    public void onStartSense() {

    }

    public void onStopSense() {

    }

    public void onMeasurementFinished (KPMemory kpMemory) {

    }
}
