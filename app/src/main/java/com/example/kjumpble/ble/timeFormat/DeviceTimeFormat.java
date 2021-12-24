package com.example.kjumpble.ble.timeFormat;

public class DeviceTimeFormat {
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
    public DeviceTimeFormat (int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public int getYear () {
        return year;
    }

    public int getMonth () {
        return month;
    }

    public int getDay () {
        return day;
    }

    public int getHour () {
        return hour;
    }

    public int getMinute () {
        return minute;
    }

    public int getSecond () {
        return second;
    }
}
