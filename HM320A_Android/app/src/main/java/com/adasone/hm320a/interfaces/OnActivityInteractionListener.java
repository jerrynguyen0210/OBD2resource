package com.adasone.hm320a.interfaces;


import android.os.Bundle;

public interface OnActivityInteractionListener {
    String getTag();
    boolean onBackPressed();
    void onUSBConnectionChanged(boolean connect);
    void onRequestCompleted(String cmd);
    void onNotifyMessage(int msg, Bundle bundle);
    void onRequestTimeout(String cmd);
    void onSendFileProgressUpdate(boolean complete, long total, long progress);
    void onReceiveFileProgressUpdate(boolean complete, long total, long progress);
}
