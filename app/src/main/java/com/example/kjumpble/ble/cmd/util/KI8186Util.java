package com.example.kjumpble.ble.cmd.util;

public class KI8186Util {
    public static final byte[] writeClockShowFlagCmd = new byte[]{0x03, 0x01, 0x00, 0x5a,
            0x04}; // 分秒
    public static final byte[] writeBeepCmd = new byte[] {0x03, 0x01, 0x00, 0x5a
            , 0x04};
    // Clock show flag
    public static byte getClockData (byte data, boolean clockFlag) {
        if ((data & 0x01) == 0x01) {
            data = (byte) (data - 0x01);
        }
        data = (byte) (data | (clockFlag ? 0x01 : 0x00));
        return data;
    }

    // Beep
    public static byte getBeepData(byte data, boolean beepFlag) {
        if ((data & 0x02) == 0x02) {
            data = (byte) (data - 0x02);
        }
        data = (byte) (data | (beepFlag ? 0x02 : 0x00));
        return data;
    }
}
