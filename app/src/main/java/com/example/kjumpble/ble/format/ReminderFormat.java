package com.example.kjumpble.ble.format;

public class ReminderFormat {
    int hour;
    int minute;
    boolean enable;

    public ReminderFormat (boolean enable, int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.enable = enable;
    }

    public int getHour () {
        return hour;
    }

    public int getMinute () {
        return minute;
    }

    public boolean isEnable () {
        return enable;
    }
}
