package com.example.kjumpble.ble.format;

import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class ReminderFormat {
    ReminderTimeFormat time;
    boolean enable;

    public ReminderFormat (boolean enable, ReminderTimeFormat time) {
        this.time = time;
        this.enable = enable;
    }

    public ReminderTimeFormat getTime () {
        return time;
    }

    public boolean isEnable () {
        return enable;
    }
}
