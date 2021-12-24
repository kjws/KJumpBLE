package com.example.kjumpble.ble.format.kd;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KD2161Settings {
    ClockTimeFormat clockTime;
    boolean clockEnabled;

    ReminderFormat reminder;

    TemperatureUnit unit = TemperatureUnit.F;

    public KD2161Settings(byte[] data) {
        setClockTime(data);
        setClockEnabled(data);
        setUnit(data);
        setReminder(data);
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

    // Reminder
    private void setReminder(byte[] data) {
            reminder = new ReminderFormat(data[10] > 64,
                    new ReminderTimeFormat(data[10] & 0x3f, data[11]));
    }
    public void setReminders(ReminderFormat reminder_clock) {
        reminder = new ReminderFormat(reminder_clock.isEnable(), reminder_clock.getTime());
    }
    public ReminderFormat getReminder () {
        return reminder;
    }

    // Clock enabled
    private void setClockEnabled(byte[] data) {
        this.clockEnabled = data[6] > 64;
    }
    public void setClockEnabled (boolean clockEnabled) {
        this.clockEnabled = clockEnabled;
    }
    public boolean isClockEnabled () {
        return clockEnabled;
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
