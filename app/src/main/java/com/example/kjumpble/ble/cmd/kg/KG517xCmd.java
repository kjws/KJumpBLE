package com.example.kjumpble.ble.cmd.kg;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.kg.KGGlucoseUnit;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KG517xCmd {
    public static final byte[] writeHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};
    public static final byte[] writeUnitCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};

    public static byte[] getWriteHandCommand(byte data, LeftRightHand hand) {
        byte[] command = writeHandCmd;
        if ((data & 0x04) == 0x04) {
            data = (byte) (data - 0x04);
        }
        data = (byte) (data | (hand == LeftRightHand.Left ? 0x04 : 0x00));
        command [4] = (byte) data;
        return command;
    }

    public static byte[] getWriteUnitCommand(byte data, KGGlucoseUnit unit) {
        byte[] command = writeUnitCmd;
        if ((data & 0x01) == 0x01) {
            data = (byte) (data - 0x01);
        }
        data = (byte) (data | (unit == KGGlucoseUnit.MmolL ? 0x01 : 0x00));
        command [4] = (byte) data;
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
        command[4] = (byte) (time.getHour() + (enabled ? 0x40 : 0x00));
        command[5] = (byte) time.getMinute();
        return command;
    }
}
