package com.example.kjumpble.ble.timeFormat;

public class ReminderTimeFormat {
    int hour;
    int minute;
    public ReminderTimeFormat (int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour () {
        return hour;
    }

    public int getMinute () {
        return minute;
    }
}
