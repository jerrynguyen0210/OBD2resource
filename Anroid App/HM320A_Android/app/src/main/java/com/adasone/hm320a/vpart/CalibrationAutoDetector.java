package com.adasone.hm320a.vpart;

import com.adasone.hm320a.data.AutoCalibrationResultData;

public class CalibrationAutoDetector {
    private static final int CHESS_GRID_SIZE = 6;

    public CalibrationAutoDetector () {
        // Required empty public constructor
    }

    public void startAutoCalibrationDetect(String firstImageFilePath, String secondImageFilePath, int cameraHeight,
                                           int cameraToBumper, int vehicleWidth, int chessHeight) {
 //       nativeStartAutoCalibrationDetect(firstImageFilePath, secondImageFilePath, cameraHeight, cameraToBumper, vehicleWidth, chessHeight, CHESS_GRID_SIZE);
    }

    public AutoCalibrationResultData getCalibrationDetectionResult() {
        AutoCalibrationResultData resultData = new AutoCalibrationResultData();
     //   nativeGetCalibrationDetectionResult(resultData.getArrayCenterX(), resultData.getArrayVanishingY(),
     //           resultData.getArrayNearLeftX(), resultData.getArrayNearRightX(), resultData.getArrayNearY(),
     //           resultData.getArrayFarLeftX(), resultData.getArrayFarRightX(), resultData.getArrayFarY(), resultData.getArrayFindChess());
        return resultData;
    }

 //   private native void nativeStartAutoCalibrationDetect(String firstImageFilePath, String secondImageFilePath, int cameraHeight, int cameraToBumper, int vehicleWidth, int chessHeight, int chessGridSize);
 //   private native void nativeGetCalibrationDetectionResult(int vpX[], int vpY[], int nearLeftX[], int nearRightX[], int nearY[], int farLeftX[], int farRightX[], int farY[], int findChess[]);

}
