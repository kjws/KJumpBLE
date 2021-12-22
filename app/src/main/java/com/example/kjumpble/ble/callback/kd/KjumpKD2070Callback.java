package com.example.kjumpble.ble.callback.kd;

import com.example.kjumpble.ble.cmd.kd.KD2070Cmd;
import com.example.kjumpble.ble.data.kd.DataFormatOfKD;
import com.example.kjumpble.ble.format.kd.KD2070Settings;
import com.example.kjumpble.ble.format.kg.KGSettings;

public abstract class KjumpKD2070Callback {
    public void onSetDeviceFinished (boolean success) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetIndexMemory (int index, DataFormatOfKD data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteHandFinished (boolean success) {

    }

    public void onReadSettings (KD2070Settings settings) {

    }
}
