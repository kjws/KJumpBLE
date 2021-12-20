package com.example.kjumpble.ble.cmd.kd;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;

public class KD2070Cmd {
    // *************************
    // ** KD-2070
    // ****** Temperature unit and hand
    // *****************************
    public static final byte[] ReadTempUnitAndHandCmd = new byte[] {0x02, 0x01, 0x00, 0x6B};
    public static final byte[] writeTempUnitAndHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6B,
            0x00};
    public static byte[] getWriteTempUnitAndHandCmd (TemperatureUnitEnum unit, LeftRightHand hand) {
        byte[] command = writeTempUnitAndHandCmd;
        command[4] = (byte) ((hand == LeftRightHand.Left ? 2 : 0) | (unit == TemperatureUnitEnum.F ? 1 : 0));
        return command;
    }
    // *************************
}
