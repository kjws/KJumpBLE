package com.example.kjumpble.ble.format.KP;

import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;

import java.util.ArrayList;

public class KPDeviceSetting {
    ArrayList<ReminderFormat> reminders;
    boolean Ambient;
    TemperatureUnitEnum unit;
    HourFormat hourFormat;
    boolean clockShowFlag;

    public KPDeviceSetting(ArrayList<ReminderFormat> reminders, boolean Ambient, TemperatureUnitEnum unit, HourFormat hourFormat, boolean clockShowFlag) {
        this.reminders = reminders;
        this.Ambient = Ambient;
        this.unit = unit;
        this.hourFormat = hourFormat;
        this.clockShowFlag = clockShowFlag;
    }

    public ArrayList<ReminderFormat> getReminders () {
        return reminders;
    }

    public HourFormat getHourFormat () {
        return hourFormat;
    }

    public TemperatureUnitEnum getUnit () {
        return unit;
    }

    public boolean getAmbient () {
        return Ambient;
    }

    public boolean getClockShowFlag () {
        return clockShowFlag;
    }
}
