package com.example.kjumpble.ble.callback.kd;

import com.example.kjumpble.ble.data.kd.KDData;
import com.example.kjumpble.ble.format.kd.KD2161Settings;

public abstract class KjumpKD2161Callback {
    public void onGetSettings (KD2161Settings settings) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetDataAtIndex (int index, KDData data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockTimeFinished (boolean success) {

    }

    public void onWriteClockFlagFinished (boolean success) {

    }

    public void onWriteReminderFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }
}
