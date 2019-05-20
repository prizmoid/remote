package com.prizmoid.setup_ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.prizmoid.setup_ble.Utils.MessageConstants;
import com.prizmoid.setup_ble.extra.AdRecord;
import com.prizmoid.setup_ble.model.BtDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;

public class BleBtManager {
    private static final String TAG = "BleBtManager";
    public static final int SCAN_PERIOD = 12000;// Stops scanning after 12 seconds.
    private static final String BT_NAME_FILTER[] = {"ct", "lt", "st","bt"};
    private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final String EXTRA_DATA = "BLE_CHARACTERISTIC";
    private boolean mScanning;
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private Context mContext;
    OnBtScanListener mCallback;
    private HandlerThread mHandlerThread = new HandlerThread("BtScanThread");
    private Handler mHandler;
    private volatile List<BtDevice> mBtDevices = new ArrayList<>();
    private volatile List<BtDevice> mBtDevicesTemp = new ArrayList<>();
    private Object mLock = new Object();
    private ConnectedThread mConnectedThread;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBluetoothGatt;

    private ScanCallback mBleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            String le = device.getType() == BluetoothDevice.DEVICE_TYPE_LE ? "BLE " : "";
            Log.v(TAG, le + "Device " + device.getName() + ":" + result.toString());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        String name = device.getName();
                        if (checkFilter(name)) {
                            synchronized (mLock) {
                                //List<ParcelUuid> UIDS  = result.getScanRecord().getServiceUuids();
                                //if (UIDS == null) return;
                                //String data = result.getScanRecord().getServiceUuids().toString();
                                //Log.d(TAG, "UUID:" + data);
                                if (name!=null) {
                                    if (!nameInList(name, mBtDevicesTemp)) {
                                        BtDevice btDevice = new BtDevice();
                                        btDevice.device = device;
                                        //btDevice.UUID = data;
                                        btDevice.address = device.getAddress();
                                        btDevice.name = device.getName();
                                        btDevice.rssi = result.getRssi();
                                        mBtDevicesTemp.add(btDevice);
                                        mCallback.onScanUpdate(btDevice);
                                    }
                                }
                            }
                        }
                    }

                }
            });
        }
    };

    public static UUID makeUuid(String uuidString) {
        String[] parts = {
                uuidString.substring(0, 7),
                uuidString.substring(9, 12),
                uuidString.substring(14, 17),
                uuidString.substring(19, 22),
                uuidString.substring(24, 35)
        };
        long m1 = Long.parseLong(parts[0], 16);
        long m2 = Long.parseLong(parts[1], 16);
        long m3 = Long.parseLong(parts[2], 16);
        long lsb1 = Long.parseLong(parts[3], 16);
        long lsb2 = Long.parseLong(parts[4], 16);
        long msb = (m1 << 32) | (m2 << 16) | m3;
        long lsb = (lsb1 << 48) | lsb2;
        return new UUID(msb, lsb);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BtDevice mmDevice;

        public ConnectThread(BtDevice btDevice) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = btDevice;

                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                UUID uid = makeUuid(MY_UUID);
                //UUID.fromString(btDevice.UUID == null? MY_UUID.replace("-", ""): btDevice.UUID);
                btDevice.device.createBond();
                btDevice.device.setPin("1234".getBytes());
                /*Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(EXTRA_DEVICE, btDevice.device);
                int PAIRING_VARIANT_PIN = 272;
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
                mContext.sendBroadcast(intent);

                Intent intent2 = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                mContext.startActivity(intent2);*/
                try {

                    Log.i(TAG, "Device Name: " + mmDevice.device.getName());
                    //Log.i(TAG, "Device UUID: " + mmDevice.device.getUuids()[0].getUuid());
                    tmp = btDevice.device.createRfcommSocketToServiceRecord(uid);

                    Log.d(TAG, " UUID from device is null, Using SPP UUID, Device name: " + btDevice.device.getName());
                    try {
                        //tmp = btDevice.device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
                        try {
                            Log.w("","trying fallback...");
                            tmp =(BluetoothSocket) mmDevice.device.getClass()
                                    .getMethod("createRfcommSocket",
                                            new Class[] {int.class}).invoke(mmDevice.device,0);
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        } catch (InvocationTargetException e2) {
                            e2.printStackTrace();
                        } catch (NoSuchMethodException e3) {
                            e3.printStackTrace();
                        }
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
            } catch (Exception e) {
                Log.e(TAG, "Socket's create() method failed: " + e.getMessage());

            }
            mmSocket =tmp;
        }


        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            mBluetoothGatt=mmDevice.device.connectGatt(mContext, true, mGattCallback);


            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, connectException.getMessage());
                // Unable to connect; close the socket and return.



                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }

            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket, mmDevice);
        }


        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

    }

    //get callbacks when something changes
    private final BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                String intentAction = "ACTION_GATT_CONNECTED";
                broadcastupdate(intentAction, null);
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();

            String uuid;
            List<BluetoothGattCharacteristic> gattCharacteristics;
            ArrayList<BluetoothGattCharacteristic> charas;
            for (BluetoothGattService gattService : services) {
                Log.v(TAG, "Service: " + gattService.getUuid().toString());
                if (gattService.getUuid().hashCode() == UUID.fromString(MY_UUID).hashCode()){
                    List<BluetoothGattService> incServices = gattService.getIncludedServices();
                }
                gattCharacteristics =
                        gattService.getCharacteristics();
                charas = new ArrayList<>();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    Log.v(TAG, "Characteristic: " + gattCharacteristic.getUuid().toString());
                    charas.add(gattCharacteristic);
                    uuid = gattCharacteristic.getUuid().toString();
                    gatt.readCharacteristic(gattCharacteristic);
                    if (uuid.equals("28b31d52-3e34-4a4b-9e8a-454a102d926c")) {
                    //if (uuid.equals("0000fff4-0000-1000-8000-00805f9b34fb")) {
                        final int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            BluetoothGattCharacteristic mNotifyCharacteristic = gattCharacteristic;
                            gatt.setCharacteristicNotification(
                                    gattCharacteristic, true);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
         //   updateStatus(characteristic);
            broadcastupdate("", characteristic);
            Log.e("gatt", "readChar");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
           // updateStatus(characteristic);
            broadcastupdate("", characteristic);
            Log.e("gatt", "writeChar");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //updateStatus(characteristic);
            broadcastupdate("", characteristic);
            Log.e("gatt", "changeChar");
        }
    };

    //Get the 'real' data out of characteristic
    private void broadcastupdate(final String action,final BluetoothGattCharacteristic characteristic){
        final Intent intent= new Intent(action);
        if (characteristic == null) return;
        //only  when it is the right characteristic?/service?
        //if (TX_CHAR_UUID.equals(characteristic.getUuid())){
            //get the 'real' data from the stream
          //  intent.putExtra(EXTRA_DATA, characteristic.getValue());
            //send the extracted data via LocalBroadcastManager
            //LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Log.v(TAG, "Char " +characteristic.getUuid().toString() + " value: " + characteristic.getValue());
        //}

    }
    private void manageMyConnectedSocket(BluetoothSocket mmSocket, BtDevice btDevice) {
        //TODO: move thread to BtDevice
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }



    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            write(String.format("1234;%s", "VER").getBytes());
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    Log.d(TAG, "Msg:" + String.valueOf(readMsg.obj));
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

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

    public BleBtManager(Context context) {
        mContext = context;
        mHandlerThread.start();

    }

    public BleBtManager(Context context, OnBtScanListener callback) {
        mContext = context;
        mCallback = callback;
        mHandlerThread.start();
    }

    public boolean connectDevice(int index) {
        synchronized (mLock){
            if (index < mBtDevices.size()) {
                ConnectThread connectThread = new ConnectThread(mBtDevices.get(index));
                connectThread.start();
                return  true;
            }
        }
        return false;
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
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MessageConstants.MESSAGE_READ) {
                        // Send the file using bluetooth
                        Log.d(TAG, String.valueOf(msg.obj));
                    } else if (msg.what == MessageConstants.MESSAGE_WRITE) {
                        // Send the file using bluetooth
                        Log.d(TAG, String.valueOf(msg.obj));
                    } else if (msg.what == MessageConstants.MESSAGE_TOAST) {
                        // Send the file using bluetooth
                        Log.d(TAG, String.valueOf(msg.obj));
                    }
                }
            };
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

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                String name = device.getName();
                                if (checkFilter(name)) {
                                    synchronized (mLock) {
                                        String data = getServiceUUID(ParseRecord(scanRecord));
                                        for (AdRecord ar : parseScanRecord(scanRecord)){
                                            Log.d(TAG, ar.toString());
                                        }
                                        if (data!=null) {
                                            if (!nameInList(name, mBtDevicesTemp)) {
                                                BtDevice btDevice = new BtDevice();
                                                btDevice.name = device.getName();
                                                btDevice.address = device.getAddress();
                                                btDevice.device = device;
                                                btDevice.UUID = data;
                                                btDevice.rssi = rssi;
                                                mBtDevicesTemp.add(btDevice);
                                                mCallback.onScanUpdate(btDevice);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    });
                }
            };

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
        for(String s: BT_NAME_FILTER) {
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
                }
                Log.v(TAG, "BT BR  FOUND " + le + device.getName() + ": " + device);
                String name = device.getName();
                if (checkFilter(name)) {
                    synchronized (mLock) {
                        name = (le + name).trim();
                        if (!nameInList(name, mBtDevicesTemp)) {
                            BtDevice btDevice = new BtDevice();
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

                IntentFilter filter = new IntentFilter();//BluetoothDevice.ACTION_FOUND
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
                            //bluetoothAdapter.stopLeScan(leScanCallback);
                            mBleScanner.stopScan(mBleScanCallback);
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
                if (bleAvailable) {
                    if (mBleScanner == null) {
                        mBleScanner = bluetoothAdapter.getBluetoothLeScanner();
                    }
                    mBleScanner.startScan(mBleScanCallback);
                    //bluetoothAdapter.startLeScan(leScanCallback);
                }
                if (mCallback != null) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 10000) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            if (checkFilter(device.getName())) {
                                BtDevice btDevice = new BtDevice();
                                btDevice.name = device.getName();
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
            if (bleAvailable) {
                //bluetoothAdapter.stopLeScan(leScanCallback);
                mBleScanner.stopScan(mBleScanCallback);
            }
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

    public String getUUID(ScanResult result){
        String UUIDx = result.getScanRecord().getServiceUuids().toString();
        Log.d("UUID", " as String ->>" + UUIDx);
        return UUIDx;
    }
    /*
        BLE Scan record type IDs
        data from:
        https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
        */
    static final int EBLE_FLAGS           = 0x01;//«Flags»	Bluetooth Core Specification:
    static final int EBLE_16BitUUIDInc    = 0x02;//«Incomplete List of 16-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_16BitUUIDCom    = 0x03;//«Complete List of 16-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_32BitUUIDInc    = 0x04;//«Incomplete List of 32-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_32BitUUIDCom    = 0x05;//«Complete List of 32-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_128BitUUIDInc   = 0x06;//«Incomplete List of 128-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_128BitUUIDCom   = 0x07;//«Complete List of 128-bit Service Class UUIDs»	Bluetooth Core Specification:
    static final int EBLE_SHORTNAME       = 0x08;//«Shortened Local Name»	Bluetooth Core Specification:
    static final int EBLE_LOCALNAME       = 0x09;//«Complete Local Name»	Bluetooth Core Specification:
    static final int EBLE_TXPOWERLEVEL    = 0x0A;//«Tx Power Level»	Bluetooth Core Specification:
    static final int EBLE_DEVICECLASS     = 0x0D;//«Class of Device»	Bluetooth Core Specification:
    static final int EBLE_SIMPLEPAIRHASH  = 0x0E;//«Simple Pairing Hash C»	Bluetooth Core Specification:​«Simple Pairing Hash C-192»	​Core Specification Supplement, Part A, section 1.6
    static final int EBLE_SIMPLEPAIRRAND  = 0x0F;//«Simple Pairing Randomizer R»	Bluetooth Core Specification:​«Simple Pairing Randomizer R-192»	​Core Specification Supplement, Part A, section 1.6
    static final int EBLE_DEVICEID        = 0x10;//«Device ID»	Device ID Profile v1.3 or later,«Security Manager TK Value»	Bluetooth Core Specification:
    static final int EBLE_SECURITYMANAGER = 0x11;//«Security Manager Out of Band Flags»	Bluetooth Core Specification:
    static final int EBLE_SLAVEINTERVALRA = 0x12;//«Slave Connection Interval Range»	Bluetooth Core Specification:
    static final int EBLE_16BitSSUUID     = 0x14;//«List of 16-bit Service Solicitation UUIDs»	Bluetooth Core Specification:
    static final int EBLE_128BitSSUUID    = 0x15;//«List of 128-bit Service Solicitation UUIDs»	Bluetooth Core Specification:
    static final int EBLE_SERVICEDATA     = 0x16;//«Service Data»	Bluetooth Core Specification:​«Service Data - 16-bit UUID»	​Core Specification Supplement, Part A, section 1.11
    static final int EBLE_PTADDRESS       = 0x17;//«Public Target Address»	Bluetooth Core Specification:
    static final int EBLE_RTADDRESS       = 0x18;;//«Random Target Address»	Bluetooth Core Specification:
    static final int EBLE_APPEARANCE      = 0x19;//«Appearance»	Bluetooth Core Specification:
    static final int EBLE_DEVADDRESS      = 0x1B;//«​LE Bluetooth Device Address»	​Core Specification Supplement, Part A, section 1.16
    static final int EBLE_LEROLE          = 0x1C;//«​LE Role»	​Core Specification Supplement, Part A, section 1.17
    static final int EBLE_PAIRINGHASH     = 0x1D;//«​Simple Pairing Hash C-256»	​Core Specification Supplement, Part A, section 1.6
    static final int EBLE_PAIRINGRAND     = 0x1E;//«​Simple Pairing Randomizer R-256»	​Core Specification Supplement, Part A, section 1.6
    static final int EBLE_32BitSSUUID     = 0x1F;//​«List of 32-bit Service Solicitation UUIDs»	​Core Specification Supplement, Part A, section 1.10
    static final int EBLE_32BitSERDATA    = 0x20;//​«Service Data - 32-bit UUID»	​Core Specification Supplement, Part A, section 1.11
    static final int EBLE_128BitSERDATA   = 0x21;//​«Service Data - 128-bit UUID»	​Core Specification Supplement, Part A, section 1.11
    static final int EBLE_SECCONCONF      = 0x22;//​«​LE Secure Connections Confirmation Value»	​Core Specification Supplement Part A, Section 1.6
    static final int EBLE_SECCONRAND      = 0x23;//​​«​LE Secure Connections Random Value»	​Core Specification Supplement Part A, Section 1.6​
    static final int EBLE_3DINFDATA       = 0x3D;//​​«3D Information Data»	​3D Synchronization Profile, v1.0 or later
    static final int EBLE_MANDATA         = 0xFF;//«Manufacturer Specific Data»	Bluetooth Core Specification:

    public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
        List<AdRecord> records = new ArrayList<AdRecord>();

        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            //Done once we run out of records
            if (length == 0) break;

            int type = scanRecord[index];
            //Done if our record isn't a valid type
            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);

            records.add(new AdRecord(length, type, data));
            //Advance
            index += length;
        }

        return records;
    }
    /*
    BLE Scan record parsing
    inspired by:
    http://stackoverflow.com/questions/22016224/ble-obtain-uuid-encoded-in-advertising-packet
     */
    static public  Map <Integer,String>  ParseRecord(byte[] scanRecord){
        Map <Integer,String> ret = new HashMap<Integer,String>();
        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            //Zero value indicates that we are done with the record now
            if (length == 0) break;

            int type = scanRecord[index];
            //if the type is zero, then we are pass the significant section of the data,
            // and we are thud done
            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);
            if(data != null && data.length > 0) {
                StringBuilder hex = new StringBuilder(data.length * 2);
                // the data appears to be there backwards
                for (int bb = data.length- 1; bb >= 0; bb--){
                    hex.append(String.format("%02X", data[bb]));
                }
                ret.put(type,hex.toString());
            }
            index += length;
        }

        return ret;
    }

    static public String getServiceUUID(Map<Integer,String> record){
        String ret = "";
        // for example: 0105FACB00B01000800000805F9B34FB --> 010510ee-0000-1000-8000-00805f9b34fb
        if(record.containsKey(EBLE_128BitUUIDCom)){
            String tmpString= record.get(EBLE_128BitUUIDCom).toString();
            ret = tmpString.substring(0, 8) + "-" + tmpString.substring(8,12)+ "-" + tmpString.substring(12,16)+ "-" + tmpString.substring(16,20)+ "-" + tmpString.substring(20,tmpString.length());
            //010510EE --> 010510ee-0000-1000-8000-00805f9b34fb
        }else if(record.containsKey(EBLE_32BitUUIDCom)){
            ret = record.get(EBLE_32BitUUIDCom).toString() + "-0000-1000-8000-00805f9b34fb";
        }
        return ret;
    }
}
