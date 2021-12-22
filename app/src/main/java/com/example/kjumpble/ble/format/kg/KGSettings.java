package com.example.kjumpble.ble.format.kg;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

import java.util.Calendar;

public class KGSettings {
    ClockTimeFormat clockTime;

    boolean clockEnabled;

    ReminderFormat[] reminders = new ReminderFormat[4];
    KGGlucoseUnit unit = KGGlucoseUnit.MgDl;
    LeftRightHand hand = LeftRightHand.Left;

    public KGSettings(byte[] data) {
        setTimeAndShowFlag(data);
        setReminder(data);
        setUnit(data);
        setHand(data);
    }

    // Time
    private void setTimeAndShowFlag(byte[] data) {
        this.clockTime = new ClockTimeFormat(data[0] + 2000, data[1], data[2],
                data[3], data[4], 0);
        this.clockEnabled = data[6] > 64;
    }

    public void setTimeAndShowFlag(ClockTimeFormat clock_time, boolean enabled) {
        this.clockTime = clock_time;

        this.clockEnabled = enabled;
    }

    public ClockTimeFormat getClockTime () {
        return clockTime;
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

    public void setReminders(int index, ReminderTimeFormat reminder_clock_time, boolean enabled) {
        reminders[index] = new ReminderFormat(enabled, reminder_clock_time);
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
