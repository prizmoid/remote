package com.prizmoid.setup_ble;

import java.util.UUID;

public class Utils {
    public static final String DEV_NAME_LTRACK = "ltrack";
    public static final String DEV_NAME_CTRACK = "ctrack";
    public static final String DEV_NAME_STRACK = "strack";
    public static final String DEV_NAME_XLINE = "xline";

    public static final String[] BT_NAME_FILTER = {
            DEV_NAME_CTRACK, DEV_NAME_LTRACK, DEV_NAME_STRACK, DEV_NAME_XLINE,"bt"};

    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final String RFCOMM_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public enum HW_TYPE_T
    {

        HW_TYPE_START(0),
        HW_TYPE_GENERIC(HW_TYPE_START.toInt()),
        HW_TYPE_X1000L, // 1: x1000 Light (GSM)
        HW_TYPE_X1000,  // 2: x1000 (GSM)
        HW_TYPE_X2000L, // 3: x2000 Light (WiFi)
        HW_TYPE_X2000,  // 4: x2000 (WiFi)
        HW_TYPE_X3000L, // 5: x3000 Light (GSM)
        HW_TYPE_X3000,  // 6: x3000 (GSM)
        HW_TYPE_X4000L, // 7: x4000 Light  (WiFi)
        HW_TYPE_X4000,  // 8: x4000  (WiFi)
        HW_TYPE_X5000,  // 9: x5000 (GSM + WiFi)
        HW_TYPE_X6000,  // 10: x6000 (GSM + WiFi + RF)
        HW_TYPE_X7000, // 11: x7000 (3G + WiFi + RF)

        HW_TYPE_CTR (20),

        HW_TYPE_OBD (30),

        HW_TYPE_JTRACK (40),
        HW_TYPE_JTUT, // 41

        HW_TYPE_XTRACK_LIGHT(50), // 50: xTrack (Low Cost GSM)
        HW_TYPE_XTRACK_BASE, // 51: xTrack (Low Cost GSM + battery)

        HW_TYPE_XTRACK_X2000(60), // 60: x2000 (Low Cost GSM)

        HW_TYPE_GM_BASE (70), // 70: GM project
        HW_TYPE_GM_O1,
        HW_TYPE_GM_O2,
        HW_TYPE_GM_O3,
        HW_TYPE_GM_O4,

        HW_TYPE_X1000R( 100), // 100: x1000 RF controller
        HW_TYPE_X3000R, // 101: x3000 RF controller

        HW_TYPE_END (HW_TYPE_X3000R.toInt());

        private static class Counter {
            private static int mCounter = 0;
        }

        HW_TYPE_T () {
            mValue = Counter.mCounter;
            Counter.mCounter++;
        }

        HW_TYPE_T(int value){
            Counter.mCounter = value;
            mValue = Counter.mCounter;
            Counter.mCounter++;
        }

        public int toInt() {
            return mValue;
        }

        public static HW_TYPE_T getTypeByInt(int hw_type) {
            for(HW_TYPE_T e : values()) {
                if (e.mValue == hw_type) {
                    return e;
                }
            }
            return null;
        }

        public String toString() {
            String buf;
            if (mValue == HW_TYPE_X1000L.toInt()) {
                buf = "x1000l";
            } else if (mValue ==  HW_TYPE_X1000.toInt()) {

                    buf = "x1000";
                    }

                else if (mValue ==  HW_TYPE_X2000L.toInt()) {

                    buf = "x2000l";
                    }

                else if (mValue ==  HW_TYPE_X2000.toInt()) {

                    buf = "x2000";
                    }

                else if (mValue ==  HW_TYPE_X3000L.toInt()) {

                    buf = "x3000l";
                    }

                else if (mValue ==  HW_TYPE_X3000.toInt()) {

                    buf = "x3000";


                } else if (mValue ==  HW_TYPE_X4000L.toInt()) {

                    buf = "x4000l";


                } else if (mValue ==  HW_TYPE_X5000.toInt()) {

                    buf = "x5000";


                } else if (mValue ==  HW_TYPE_X6000.toInt()) {

                    buf = "x6000";


                } else if (mValue ==  HW_TYPE_X7000.toInt()) {

                    buf = "x7000";


                } else if (mValue ==  HW_TYPE_OBD.toInt()) {

                    buf = "xOBD";


                } else if (mValue ==  HW_TYPE_JTRACK.toInt()) {

                    buf = "JTRACK";


                } else if (mValue ==  HW_TYPE_JTUT.toInt()) {

                    buf = "JTUT";


                } else if (mValue ==  HW_TYPE_XTRACK_LIGHT.toInt()) {

                    buf = "xTrackl";


                } else if (mValue ==  HW_TYPE_XTRACK_BASE.toInt()) {

                    buf = "xTrack";


                } else if (mValue ==  HW_TYPE_CTR.toInt()) {

                    buf = "CTR";


                } else if (mValue ==  HW_TYPE_GM_BASE.toInt()) {

                    buf = "Flex";


                } else if (mValue ==  HW_TYPE_GM_O1.toInt()) {

                    buf = "gmO1";


                } else if (mValue ==  HW_TYPE_GM_O2.toInt()) {

                    buf = "gmO2";


                } else if (mValue ==  HW_TYPE_GM_O3.toInt()) {

                    buf = "gmO3";


                } else if (mValue ==  HW_TYPE_GM_O4.toInt()) {

                buf = "gmO4";

                 }
                else {

                    buf = "Flex";

            }
            return buf;
        }
        private final int mValue;

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
}
