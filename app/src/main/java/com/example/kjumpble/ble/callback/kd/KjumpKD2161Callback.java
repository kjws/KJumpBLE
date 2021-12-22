package com.example.kjumpble.ble.callback.kd;

import com.example.kjumpble.ble.data.kd.DataFormatOfKD;

public abstract class KjumpKD2161Callback {
    public void onSetDeviceFinished (boolean success) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetIndexMemory (int index, DataFormatOfKD data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteReminderClockFinished (boolean success) {

    }

    public void onWriteClockFlagFinished (boolean success) {

    }
}
