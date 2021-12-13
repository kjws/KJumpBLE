package com.example.kjumpble.ble.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.kjumpble.ble.SensePositionEnum;

import java.io.Serializable;

public class DataFor8360 implements Serializable {
    public int Year;
    public int Month;
    public int Day;
    public int Hour;
    public int Minute;
    public int SensePosition;
    public float Temperature;

    public DataFor8360 (byte[] data) {
        Year = 2000 + data[1];
        Month = data[2];
        Day = data[3];
        Hour = data[4];
        Minute = data[5];
        SensePosition = calSensePosition(data[6]);
        Temperature = (float) (((data[7] & 0xff) * 256 + (data[8] & 0xff)) / 100.0);

        logAll();
    }

    private int calSensePosition (byte data) {
        data = (byte) (data & 0x0f);
        switch (data) {
            case 0:
                return SensePositionEnum.House.ordinal();
            case 1:
                return SensePositionEnum.Ear.ordinal();
            default:
                return SensePositionEnum.Head.ordinal();
        }
    }

    private void logAll() {
        Log.d("test8360", "year = " + Year);
        Log.d("test8360", "Month = " + Month);
        Log.d("test8360", "Day = " + Day);
        Log.d("test8360", "Hour = " + Hour);
        Log.d("test8360", "Minute = " + Minute);
        Log.d("test8360", "SensePosition = " + SensePosition);
        Log.d("test8360", "Temperature = " + Temperature);
    }

    private int mData;

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<DataFor8360> CREATOR = new Parcelable.Creator<DataFor8360>() {
        public DataFor8360 createFromParcel(Parcel in) {
            return new DataFor8360(in);
        }

        public DataFor8360[] newArray(int size) {
            return new DataFor8360[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private DataFor8360(Parcel in) {
        mData = in.readInt();
    }
}
