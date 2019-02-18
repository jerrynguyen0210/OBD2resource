package com.adasone.hm320a.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.adasone.hm320a.util.VehicleTypeUtil;

public class VehicleTypeData implements Parcelable {
    private int mCode;


    public VehicleTypeData(int code) {
        this.mCode = code;
    }

    public int getStringResId() {
        return VehicleTypeUtil.getVehicleTypeStringResId(mCode);
    }

    public int getCode() {
        return mCode;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Constructs a VehicleTypeData from a Parcel
     * @param parcel Source Parcel
     */
    public VehicleTypeData (Parcel parcel) {
        this.mCode = parcel.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCode);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<VehicleTypeData> CREATOR = new Creator<VehicleTypeData>() {

        @Override
        public VehicleTypeData createFromParcel(Parcel source) {
            return new VehicleTypeData(source);
        }

        @Override
        public VehicleTypeData[] newArray(int size) {
            return new VehicleTypeData[size];
        }

    };
}
