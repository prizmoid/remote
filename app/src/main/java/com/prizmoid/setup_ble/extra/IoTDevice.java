package com.prizmoid.setup_ble.extra;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by basagee on 2015. 8. 6..
 */
public class IoTDevice implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEVICE_TYPE_STRING_IR = "IR";
    public static final String DEVICE_TYPE_STRING_BT = "BT";
    public static final String DEVICE_TYPE_STRING_ZW = "ZW";

    // for grid view
    public static final int DEVICE_TYPE_ID_NONE = 0x0000;
    public static final int DEVICE_TYPE_ID_IR = 0x0001;
    public static final int DEVICE_TYPE_ID_BT = 0x0002;
    public static final int DEVICE_TYPE_ID_ZW = 0x0003;


    public static final int DEVICE_BT_UUID_LEN_16 = 0x0001;
    public static final int DEVICE_BT_UUID_LEN_32 = 0x0002;
    public static final int DEVICE_BT_UUID_LEN_128 = 0x0003;

    @SerializedName("IOT_DEVICE_ID")
    private String deviceId;
    @SerializedName("IOT_DEVICE_NAME")
    private String deviceName;
    @SerializedName("IOT_DEVICE_MAKER")
    private String deviceVendor;
    @SerializedName("IOT_DEVICE_MODEL")
    private String deviceModel;
    @SerializedName("IOT_DEVICE_TYPE")
    private String deviceType;          // "IR", "BT", "ZW", etc.....
    @SerializedName("IOT_DEVICE_BT_UUIDLEN")
    private int uuidLen;                // 0: 16, 1: 32, 2: 128
    @SerializedName("IOT_DEVICE_UUIDS")            // for bluetooth
    private ArrayList<String> uuids;

    @SerializedName("BONDED")
    private boolean isBondedWithServer;

    @SerializedName("LAST_SEQ")
    private int lastSequenceNumber;

    transient HashMap<Integer, AdRecord> adRecordHashMap;
    transient HashMap<String, ArrayList<String>> discoveredServices;
    //transient ArrayList<IoTDeviceScenario> deviceScenario;
    transient int scenarioPosition = 0;
    transient boolean isKnownDevice;

    public int getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public void setLastSequenceNumber(int lastSequenceNumber) {
        this.lastSequenceNumber = lastSequenceNumber;
    }

    public HashMap<String, ArrayList<String>> getDiscoveredServices() {
        return discoveredServices;
    }

    public int getScenarioPosition() {
        return scenarioPosition;
    }

    public void setScenarioPosition(int scenarioPosition) {
        this.scenarioPosition = scenarioPosition;
    }

    public boolean isKnownDevice() {
        return isKnownDevice;
    }

    public void setIsKnownDevice(boolean isKnownDevice) {
        this.isKnownDevice = isKnownDevice;
    }

    public boolean isBondedWithServer() {
        return isBondedWithServer;
    }

    public void setIsBondedWithServer(boolean isBonded) {
        this.isBondedWithServer = isBonded;
    }

    public void setDiscoveredServices(HashMap<String, ArrayList<String>> discoveredServices) {
        this.discoveredServices = discoveredServices;
    }



    public int getUuidLen() {
        return uuidLen;
    }

    public void setUuidLen(int uuidLen) {
        this.uuidLen = uuidLen;
    }

    public ArrayList<String> getUuids() {
        return uuids;
    }

    public void setUuids(ArrayList<String> uuids) {
        this.uuids = uuids;
    }

    public HashMap<Integer, AdRecord> getAdRecordHashMap() {
        return adRecordHashMap;
    }

    public void setAdRecordHashMap(HashMap<Integer, AdRecord> adRecordHashMap) {
        this.adRecordHashMap = adRecordHashMap;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceVendor() {
        return deviceVendor;
    }

    public void setDeviceVendor(String deviceVendor) {
        this.deviceVendor = deviceVendor;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getDeviceTypeId() {
        if (DEVICE_TYPE_STRING_IR.equals(deviceType)) {
            return DEVICE_TYPE_ID_IR;
        } else if (DEVICE_TYPE_STRING_BT.equals(deviceType)) {
            return DEVICE_TYPE_ID_BT;
        } else if (DEVICE_TYPE_STRING_ZW.equals(deviceType)) {
            return DEVICE_TYPE_ID_ZW;
        } else {
            return DEVICE_TYPE_ID_NONE;
        }
    }

    public IoTDevice() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeString(this.deviceVendor);
        dest.writeString(this.deviceModel);
        dest.writeString(this.deviceType);
        dest.writeInt(this.uuidLen);
        dest.writeStringList(this.uuids);
        dest.writeByte(isBondedWithServer ? (byte) 1 : (byte) 0);
        dest.writeInt(this.lastSequenceNumber);
        dest.writeSerializable(this.adRecordHashMap);
        dest.writeSerializable(this.discoveredServices);
        //dest.writeTypedList(deviceScenario);
        dest.writeInt(this.scenarioPosition);
        dest.writeByte(isKnownDevice ? (byte) 1 : (byte) 0);
    }

    protected IoTDevice(Parcel in) {
        this.deviceId = in.readString();
        this.deviceName = in.readString();
        this.deviceVendor = in.readString();
        this.deviceModel = in.readString();
        this.deviceType = in.readString();
        this.uuidLen = in.readInt();
        this.uuids = in.createStringArrayList();
        this.isBondedWithServer = in.readByte() != 0;
        this.lastSequenceNumber = in.readInt();
        this.adRecordHashMap = (HashMap<Integer, AdRecord>) in.readSerializable();
        this.discoveredServices = (HashMap<String, ArrayList<String>>) in.readSerializable();
        //this.deviceScenario = null;//in.createTypedArrayList(IoTDeviceScenario.CREATOR);
        this.scenarioPosition = in.readInt();
        this.isKnownDevice = in.readByte() != 0;
    }

    public static final Creator<IoTDevice> CREATOR = new Creator<IoTDevice>() {
        public IoTDevice createFromParcel(Parcel source) {
            return new IoTDevice(source);
        }

        public IoTDevice[] newArray(int size) {
            return new IoTDevice[size];
        }
    };
}