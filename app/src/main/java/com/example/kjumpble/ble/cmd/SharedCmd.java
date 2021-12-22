package com.example.kjumpble.ble.cmd;

import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class SharedCmd {
    // *************************
    // ****** Reminder
    // ********** Read
    public static final byte[] readRemindersCmd = new byte[]{0x02, 0x02, 0x00, 0x5e}; // (enable時)分
    public static byte[] getReadRemindersCommand (int index, ReminderTimeFormat time, boolean enabled) {
        byte[] command = readRemindersCmd;
        command[3] = (byte) ((byte) 0x5e + 2 * index);
        command[4] = (byte) (time.hour + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.minute;
        return command;
    }
    // ********** Write
    public static final byte[] writeRemindersCmd = new byte[]{0x03, 0x02, 0x00, 0x5e,
            0x01, 0x02}; // (enable時)分
    public static byte[] getWriteRemindersCommand (int index, ReminderTimeFormat time, boolean enabled) {
        byte[] command = writeRemindersCmd;
        command[3] = (byte) ((byte) 0x5e + 2 * index);
        command[4] = (byte) (time.hour + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.minute;
        return command;
    }
    // *************************


}
