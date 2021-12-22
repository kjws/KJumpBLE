package com.example.kjumpble.ble.callback.ki;

import com.example.kjumpble.ble.data.kd.DataFormatOfKD;
import com.example.kjumpble.ble.data.ki.DataFormatOfKI;
import com.example.kjumpble.ble.format.kg.KGSettings;
import com.example.kjumpble.ble.format.ki.KI8180Settings;

public abstract class KjumpKI8180Callback {
    public void onSetDeviceFinished (boolean success) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetIndexMemory (int index, DataFormatOfKI data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteAmbientFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteClockFlagFinished (boolean success) {

    }

    public void onWriteReminderFinished (int index, boolean success) {

    }

    public void onReadSettings (KI8180Settings settings) {

    }
}
