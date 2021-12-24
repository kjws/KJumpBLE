package com.example.kjumpble.ble.format.kg;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.timeFormat.DeviceTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KG517xSettings {
    DeviceTimeFormat clockTime;
    boolean clockEnabled;

    ReminderFormat[] reminders = new ReminderFormat[4];

    KGGlucoseUnit unit = KGGlucoseUnit.MgDl;

    LeftRightHand hand = LeftRightHand.Left;

    public KG517xSettings (byte[] data) {
        setClockTime(data);
        setClockEnabled(data);
        setReminder(data);
        setUnit(data);
        setHand(data);
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

    private void setUnit(byte[] data) {
        this.unit = (data[15] & 0x01) == 0x01 ? KGGlucoseUnit.MmolL : KGGlucoseUnit.MgDl;
    }

    public void setUnit(KGGlucoseUnit unit) {
        this.unit = unit;
    }

    public KGGlucoseUnit getUnit () {
        return unit;
    }

    private void setHand(byte[] data) {
        this.hand = (data[15] & 0x04) == 0x04 ? LeftRightHand.Right : LeftRightHand.Left;
    }

    public void setHand(LeftRightHand hand) {
        this.hand = hand;
    }

    public LeftRightHand getHand () {
        return hand;
    }
}
