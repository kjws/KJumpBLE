package com.example.kjumpble.ble.callback.kd;

import com.example.kjumpble.ble.data.kd.KDData;
import com.example.kjumpble.ble.format.kd.KD2070Settings;

public abstract class KjumpKD2070Callback {
    public void onGetSettings (KD2070Settings settings) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetDataAtIndex (int index, KDData data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockTimeFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteHandFinished (boolean success) {

    }
}
