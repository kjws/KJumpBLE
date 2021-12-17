package com.example.kjumpble.ble.format.KP;

public class BloodPressure {
    int Systolic;
    int Diastolic;
    int Pressure;

    public BloodPressure (int systolic, int diastolic, int pressure) {
        this.Systolic = systolic;
        this.Diastolic = diastolic;
        this.Pressure = pressure;
    }

    public int getSystolic () {
        return Systolic;
    }

    public int getDiastolic () {
        return Diastolic;
    }

    public int getPressure () {
        return Pressure;
    }

}
