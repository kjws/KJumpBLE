package com.example.kjumpble.ble.cmd;

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
        command [4] = (byte) ((beepFlag ? 2 : 0) | (clockFlag ? 1 : 0));
        return command;
    }
    // *************************
}
