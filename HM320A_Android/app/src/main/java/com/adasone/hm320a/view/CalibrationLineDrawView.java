package com.adasone.hm320a.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

public class CalibrationLineDrawView extends View {
    private static final String TAG = CalibrationLineDrawView.class.getSimpleName();

    private static final float DEFAULT_LINE_WIDTH_DP = 4.00f;

    private static final int P_FAR_LEFT = 0;
    private static final int P_FAR_RIGHT = 1;
    private static final int P_NEAR_LEFT = 2;
    private static final int P_NEAR_RIGHT = 3;
    private static final int P_VANISHING = 4;
    private static final int P_MAX = 5;

    private ImageView[] mPointViewArray = null;

    private float mPointHalfWidth = 0f;
    private float mPointHalfHeight = 0f;

    private float [] mFarLeftPointPos = new float[2];
    private float [] mFarRightPointPos = new float[2];

    private float [] mNearLeftPointPos = new float[2];
    private float [] mNearRightPointPos = new float[2];

    private float [] mVanishingPointPos = new float[2];

    private Paint mVerticalLinePaint;
    private Paint mVerticalDotLinePaint;
    private Paint mHorizontalLinePaint;

    private boolean mShowVanishingPoint = Boolean.FALSE;

    public CalibrationLineDrawView(Context context) {
        super(context);
        initDrawLine();
    }

    public CalibrationLineDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initDrawLine();
    }

    public CalibrationLineDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDrawLine();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPointViewArray != null) {
            canvas.drawLine(mFarLeftPointPos[0], mFarLeftPointPos[1],
                    mFarRightPointPos[0], mFarRightPointPos[1], mHorizontalLinePaint);

            canvas.drawLine(mNearLeftPointPos[0], mNearLeftPointPos[1],
                    mNearRightPointPos[0], mNearRightPointPos[1], mHorizontalLinePaint);

            if (mShowVanishingPoint) {
                canvas.drawLine(mFarLeftPointPos[0], mFarLeftPointPos[1],
                        mNearLeftPointPos[0], mNearLeftPointPos[1], mVerticalLinePaint);

                canvas.drawLine(mFarRightPointPos[0], mFarRightPointPos[1],
                        mNearRightPointPos[0], mNearRightPointPos[1], mVerticalLinePaint);

                canvas.drawLine(mVanishingPointPos[0], mVanishingPointPos[1],
                        mFarLeftPointPos[0], mFarLeftPointPos[1], mVerticalDotLinePaint);

                canvas.drawLine(mVanishingPointPos[0], mVanishingPointPos[1],
                        mFarRightPointPos[0], mFarRightPointPos[1], mVerticalDotLinePaint);

                mPointViewArray[P_VANISHING].setX(mVanishingPointPos[0] - mPointHalfWidth);
                mPointViewArray[P_VANISHING].setY(mVanishingPointPos[1] - mPointHalfHeight);
            } else {
                canvas.drawLine(mFarLeftPointPos[0], mFarLeftPointPos[1],
                        mNearLeftPointPos[0], mNearLeftPointPos[1], mVerticalLinePaint);

                canvas.drawLine(mFarRightPointPos[0], mFarRightPointPos[1],
                        mNearRightPointPos[0], mNearRightPointPos[1], mVerticalLinePaint);
            }
        }
    }

    public void initDrawLine(){
        //setWillNotDraw(false);
        float lineWidth = CalibrationLineDrawView.getLineDefaultPixel(getContext());
        mVerticalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mVerticalLinePaint.setStyle(Paint.Style.STROKE);
        mVerticalLinePaint.setColor(0xFFFFBA00);
        mVerticalLinePaint.setStrokeWidth(lineWidth);

        mHorizontalLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHorizontalLinePaint.setStyle(Paint.Style.STROKE);
        mHorizontalLinePaint.setColor(0xFF00F6FF);
        mHorizontalLinePaint.setStrokeWidth(lineWidth);

        mVerticalDotLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mVerticalDotLinePaint.setStyle(Paint.Style.STROKE);
        mVerticalDotLinePaint.setColor(0xFFFFBA00);
        mVerticalDotLinePaint.setStrokeWidth(lineWidth);
        mVerticalDotLinePaint.setPathEffect(new DashPathEffect(new float[] {5, 5 }, 0));
    }

    public void setViewArray(ImageView[] viewArray) {
        mPointViewArray = viewArray;
        mPointHalfWidth = mPointViewArray[P_NEAR_RIGHT].getWidth() / 2f;
        mPointHalfHeight = mPointViewArray[P_NEAR_RIGHT].getHeight() / 2f;
    }

    public void drawLine() {
        if (mPointViewArray != null) {
            mFarLeftPointPos[0] = mPointViewArray[P_FAR_LEFT].getX() + mPointHalfWidth;
            mFarLeftPointPos[1] = mPointViewArray[P_FAR_LEFT].getY() + mPointHalfHeight;
            mFarRightPointPos[0] = mPointViewArray[P_FAR_RIGHT].getX() + mPointHalfWidth;
            mFarRightPointPos[1] = mFarLeftPointPos[1];

            mNearLeftPointPos[0] = mPointViewArray[P_NEAR_LEFT].getX() + mPointHalfWidth;
            mNearLeftPointPos[1] = mPointViewArray[P_NEAR_LEFT].getY() + mPointHalfHeight;
            mNearRightPointPos[0] = mPointViewArray[P_NEAR_RIGHT].getX() + mPointHalfWidth;
            mNearRightPointPos[1] = mNearLeftPointPos[1];

            if (mShowVanishingPoint) {
                float x1 = mNearLeftPointPos[0];
                float y1 = mNearLeftPointPos[1];
                float x2 = mFarLeftPointPos[0];
                float y2 = mFarLeftPointPos[1];
                float x3 = mNearRightPointPos[0];
                float y3 = mNearRightPointPos[1];
                float x4 = mFarRightPointPos[0];
                float y4 = mFarRightPointPos[1];
                /*
                * Left line 1: (x1,y1), (x2,y2)
                * Right Line 2 : (x3,y3), (x4,y4)
                * Vanishing point  : (Px, Py)
                * Px = ((x1y2-y1x2)(x3-x4)-(x1-x2)(x3y4-y3x4)) / ((x1-x2)(y3-y4)-(y1-y2)(x3-x4))
                * Py = ((x1y2-y1x2)(y3-y4)-(y1-y2)(x3y4-y3x4)) / ((x1-x2)(y3-y4)-(y1-y2)(x3-x4))
                */

                mVanishingPointPos[0] = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4))
                        / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
                mVanishingPointPos[1] = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4))
                        / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));
            }
            invalidate();
        }
    }

    public float getFarLeftPointX() {
        return mFarLeftPointPos[0];
    }
    public float getFarLeftPointY() {
        return mFarLeftPointPos[1];
    }
    public float getFarRightPointX() {
        return mFarRightPointPos[0];
    }
    public float getFarRightPointY() {
        return mFarRightPointPos[1];
    }
    public float getNearLeftPointX() {
        return mNearLeftPointPos[0];
    }
    public float getNearLeftPointY() {
        return mNearLeftPointPos[1];
    }
    public float getNearRightPointX() {
        return mNearRightPointPos[0];
    }
    public float getNearRightPointY() {
        return mNearRightPointPos[1];
    }

    public void showVanishingPoint (boolean show) {
        mShowVanishingPoint = show;
    }

    private static int getLineDefaultPixel(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_WIDTH_DP, displayMetrics);
    }
}

