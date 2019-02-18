package com.adasone.hm320a.data;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.regex.Pattern;

public class DriverData implements Parcelable {
    private static final String TAG = DriverData.class.getSimpleName();
    private int mVehicleType;
    private String mVIN;
    private String mVehicleRegNo;
    private String mBusinessRegNo;
    private String mDriverCode;
    private String mFormatDriverCode;

    public DriverData() {
        this.mVehicleType = -1;
        this.mVIN = "";
        this.mVehicleRegNo = "";
        this.mBusinessRegNo = "";
        this.mDriverCode = "";
        this.mFormatDriverCode = "";
    }

    public DriverData(int userType, int vehicleType, String vin, String vehicleRegNo,
                      String businessRegNo, String driverCode) {
        this.mVehicleType = vehicleType;
        this.mVIN = vin;
        this.mVehicleRegNo = vehicleRegNo;
        this.mBusinessRegNo = businessRegNo;
        this.setDriverCode(driverCode);
    }

    public static boolean validateVIN(String number) {
        boolean ret = true;
        if (number.length() != 17) {
            ret = false;
        } else {
            Pattern ps = Pattern.compile("^[A-Z0-9]+$");
            if (!ps.matcher(number).matches()) {
                ret = false;
            }
        }
        return ret;
    }

    public static boolean validateVehicleRegNumber(String number) {
        boolean ret = true;
        Pattern ps;
        int strLength = number.length();

        if (strLength == 9) {
            ps = Pattern.compile("^[가-힣]{2}[0-9]{2}[가-힣][0-9]{4}+$");
            if (!ps.matcher(number).matches()) {
                ret = false;
            } else {
                boolean find = false;
                String regions[] = {"서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
                        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"};
                for (String region : regions) {
                    find = number.startsWith(region);
                    if (find) {
                        break;
                    }
                }
                if (!find) {
                    ret = false;
                }
            }
        } else if (strLength == 7) {
            ps = Pattern.compile("^[0-9]{2}[가-힣][0-9]{4}+$");
            if (!ps.matcher(number).matches()) {
                ret = false;
            }
        } else {
            ret = false;
        }
        return ret;
    }

    public static boolean validateBusinessRegNumber(String number) {
        boolean ret = true;

        if (number.length() != 10) {
            ret = false;
        } else {
            Pattern ps = Pattern.compile("^[0-9]+$");
            if (!ps.matcher(number).matches()) {
                ret = false;
            }
        }
        return ret;
    }

    public static boolean validateDriverCode(String number) {
        boolean ret = true;

        Pattern ps = Pattern.compile("^[0-9]+$");
        if (!ps.matcher(number).matches()) {
            ret = false;
        }
        return ret;
    }


    public int getVehicleType() {
        return mVehicleType;
    }
    public String getVIN() {
        return mVIN;
    }
    public String getVehicleRegNo() {
        return mVehicleRegNo;
    }
    public String getBusinessRegNo() {
        return mBusinessRegNo;
    }
    public String getDriverCode() {
        return mDriverCode;
    }
    public String getFormatDriverCode() {
        return mFormatDriverCode;
    }

    public void setVehicleType(int vehicleType) {
        mVehicleType = vehicleType;
    }
    public void setVIN(String vin) {
        mVIN = vin;
    }
    public void setVehicleRegNo(String vehicleRegNo) {
        mVehicleRegNo = vehicleRegNo;
    }
    public void setBusinessRegNo(String businessRegNo) {
        mBusinessRegNo = businessRegNo;
    }
    public void setDriverCode(String driverCode) {
        int length = driverCode.length();

        if (length < 18) {
            mDriverCode = driverCode;
            mFormatDriverCode = String.format("%18s", mDriverCode);
            mFormatDriverCode = mFormatDriverCode.replace(" ", "#");
        } else if (driverCode.length() == 18) {
            mDriverCode = driverCode;
            mFormatDriverCode = mDriverCode;
        } else {
            Log.e(TAG, "Error : Driver code length is long (" + length + ")");
        }
    }
    public void setFormatDriverCode(String formatDriverCode) {
        if (formatDriverCode.length() == 18 ) {
            mFormatDriverCode = formatDriverCode;
            mDriverCode = mFormatDriverCode.replace("#", "");
        } else {
            Log.e(TAG, "Error : Driver code is not format number.(" + formatDriverCode + ")");
        }
    }

    public void copyFrom(DriverData data) {
        mVehicleType = data.getVehicleType();
        mVIN = data.getVIN();
        mVehicleRegNo = data.getVehicleRegNo();
        mBusinessRegNo = data.getBusinessRegNo();
        mDriverCode = data.getDriverCode();
        mFormatDriverCode = data.getFormatDriverCode();
    }

    public void initialize() {
        this.mVehicleType = -1;
        this.mVIN = "";
        this.mVehicleRegNo = "";
        this.mBusinessRegNo = "";
        this.mDriverCode = "";
        this.mFormatDriverCode = "";
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Constructs a DriverData from a Parcel
     * @param parcel Source Parcel
     */
    public DriverData (Parcel parcel) {
        this.mVehicleType = parcel.readInt();
        this.mVIN = parcel.readString();
        this.mVehicleRegNo = parcel.readString();
        this.mBusinessRegNo = parcel.readString();
        this.mDriverCode = parcel.readString();
        this.mFormatDriverCode = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mVehicleType);
        dest.writeString(mVIN);
        dest.writeString(mVehicleRegNo);
        dest.writeString(mBusinessRegNo);
        dest.writeString(mDriverCode);
        dest.writeString(mFormatDriverCode);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<DriverData> CREATOR = new Creator<DriverData>() {

        @Override
        public DriverData createFromParcel(Parcel source) {
            return new DriverData(source);
        }

        @Override
        public DriverData[] newArray(int size) {
            return new DriverData[size];
        }

    };

}