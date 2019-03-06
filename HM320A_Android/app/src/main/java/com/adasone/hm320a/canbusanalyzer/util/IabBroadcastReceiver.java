package com.adasone.hm320a.canbusanalyzer.util;//package vn.penkun.obd2scantool.canbusanalyzer.util;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//public class IabBroadcastReceiver extends BroadcastReceiver {
//    public static final String ACTION = "com.android.vending.billing.PURCHASES_UPDATED";
//    private final IabBroadcastListener mListener;
//
//    public interface IabBroadcastListener {
//        void receivedBroadcast();
//    }
//
//    public IabBroadcastReceiver(IabBroadcastListener listener) {
//        this.mListener = listener;
//    }
//
//    public void onReceive(Context context, Intent intent) {
//        if (this.mListener != null) {
//            this.mListener.receivedBroadcast();
//        }
//    }
//}
