package com.example.kjumpble.ble.format.ki;

import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;

public class KI8180Settings {
    ClockTimeFormat clockTime;
    boolean clockEnabled;
    boolean ambient;
    TemperatureUnit unit = TemperatureUnit.F;

    public KI8180Settings(byte[] data) {
        setClockTime(data);
        setClockEnabled(data);
        setAmbient(data);
        setUnit(data);
    }

    // Time
    private void setClockTime(byte[] data) {
        this.clockTime = new ClockTimeFormat(data[0] + 2000, data[1], data[2],
                data[3], data[4], 0);
    }
    public void setClockTime (ClockTimeFormat clockTime) {
        this.clockTime = clockTime;
    }
    public ClockTimeFormat getClockTime () {
        return clockTime;
    }

    // Clock enabled
    private void setClockEnabled(byte[] data) {
        this.clockEnabled = (data[6] & 0x01) == 0x01;
    }
    public void setClockEnabled (boolean clockEnabled) {
        this.clockEnabled = clockEnabled;
    }
    public boolean isClockEnabled () {
        return clockEnabled;
    }

    // Ambient
    private void setAmbient (byte[] data) {
        this.ambient = data[10] == (byte) 0x80;
    }
    public void setAmbient (boolean ambient) {
        this.ambient = ambient;
    }
    public boolean isAmbient () {
        return ambient;
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
}
