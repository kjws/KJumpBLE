package com.example.kjumpble.ble.cmd.kg;

import com.example.kjumpble.ble.format.DayFormat;
import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.LeftRightHand;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.kg.KGGlucoseACPC;

public class KG517xCmd {
    /**
     * User:
     * Bit0:(飯前飯後設定)
     * "1" PC ,"0" AC
     * Bit1:(時間小時制設定)
     * "1" 24-hour clock ,"0" 12-hour clock
     * Bit2: (LCD左/右手顯示設定)
     * "1" left hand display, "0"  right hand
     * Bit3:(月/日顯示方式設定)
     * "1" dd mm,"0" mm dd
     */
    private static final byte[] writeACPCCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};
    private static final byte[] writeHourFormatCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};
    private static final byte[] writeHandCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};
    private static final byte[] writeDayFormatCmd = new byte[] {0x03, 0x01, 0x00, 0x6b
            , 0x00};

    private static final int ACPCCmdPosition = 0;
    private static final int HourFormatCmdPosition = 1;
    private static final int HandCmdPosition = 2;
    private static final int DayFormatCmdPosition = 3;

    public static final byte ACPCPositionByte = (byte) Math.pow(2, ACPCCmdPosition);
    public static final byte HourFormatPositionByte = (byte) Math.pow(2, HourFormatCmdPosition);
    public static final byte HandCmdPositionByte = (byte) Math.pow(2, HandCmdPosition);
    public static final byte DayFormatPositionByte = (byte) Math.pow(2, DayFormatCmdPosition);

    public static byte[] getWriteACPCCommand(byte data, KGGlucoseACPC meat) {
        return getFormatBytes(writeACPCCmd, ACPCPositionByte, meat);
    }

    public static byte[] getWriteHourFormatCommand(byte data, HourFormat hourFormat) {
        return getFormatBytes(writeHourFormatCmd, HourFormatPositionByte, hourFormat);
    }

    public static byte[] getWriteHandCommand(byte data, LeftRightHand hand) {
        return getFormatBytes(writeHandCmd, HandCmdPositionByte, hand);
    }

    public static byte[] getWriteDayFormatCommand(byte data, DayFormat dayFormat) {
        return getFormatBytes(writeDayFormatCmd, DayFormatPositionByte, dayFormat);
    }

    private static byte[] getFormatBytes (byte[] command, int locationBit, Object type) {
        byte data = command[4];
        byte positionByte = (byte) Math.pow(2, locationBit);
        data = deleteExistBit(data, positionByte);
        if (type instanceof KGGlucoseACPC)
            data = (byte) (data | (type == KGGlucoseACPC.PC ? positionByte : 0x00));
        else if (type instanceof HourFormat)
            data = (byte) (data | (type == HourFormat.is24 ? positionByte : 0x00));
        else if (type instanceof LeftRightHand)
            data = (byte) (data | (type == LeftRightHand.Left ? positionByte : 0x00));
        else if (type instanceof DayFormat)
            data = (byte) (data | (type == DayFormat.ddmm ? positionByte : 0x00));
        command[4] = data;
        return command;
    }

    private static byte deleteExistBit (byte data, byte positionByte) {
        if ((data & positionByte) == positionByte) {
            data = (byte) (data - positionByte);
        }
        return data;
    }

    // *************************
    // ****** Reminder 1~4
    // *****************************
    // reminder
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x02, 0x00, 0x5c,
            0x01, 0x02}; // (enable時)分
    public static byte[] getWriteReminderAndFlagCommand(int index, ReminderFormat reminder) {
        byte[] command = writeReminderClockTimeAndFlagCmd;
        command[3] = (byte) (command[3] + index * 2);
        command[4] = (byte) (reminder.getTime().getHour() + (reminder.isEnable() ? 0x40 : 0x00));
        command[5] = (byte) reminder.getTime().getMinute();
        return command;
    }
}
