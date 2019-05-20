package com.prizmoid.setup_ble.ui.btdevicecontrol;

import android.arch.lifecycle.LiveData;

import com.prizmoid.setup_ble.model.BtDevice;

public class BtDeviceLiveData<BtDevice> extends LiveData {
    private BtDevice mDevice;

    BtDeviceLiveData () {
        mDevice = null;
    }

    BtDeviceLiveData (BtDevice device) {
        mDevice = device;
    }

    public void setDevice(BtDevice device) {
        mDevice = device;
    }
}
