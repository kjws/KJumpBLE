package com.example.kjumpble.ble.format.KP;

import android.util.Log;

import java.util.Calendar;

public class KPUser {
    int NumberOfMemory;
    int MaxNumberOfMemory;
    int MaxNumberOfUser;
    public KPUser (int maxNumberOfUser, int maxNumberOfMemory, int numberOfMemory) {
        this.NumberOfMemory = numberOfMemory;
        this.MaxNumberOfMemory = maxNumberOfMemory;
        this.MaxNumberOfUser = maxNumberOfUser;

        logAll();
    }

    public int getMaxNumberOfMemory () {
        return MaxNumberOfMemory;
    }

    public int getMaxNumberOfUser () {
        return MaxNumberOfUser;
    }

    public int getNumberOfMemory () {
        return NumberOfMemory;
    }

    private void logAll() {
        Log.d("KP", "Max number of user = " + MaxNumberOfUser);
        Log.d("KP", "Max number of memory = " + MaxNumberOfMemory);
        Log.d("KP", "Number Of memory = " + NumberOfMemory);
    }
}
