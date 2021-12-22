package com.example.kjumpble.ble.cmd.kd;

import com.example.kjumpble.ble.cmd.util.KI8186Util;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KD2161Cmd {
    // ****** Clock
    // ********** 時鐘要不要顯示
    // *****************************
    // command : 0x03, 0x02, 0x00, 0x5a,
    //           0x01; clock 顯示
    // command[4] : ( command[4] > 64) 為 On
    public static final byte[] writeClockShowFlagCmd = new byte[]{0x03, 0x01, 0x00, 0x5a,
            0x04}; // 分秒
    public static byte[] getWriteClockShowFlagCommand(boolean clockFlag) {
        byte[] command = writeClockShowFlagCmd;
        command[4] = (byte) (clockFlag ? 0x40 : 0x00);
        return command;
    }

    // ****** Reminder
    // *****************************
    // command : {0x03, 0x02, 0x00, 0x5e, 0x8a, 0x12};
    // command[4] : 鬧鐘及小時 Bits[6] : Reminder on/off
    //            : Bits[0 ~ 5] : 小時
    //            : 0100 0000 -> Reminder on
    //            : 0100 0000 -> Reminder off
    //            : 0100 0101 -> Reminder on & hour = 3
    // command[5] : 分鐘
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x02, 0x00, 0x5e,
            0x04, 0x05}; // (enable時)分
    public static byte[] getWriteReminderAndFlagCommand(ReminderFormat reminder) {
        byte[] command = writeReminderClockTimeAndFlagCmd;
        command[4] = (byte) (reminder.getTime().getHour() + (reminder.isEnable() ? 0x40 : 0x00));
        command[5] = (byte) reminder.getTime().getMinute();
        return command;
    }
}
