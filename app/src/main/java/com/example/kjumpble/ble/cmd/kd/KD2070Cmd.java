package com.example.kjumpble.ble.cmd.kd;

import com.example.kjumpble.ble.cmd.ki.Ki8360Cmd;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;

public class KD2070Cmd {
    // *************************
    // ** KD-2070
    // ****** Temperature unit and hand
    // *****************************
    public static final byte[] readTempUnitAndHandCmd = new byte[] {0x02, 0x01, 0x00, 0x6B};
    public static final byte[] writeTempUnitAndHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6B,
            0x00};
    public static byte[] getWriteTempUnitAndHandCmd (TemperatureUnitEnum unit, LeftRightHand hand) {
        byte[] command = writeTempUnitAndHandCmd;
        command[4] = (byte) ((hand == LeftRightHand.Left ? 2 : 0) | (unit == TemperatureUnitEnum.F ? 1 : 0));
        return command;
    }
    // *************************

    // clock
    public static final byte[] writeClockTimePreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x01, 0x02, 0x03, 0x04}; // 年月日時
    public static final byte[] writeClockTimePostCmd = new byte[]{0x03, 0x02, 0x00, 0x58,
            0x05, 0x06}; // 分秒

    public static byte[] getWriteClockTimePreCommand (ClockTimeFormat time) {
        byte[] command = Ki8360Cmd.writeClockTimeAndFlagPreCmd;
        command[4] = (byte) (time.year - 2000);
        command[5] = (byte) time.month;
        command[6] = (byte) time.day;
        command[7] = (byte) time.hour;
        return command;
    }

    public static byte[] getWriteClockTimePostCommand (ClockTimeFormat time) {
        byte[] command = Ki8360Cmd.writeClockTimeAndFlagPostCmd;
        command[4] = (byte) time.minute;
        command[5] = (byte) time.second;
        return command;
    }
}
