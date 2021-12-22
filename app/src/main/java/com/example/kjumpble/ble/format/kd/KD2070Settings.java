package com.example.kjumpble.ble.format.kd;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;

public class KD2070Settings {
    ClockTimeFormat clockTime;
    TemperatureUnit unit = TemperatureUnit.F;
    LeftRightHand hand = LeftRightHand.Left;

    public KD2070Settings(byte[] data) {
        setClockTime(data);
        setUnit(data);
        setHand(data);
    }

    // Time
    private void setClockTime(byte[] data) {
        this.clockTime = new ClockTimeFormat(data[0] + 2000, data[1], data[2],
                data[3], data[4], data[5]);
    }
    public void setClockTime (ClockTimeFormat clockTime) {
        this.clockTime = clockTime;
    }
    public ClockTimeFormat getClockTime () {
        return clockTime;
    }

    // Unit
    private void setUnit(byte[] data) {
        this.unit = (data[23] & 0x01) == 0x01 ? TemperatureUnit.F : TemperatureUnit.C;
    }
    public void setUnit(TemperatureUnit unit) {
        this.unit = unit;
    }
    public TemperatureUnit getUnit () {
        return unit;
    }

    // Hand
    private void setHand(byte[] data) {
        this.hand = (data[23] & 0x02) == 0x02 ? LeftRightHand.Left : LeftRightHand.Right;
    }
    public void setHand(LeftRightHand hand) {
        this.hand = hand;
    }
    public LeftRightHand getHand () {
        return hand;
    }
}
