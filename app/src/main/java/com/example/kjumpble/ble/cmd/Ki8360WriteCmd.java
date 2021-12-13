package com.example.kjumpble.ble.cmd;

public class Ki8360WriteCmd {
    public static final byte[] confirmUserAndMemoryCmd = new byte[]{0x02, 0x0a, 0x00, (byte) 0x80};
    public static final byte[] readNumberOfDataCmd = new byte[]{0x02, 0x01, 0x00, 0x6c};
    public static final byte[] readDataCmd = new byte[]{0x02, 0x09, 0x00, (byte) 0xa8}; // 02, 09, 00, 0xa8(起始位置)
    public static final byte[] clearDataCmd = new byte[]{0x03, 0x01, 0x00, (byte) 0x6c, 0x00};

    // clock
    public static final byte[] writeClockTimeAndFlagPreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x01, 0x02, 0x03, 0x04}; // 年月日時
    public static final byte[] writeClockTimeAndFlagPostCmd = new byte[]{0x03, 0x03, 0x00, 0x58,
            0x05, 0x06, 0x01}; // 年月日時分秒enable

    // reminder
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x03, 0x00, 0x5e,
            0x01, 0x02}; // enable時分


    public static final byte[] refreshDeviceCmd = new byte[]{0x03, 0x01, 0x00, 0x64,
            (byte) 0x99};

    public static final byte[] writeReturnCmd = new byte[]{0x03, 0x55, (byte) 0xaa};

    public static final byte[] writeTemperatureUnitCmd = new byte[]{0x03, 0x01, 0x00, 0x6b,
            0x01};
}
