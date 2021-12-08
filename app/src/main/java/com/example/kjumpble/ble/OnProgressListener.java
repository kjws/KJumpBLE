package com.example.kjumpble.ble;

public interface OnProgressListener {
    void onScanMotionFailed();
    void onStartScan();
    void onStopScan();
    void onConnected();
    void onDisConnected();
}