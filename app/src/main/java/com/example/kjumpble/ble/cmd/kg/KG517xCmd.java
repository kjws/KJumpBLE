package com.example.kjumpble.ble.cmd.kg;

import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.kg.KGGlucoseUnit;

public class KG517xCmd {
    public static final byte[] writeHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};
    public static final byte[] writeUnitCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};

    public static byte[] getWriteHandCommand(byte data, LeftRightHand hand) {
        byte[] command = writeHandCmd;
        if ((data & 0x04) == 0x04) {
            data = (byte) (data - 0x04);
        }
        data = (byte) (data | (hand == LeftRightHand.Left ? 0x04 : 0x00));
        command [4] = (byte) data;
        return command;
    }

    public static byte[] getWriteUnitCommand(byte data, KGGlucoseUnit unit) {
        byte[] command = writeUnitCmd;
        if ((data & 0x01) == 0x01) {
            data = (byte) (data - 0x01);
        }
        data = (byte) (data | (unit == KGGlucoseUnit.MmolL ? 0x01 : 0x00));
        command [4] = (byte) data;
        return command;
    }
}
