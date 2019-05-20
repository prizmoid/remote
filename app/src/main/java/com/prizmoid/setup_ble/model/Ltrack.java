package com.prizmoid.setup_ble.model;

import android.util.Log;

import java.util.ArrayList;

public class Ltrack extends BtDevice {


    public Ltrack() {

        Cmd c = new Cmd("VER");
        c.addAttr("HW_TYPE");
        c.addAttr("HW_VERSION");
        c.addAttr("FW_VERSION");
        c.addAttr("FLEX_VERSION");
        c.addAttr("RELEASE_DATE");
        mCmdList.put(c.cmdName, c);

        c = new Cmd("BOOTVER");
        c.addAttr("BOOT_VERSION");
        c.addAttr("MODIFICATION_DATE");
        mCmdList.put(c.cmdName,c);

        c = new Cmd("HWCFG");
        c.addAttr("HW_TYPE");
        c.addAttr("SERIAL");
        mCmdList.put(c.cmdName, c);

        c = new Cmd("DEV");
        c.addAttr("DEV_NAME");
        c.addAttr("DEV_ID");
        c.addAttr("DEV_SERIAL");
        mCmdList.put(c.cmdName, c);

        mCmdList.put("MODE",new Cmd("MODE"));
        mCmdList.put("PASS",new Cmd("PASS"));
        mCmdList.put("LOG",new Cmd("LOG"));

        c = new Cmd("DATE");
        c.addAttr("DATE");
        c.addAttr("TIME");
        c.addAttr("HOUR_SHIFT");
        c.addAttr("GPS_SYNC");
        c.addAttr("DST");
        c.fmt = "ddMMyy HHmmss Z";
        mCmdList.put(c.cmdName,c);

        c = new Cmd("SERVER");
        c.addAttr("SERVER_TOTAL");
        c.addAttr("SERVER_NUMBER");
        mCmdList.put(c.cmdName, c);

        c = new Cmd("SERVER1");
        c.addAttr("SERVER_PROPERTY");
        c.addAttr("PROTOCOL_PROPERTY");
        c.addAttr("IP");
        c.addAttr("PORT");
        c.addAttr("LOGIN");
        c.addAttr("PASS");
        c.addAttr("LOGIN_TIMEOUT");
        c.addAttr("TRANSFER_TIMEOUT");
        c.addAttr("PING_TIMEOUT");
        c.addAttr("RESPONSE_TIMEOUT");
        c.addAttr("IP_PROTOCOL_TYPE");
        c.addAttr("WIRELESS_CHANNEL");
        c.addAttr("DATA_PROTOCOL_TYPE");
        c.addAttr("KEEP_ALIVE");
        mCmdList.put(c.cmdName, c);


        mCmdList.put("REP",new Cmd("REP"));
        mCmdList.put("EKEY",new Cmd("EKEY"));
        mCmdList.put("MSENS",new Cmd("MSENS"));
        mCmdList.put("TILT",new Cmd("TILT"));
        mCmdList.put("TSENS",new Cmd("TSENS"));
        mCmdList.put("GNSS",new Cmd("GNSS"));
        mCmdList.put("GSM",new Cmd("GSM"));
        mCmdList.put("PHONE",new Cmd("PHONE"));
        mCmdList.put("SMS",new Cmd("SMS"));
        mCmdList.put("RING",new Cmd("RING"));
        mCmdList.put("AUDIO",new Cmd("AUDIO"));
        mCmdList.put("SIMS",new Cmd("SIMS"));
        mCmdList.put("SIM",new Cmd("SIM"));
        mCmdList.put("SIM1",new Cmd("SIM1"));
        mCmdList.put("SIM2",new Cmd("SIM2"));
        mCmdList.put("APN",new Cmd("APN"));
        mCmdList.put("APN1",new Cmd("APN1"));
        mCmdList.put("APN2",new Cmd("APN2"));
        mCmdList.put("WIFI",new Cmd("WIFI"));
        mCmdList.put("WIFIINET",new Cmd("WIFINET"));
        mCmdList.put("INTRSHD",new Cmd("INTRSHD"));
        mCmdList.put("IN",new Cmd("IN"));
        mCmdList.put("COUNTER",new Cmd("COUNTER"));
        mCmdList.put("IGN",new Cmd("IGN"));
        mCmdList.put("SOS",new Cmd("SOS"));
        mCmdList.put("OUT",new Cmd("OUT"));
        mCmdList.put("RS232",new Cmd("RS232"));
        mCmdList.put("RS485",new Cmd("RS485"));
        mCmdList.put("LLS",new Cmd("LLS"));
        mCmdList.put("THERMOID",new Cmd("THERMOID"));
        mCmdList.put("THERMO",new Cmd("THERMO"));
        mCmdList.put("FILTER",new Cmd("FILTER"));
        mCmdList.put("SERVER",new Cmd("SERVER"));
        mCmdList.put("STATIST",new Cmd("STATIST"));
        mCmdList.put("PRSET",new Cmd("PRSET"));
        mCmdList.put("RESET",new Cmd("RESET"));
        mCmdList.put("DEFAULT",new Cmd("DEFAULT"));
        mCmdList.put("ERASURE",new Cmd("ERASURE"));
        mCmdList.put("FACTORY",new Cmd("FACTORY"));

    }

    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        Log.d("Ltrack", "Requesting device info");
        writeToDevice("DEV");
        writeToDevice("VER");
        writeToDevice("HWCFG");
        writeToDevice("BOOTVER");
        writeToDevice("SERVER");
        writeToDevice("SERVER0");
    }
}
