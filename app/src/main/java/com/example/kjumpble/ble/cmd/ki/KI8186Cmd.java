package com.example.kjumpble.ble.cmd.ki;

import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KI8186Cmd {
    // *************************
    // ** KI-8186
    // ****** OFFSET
    // *****************************
    public static final byte[] readOffsetCmd = new byte[] {0x02, 0x01, 0x00, 0x65};
    public static final byte[] writeOffsetCmd = new byte[] {0x03, 0x01, 0x00, 0x65
            , 0x00};
    public static byte[] getWriteOffsetCommand(int offset) {
        byte[] command = writeOffsetCmd;
        command [4] = (byte) offset;
        return command;
    }
    // ****** BEEP
    // *****************************
    public static final byte[] readClockFlagAndBeepCmd = new byte[] {0x02, 0x01, 0x00, 0x5a};
    public static final byte[] writeClockAndBeepCmd = new byte[] {0x03, 0x01, 0x00, 0x5a
            , 0x00};
    public static byte[] getWriteClockFlagAndBeepCommand(boolean clockFlag, boolean beepFlag) {
        byte[] command = writeClockAndBeepCmd;
        command[4] = (byte) ((beepFlag ? 2 : 0) | (clockFlag ? 1 : 0));
        return command;
    }
    // *************************
    // ****** Reminder 1~4
    // *****************************
    // reminder
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x02, 0x00, 0x5c,
            0x01, 0x02}; // (enable時)分
    public static byte[] getWriteReminderAndFlagCommand(int index, ReminderTimeFormat time, boolean enabled) {
        byte[] command = writeReminderClockTimeAndFlagCmd;
        command[3] = (byte) (command[3] + index * 2);
        command[4] = (byte) (time.hour + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.minute;
        return command;
    }
}
