package com.example.kjumpble.ble.cmd.ki;

import com.example.kjumpble.ble.format.TemperatureUnit;
import com.example.kjumpble.ble.timeFormat.ClockTimeFormat;
import com.example.kjumpble.ble.timeFormat.ReminderTimeFormat;

public class KI8360Cmd {
    public static final byte[] readSettingsCmd = new byte[]{0x02, 0x01, 0x00, (byte) 0x54};
    public static final byte[] confirmUserAndMemoryCmd = new byte[]{0x02, 0x0a, 0x00, (byte) 0x80};
    public static final byte[] readNumberOfDataCmd = new byte[]{0x02, 0x01, 0x00, 0x6c};
    public static final byte[] readDataCmd = new byte[]{0x02, 0x09, 0x00, (byte) 0xa8}; // 02, 09, 00, 0xa8(起始位置)
    public static final byte[] clearDataCmd = new byte[]{0x03, 0x01, 0x00, (byte) 0x6c, 0x00};

    // clock
    public static final byte[] writeClockTimeAndFlagPreCmd = new byte[]{0x03, 0x04, 0x00, 0x54,
            0x01, 0x02, 0x03, 0x04}; // 年月日時
    public static final byte[] writeClockTimeAndFlagPostCmd = new byte[]{0x03, 0x03, 0x00, 0x58,
            0x05, 0x06, 0x01}; // 分秒enable

    // reminder
    public static final byte[] writeReminderClockTimeAndFlagCmd = new byte[]{0x03, 0x02, 0x00, 0x5e,
            0x01, 0x02}; // (enable時)分
    public static final byte[] setDeviceCmd = new byte[]{0x03, 0x01, 0x00, 0x64,
            (byte) 0x99};
    public static final byte[] writeReturnCmd = new byte[]{0x03, 0x55, (byte) 0xaa};
    public static final byte[] writeTemperatureUnitCmd = new byte[]{0x03, 0x01, 0x00, 0x6b,
            0x01};

    public static byte[] getReadSettingsStep1Cmd() {
        byte[] commands = readSettingsCmd;
        commands[1] = 0x12;
        commands[3] = 0x54;
        return commands;
    }

    public static byte[] getReadSettingsStep2Cmd() {
        byte[] commands = readSettingsCmd;
        commands[1] = 0x06;
        commands[3] = 0x66;
        return commands;
    }

    public static byte[] getConfirmUserAndMemoryCmd() {
        return KI8360Cmd.readNumberOfDataCmd;
    }

    public static byte[] getConfirmNumberOfDataCmd(int user) {
        return commandForConfirmNumberOfData(user);
    }

    public static byte[] getReadDataCmd(int dataIndex) {
        return commandForReadData(dataIndex);
    }

    public static byte[] getClearDataCmd() {
        return KI8360Cmd.clearDataCmd;
    }

    public static byte[] getWriteClockTimeAndEnabledPreCommand (ClockTimeFormat time) {
        byte[] command = KI8360Cmd.writeClockTimeAndFlagPreCmd;
        command[4] = (byte) (time.getYear() - 2000);
        command[5] = (byte) time.getMonth();
        command[6] = (byte) time.getDay();
        command[7] = (byte) time.getHour();
        return command;
    }

    public static byte[] getWriteClockTimeAndEnabledPostCommand (ClockTimeFormat time, boolean enabled) {
        byte[] command = KI8360Cmd.writeClockTimeAndFlagPostCmd;
        command[4] = (byte) time.getMinute();
        command[5] = (byte) time.getSecond();
        command[6] = (byte) (enabled ? 0x01 : 0x00);
        return command;
    }

    public static byte[] getWriteReminderClockTimeAndEnabledCommand (ReminderTimeFormat time, boolean enabled) {
        byte[] command = KI8360Cmd.writeReminderClockTimeAndFlagCmd;
        command[4] = (byte) (time.getHour() + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.getMinute();
        return command;
    }

    // 四個Reminder的位置
    public static byte[] getWriteRemindersCommand (int index, ReminderTimeFormat time, boolean enabled) {
        byte[] command = KI8360Cmd.writeReminderClockTimeAndFlagCmd;
        command[3] = (byte) ((byte) 0x5e + 2 * index);
        command[4] = (byte) (time.getHour() + (enabled ? 0x80 : 0x00));
        command[5] = (byte) time.getMinute();
        return command;
    }

    public static byte[] getWriteTemperatureUnitCommand (TemperatureUnit unit) {
        byte[] command = KI8360Cmd.writeTemperatureUnitCmd;
        byte unitByte = 0x00;
        switch (unit) {
            case C:
                unitByte = 0x00;
                break;
            case F:
                unitByte = 0x01;
                break;
        }
        command[4] = unitByte;
        return command;
    }

    private static byte[] commandForConfirmNumberOfData (int user) {
        byte[] command = KI8360Cmd.readNumberOfDataCmd;
        command[3] = (byte) (0x6c + (user - 1) * 2);
        return command;
    }

    private static byte[] commandForReadData (int dataIndex) {
        byte[] command = KI8360Cmd.readDataCmd;
        command[3] = (byte) (0xa8 + dataIndex * 0x08);
        return command;
    }
}
