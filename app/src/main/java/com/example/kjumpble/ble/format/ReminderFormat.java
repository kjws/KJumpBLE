package com.example.kjumpble.ble.format;

public class ReminderFormat {
    int hour;
    int minute;
    boolean enable;

    ReminderFormat (boolean enable, int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.enable = enable;
    }
}
