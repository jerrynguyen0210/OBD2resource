package com.adasone.hm320a.data;


public class AutoCalibrationResultData {
    private int mCenterX[];
    private int mVanishingY[];
    private int mFarY[];
    private int mFarLeftX[];
    private int mFarRightX[];
    private int mNearY[];
    private int mNearLeftX[];
    private int mNearRightX[];
    private int mFindChess[];

    public AutoCalibrationResultData() {
        this.mCenterX = new int[1];
        this.mVanishingY = new int[1];
        this.mFarY = new int[1];
        this.mFarLeftX = new int[1];
        this.mFarRightX = new int[1];
        this.mNearY = new int[1];
        this.mNearLeftX = new int[1];
        this.mNearRightX = new int[1];
        this.mFindChess = new int[1];
    }

    public int[] getArrayCenterX() {
        return mCenterX;
    }
    public int[] getArrayVanishingY() {
        return mVanishingY;
    }
    public int[] getArrayFarY() {
        return mFarY;
    }
    public int[] getArrayFarLeftX() {
        return mFarLeftX;
    }
    public int[] getArrayFarRightX() {
        return mFarRightX;
    }
    public int[] getArrayNearY() {
        return mNearY;
    }
    public int[] getArrayNearLeftX() {
        return mNearLeftX;
    }
    public int[] getArrayNearRightX() {
        return mNearRightX;
    }
    public int[] getArrayFindChess() {
        return mFindChess;
    }

    public int getCenterX() {
        return mCenterX[0];
    }
    public int getVanishingY() {
        return mVanishingY[0];
    }
    public int getFarY() {
        return mFarY[0];
    }
    public int getFarLeftX() {
        return mFarLeftX[0];
    }
    public int getFarRightX() {
        return mFarRightX[0];
    }
    public int getNearY() {
        return mNearY[0];
    }
    public int getNearLeftX() {
        return mNearLeftX[0];
    }
    public int getNearRightX() {
        return mNearRightX[0];
    }
    public int getFindChess() {
        return mFindChess[0];
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AutoCalibrationResultData { CenterX : ").append(String.valueOf(mCenterX[0]))
        .append(", VanishingY : ").append(String.valueOf(mVanishingY[0]))
                .append(", FarY : ").append(String.valueOf(mFarY[0]))
                .append(", FarLeftX : ").append(String.valueOf(mFarLeftX[0]))
                .append(", FarRightX : ").append(String.valueOf(mFarRightX[0]))
                .append(", NearY : ").append(String.valueOf(mNearY[0]))
                .append(", NearLeftX : ").append(String.valueOf(mNearLeftX[0]))
                .append(", NearRightX : ").append(String.valueOf(mNearRightX[0]))
                .append(", FindChessCorner : ").append(String.valueOf(mFindChess[0])).append('}');
        return sb.toString();
    }
}