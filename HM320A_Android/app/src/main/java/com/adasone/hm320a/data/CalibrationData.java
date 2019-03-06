package com.adasone.hm320a.data;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

public class CalibrationData implements Parcelable {
    private Bitmap mBackgroundBitmap;
    private String mDate; // yyyy-mm--dd
    private int mCameraHeight; // centimeter
    private int mVehicleWidth;  // centimeter
    private int mCameraToBumper; // centimeter
    private Point mBonnetPoint;   // use only y value
    private int mCenterX;
    private int mVanishingY;
    private int mFarY;
    private int mFarLeftX;
    private int mFarRightX;
    private int mNearY;
    private int mNearLeftX;
    private int mNearRightX;
    private int mFarDistance;
    private int mNearDistance;
    // Auto Calibration
    private int mChessHeight;  // centimeter

    public CalibrationData() {
        this.mBackgroundBitmap = null;
        this.mDate = "";
        this.mCameraHeight = 0;
        this.mVehicleWidth = 0;
        this.mCameraToBumper = 0;
        this.mBonnetPoint = new Point(0,0);
        this.mCenterX = 0;
        this.mVanishingY= 0;
        this.mFarY = 0;
        this.mFarLeftX = 0;
        this.mFarRightX = 0;
        this.mNearY = 0;
        this.mNearLeftX = 0;
        this.mNearRightX = 0;
        this.mFarDistance = 0;
        this.mNearDistance = 0;
        this.mChessHeight = 0;
    }

    public CalibrationData(String date, int cameraHeight, int vehicleWidth, int cameraToBumper,
                           Point bonnetPoint, int centerX, int vanishingY, int farY, int farLeftX, int farRightX,
                           int nearY, int nearLeftX, int nearRightX, int farDistance, int nearDistance) {
        this.mDate = date;
        this.mCameraHeight = cameraHeight;
        this.mVehicleWidth = vehicleWidth;
        this.mCameraToBumper = cameraToBumper;
        this.mBonnetPoint = new Point(bonnetPoint.x, bonnetPoint.y);
        this.mCenterX = centerX;
        this.mVanishingY= vanishingY;
        this.mFarY = farY;
        this.mFarLeftX = farLeftX;
        this.mFarRightX = farRightX;
        this.mNearY = nearY;
        this.mNearLeftX = nearLeftX;
        this.mNearRightX = nearRightX;
        this.mFarDistance = farDistance;
        this.mNearDistance = nearDistance;
        this.mChessHeight = 0;
    }

    public Bitmap getBackgroundBitmap() {
        return mBackgroundBitmap;
    }
    public String getDate() {
        return mDate;
    }
    public int getCameraHeight() {
        return mCameraHeight;
    }
    public int getVehicleWidth() {
        return mVehicleWidth;
    }
    public int getCameraToBumper() {
        return mCameraToBumper;
    }
    public Point getBonnetPoint() {
        return mBonnetPoint;
    }
    public int getCenterX() {
        return mCenterX;
    }
    public int getVanishingY() {
        return mVanishingY;
    }
    public int getFarY() {
        return mFarY;
    }
    public int getFarLeftX() {
        return mFarLeftX;
    }
    public int getFarRightX() {
        return mFarRightX;
    }
    public int getNearY() {
        return mNearY;
    }
    public int getNearLeftX() {
        return mNearLeftX;
    }
    public int getNearRightX() {
        return mNearRightX;
    }
    public int getFarDistance() {
        return mFarDistance;
    }
    public int getNearDistance() {
        return mNearDistance;
    }
    public int getChessHeight() {
        return mChessHeight;
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        mBackgroundBitmap = bitmap;
    }
    public void setDate(String date) {
        mDate = date;
    }
    public void setCameraHeight(int cameraHeight) {
        mCameraHeight = cameraHeight;
    }
    public void setVehicleWidth(int vehicleWidth) {
        mVehicleWidth = vehicleWidth;
    }
    public void setCameraToBumper(int cameraToBumper) {
        mCameraToBumper = cameraToBumper;
    }
    public void setBonnetPoint(Point point) {
        mBonnetPoint.set(point.x, point.y);
    }
    public void setCenterX(int centerX) {
        mCenterX = centerX;
    }
    public void setVanishingY(int vanishingY) {
        mVanishingY = vanishingY;
    }
    public void setFarY(int farY) {
        mFarY = farY;
    }
    public void setFarLeftX(int farLeftX) {
        mFarLeftX = farLeftX;
    }
    public void setFarRightX(int farRightX) {
        mFarRightX = farRightX;
    }
    public void setNearY(int nearY) {
        mNearY = nearY;
    }
    public void setNearLeftX(int nearLeftX) {
        mNearLeftX = nearLeftX;
    }
    public void setNearRightX(int nearRightX) {
        mNearRightX = nearRightX;
    }
    public void setFarDistance(int farDistance) {
        mFarDistance = farDistance;
    }
    public void setNearDistance(int nearDistance) {
        mNearDistance = nearDistance;
    }
    public void setChessHeight(int chessHeight) {
        mChessHeight = chessHeight;
    }

    public void copyFrom(CalibrationData data) {
        mBackgroundBitmap = data.getBackgroundBitmap();
        mDate = data.getDate();
        mCameraHeight = data.getCameraHeight();
        mVehicleWidth = data.getVehicleWidth();
        mCameraToBumper = data.getCameraToBumper();
        mBonnetPoint.set(data.getBonnetPoint().x, data.getBonnetPoint().y);
        mCenterX = data.getCenterX();
        mVanishingY = data.getVanishingY();
        mFarY = data.getFarY();
        mFarLeftX = data.getFarLeftX();
        mFarRightX = data.getFarRightX();
        mNearY = data.getNearY();
        mNearLeftX = data.getNearLeftX();
        mNearRightX = data.getNearRightX();
        mFarDistance = data.getFarDistance();
        mNearDistance = data.getNearDistance();
        mChessHeight = data.getChessHeight();
    }

    public void initialize() {
        this.mBackgroundBitmap = null;
        this.mDate = "";
        this.mCameraHeight = 0;
        this.mVehicleWidth = 0;
        this.mCameraToBumper = 0;
        this.mBonnetPoint.set(0, 0);
        this.mCenterX = 0;
        this.mVanishingY = 0;
        this.mFarY = 0;
        this.mFarLeftX = 0;
        this.mFarRightX = 0;
        this.mNearY = 0;
        this.mNearLeftX = 0;
        this.mNearRightX = 0;
        this.mFarDistance = 0;
        this.mNearDistance = 0;
        this.mChessHeight = 0;
    }


    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Constructs a CalibrationData from a Parcel
     * @param parcel Source Parcel
     */
    public CalibrationData (Parcel parcel) {
        this.mBackgroundBitmap = Bitmap.CREATOR.createFromParcel(parcel);
        this.mDate = parcel.readString();
        this.mCameraHeight = parcel.readInt();
        this.mVehicleWidth = parcel.readInt();
        this.mCameraToBumper = parcel.readInt();
        this.mBonnetPoint = Point.CREATOR.createFromParcel(parcel);
        this.mCenterX = parcel.readInt();
        this.mVanishingY = parcel.readInt();
        this.mFarY = parcel.readInt();
        this.mFarLeftX = parcel.readInt();
        this.mFarRightX = parcel.readInt();
        this.mNearY = parcel.readInt();
        this.mNearLeftX = parcel.readInt();
        this.mNearRightX = parcel.readInt();
        this.mFarDistance = parcel.readInt();
        this.mNearDistance = parcel.readInt();
        this.mChessHeight = parcel.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mBackgroundBitmap.writeToParcel(dest, flags);
        dest.writeString(mDate);
        dest.writeInt(mCameraHeight);
        dest.writeInt(mVehicleWidth);
        dest.writeInt(mCameraToBumper);
        mBonnetPoint.writeToParcel(dest, flags);
        dest.writeInt(mCenterX);
        dest.writeInt(mVanishingY);
        dest.writeInt(mFarY);
        dest.writeInt(mFarLeftX);
        dest.writeInt(mFarRightX);
        dest.writeInt(mNearY);
        dest.writeInt(mNearLeftX);
        dest.writeInt(mNearRightX);
        dest.writeInt(mFarDistance);
        dest.writeInt(mNearDistance);
        dest.writeInt(mChessHeight);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<CalibrationData> CREATOR = new Creator<CalibrationData>() {

        @Override
        public CalibrationData createFromParcel(Parcel source) {
            return new CalibrationData(source);
        }

        @Override
        public CalibrationData[] newArray(int size) {
            return new CalibrationData[size];
        }

    };

}