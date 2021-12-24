package com.example.kjumpble.ble.callback.ki;

import com.example.kjumpble.ble.data.ki.KIData;
import com.example.kjumpble.ble.format.ki.KI8180Settings;

public abstract class KjumpKI8180Callback {
    public void onGetSettings (KI8180Settings settings) {

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

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteAmbientFinished (boolean success) {

    }
}
