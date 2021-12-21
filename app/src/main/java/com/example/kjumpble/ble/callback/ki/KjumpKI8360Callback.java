package com.example.kjumpble.ble.callback.ki;

import com.example.kjumpble.ble.data.ki.DataFormatOfKI;

import java.util.ArrayList;

public abstract class KjumpKI8360Callback {
    public void onGetNumberOfData (int number) {

    }

    public void onGetUserAndMemory (int startPosition) {

    }

    public void onGetLastMemory (DataFormatOfKI data) {

    }

    public void onGetAllMemory (ArrayList<DataFormatOfKI> data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteReminderClockFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onSetDeviceFinished (boolean success) {

    }
}
