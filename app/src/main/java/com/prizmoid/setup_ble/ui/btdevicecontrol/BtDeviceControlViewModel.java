package com.prizmoid.setup_ble.ui.btdevicecontrol;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import com.prizmoid.setup_ble.Utils;
import com.prizmoid.setup_ble.model.Attr;
import com.prizmoid.setup_ble.model.BtDevice;
import com.prizmoid.setup_ble.model.Cmd;
import com.prizmoid.setup_ble.model.Ltrack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.UUID;

public class BtDeviceControlViewModel extends ViewModel {
    private final static String TAG = "BtViewModel";

    public final MutableLiveData<BtDevice> mDeviceLiveData = new MutableLiveData<BtDevice>();
    private Object mLock = new Object();
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;

    public MutableLiveData<BtDevice> getCurrentDevice() {
        return mDeviceLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mHandlerThread.quit();
    }

    public BtDeviceControlViewModel() {
        // trigger user load.
        mHandlerThread = new HandlerThread("HandlerDemo");
        mHandlerThread.start();
        mHandler =  new Handler(mHandlerThread.getLooper()) {//Looper.getMainLooper()
                @Override
                public void handleMessage(Message msg) {

                    if (msg.what == Utils.MessageConstants.MESSAGE_READ) {
                        // Send the file using bluetooth
                        String str = new String( (byte[]) msg.obj,0, msg.arg1);
                        Log.d(TAG, "Just read: " + str);
                        parseDeviceResponse(str);
                    } else if (msg.what == Utils.MessageConstants.MESSAGE_WRITE) {
                        String str = new String( (byte[]) msg.obj,0, msg.arg1);
                        // Send the file using bluetooth
                        Log.d(TAG, "Written byte msg: " + str);
                    } else if (msg.what == Utils.MessageConstants.MESSAGE_TOAST) {
                        String str = new String( (byte[]) msg.obj,0, msg.arg1);
                        // Send the file using bluetooth
                        Log.d(TAG, "Toast: " + str);
                    }
                }
            };
    }

    private void parseDeviceResponse(final String strResponse) {
        //(new Thread(new  Runnable() {@Override public void run () {
               synchronized (mLock) {
                    String[] tokenz = strResponse.split(";");
                    if ((tokenz == null) || (tokenz.length < 2)) {
                        Log.w(TAG, "Not valid response:" + strResponse);
                        //TODO: handle incorrect cmd
                        return;
                    }
                    if (!mDeviceLiveData.getValue().validCmd(tokenz[0].substring(1))) {
                        Log.w(TAG, "Not valid command:" + strResponse);
                        //TODO: handle incorrect cmd
                        return;
                    }
                    if (tokenz[1].toUpperCase().startsWith("FE")) {
                        Log.w(TAG, "Frame error:" + strResponse);
                        //TODO: handle incorrect cmd
                        return;
                    }
                    Log.d(TAG, "Received data tokens: " + tokenz.length);
                    if (tokenz[0].toUpperCase().startsWith("$VER")) {
                        Cmd c = mDeviceLiveData.getValue().getCommand("VER");
                        if (c != null) {
                            if (c.getAttributes().size() == tokenz.length - 1) {
                                Attr a = c.getAttr("HW_TYPE");
                                int hw_config_type = Integer.valueOf(tokenz[1]);
                                if (a != null) {
                                    Utils.HW_TYPE_T t = Utils.HW_TYPE_T.getTypeByInt(hw_config_type);
                                    if (t != null) {
                                        a.a_value = t.toString();
                                    } else {
                                        a.a_value = "UNKNOWN_TYPE";
                                    }
                                }
                                a = c.getAttr("HW_VERSION");
                                if (a != null) {
                                    a.a_value = tokenz[2];
                                }
                                a = c.getAttr("FW_VERSION");
                                if (a != null) {
                                    a.a_value = tokenz[3];
                                }
                                a = c.getAttr("FLEX_VERSION");
                                if (a != null) {
                                    a.a_value = tokenz[4];
                                }
                                a = c.getAttr("RELEASE_DATE");
                                if (a != null) {
                                    a.a_value = tokenz[5];
                                }
                                mDeviceLiveData.postValue(mDeviceLiveData.getValue());
                                // mDeviceLiveData.notifyAll();
                            } else {
                                Log.e(TAG, c.cmdName + " data parsing error. Wrong count of attributes:" + (tokenz.length - 1));
                            }
                        }
                    } else if (tokenz[0].toUpperCase().startsWith("$DEV")) {
                        Cmd c = mDeviceLiveData.getValue().getCommand("DEV");
                        if (c != null) {
                            if (c.getAttributes().size() == tokenz.length - 1) {
                                Attr a = c.getAttr("DEV_NAME");
                                if (a != null) {
                                    a.a_value = tokenz[1];
                                }
                                a = c.getAttr("DEV_ID");
                                if (a != null) {
                                    a.a_value = tokenz[2];
                                }
                                a = c.getAttr("DEV_SERIAL");
                                if (a != null) {
                                    a.a_value = tokenz[3];
                                }
                                mDeviceLiveData.postValue(mDeviceLiveData.getValue());
                                // mDeviceLiveData.notifyAll();
                            } else {
                                Log.e(TAG, c.cmdName + " data parsing error. Wrong count of attributes:" + (tokenz.length - 1));
                            }
                        }
                    } else if (tokenz[0].toUpperCase().startsWith("$BOOTVER")) {
                        Cmd c = mDeviceLiveData.getValue().getCommand("BOOTVER");
                        if (c != null) {
                            if (c.getAttributes().size() == tokenz.length - 1) {
                                Attr a = c.getAttr("BOOT_VERSION");
                                if (a != null) {
                                    a.a_value = tokenz[1];
                                }
                                a = c.getAttr("MODIFICATION_DATE");
                                if (a != null) {
                                    a.a_value = tokenz[2];
                                }
                                mDeviceLiveData.postValue(mDeviceLiveData.getValue());
                                // mDeviceLiveData.notifyAll();
                            } else {
                                Log.e(TAG, c.cmdName + " data parsing error. Wrong count of attributes:" + (tokenz.length - 1));
                            }
                        }
                    } else if (tokenz[0].toUpperCase().startsWith("$HWCFG")) {
                        Cmd c = mDeviceLiveData.getValue().getCommand("HWCFG");
                        if (c != null) {
                            if (c.getAttributes().size() == tokenz.length - 1) {
                                Attr a = c.getAttr("SERIAL");
                                if (a != null) {
                                    a.a_value = tokenz[2];
                                }
                                a = c.getAttr("HW_TYPE");
                                Log.v(TAG, "token HW_TYPE : " + tokenz[1]);
                                int hw_config_type = Integer.valueOf(tokenz[1]);
                                if (a != null) {
                                    Utils.HW_TYPE_T t = Utils.HW_TYPE_T.getTypeByInt(hw_config_type);
                                    if (t != null) {
                                        a.a_value = t.toString();
                                    } else {
                                        a.a_value = "UNKNOWN_TYPE";
                                    }
                                }
                                mDeviceLiveData.postValue(mDeviceLiveData.getValue());
                                // mDeviceLiveData.notifyAll();
                            } else {
                                Log.e(TAG, c.cmdName + " data parsing error. Wrong count of attributes:" + (tokenz.length - 1));
                            }
                        }
                    } else {
                        Log.w(TAG, "Unsupported command " + tokenz[0]);
                    }
               }
        //      } })).start();
        //


    }

    public boolean connect(BtDevice btDevice, BtDevice.OnBtDeviceConnected callback) {
        boolean res = false;
        synchronized (mLock){
            if (btDevice != null && (btDevice.device != null)) {
                res =  btDevice.connect(mHandler, callback);
                if  (res) {
                    mDeviceLiveData.setValue(btDevice);
                } else {
                    Log.e(TAG, "Could not connect to " + btDevice.name);
                }
            }
        }
        return res;
    }


    void doAction() {
        // depending on the action, do necessary business logic calls and update the
        // userLiveData.
        String strCmd = "DATE";
        mDeviceLiveData.getValue().writeToDevice( strCmd);//"DATE;050319;113000;+0;1;0");
    }



    public void readDeviceData() {
        if (mDeviceLiveData.getValue() != null) {
            if (mDeviceLiveData.getValue().name.toLowerCase().startsWith(Utils.DEV_NAME_LTRACK)) {
                ((Ltrack)mDeviceLiveData.getValue()).requestDeviceInfo();
            }
        }
    }




}
