package com.example.kjumpble.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Helper {
    public static String getHexStr(byte[] data) {
        if (data == null) {
            return "null";
        }
        String s = "";
        for (byte b : data) {
            s += String.format("%02X ", b & 0xff);
        }
        return s;
    }

    public static String getIntArrStr(int[] data) {
        if (data == null) {
            return "null";
        }
        String s = "";
        for (int i : data) {
            s += i + " ";
        }
        return s;
    }

    public static int getAge(String birthday) {
        if (birthday == null || !birthday.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new IllegalArgumentException("Birthday is not ISO Date String \"" + birthday + "\"");
        }

        int birthYear = Integer.parseInt(birthday.split("-")[0], 10);
        int birthMonth = Integer.parseInt(birthday.split("-")[1], 10);
        int birthDay = Integer.parseInt(birthday.split("-")[2], 10);

        Calendar now = new GregorianCalendar();

        int age = now.get(Calendar.YEAR) - birthYear;

        if (((now.get(Calendar.MONTH) + 1) < birthMonth) ||
                ((now.get(Calendar.MONTH) + 1) == birthMonth &&
                        now.get(Calendar.DATE) < birthDay)) {
            age -= 1;
        }
        return age;
    }

    public static String getIntArrayString(int[] int_array) {
        String return_Str = "";
        for (int i = 0; i < int_array.length; i++) {
            return_Str = String.valueOf(int_array[i]) + " ";
        }
        return return_Str;
    }

    public static byte[] mergeBytes(int resPos, byte[] res, byte[] des) {
        for (int i = 0; i < des.length; i++) {
            if (res.length - resPos > i) {
                res[resPos + i] = des[i];
            }
        }
        return res;
    }
}
