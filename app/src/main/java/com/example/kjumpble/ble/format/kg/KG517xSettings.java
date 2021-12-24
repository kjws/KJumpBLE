package com.example.kjumpble.ble.format.kg;

import com.example.kjumpble.ble.cmd.kg.KG517xCmd;
import com.example.kjumpble.ble.format.DayFormat;
import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KG517xSettings {
    DeviceTimeFormat clockTime;
    boolean clockEnabled;

    ReminderFormat[] reminders = new ReminderFormat[4];

    LeftRightHand hand = LeftRightHand.Left;

    KGGlucoseACPC ACPC = KGGlucoseACPC.AC;

    HourFormat hourFormat = HourFormat.is12;

    DayFormat dayFormat = DayFormat.ddmm;

    public KG517xSettings (byte[] data) {
        setClockTime(data);
        setClockEnabled(data);
        setReminder(data);
        setACPC(data);
        setHourFormat(data);
        setHand(data);
        setDayFormat(data);
    }

    // Time
    private void setClockTime(byte[] data) {
        this.clockTime = new DeviceTimeFormat(data[0] + 2000, data[1], data[2],
                data[3], data[4], data[5]);
    }
    public void setClockTime (DeviceTimeFormat clockTime) {
        this.clockTime = clockTime;
    }
    public DeviceTimeFormat getClockTime () {
        return clockTime;
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

    // Reminder
    private void setReminder(byte[] data) {
        int remindersIndex = 0;
        while (reminders.length < 4) {
            int index = remindersIndex * 2;
            reminders[remindersIndex] = new ReminderFormat(data[8 + index] > 64, new ReminderTimeFormat(data[8 + index] & 0x31, data[9 + index]));
            remindersIndex++;
        }
    }
    public void setReminders(int index, ReminderFormat reminder) {
        reminders[index] = new ReminderFormat(reminder.isEnable(), reminder.getTime());
    }
    public ReminderFormat[] getReminders () {
        return reminders;
    }

    // ACPC
    private void setACPC(byte[] data) {
        byte positionByte = KG517xCmd.ACPCPositionByte;
        this.ACPC = (data[23] & positionByte) == positionByte ? KGGlucoseACPC.PC : KGGlucoseACPC.AC;
    }
    public void setACPC(KGGlucoseACPC ACPC) {
        this.ACPC = ACPC;
    }
    public KGGlucoseACPC getACPC () {
        return ACPC;
    }

    // Hour format
    private void setHourFormat(byte[] data) {
        byte positionByte = KG517xCmd.HourFormatPositionByte;
        this.hourFormat = (data[23] & positionByte) == positionByte ? HourFormat.is24 : HourFormat.is12;
    }
    public void setHourFormat(HourFormat hourFormat) {
        this.hourFormat = hourFormat;
    }
    public HourFormat getHourFormat () {
        return hourFormat;
    }

    // Hand
    private void setHand(byte[] data) {
        byte positionByte = KG517xCmd.HandCmdPositionByte;
        this.hand = (data[23] & positionByte) == positionByte ? LeftRightHand.Left : LeftRightHand.Right;
    }
    public void setHand(LeftRightHand hand) {
        this.hand = hand;
    }
    public LeftRightHand getHand () {
        return hand;
    }

    // Day format
    private void setDayFormat(byte[] data) {
        byte positionByte = KG517xCmd.DayFormatPositionByte;
        this.dayFormat = (data[23] & positionByte) == positionByte ? DayFormat.ddmm : DayFormat.mmdd;
    }
    public void setDayFormat(DayFormat dayFormat) {
        this.dayFormat = dayFormat;
    }
    public DayFormat getDayFormat () {
        return dayFormat;
    }
}
