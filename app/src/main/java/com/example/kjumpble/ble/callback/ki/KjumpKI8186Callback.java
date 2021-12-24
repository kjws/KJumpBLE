package com.example.kjumpble.ble.callback.ki;

import com.example.kjumpble.ble.data.ki.KIData;
import com.example.kjumpble.ble.format.ki.KI8186Settings;

public abstract class KjumpKI8186Callback {
    public void onGetSettings (KI8186Settings settings) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetDataAtIndex (int index, KIData data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteClockFlagFinished (boolean success) {

    }

    public void onWriteReminderFinished (int index, boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteOffsetFinished (boolean success) {

    }

    public void onWriteBeepFinished (boolean success) {

    }
}
