package com.prizmoid.setup_ble.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import com.prizmoid.setup_ble.Utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedTransferQueue;


public class BtDevice {
    private static final String TAG = "BtDevice";
    public static final String DM = ";";
    public static final String CRLF = "\r\n";
    private static int sChannel = 12;
    protected HashMap<String, Cmd> mCmdList = new HashMap<String, Cmd>();
    LinkedTransferQueue<String> mCmdPool = new LinkedTransferQueue<>();

    public interface OnBtDeviceConnected {
        public void onBtDeviceConnected();
    }

    private Object mLock = new Object();
    public BluetoothDevice device;
    public int rssi;
    public String name;
    public String address;
    public String pass = "1234";
    public String FMT = "$" + pass + DM + "%s" + CRLF;
    public String UUID;
    private OnBtDeviceConnected mConnectionCallback = null;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    private boolean mConnected = false;

    public void requestDeviceInfo() {
        Log.v(TAG, "Stub method request - need to be redefined by device class");
    }

    public boolean connect(Handler handler, OnBtDeviceConnected callback) {
        try {
            mHandler = handler;
            mConnectionCallback = callback;
            ConnectThread connectThread = new ConnectThread();
            connectThread.start();

        } catch (Exception e) {
            Log.e(TAG, "Connection failed", e);
            return false;
        }
        return true;
    }

    public void writeToDevice(String str) {
            mCmdPool.add(str);
    }

    private boolean writeFromQueue(String str) {
        if (mConnectedThread != null) {
            String s = String.format(FMT, str);
            //byte[] test = {0x24, 0x31, 0x32, 0x33, 0x34, 0x3b, 0x56, 0x45, 0x52 };
            Log.d(TAG, "Write from queue: " + s );//+ "   test: " + new String(test, Charset.forName("US-ASCII")));
            mConnectedThread.write(s.getBytes(Charset.forName("US-ASCII")));//
            return true;
        } else {
            Log.e(TAG, "Connection broken!");
            return false;
        }
    }
    public Cmd getCommand(String cmd) {
        return mCmdList.get(cmd);
    }

    public boolean validCmd(String cmd) {
        if(mCmdList.containsKey(cmd)) {
            return true;
        }
        return false;
    }

    private boolean isDeviceBonded(BluetoothDevice device) {
        return device.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;


        public ConnectThread() {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // RFCOMM_UUID is the app's UUID string, also used in the server code.
            java.util.UUID uid = Utils.makeUuid(Utils.RFCOMM_UUID);
            //UUID.fromString(btDevice.UUID == null? RFCOMM_UUID.replace("-", ""): btDevice.UUID);
            if (!isDeviceBonded(device)) {
                Log.d(TAG, "Device not bonded. Starting pairing...");
                device.setPin(pass.getBytes());
                device.createBond();
            } else {
                Log.d(TAG, "Device already bonded");
            }
            ParcelUuid[] parcelUuids = device.getUuids();
            for (ParcelUuid u : parcelUuids) {
                Log.d(TAG, "Found UUID: " + u.getUuid().toString());
            }
                /*Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(EXTRA_DEVICE, btDevice.device);
                int PAIRING_VARIANT_PIN = 272;
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
                mContext.sendBroadcast(intent);

                Intent intent2 = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                mContext.startActivity(intent2);*/
            try {

                Log.i(TAG, "Device Name: " + device.getName());
                //Log.i(TAG, "Device UUID: " + mmDevice.device.getUuids()[0].getUuid());
                //tmp = btDevice.device.createRfcommSocketToServiceRecord(uid);
                //tmp = btDevice.device.createInsecureRfcommSocketToServiceRecord(uid);
                if (sChannel>12) {
                    throw new Exception("Can't find available BT channel");
                } else {
                    sChannel++;
                    Log.d(TAG, "Will be trying on channel " + sChannel);
                }
                Log.d(TAG, " UUID from device is null, Using SPP UUID, Device name: " + device.getName());
                try {
                    //tmp = btDevice.device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
                    try {
                        Log.w(TAG,"trying fallback...");
                        tmp =(BluetoothSocket) device.getClass()
                                .getMethod("createRfcommSocket",
                                        new Class[] {int.class}).invoke(device,sChannel);
                    } catch (IllegalAccessException e1) {
                        Log.e(TAG, "Illegal access: ", e1);
                    } catch (InvocationTargetException e2) {
                        Log.e(TAG, "invocation error:  ", e2);
                    } catch (NoSuchMethodException e3) {
                        Log.e(TAG, "No such method: ", e3);
                    }
                } catch (Exception e4) {
                    Log.e(TAG, "Socket creation failed: ", e4);

                }

            } catch (Exception e) {
                Log.e(TAG, "Socket's create() method failed: " + e.getMessage());

            }
            mmSocket =tmp;
        }


        public void run() {
            if (mmSocket == null) {
                Log.e(TAG, "No socket to connect!");
                cancel();
                return;
            }
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e(TAG, "Could not connect: " + connectException.getMessage());
                // Unable to connect; close the socket and return.

                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            while (! mmSocket.isConnected()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            manageMyConnectedSocket(mmSocket);
        }


        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        //TODO: move thread to BtDevice
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
        //
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
                cancel();

            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                cancel();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            mConnected = true;
            // Keep listening to the InputStream until an exception occurs.
            Log.v(TAG, "Listening incoming data from device " + name);
            mConnectionCallback.onBtDeviceConnected();
            while (true) {
                try {
                    //========== check command in the queue ============
                    synchronized (mLock) {
                        String s = (String) mCmdPool.take();
                        if (s != null) {
                            Log.v(TAG, "found cmd in the queue: " + s);
                            writeFromQueue(s);
                        } else {
                            Log.w(TAG, "Empty message found cmd in the queue!");
                            continue;
                        }

                        //============== Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);

                        // Send the obtained bytes to the UI activity.
                        Message readMsg = mHandler.obtainMessage();
                        readMsg.what =  Utils.MessageConstants.MESSAGE_READ;
                        readMsg.arg1 = numBytes;
                        readMsg.arg2 = -1;
                        readMsg.obj = mmBuffer;
                        // ============ remove command from queue
                        //readMsg.setAsynchronous(false);
                        sleep(150);//TODO implement event based delay - check feedback from UI
                        readMsg.sendToTarget();
                        Log.v(TAG,  s +" cmd response sent to target.");
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    //connect(mDevice);
                    cancel();
                    break;
                } catch (Exception e2) {
                    Log.w(TAG, "Read failed:", e2);
                    cancel();
                    //connect(mDevice);
                    break;
                }
            }
            mConnected = false;
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        Utils.MessageConstants.MESSAGE_WRITE, bytes.length, -1, bytes);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(Utils.MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.arg1 = bundle.size();
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
                //connect(mDevice);
                cancel();
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
}
