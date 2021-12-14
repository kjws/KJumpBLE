package com.example.kjumpble.ble.callback;

import com.example.kjumpble.ble.data.DataFormatOfKI8360;

import java.util.ArrayList;

public abstract class Kjump8360Callback {
    public void onGetNumberOfData (int number) {

    }

    public void onGetUserAndMemory (int startPosition) {

    }

    public void onGetLastMemory (DataFormatOfKI8360 data) {

    }

    public void onGetAllMemory (ArrayList<DataFormatOfKI8360> data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockFinished (boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onInitDeviceFinished (boolean success) {

    }
}
