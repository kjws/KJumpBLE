package com.example.kjumpble.ble.cmd;

import com.example.kjumpble.ble.cmd.util.KI8186Util;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class SharedCmd {
    // ****** CLOCK
    // ********** 時間
    public static final byte[] writeClockTimePreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x04, 0x05, 0x06, 0x07}; // 年月日時
    public static final byte[] writeClockTimePostCmd = new byte[]{0x03, 0x02, 0x00, 0x58,
            0x04, 0x05}; // 分秒
    public static final byte[] writeClockShowFlagCmd = new byte[]{0x03, 0x01, 0x00, 0x5a,
            0x04}; // 分秒
    // *************************
    // ****** Clock Time (Pre)
    // ********** 會分為前後是因為一次最多只能修改 4 個記憶體位置
    // *****************************
    // command : 0x03, 0x04, 0x00, 0x54,
    //           0x12, 0x06, 0x13, 0x15; 2012, 6, 19, 21時
    // command[4] : 年 -> year - 2000
    // command[5] : 月 -> month
    // command[6] : 日 -> day
    // command[7] : 時 -> hour
    public static byte[] getWriteClockTimePreCommand (ClockTimeFormat time) {
        byte[] command = writeClockTimePreCmd;
        command[4] = (byte) (time.year - 2000);
        command[5] = (byte) time.month;
        command[6] = (byte) time.day;
        command[7] = (byte) time.hour;
        return command;
    }

    // *************************
    // ****** Clock Time (Post)
    // ********** 會分為前後是因為一次最多只能修改 4 個記憶體位置
    // *****************************
    // command : 0x03, 0x02, 0x00, 0x58,
    //           0x25, 0x31; 37分, 49秒
    // command[4] : 年 -> year - 2000
    // command[5] : 月 -> month
    // command[6] : 日 -> day
    // command[7] : 時 -> hour
    public static byte[] getWriteClockTimePostCommand (ClockTimeFormat time) {
        byte[] command = writeClockTimePostCmd;
        command[4] = (byte) time.minute;
        command[5] = (byte) time.second;
        return command;
    }
}
