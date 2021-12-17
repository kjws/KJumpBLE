package com.example.kjumpble.ble.cmd;

public class KI8180Cmd {
    // *************************
    // ** KI-8180
    // ****** Ambient show flag
    // *****************************
    public static final byte[] readAmbientFlagCmd = new byte[] {0x02, 0x01, 0x00, 0x5e};
    public static final byte[] writeAmbientFlagCmd = new byte[] {0x03, 0x01, 0x00, 0x5e,
            0x00};
    public static byte[] getWriteAmbientFlagCmd (boolean enable) {
        byte[] command = writeAmbientFlagCmd;
        command[4] = (byte) (enable ? 128 : 0);
        return command;
    }
    // *************************
}
