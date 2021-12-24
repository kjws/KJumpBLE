package com.example.kjumpble.ble.cmd.kd;

import com.example.kjumpble.ble.cmd.util.KD2070Util;
import com.example.kjumpble.ble.cmd.util.KI8186Util;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;

public class KD2070Cmd {
    // *************************
    // ** KD-2070
    // *************************

    // clock
    public static final byte[] writeClockTimePreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x01, 0x02, 0x03, 0x04}; // 年月日時
    public static final byte[] writeClockTimePostCmd = new byte[]{0x03, 0x02, 0x00, 0x58,
            0x05, 0x06}; // 分秒

    public static byte[] getWriteClockTimePreCommand (ClockTimeFormat time) {
        byte[] command = writeClockTimePreCmd;
        command[4] = (byte) (time.getYear() - 2000);
        command[5] = (byte) time.getMonth();
        command[6] = (byte) time.getDay();
        command[7] = (byte) time.getHour();
        return command;
    }

    public static byte[] getWriteClockTimePostCommand (ClockTimeFormat time) {
        byte[] command = writeClockTimePostCmd;
        command[4] = (byte) time.getMinute();
        command[5] = (byte) time.getSecond();
        return command;
    }

    // *************************
    // ****** Unit and Hand
    // ********** 溫度單位以及手部顯示
    // *****************************
    // command : 0x03, 0x02, 0x00, 0x5a,
    //           0x02; clock 逼逼叫 但不顯示
    // command[4] : bits[0] : clock 要不要顯示
    //              bits[1] : beep 要不要開
    public static final byte[] writeUnitCmd = new byte[]{0x03, 0x01, 0x00, 0x6b,
            0x04};
    public static byte[] getWriteUnitCommand(byte data, TemperatureUnit unit) {
        byte[] command = writeUnitCmd;
        command [4] = (byte) KD2070Util.getUnitData(data, unit);
        return command;
    }

    public static final byte[] writeHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x04};
    public static byte[] getWriteHandCommand(byte data, LeftRightHand hand) {
        byte[] command = writeHandCmd;
        command [4] = (byte) KD2070Util.getHandData(data, hand);
        return command;
    }
}
