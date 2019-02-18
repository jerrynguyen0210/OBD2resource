package com.adasone.hm320a.data;


import com.adasone.hm320a.application.Constants;

import java.util.ArrayList;

public class Session  {
    // Current device token
    private String mDeviceToken = "";
    // Current device version
    private String mDeviceVersionName = "";
    // Current device version code
    private int mDeviceVersionCode = 0;
    // Current device DTG support
    private int mDeviceDtgSupport = Constants.DtgSupport.HW_NOT_SUPPORTED;

    // Current device data
    private VehicleData mDevVehicleData = new VehicleData();
    private DriverData mDevDriverData = new DriverData();
    private CalibrationData mDevCalibrationData = new CalibrationData();

    // Preset data
    private VehicleData mPreVehicleData = new VehicleData();
    private DriverData mPreDriverData = new DriverData();
    private CalibrationData mPreCalibrationData = new CalibrationData();

    // Video List
    private ArrayList<VideoData> mNormalVideoArrayList = new ArrayList<>();
    private ArrayList<VideoData> mEventVideoArrayList = new ArrayList<>();

    public Session() {

    }

    public String getDeviceToken() {
        return mDeviceToken;
    }
    public String getDeviceVersionName() {
        return mDeviceVersionName;
    }
    public int getDeviceVersionCode() {
        return mDeviceVersionCode;
    }
    public int getDeviceDtgSupport() {
        return mDeviceDtgSupport;
    }

    public VehicleData getDevVehicleData() {
        return mDevVehicleData;
    }
    public DriverData getDevDriverData() {
        return mDevDriverData;
    }
    public CalibrationData getDevCalibrationData() {
        return mDevCalibrationData;
    }

    public VehicleData getPreVehicleData() {
        return mPreVehicleData;
    }
    public DriverData getPreDriverData() {
        return mPreDriverData;
    }
    public CalibrationData getPreCalibrationData() {
        return mPreCalibrationData;
    }

    public ArrayList<VideoData> getNormalVideoArrayList() {
        return mNormalVideoArrayList;
    }
    public ArrayList<VideoData> getEventVideoArrayList() {
        return mEventVideoArrayList;
    }

    public void setDeviceToken(String token) {
        mDeviceToken = token;
    }
    public void setDeviceVersionName(String versionName) {
        mDeviceVersionName = versionName;
    }
    public void setDeviceVersionCode(int versionCode) {
        mDeviceVersionCode = versionCode;
    }
    public void setDeviceDtgSupport(int dtgSupport) {
        mDeviceDtgSupport = dtgSupport;
    }

    public void initialize() {
        mDeviceToken = "";
        mDeviceVersionName = "";
        mDeviceVersionCode = 0;
        mDeviceDtgSupport = Constants.DtgSupport.HW_NOT_SUPPORTED;

        mDevVehicleData.initialize();
        mDevDriverData.initialize();
        mDevCalibrationData.initialize();

        mPreVehicleData.initialize();
        mPreDriverData.initialize();
        mPreCalibrationData.initialize();


        mNormalVideoArrayList.clear();
        mEventVideoArrayList.clear();
    }

    public void initializeVideoList() {
        mNormalVideoArrayList.clear();
        mEventVideoArrayList.clear();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}