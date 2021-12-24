package com.example.kjumpble.ble.callback.kg;

import com.example.kjumpble.ble.data.kg.KGData;
import com.example.kjumpble.ble.format.kg.KG517xSettings;

public abstract class KjumpKG517xCallback {public void onGetNumberOfData (int number) {

    }

    public void onGetDataAtIndex (int index, KGData data) {

    }

    public void onClearAllDataFinished (boolean success) {

    }

    public void onWriteClockTimeFinished (boolean success) {

    }

    public void onWriteClockFlagFinished (boolean success) {

    }

    public void onWriteReminderFinished (int index, boolean success) {

    }

    public void onWriteUnitFinished (boolean success) {

    }

    public void onWriteHandFinished (boolean success) {

    }

    public void onGetSettings (KG517xSettings settings) {

    }
}
