package com.adasone.hm320a.canbusanalyzer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class CCanBusInterface {
    public static final int RATE_10 = 20;
    public static final int RATE_100 = 70;
    public static final int RATE_1000 = 150;
    public static final int RATE_125 = 80;
    public static final int RATE_20 = 30;
    public static final int RATE_200 = 90;
    public static final int RATE_250 = 100;
    public static final int RATE_40 = 40;
    public static final int RATE_400 = 110;
    public static final int RATE_5 = 10;
    public static final int RATE_50 = 50;
    public static final int RATE_500 = 120;
    public static final int RATE_666 = 130;
    public static final int RATE_80 = 60;
    public static final int RATE_800 = 140;
    public static final int RATE_CUSTOM = 0;
    private static CCanBusInterface sCanBusInterface;
    private ArrayList<CCanRate> mRateList;

    public static class CCanFrame {
        public byte[] Data = new byte[8];
        public int ID = 0;
        public int Len = 8;
        public boolean RTR = false;
        public int TimeStamp;
    }

    public static class CCanRate {
        protected byte mBTR0;
        protected byte mBTR1;
        public int rate;

        public String toStringEx() {
            return toString();
        }

        public String toString() {
            String s;
            switch (this.rate) {
                case 10:
                    s = "5";
                    break;
                case 20:
                    s = "10";
                    break;
                case 30:
                    s = "20";
                    break;
                case 40:
                    s = "40";
                    break;
                case 50:
                    s = "50";
                    break;
                case CCanBusInterface.RATE_80 /*60*/:
                    s = "80";
                    break;
                case CCanBusInterface.RATE_100 /*70*/:
                    s = "100";
                    break;
                case CCanBusInterface.RATE_125 /*80*/:
                    s = "125";
                    break;
                case 90:
                    s = "200";
                    break;
                case CCanBusInterface.RATE_250 /*100*/:
                    s = "250";
                    break;
                case CCanBusInterface.RATE_400 /*110*/:
                    s = "400";
                    break;
                case CCanBusInterface.RATE_500 /*120*/:
                    s = "500";
                    break;
                case 130:
                    s = "666";
                    break;
                case CCanBusInterface.RATE_800 /*140*/:
                    s = "800";
                    break;
                case CCanBusInterface.RATE_1000 /*150*/:
                    s = "1000";
                    break;
                default:
                    s = "???";
                    break;
            }
            return s + " kbit/sec";
        }

        private void setBTR() {
            switch (this.rate) {
                case 10:
                    this.mBTR0 = Byte.MAX_VALUE;
                    this.mBTR1 = Byte.MAX_VALUE;
                    return;
                case 20:
                    this.mBTR0 = (byte) -97;
                    this.mBTR1 = (byte) -1;
                    return;
                case 30:
                    this.mBTR0 = (byte) 24;
                    this.mBTR1 = (byte) 28;
                    return;
                case 40:
                    this.mBTR0 = (byte) -121;
                    this.mBTR1 = (byte) -1;
                    return;
                case 50:
                    this.mBTR0 = (byte) 9;
                    this.mBTR1 = (byte) 28;
                    return;
                case CCanBusInterface.RATE_80 /*60*/:
                    this.mBTR0 = (byte) -125;
                    this.mBTR1 = (byte) -1;
                    return;
                case CCanBusInterface.RATE_100 /*70*/:
                    this.mBTR0 = (byte) 4;
                    this.mBTR1 = (byte) 28;
                    return;
                case CCanBusInterface.RATE_125 /*80*/:
                    this.mBTR0 = (byte) 3;
                    this.mBTR1 = (byte) 28;
                    return;
                case 90:
                    this.mBTR0 = (byte) -127;
                    this.mBTR1 = (byte) -6;
                    return;
                case CCanBusInterface.RATE_250 /*100*/:
                    this.mBTR0 = (byte) 1;
                    this.mBTR1 = (byte) 28;
                    return;
                case CCanBusInterface.RATE_400 /*110*/:
                    this.mBTR0 = Byte.MIN_VALUE;
                    this.mBTR1 = (byte) -6;
                    return;
                case CCanBusInterface.RATE_500 /*120*/:
                    this.mBTR0 = (byte) 0;
                    this.mBTR1 = (byte) 28;
                    return;
                case 130:
                    this.mBTR0 = Byte.MIN_VALUE;
                    this.mBTR1 = (byte) -74;
                    return;
                case CCanBusInterface.RATE_800 /*140*/:
                    this.mBTR0 = (byte) 0;
                    this.mBTR1 = (byte) 22;
                    return;
                case CCanBusInterface.RATE_1000 /*150*/:
                    this.mBTR0 = (byte) 0;
                    this.mBTR1 = (byte) 20;
                    return;
                default:
                    this.mBTR0 = (byte) 0;
                    this.mBTR1 = (byte) 0;
                    return;
            }
        }

        public byte getBTR0() {
            return this.mBTR0;
        }

        public byte getBTR1() {
            return this.mBTR1;
        }

        protected CCanRate() {
        }

        public CCanRate(int rateI) {
            this.rate = rateI;
            setBTR();
        }
    }

    public static class CCanRateCustom extends CCanRate {
        private static final String KEY_PREF_BTR0 = "CCanRateCustom.BTR0";
        private static final String KEY_PREF_BTR1 = "CCanRateCustom.BTR1";

        protected CCanRateCustom(int rateI) {
        }

        public CCanRateCustom() {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(CGeneral.context);
            this.mBTR0 = (byte) pref.getInt(KEY_PREF_BTR0, 0);
            this.mBTR1 = (byte) pref.getInt(KEY_PREF_BTR1, 0);
        }

        public String toStringEx() {
            return String.format("Custom (%1$02X %2$02X)\n%3$d kbit/sec", new Object[]{Byte.valueOf(this.mBTR0), Byte.valueOf(this.mBTR1), Integer.valueOf(CCanBusInterface.calcRateByBTR(this.mBTR0, this.mBTR1) / CAdapter.USB_SEND_MAX_TIMEOUT)});
        }

        public String toString() {
            return "Custom";
        }

        public void setBTRs(byte BTR0, byte BTR1) {
            this.mBTR0 = BTR0;
            this.mBTR1 = BTR1;
            Editor editor = PreferenceManager.getDefaultSharedPreferences(CGeneral.context).edit();
            editor.putInt(KEY_PREF_BTR0, this.mBTR0);
            editor.putInt(KEY_PREF_BTR1, this.mBTR1);
            editor.commit();
        }
    }

    private void initRateList() {
        this.mRateList = new ArrayList();
        this.mRateList.add(new CCanRateCustom());
        this.mRateList.add(new CCanRate(10));
        this.mRateList.add(new CCanRate(20));
        this.mRateList.add(new CCanRate(30));
        this.mRateList.add(new CCanRate(40));
        this.mRateList.add(new CCanRate(50));
        this.mRateList.add(new CCanRate(60));
        this.mRateList.add(new CCanRate(70));
        this.mRateList.add(new CCanRate(80));
        this.mRateList.add(new CCanRate(90));
        this.mRateList.add(new CCanRate(100));
        this.mRateList.add(new CCanRate(RATE_400));
        this.mRateList.add(new CCanRate(RATE_500));
        this.mRateList.add(new CCanRate(130));
        this.mRateList.add(new CCanRate(RATE_800));
        this.mRateList.add(new CCanRate(RATE_1000));
    }

    public static int calcRateByBTR(byte BTR1, byte BTR2) {
        return 8000000 / (((((BTR2 & 15) + 1) + (((BTR2 >> 4) & 7) + 1)) + 1) * ((BTR1 & 63) + 1));
    }

    public ArrayList<CCanRate> getRateList() {
        return this.mRateList;
    }

    private CCanBusInterface() {
        initRateList();
    }

    public static CCanBusInterface getInstance() {
        if (sCanBusInterface == null) {
            sCanBusInterface = new CCanBusInterface();
        }
        return sCanBusInterface;
    }

    public CCanRate getRateByIndex(int rateIndex) {
        for (int i = 0; i < this.mRateList.size(); i++) {
            CCanRate canRate = (CCanRate) this.mRateList.get(i);
            if (canRate.rate == rateIndex) {
                return canRate;
            }
        }
        return null;
    }

    public int getRatePosition(int rateIndex) {
        for (int i = 0; i < this.mRateList.size(); i++) {
            if (((CCanRate) this.mRateList.get(i)).rate == rateIndex) {
                return i;
            }
        }
        return -1;
    }
}
