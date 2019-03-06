package com.adasone.hm320a.interfaces;

import android.os.Bundle;

import com.adasone.hm320a.data.Session;

public interface OnFragmentInteractionListener {
    boolean isUSBConnected();
    boolean isUSBPermissionRequestPending();
    boolean isUSBDefaultInfoRequestPending();
    boolean isUSBConnectionFailPending();
    void onSplashDisplayFinish();
    void onMenuSelected(int menu, int arg);
    void onRequestMainMenu(boolean first);
    void notifyMessage(String fragmentTag, int msg, Bundle bundle);
    void addActivityInteractionListener(OnActivityInteractionListener listener);
    void removeActivityInteractionListener(OnActivityInteractionListener listener);

    Session getSession();

    void reqDeviceInfo();
    boolean reqReadVersionInfo();
    boolean reqReadVehicleInfo();
    boolean reqWriteVehicleInfo();
    boolean reqReadCalibrationInfo();
    boolean reqWriteCalibrationInfo();
    boolean reqReadDTGDriverInfo();
    boolean reqWriteDTGDriverInfo();
    boolean reqVideoFileList();
    boolean reqDownloadVideoFile(String filepath, String filename);
    boolean reqCancelDownloadVideoFile();
    boolean reqSendFirmwareFile(String filepath,  String filename, String size);
}