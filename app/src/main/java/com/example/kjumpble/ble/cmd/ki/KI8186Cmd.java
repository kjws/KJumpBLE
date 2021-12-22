package com.example.kjumpble.ble.cmd.ki;

import com.example.kjumpble.ble.cmd.util.KI8186Util;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KI8186Cmd {
    // *************************
    // ** KI-8186

    // *************************
    // ****** Clock show flag
    // ********** 時鐘要不要顯示
    // *****************************
    // command : 0x03, 0x02, 0x00, 0x5a,
    //           0x01; clock 顯示 但不逼逼叫
    // command[4] : bits[0] : clock 要不要顯示
    //              bits[1] : beep 要不要開
    public static final byte[] writeClockShowFlagCmd = new byte[]{0x03, 0x01, 0x00, 0x5a,
            0x04}; // 分秒
    public static byte[] getWriteClockShowFlagCommand(byte data, boolean clockFlag) {
        byte[] command = writeClockShowFlagCmd;
        command[4] = (byte) KI8186Util.getClockData(data, clockFlag);
        return command;
    }

    // ****** BEEP
    // *****************************
    public static final byte[] writeBeepCmd = new byte[] {0x03, 0x01, 0x00, 0x5a
            , 0x04};

    // *************************
    // ****** Beep
    // ********** 要不要逼逼叫
    // *****************************
    // command : 0x03, 0x02, 0x00, 0x5a,
    //           0x02; clock 逼逼叫 但不顯示
    // command[4] : bits[0] : clock 要不要顯示
    //              bits[1] : beep 要不要開
    public static byte[] getWriteBeepCommand(byte data, boolean beepFlag) {
        byte[] command = writeBeepCmd;
        command [4] = (byte) KI8186Util.getBeepData(data, beepFlag);
        return command;
    }
    // *************************
    // ****** Reminders 1 ~ 4
    // *****************************
    // command : {0x03, 0x02, 0x00, 0x5c, 0x8a, 0x12};
    // command[3] : 0x5c 第一個 Reminder
    //            : 0x5e 第二個 Reminder
    //            : 0x60 第三個 Reminder
    //            : 0x62 第四個 Reminder
    // command[4] : 鬧鐘及小時 Bits[7] : Reminder on/off
    //            : Bits[0 ~ 6] : 小時
    //            : 1000 0000 -> Reminder on
    //            : 0000 0000 -> Reminder off
    //            : 1000 0101 -> Reminder on & hour = 3
    // command[5] : 分鐘
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x02, 0x00, 0x5c,
            0x01, 0x02}; // (enable時)分
    public static byte[] getWriteReminderAndFlagCommand(int index, ReminderTimeFormat time, boolean enabled) {
        byte[] command = writeReminderClockTimeAndFlagCmd;
        command[3] = (byte) (command[3] + index * 2);
        command[4] = (byte) (time.getHour() + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.getMinute();
        return command;
    }

    // ****** OFFSET
    // ********** 韌體顯示的值會根據 offset
    // ********** when offset 為 (-10 ~ 10)
    // ********** 調整 C (-1 ~ 1), F (-2 ~ 2)
    // *****************************
    // command : 0x03, 0x01, 0x00, 0x65,
    //           0x08; offset = 8
    // command[4] : offset
    public static final byte[] writeOffsetCmd = new byte[] {0x03, 0x01, 0x00, 0x65
            , 0x04};
    public static byte[] getWriteOffsetCommand(int offset) {
        byte[] command = writeOffsetCmd;
        command [4] = (byte) offset;
        return command;
    }
}
