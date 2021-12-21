package com.example.kjumpble.ble.callback.kg;

import com.example.kjumpble.ble.data.kg.DataFormatOfKG;
import com.example.kjumpble.ble.data.ki.DataFormatOfKI;
import com.example.kjumpble.ble.format.kg.KGSettings;

public abstract class KjumpKG517xCallback {
    public void onSetDeviceFinished (boolean success) {

    }

    public void onGetNumberOfData (int number) {

    }

    public void onGetIndexMemory (int index, DataFormatOfKG data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteReminderFinished (int index, boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteHandDisplayFinished (boolean success) {

    }

    public void onReadSettings (KGSettings settings) {

    }
}
