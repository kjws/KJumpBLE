package com.example.kjumpble.ble.format.KP;

public class KPTemperature {
    boolean HaveTemperature;
    int Temperature;

    public KPTemperature (boolean haveTemperature, int Temperature) {
        this.HaveTemperature = haveTemperature;
        this.Temperature = Temperature;
    }

    public int getTemperature () {
        return Temperature;
    }

    public boolean isHaveTemperature () {
        return HaveTemperature;
    }
}
