package com.adasone.hm320a.data;


import android.os.Parcel;
import android.os.Parcelable;

public class VehicleData implements Parcelable {
    public static final int TYPE_CAR = 1;
    public static final int TYPE_BUS = 2;
    public static final int TYPE_TRUCK = 3;

    public static final int LIST_POS_TYPE_CAR = 0;
    public static final int LIST_POS_TYPE_BUS = 1;
    public static final int LIST_POS_TYPE_TRUCK = 2;

    private int mVehicleType;
    private String mUniqueID;
    private String mManufacturer;
    private String mModel;
    private String mFuelType;
    private String mReleaseDate;
    private String mOBDManufacturerID;
    private String mOBDModelID;
    private String mOBDFuelTypeID;

    public VehicleData() {
        this.mVehicleType = VehicleData.TYPE_CAR;
        this.mUniqueID = "";
        this.mManufacturer = "";
        this.mModel = "";
        this.mFuelType = "";
        this.mReleaseDate = "";
        this.mOBDManufacturerID = "";
        this.mOBDModelID = "";
        this.mOBDFuelTypeID = "";
    }

    public VehicleData(int vehicleType, String uniqueID, String manufacturer, String model, String fuelType, String releaseDate,
                           String obdManufacturerID, String obdModelID, String obdFuelTypeID) {
        this.mVehicleType = vehicleType;
        this.mUniqueID = uniqueID;
        this.mManufacturer = manufacturer;
        this.mModel = model;
        this.mFuelType = fuelType;
        this.mReleaseDate = releaseDate;
        this.mOBDManufacturerID = obdManufacturerID;
        this.mOBDModelID = obdModelID;
        this.mOBDFuelTypeID = obdFuelTypeID;
    }

    public int getVehicleType() {
        return mVehicleType;
    }
    public String getUniqueID() {
        return mUniqueID;
    }
    public String getManufacturer() {
        return mManufacturer;
    }
    public String getModel() {
        return mModel;
    }
    public String getFuelType() {
        return mFuelType;
    }
    public String getReleaseDate() {
        return mReleaseDate;
    }
    public String getOBDManufacturerID() {
        return mOBDManufacturerID;
    }
    public String getOBDModelID() {
        return mOBDModelID;
    }
    public String getOBDFuelTypeID() {
        return mOBDFuelTypeID;
    }

    public void setVehicleType(int vehicleType) {
        mVehicleType = vehicleType;
    }
    public void setUniqueID(String uniqueID) {
        mUniqueID = uniqueID;
    }
    public void setManufacturer(String manufacturer) {
        mManufacturer = manufacturer;
    }
    public void setModel(String model) {
        mModel = model;
    }
    public void setFuelType(String fuelType) {
        mFuelType = fuelType;
    }
    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }
    public void setOBDManufacturerID(String obdManufacturerID) {
        mOBDManufacturerID = obdManufacturerID;
    }
    public void setOBDModelID(String obdModelID) {
        mOBDModelID = obdModelID;
    }
    public void setOBDFuelTypeID(String obdFuelType) {
        mOBDFuelTypeID = obdFuelType;
    }

    public void copyFrom(VehicleData data) {
        mVehicleType = data.getVehicleType();
        mUniqueID = data.getUniqueID();
        mManufacturer = data.getManufacturer();
        mModel = data.getModel();
        mFuelType = data.getFuelType();
        mReleaseDate = data.getReleaseDate();
        mOBDManufacturerID = data.getOBDManufacturerID();
        mOBDModelID = data.getOBDModelID();
        mOBDFuelTypeID = data.getOBDFuelTypeID();
    }

    public void initialize() {
        this.mVehicleType = VehicleData.TYPE_CAR;
        this.mUniqueID = "";
        this.mManufacturer = "";
        this.mModel = "";
        this.mFuelType = "";
        this.mReleaseDate = "";
        this.mOBDManufacturerID = "";
        this.mOBDModelID = "";
        this.mOBDFuelTypeID = "";
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Constructs a VehicleData from a Parcel
     * @param parcel Source Parcel
     */
    public VehicleData (Parcel parcel) {
        this.mVehicleType = parcel.readInt();
        this.mUniqueID = parcel.readString();
        this.mManufacturer = parcel.readString();
        this.mModel = parcel.readString();
        this.mFuelType = parcel.readString();
        this.mReleaseDate = parcel.readString();
        this.mOBDManufacturerID = parcel.readString();
        this.mOBDModelID = parcel.readString();
        this.mOBDFuelTypeID = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mVehicleType);
        dest.writeString(mUniqueID);
        dest.writeString(mManufacturer);
        dest.writeString(mModel);
        dest.writeString(mFuelType);
        dest.writeString(mReleaseDate);
        dest.writeString(mOBDManufacturerID);
        dest.writeString(mOBDModelID);
        dest.writeString(mOBDFuelTypeID);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<VehicleData> CREATOR = new Creator<VehicleData>() {

        @Override
        public VehicleData createFromParcel(Parcel source) {
            return new VehicleData(source);
        }

        @Override
        public VehicleData[] newArray(int size) {
            return new VehicleData[size];
        }

    };

}