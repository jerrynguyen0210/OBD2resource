package com.adasone.hm320a.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;


public class DisplayUtil {
    public static final  String TAG = DisplayUtil.class.getSimpleName();

    public static int CALIBRATION_PIC_WIDTH = 1280;
    public static int CALIBRATION_PIC_HEIGHT = 720;
    public static int CALIBRATION_NO_BONNET = CALIBRATION_PIC_HEIGHT - 1;

    /* native resolution */
    public static int getNativeResolutionWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    /* native resolution */
    public static int getNativeResolutionHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /* real display width */
    public static int getRealDisplayWidth(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return metrics.widthPixels;
    }
    /* real display height */
    public static int getRealDisplayHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    public static Point getTransDisplayToPicPos(int displayWidth, int displayHeight, float scale,
                                                float displayX, float displayY,
                                                float marginStart, float marginTop) {
        int x, y;
        float scaledDisplayWidth, scaledDisplayHeight;
        float scaledDisplayX, scaledDisplayY;

        scaledDisplayWidth = displayWidth * scale;
        scaledDisplayHeight = displayHeight * scale;

        scaledDisplayX = marginStart + displayX;
        scaledDisplayY = marginTop + displayY;

        x = Math.round((scaledDisplayX * CALIBRATION_PIC_WIDTH) / scaledDisplayWidth);
        y = Math.round((scaledDisplayY * CALIBRATION_PIC_HEIGHT) / scaledDisplayHeight);
        return new Point(x, y);
    }

    public static Point getTransPicToDisplayPos(int displayWidth, int displayHeight, float scale,
                                                int picX, int picY,
                                                float marginStart, float marginTop) {
        int x, y;
        float scaledDisplayWidth, scaledDisplayHeight;
        float scaledDisplayX, scaledDisplayY;

        scaledDisplayWidth = displayWidth * scale;
        scaledDisplayHeight = displayHeight * scale;

        scaledDisplayX = (picX * scaledDisplayWidth) / CALIBRATION_PIC_WIDTH;
        scaledDisplayY = (picY * scaledDisplayHeight) / CALIBRATION_PIC_HEIGHT;

        x = Math.round(scaledDisplayX - marginStart);
        y = Math.round(scaledDisplayY - marginTop);

        return new Point(x, y);
    }

    public static Bitmap getBitmapCalibrationFile(String deviceToken, String filepath) {
        Bitmap bitmap = null;

        if (!"".equals(filepath)) {
            bitmap = BitmapFactory.decodeFile(filepath);
        }
        return bitmap;
    }

    public static void immersiveModeOn(Activity activity) {
        if (activity != null) {
            int uiOptions  = activity.getWindow().getDecorView().getSystemUiVisibility();
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    public static void immersiveModeOff(Activity activity) {
        if (activity != null) {
            int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
            uiOptions &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOptions &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }
}
