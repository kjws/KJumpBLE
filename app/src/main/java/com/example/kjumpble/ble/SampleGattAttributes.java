package com.example.kjumpble.ble;

import java.util.UUID;

public class SampleGattAttributes {
//    public static String OUcare_CHARACTERISTIC_READ = "0000fff1-0000-1000-8000-00805f9b34fb";
//    public static String OUcare_CHARACTERISTIC_WRITE_FFF3 = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID OUcare_CHARACTERISTIC_READ_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static UUID OUcare_CHARACTERISTIC_WRITE_FFF3_UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
}
