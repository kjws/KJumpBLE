package com.example.kjumpble.ble.format.ki;

import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KI8186Settings {
    DeviceTimeFormat clockTime;
    boolean clockEnabled;
    boolean beepEnabled;
    ReminderFormat[] reminders = new ReminderFormat[4];
    TemperatureUnit unit = TemperatureUnit.F;
    int offset = 0;

    public KI8186Settings(byte[] data) {
        setClockTime(data);
        setClockEnabled(data);
        setBeep(data);
        setReminder(data);
        setUnit(data);
        setOffset(data);
    }

    // Time
    private void setClockTime(byte[] data) {
        this.clockTime = new DeviceTimeFormat(data[0] + 2000, data[1], data[2],
                data[3], data[4], 0);
    }
    public void setClockTime (DeviceTimeFormat clockTime) {
        this.clockTime = clockTime;
    }
    public DeviceTimeFormat getClockTime () {
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

    // Beep
    private void setBeep (byte[] data) {
        this.beepEnabled = ((data[6] & 0x02) == 0x02);
    }
    public void setBeepEnabled (boolean beepEnabled) {
        this.beepEnabled = beepEnabled;
    }
    public boolean isBeepEnabled () {
        return beepEnabled;
    }

    // Reminder
    private void setReminder(byte[] data) {
        int remindersIndex = 0;
        while (reminders.length < 4) {
            int index = remindersIndex * 2;
            reminders[remindersIndex] = new ReminderFormat((data[8 + index] & 0x80) == 0x80,
                    new ReminderTimeFormat(data[8 + index] & 0x7f, data[9 + index]));
            remindersIndex++;
        }
    }
    public void setReminders(int index, ReminderFormat reminder) {
        reminders[index] = new ReminderFormat(reminder.isEnable(), reminder.getTime());
    }
    public ReminderFormat[] getReminders () {
        return reminders;
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

    // Offset
    private void setOffset(byte[] data) {
        this.offset = data[17];
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
    public int getOffset () {
        return offset;
    }
}
