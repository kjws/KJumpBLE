package com.example.kjumpble.ble.cmd.kp;

import com.example.kjumpble.ble.format.HourFormat;
import com.example.kjumpble.ble.format.KP.SenseMode;
import com.example.kjumpble.ble.format.ReminderFormat;
import com.example.kjumpble.ble.format.TemperatureUnitEnum;

import java.util.ArrayList;
import java.util.Calendar;

public class KPCmdCalculator {
    private final static byte[] writeReminderTimeCmd = new byte[] {0x2c, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, (byte) 0x99, 0x00};
    private final static byte[] writeTimeCmd = new byte[] {0x2c, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, (byte) 0x88, 0x00};
    private final static byte[] clearMemoryCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xee, 0x00};
    private final static byte[] readMemoryCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xdd, 0x00};
    private final static byte[] readMemorySizeCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xcc, 0x00};
    private final static byte[] deleteMemoryCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x32, 0x00};
    private final static byte[] startCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x32, 0x00};
    private final static byte[] stopCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x31, 0x00};
    private final static byte[] changeModeCmd = new byte[] {0x2c, 0x00, 0x36, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xae, 0x00};
    private final static byte[] readNumberOfUserCmd = new byte[] {0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xca, 0x00};

    public static byte[] getReminderBytes (ArrayList<ReminderFormat> reminders) {
        byte[] bytes = writeReminderTimeCmd;
        bytes[1] = (byte) reminders.get(0).getHour();
        bytes[2] = (byte) reminders.get(0).getMinute();
        bytes[3] = (byte) reminders.get(1).getHour();
        bytes[4] = (byte) reminders.get(1).getMinute();
        bytes[5] = (byte) reminders.get(2).getHour();
        bytes[6] = (byte) reminders.get(2).getMinute();
        bytes[7] = (byte) reminders.get(3).getHour();
        bytes[9] = (byte) reminders.get(3).getMinute();
        return getCommand(bytes);
    }

    public static byte[] getTimeBytes (ArrayList<ReminderFormat> reminders, boolean Ambient, TemperatureUnitEnum unit, HourFormat hourFormat, boolean clockShowFlag) {
        byte[] bytes = writeTimeCmd;
        Calendar calendar = Calendar.getInstance();
        bytes[1] = (byte) (calendar.get(Calendar.YEAR) - 1999);
        bytes[2] = (byte) (calendar.get(Calendar.MONTH));
        bytes[3] = (byte) (calendar.get(Calendar.DAY_OF_MONTH));
        bytes[4] = (byte) (calendar.get(Calendar.HOUR));
        bytes[5] = (byte) (calendar.get(Calendar.MINUTE));
        bytes[6] = (byte) (calendar.get(Calendar.SECOND));
        bytes[7] = getInformationByte(reminders, Ambient, unit, hourFormat, clockShowFlag);

        return getCommand(bytes);
    }

    public static byte[] getReadMemoryBytes (int index) {
        byte[] bytes = readMemoryCmd;
        bytes[1] = (byte) index;
        return getCommand(bytes);
    }

    public static byte[] getReadNumberOfMemoryBytes () {
        return getCommand(readNumberOfUserCmd);
    }

    public static byte[] getStartSenseBytes () {
        return getCommand(startCmd);
    }

    public static byte[] getStopSenseBytes () {
        return getCommand(stopCmd);
    }

    public static byte[] getChangeModeBytes (SenseMode mode) {
        byte[] bytes = changeModeCmd;
        bytes[4] = (byte) mode.ordinal();
        return getCommand(bytes);
    }

    public static byte[] getClearMemoryBytes () {
        return getCommand(clearMemoryCmd);
    }

    private static byte[] getCommand(byte[] bytes) {
        byte[] cmd = new byte[4 + bytes.length + 1 + 2];
        cmd[0] = 0x03;
        cmd[1] = (byte) (bytes.length + 1 + 2);
        cmd[2] = 0;
        cmd[3] = 0;
        System.arraycopy(bytes, 0, cmd, 4, bytes.length);
        cmd[4 + bytes.length] = getCheckSum(bytes);
        cmd[4 + bytes.length + 1] = 0x0d;
        cmd[4 + bytes.length + 2] = 0x0a;
        return cmd;
    }

    private static byte getCheckSum(byte[] res) {
        int sum = 0;
        for (byte timeByte : res)
            sum = sum + ((byte) timeByte & 0xff);

        return (byte) ((256 - (sum & 0xff)) & 0xff);
    }

    private static byte getInformationByte (ArrayList<ReminderFormat> reminders, boolean Ambient, TemperatureUnitEnum unit, HourFormat hourFormat, boolean clockShowFlag) {
        byte InformationByte = getReminderEnabledByte(reminders);
        InformationByte = Ambient ? (byte) (InformationByte | 0x10) : InformationByte;
        InformationByte = unit == TemperatureUnitEnum.F ? (byte) (InformationByte | 0x20) : InformationByte;
        InformationByte = hourFormat == HourFormat.is12 ? (byte) (InformationByte | 0x40) : InformationByte;//device 布林值為12小時制，避免與reminder的24時制混淆，另創專屬device時制的設定
        InformationByte = clockShowFlag ? (byte) (InformationByte | 0x80) : InformationByte;
        return InformationByte;
    }

    private static byte getReminderEnabledByte (ArrayList<ReminderFormat> reminders) {
        byte reminderEnabledBytes = 0x00;
        for (int i = 0; i < reminders.size(); i++)
            reminderEnabledBytes = (byte) (reminderEnabledBytes | (reminders.get(i).isEnable() ? (byte) Math.pow(2, 3 - i) : 0));
        return reminderEnabledBytes;
    }
}
