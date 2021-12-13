package com.example.kjumpble.ble.callback;

public interface OnProgressListener {
    void onScanMotionFailed();
    void onStartScan();
    void onStopScan();
    void onConnected();
    void onDisConnected();
}