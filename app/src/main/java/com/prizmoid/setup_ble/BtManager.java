package com.prizmoid.setup_ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.util.Log;

import com.prizmoid.setup_ble.model.BtDevice;
import com.prizmoid.setup_ble.model.Ltrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

public class BtManager {
    private static final String TAG = "BtManager";
    public static final int SCAN_PERIOD = 12000;// Stops scanning after 12 seconds.

    private static final String EXTRA_DATA = "BLE_CHARACTERISTIC";
    private boolean mScanning;
    private BluetoothAdapter bluetoothAdapter;
    private Context mContext;
    OnBtScanListener mCallback;
    private HandlerThread mHandlerThread = new HandlerThread("BtScanThread");
    private Handler mHandler;
    private volatile List<BtDevice> mBtDevices = new ArrayList<>();
    private volatile List<BtDevice> mBtDevicesTemp = new ArrayList<>();
    private Object mLock = new Object();




    public void setOnBtScanListener(OnBtScanListener activity) {
        mCallback = activity;
    }

    public void release() {
        mBtDevices.clear();
        mBtDevicesTemp.clear();
        mHandlerThread.quitSafely();
    }

    public interface OnBtScanListener {
        public void onScanUpdate(BtDevice btDevice);
        public void onScanComplete(List<BtDevice> list);
    }

    public BtManager(Context context) {
        mContext = context;
        mHandlerThread.start();

    }

    public BtManager(Context context, OnBtScanListener callback) {
        mContext = context;
        mCallback = callback;
        mHandlerThread.start();
    }


    public BtDevice getDevice(int index) {
        synchronized (mLock) {
            if (mBtDevices != null) {
                if (index < mBtDevices.size()) {
                    return mBtDevices.get(index);
                }
            }
        }
        return null;
    }

    public boolean checkBtAvailable() {
        if (mHandler == null) {
            mHandler =  new Handler();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        } else {
            return true;
        }
    }


    private boolean nameInList(String name, List<BtDevice> mBtDevicesTemp) {
        if (name == null) return false;
        if (name.isEmpty()) return false;
        //TODO: check MAC
        for(BtDevice bt: mBtDevicesTemp) {
            String str = bt.name == null ? bt.address : bt.name;
            if (name.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFilter(String name) {
        if (name == null ) return false;
        if (name.isEmpty()) return false;
        for(String s: Utils.BT_NAME_FILTER) {
            if (name.toLowerCase().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.

                BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                String le = "   ";
                if(BluetoothDevice.DEVICE_TYPE_LE == device.getType()){
                    le =    "LE ";
                    return;
                }
                Log.v(TAG, "BT BR  FOUND " + le + device.getName() + ": " + device);
                String name = device.getName();
                if (checkFilter(name)) {
                    synchronized (mLock) {
                        name = (le + name).trim();
                        if (!nameInList(name, mBtDevicesTemp)) {
                            BtDevice btDevice = null;
                            if (name.toLowerCase().startsWith(Utils.DEV_NAME_LTRACK)) {
                                btDevice = new Ltrack();
                            }
                            btDevice.name = name;
                            btDevice.address = device.getAddress(); // MAC address
                            btDevice.rssi = 0;
                            btDevice.device = device;
                            mBtDevicesTemp.add(btDevice);
                            mCallback.onScanUpdate(btDevice);
                        }
                    }
                }

            }else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                Log.d(TAG, "Pairing request");
                BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                device.setPin("1234".getBytes());
            } else {
                if (BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if (uuidExtra != null) {
                        for (int i = 0; i < uuidExtra.length; i++) {
                            Log.d(TAG, " Device: " + device.getName() + ", " + device + ", Service: " + uuidExtra[i].toString());
                        }
                    } else {
                        Log.w(TAG, "UUID action:" + intent.getExtras().toString());
                    }
                } else {
                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        Log.d(TAG, "Discovery Started...");
                    } else {
                        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                            Log.d(TAG, "Discovery Finished");
                            //initiateSdp();
                        }
                    }
                }
            }
        }
    };

    private void initiateSdp() {

        synchronized (mLock) {
            Log.d(TAG, "SDP list size: " + mBtDevices.size());
            Iterator<BtDevice> itr = mBtDevices.iterator();
            while (itr.hasNext()) {
                // Get Services for paired devices
                BluetoothDevice device = itr.next().device;

                Log.d(TAG, "Getting Services for " + device.getName() + ", " + device);
                if (!device.fetchUuidsWithSdp()) {
                    Log.w(TAG, "SDP Failed for " + device.getName());
                }

            }
        }
    }

    public void startScan(final boolean enable, boolean bleAvailable) {
        if (enable) {
            if (!mScanning) {

                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//
                filter.addAction(BluetoothDevice.ACTION_UUID);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                mContext.registerReceiver(mReceiver, filter);
                bluetoothAdapter.startDiscovery();
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanning) {
                            mScanning = false;
                            bluetoothAdapter.cancelDiscovery();

                            if (mCallback != null) {
                                synchronized (mLock) {
                                    mBtDevices.clear();
                                    mBtDevices.addAll(mBtDevicesTemp);
                                    mCallback.onScanComplete(mBtDevices);
                                }
                                initiateSdp();
                            }
                        }
                    }
                }, SCAN_PERIOD);
                mBtDevicesTemp.clear();
                mScanning = true;
                if (mCallback != null) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 10000) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String name = device.getName();
                            if (checkFilter(name)) {
                                BtDevice btDevice = null;
                                if (name.toLowerCase().startsWith(Utils.DEV_NAME_LTRACK)) {
                                    btDevice = new Ltrack();
                                }
                                btDevice.name = name;
                                btDevice.address = device.getAddress(); // MAC address
                                btDevice.rssi = 0;
                                btDevice.device = null;
                                synchronized (mLock) {
                                    mBtDevicesTemp.add(btDevice);
                                }
                                mCallback.onScanUpdate(btDevice);
                            }
                        }
                    }
                }

            }
        } else {
            bluetoothAdapter.cancelDiscovery();
            mScanning = false;

            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                Log.e(TAG,  e.getMessage());
            }
            if (mCallback != null) {
                synchronized (mLock) {
                    mBtDevices.clear();
                    mBtDevices.addAll(mBtDevicesTemp);
//                    mCallback.onScanComplete(mBtDevices);
                }
            }

        }
    }



}
