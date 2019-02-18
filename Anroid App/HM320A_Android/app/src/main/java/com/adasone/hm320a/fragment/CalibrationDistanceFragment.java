package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.DisplayUtil;
import com.adasone.hm320a.util.RepeatListener;
import com.adasone.hm320a.view.CalibrationLineDrawView;
import com.adasone.hm320a.view.ZoomView;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalibrationDistanceFragment extends Fragment implements ZoomView.ZoomViewListener{
    private static final String TAG = CalibrationDistanceFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = CalibrationDistanceFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private LinearLayout mDebugLayout;
    private TextView mDebugTextView;

    private ImageView mCalibrationBackgroundView;

    private ZoomView mLayoutCalibration;
    private CalibrationLineDrawView mCalibrationLineDrawView;

    private Button mTitleButton;

    private static final int P_FAR_LEFT = 0;
    private static final int P_FAR_RIGHT = 1;
    private static final int P_NEAR_LEFT = 2;
    private static final int P_NEAR_RIGHT = 3;
    private static final int P_VANISHING = 4;
    private static final int P_MAX = 5;

    private ImageView[] mPointViewArray = new ImageView[P_MAX];

    private ImageView mVanishingPointView;

    private static final float DISPLAY_START_X = 0f;
    private static final float DISPLAY_START_Y = 0f;

    private float mPicDisplayStartX = 0f;
    private float mPicDisplayTopY = 0f;

    public CalibrationDistanceFragment() {
        // Required empty public constructor
    }

    public static CalibrationDistanceFragment newInstance() {
        return new CalibrationDistanceFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.addActivityInteractionListener(mActivityInteractionListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            Log.e(TAG, "getActivity() is null !!");
        }
        mSession = mListener.getSession();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_calibration_distance, container, false);

        mLayoutCalibration = (ZoomView) root.findViewById(R.id.layout_calibration_zoom);
        mLayoutCalibration.setListener(this);
        mLayoutCalibration.setMaxZoom(Constants.Zoom.LEVEL4);
        mLayoutCalibration.setMiniMapEnabled(false);

        mCalibrationBackgroundView = (ImageView) root.findViewById(R.id.iv_calibration_preview);
        CalibrationData preData = mSession.getPreCalibrationData();
        if (preData.getBackgroundBitmap() != null) {
            mCalibrationBackgroundView.setBackground(
                    new BitmapDrawable(getResources(), preData.getBackgroundBitmap()));
        }

        mCalibrationLineDrawView = (CalibrationLineDrawView) root.findViewById(R.id.calibration_line_view);
        mCalibrationLineDrawView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mTitleButton = (Button) root.findViewById(R.id.btn_title);

        ImageButton okButton = (ImageButton) root.findViewById(R.id.btn_ok);
        ImageButton cancelButton = (ImageButton) root.findViewById(R.id.btn_cancel);
        okButton.setOnClickListener(mOnClickListener);
        cancelButton.setOnClickListener(mOnClickListener);

        Button zoomInButton = (Button) root.findViewById(R.id.btn_zoom_in);
        Button zoomOutButton = (Button) root.findViewById(R.id.btn_zoom_out);
        zoomInButton.setOnClickListener(mOnClickListener);
        zoomOutButton.setOnClickListener(mOnClickListener);

        mDebugLayout = (LinearLayout) root.findViewById(R.id.layout_debug);
        mDebugTextView = (TextView) root.findViewById(R.id.tv_debug);
        if (Boolean.TRUE/*AppApplication.isDebug()*/) {
            mDebugLayout.setVisibility(View.VISIBLE);
        }

        mVanishingPointView = (ImageView) root.findViewById(R.id.iv_point_vanishing);

        mPointViewArray[P_FAR_LEFT] = (ImageView) root.findViewById(R.id.iv_point_far_left);
        mPointViewArray[P_FAR_RIGHT] = (ImageView) root.findViewById(R.id.iv_point_far_right);
        mPointViewArray[P_NEAR_LEFT] = (ImageView) root.findViewById(R.id.iv_point_near_left);
        mPointViewArray[P_NEAR_RIGHT] = (ImageView) root.findViewById(R.id.iv_point_near_right);

        mPointViewArray[P_VANISHING] = (ImageView) root.findViewById(R.id.iv_point_prediction_vanishing);

        mSelectedView = mPointViewArray[P_NEAR_LEFT];

        for (int i = 0; i < P_VANISHING; i ++) {
            mPointViewArray[i].setOnTouchListener(mPointOnTouchListener);
        }

        mPointViewArray[P_VANISHING].post(new Runnable() {
            @Override
            public void run() {
                final int displayWidth;
                final int displayHeight;
                if (mLayoutCalibration.getWidth() > 0) {
                    displayWidth = mLayoutCalibration.getWidth();
                } else {
                    displayWidth = DisplayUtil.getRealDisplayWidth(getActivity());
                }
                if (mLayoutCalibration.getHeight() > 0) {
                    displayHeight = mLayoutCalibration.getHeight();
                } else {
                    displayHeight = DisplayUtil.getRealDisplayHeight(getActivity());
                }
                float zoom = mLayoutCalibration.getZoom();

                CalibrationData userData = mSession.getDevCalibrationData();
                CalibrationData preData = mSession.getPreCalibrationData();

                Point displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                        preData.getCenterX(), preData.getVanishingY(), mPicDisplayStartX, mPicDisplayTopY);
                setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);

                if (!"".equals(userData.getDate())) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, userData.getFarLeftX(), userData.getFarY(),mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_FAR_LEFT], displayPoint.x, displayPoint.y);

                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, userData.getFarRightX(), userData.getFarY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_FAR_RIGHT], displayPoint.x, displayPoint.y);

                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, userData.getNearLeftX(), userData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_NEAR_LEFT], displayPoint.x, displayPoint.y);

                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, userData.getNearRightX(), userData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_NEAR_RIGHT], displayPoint.x, displayPoint.y);
                } else {
                    int pointWidth = mPointViewArray[P_NEAR_RIGHT].getWidth();
                    int pointHeight = mPointViewArray[P_NEAR_RIGHT].getHeight();
                    float displayCenterX = displayWidth / 2f;
                    float displayCenterY = displayHeight / 2f;

                    setPointXY(mPointViewArray[P_FAR_LEFT],
                            displayCenterX - pointWidth, displayCenterY - pointHeight);
                    setPointXY(mPointViewArray[P_FAR_RIGHT],
                            displayCenterX + pointWidth, displayCenterY - pointHeight);
                    setPointXY(mPointViewArray[P_NEAR_LEFT],
                            displayCenterX - pointWidth * 2, displayCenterY + pointHeight * 2);
                    setPointXY(mPointViewArray[P_NEAR_RIGHT],
                            displayCenterX + pointWidth * 2, displayCenterY + pointHeight * 2);

                }

                mCalibrationLineDrawView.showVanishingPoint(true);
                mCalibrationLineDrawView.setViewArray(mPointViewArray);
                mCalibrationLineDrawView.drawLine();

                /* workaround code : immersive mode not supported device (soft-key model) */
                Point point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getFarLeftPointX(),
                        mCalibrationLineDrawView.getFarLeftPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setFarY(point.y);
                preData.setFarLeftX(point.x);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getFarRightPointX(),
                        mCalibrationLineDrawView.getFarRightPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setFarRightX(point.x);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getNearLeftPointX(),
                        mCalibrationLineDrawView.getNearLeftPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setNearY(point.y);
                preData.setNearLeftX(point.x);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getNearRightPointX(),
                        mCalibrationLineDrawView.getNearRightPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setNearRightX(point.x);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        getPointX(mVanishingPointView), getPointY(mVanishingPointView),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setCenterX(point.x);
                preData.setVanishingY(point.y);

                refreshDebugView(preData);

                mLayoutCalibration.getViewTreeObserver().addOnGlobalLayoutListener(mEndImageGlobalLayoutListener);
            }
        });

        ImageView leftArrowBtn = (ImageView) root.findViewById(R.id.btn_arrow_left);
        ImageView rightArrowBtn = (ImageView) root.findViewById(R.id.btn_arrow_right);
        ImageView downArrowBtn = (ImageView) root.findViewById(R.id.btn_arrow_down);
        ImageView upArrowBtn = (ImageView) root.findViewById(R.id.btn_arrow_up);
        leftArrowBtn.setOnTouchListener(mTouchRepeatListener);
        rightArrowBtn.setOnTouchListener(mTouchRepeatListener);
        downArrowBtn.setOnTouchListener(mTouchRepeatListener);
        upArrowBtn.setOnTouchListener(mTouchRepeatListener);

        AppApplication.getAppApplication().setFontHYGothic900(mTitleButton);
        AppApplication.getAppApplication().setFontHYGothic600(zoomInButton, zoomOutButton, mDebugTextView);

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLayoutCalibration != null) {
            mLayoutCalibration.getViewTreeObserver().removeOnGlobalLayoutListener(mEndImageGlobalLayoutListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.removeActivityInteractionListener(mActivityInteractionListener);
        mListener = null;
    }

    public OnActivityInteractionListener mActivityInteractionListener = new OnActivityInteractionListener() {
        @Override
        public String getTag() {
            return FRAGMENT_TAG;
        }

        @Override
        public boolean onBackPressed() {
            return true;
        }

        @Override
        public void onUSBConnectionChanged(boolean connect) {
        }

        @Override
        public void onRequestCompleted(String cmd) {
        }

        @Override
        public void onNotifyMessage(int msg, Bundle bundle) {
            switch (msg) {
                case Constants.NotifyMsg.CALIBRATION_IMAGE_REFRESH :
                    CalibrationData preData = mSession.getPreCalibrationData();
                    if (preData.getBackgroundBitmap() != null) {
                        mCalibrationBackgroundView.setBackground(
                                new BitmapDrawable(getResources(), preData.getBackgroundBitmap()));
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onRequestTimeout(String cmd) {
        }

        @Override
        public void onSendFileProgressUpdate(boolean complete, long total, long progress) {
        }

        @Override
        public void onReceiveFileProgressUpdate(boolean complete, long total, long progress) {
        }
    };

    public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_ok :
                    if (mListener != null ) {
                        if (checkValidateVanishingPoint()) {
                            mListener.onMenuSelected(Constants.Menu.CALIBRATION_TOTAL, 0);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.vanishing_point_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.btn_cancel :
                    getActivity().onBackPressed();
                    break;
                case R.id.btn_zoom_in :
                    if (mLayoutCalibration.getZoom() == Constants.Zoom.LEVEL4) {
                        // Max zoom
                    } else if (mLayoutCalibration.getZoom() >= Constants.Zoom.LEVEL3) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL4,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    } else if (mLayoutCalibration.getZoom() >= Constants.Zoom.LEVEL2) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL3,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    } else if (mLayoutCalibration.getZoom() >= Constants.Zoom.LEVEL1) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL2,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    }
                    break;
                case R.id.btn_zoom_out :
                    if (mLayoutCalibration.getZoom() == Constants.Zoom.LEVEL1) {
                        // Min zoom
                    } else if (mLayoutCalibration.getZoom() <= Constants.Zoom.LEVEL2) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL1,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    } else if (mLayoutCalibration.getZoom() <= Constants.Zoom.LEVEL3) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL2,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    } else if (mLayoutCalibration.getZoom() <= Constants.Zoom.LEVEL4) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL3,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private int mSelectedViewID = -1;
    private View mSelectedView = null;
    private int mIgnoreActionMoveCount = 0;
    private static final int IGNORE_ACTION_MOVE_MAX_COUNT = 3;
    public View.OnTouchListener mPointOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    if (mSelectedView != null) {
                        ((ImageView) mSelectedView).setImageResource(R.drawable.cali_point);
                    }
                    mSelectedView = v;
                    ((ImageView)mSelectedView).setImageResource(R.drawable.cali_select_point_2);
                    mSelectedViewID = v.getId();
                    switch (mSelectedViewID) {
                        case R.id.iv_point_near_left:
                        case R.id.iv_point_near_right:
                            mTitleButton.setText(R.string.meter_5);
                            break;
                        case R.id.iv_point_far_left:
                        case R.id.iv_point_far_right:
                            mTitleButton.setText(R.string.meter_8);
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP :
                    mSelectedViewID = -1;
                    mIgnoreActionMoveCount = 0;
                    break;
                case MotionEvent.ACTION_MOVE :
                    if (mSelectedViewID != -1) {
                        if (mIgnoreActionMoveCount > IGNORE_ACTION_MOVE_MAX_COUNT) {
                            drawPointAndLineByTouch(v, event.getRawX(), event.getRawY());
                        } else {
                            mIgnoreActionMoveCount++;
                        }
                    }
                    break;
            }

            return true;
        }
    };

    public RepeatListener mTouchRepeatListener = new RepeatListener(100, 50, 5, 3, new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mSelectedView == null) {
                return;
            }
            float touchLawX = mSelectedView.getX() + (mSelectedView.getWidth() / 2f);
            float touchLawY = mSelectedView.getY() + (mSelectedView.getHeight() / 2f);
            switch (view.getId()) {
                case R.id.btn_arrow_left:
                    touchLawX -= 2;
                    break;
                case R.id.btn_arrow_right:
                    touchLawX += 2;
                    break;
                case R.id.btn_arrow_down:
                    touchLawY += 2;
                    break;
                case R.id.btn_arrow_up:
                    touchLawY -= 2;
                    break;
                default:
                    break;
            }
            drawPointAndLineByTouch(mSelectedView, touchLawX, touchLawY);
        }
    });

    private void drawPointAndLineByTouch(View view, float touchLawX, float touchLawY) {
        final float zoom = mLayoutCalibration.getZoom();
        final float EDGE_MARGIN = 5.0f;
        final float MARGIN = (view.getWidth() * zoom) / 2f;
        final int displayWidth = mLayoutCalibration.getWidth();
        final int displayHeight = mLayoutCalibration.getHeight();

        float pointImageViewStartX = touchLawX - view.getWidth() / 2f;
        float pointImageViewStartY = touchLawY - view.getHeight() / 2f;

        float topLimit = DISPLAY_START_Y + EDGE_MARGIN;
        float bottomLimit = mLayoutCalibration.getHeight() - EDGE_MARGIN;
        float startLimit = DISPLAY_START_X + EDGE_MARGIN;
        float endLimit = mLayoutCalibration.getWidth() - EDGE_MARGIN;

        CalibrationData preData = mSession.getPreCalibrationData();
        Point point;

        switch (view.getId()) {
            case R.id.iv_point_near_left:

                if (touchLawX < startLimit) {
                    if (mPointViewArray[P_NEAR_RIGHT].getX() + view.getWidth() / 2f > startLimit + MARGIN) {
                        pointImageViewStartX = startLimit - view.getWidth() / 2f;
                    } else if (touchLawX + mPicDisplayStartX < startLimit) {
                        pointImageViewStartX = view.getX();
                    }
                } else if (touchLawX > mPointViewArray[P_NEAR_RIGHT].getX() + view.getWidth() / 2f - MARGIN) {
                    pointImageViewStartX = mPointViewArray[P_NEAR_RIGHT].getX() - MARGIN;
                }

                if (touchLawY > bottomLimit) {
                    if (mPointViewArray[P_FAR_LEFT].getY() + view.getHeight() / 2f < bottomLimit - MARGIN) {
                        pointImageViewStartY = bottomLimit - view.getHeight() / 2f;
                    }
                    else if (touchLawY - (displayHeight * zoom - displayHeight - mPicDisplayTopY) > bottomLimit) {
                        pointImageViewStartY = view.getY();
                    }
                } else if (touchLawY < mPointViewArray[P_FAR_LEFT].getY() + view.getHeight() / 2f + MARGIN) {
                    pointImageViewStartY =  mPointViewArray[P_FAR_LEFT].getY() +  MARGIN;
                }

                view.setX(pointImageViewStartX);
                view.setY(pointImageViewStartY);

                mPointViewArray[P_NEAR_RIGHT].setY(pointImageViewStartY);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getNearLeftPointX(),
                        mCalibrationLineDrawView.getNearLeftPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);

                preData.setNearY(point.y);
                preData.setNearLeftX(point.x);
                break;
            case R.id.iv_point_near_right:

                if (touchLawX > endLimit) {
                    if (mPointViewArray[P_NEAR_LEFT].getX() + view.getWidth() / 2f < endLimit - MARGIN) {
                        pointImageViewStartX = endLimit - view.getWidth() / 2f;
                    } else if (touchLawX - (displayWidth * zoom - displayWidth - mPicDisplayStartX) > endLimit) {
                        pointImageViewStartX = view.getX();
                    }
                } else if (touchLawX < mPointViewArray[P_NEAR_LEFT].getX() + view.getWidth() / 2f + MARGIN) {
                    pointImageViewStartX =  mPointViewArray[P_NEAR_LEFT].getX() + MARGIN;
                }

                if (touchLawY > bottomLimit) {
                    if (mPointViewArray[P_FAR_RIGHT].getY() + view.getHeight() / 2f < bottomLimit - MARGIN) {
                        pointImageViewStartY = bottomLimit - view.getHeight() / 2f;
                    } else if (touchLawY - (displayHeight * zoom - displayHeight - mPicDisplayTopY) > bottomLimit) {
                        pointImageViewStartY = view.getY();
                    }
                } else if (touchLawY < mPointViewArray[P_FAR_RIGHT].getY() + view.getHeight() / 2f + MARGIN) {
                    pointImageViewStartY =  mPointViewArray[P_FAR_RIGHT].getY() +  MARGIN;
                }
                view.setX(pointImageViewStartX);
                view.setY(pointImageViewStartY);

                mPointViewArray[P_NEAR_LEFT].setY(pointImageViewStartY);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getNearRightPointX(),
                        mCalibrationLineDrawView.getNearRightPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setNearY(point.y);
                preData.setNearRightX(point.x);
                break;
            case R.id.iv_point_far_left:
                if (touchLawX < startLimit) {
                    if (mPointViewArray[P_FAR_RIGHT].getX() + view.getWidth() / 2f > startLimit + MARGIN) {
                        pointImageViewStartX = startLimit - view.getWidth() / 2f;
                    } else if (touchLawX + mPicDisplayStartX < startLimit) {
                        pointImageViewStartX = view.getX();
                    }
                } else if (touchLawX > mPointViewArray[P_FAR_RIGHT].getX() + view.getWidth() / 2f - MARGIN) {
                    pointImageViewStartX = mPointViewArray[P_FAR_RIGHT].getX() - MARGIN;
                }

                if (touchLawY < topLimit) {
                    if (mPointViewArray[P_NEAR_LEFT].getY() + view.getHeight() / 2f > topLimit + MARGIN) {
                        pointImageViewStartY = topLimit;
                    } else if (touchLawY + mPicDisplayTopY < topLimit) {
                        pointImageViewStartY = view.getY();
                    }
                } else if (touchLawY > mPointViewArray[P_NEAR_LEFT].getY() + view.getWidth() / 2f - MARGIN) {
                    pointImageViewStartY =  mPointViewArray[P_NEAR_LEFT].getY() - MARGIN;
                }

                view.setX(pointImageViewStartX);
                view.setY(pointImageViewStartY);

                mPointViewArray[P_FAR_RIGHT].setY(pointImageViewStartY);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getFarLeftPointX(),
                        mCalibrationLineDrawView.getFarLeftPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);
                preData.setFarY(point.y);
                preData.setFarLeftX(point.x);
                break;
            case R.id.iv_point_far_right:

                if (touchLawX > endLimit) {
                    if (mPointViewArray[P_FAR_LEFT].getX() + view.getWidth() / 2f < endLimit - MARGIN) {
                        pointImageViewStartX = endLimit - view.getWidth() / 2f;
                    } else if (touchLawX - (displayWidth * zoom - displayWidth - mPicDisplayStartX) > endLimit) {
                        pointImageViewStartX = view.getX();
                    }
                } else if (touchLawX < mPointViewArray[P_FAR_LEFT].getX() + view.getWidth() / 2f + MARGIN) {
                    pointImageViewStartX =  mPointViewArray[P_FAR_LEFT].getX() + MARGIN;
                }

                if (touchLawY < topLimit) {
                    if (mPointViewArray[P_NEAR_RIGHT].getY() + view.getHeight() / 2f > topLimit + MARGIN) {
                        pointImageViewStartY = topLimit;
                    } else if (touchLawY + mPicDisplayTopY < topLimit) {
                        pointImageViewStartY = view.getY();
                    }
                } else if (touchLawY > mPointViewArray[P_NEAR_RIGHT].getY() + view.getWidth() / 2f - MARGIN) {
                    pointImageViewStartY =  mPointViewArray[P_NEAR_RIGHT].getY() - MARGIN;
                }

                view.setX(pointImageViewStartX);
                view.setY(pointImageViewStartY);
                mPointViewArray[P_FAR_LEFT].setY(pointImageViewStartY);

                point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight, zoom,
                        mCalibrationLineDrawView.getFarRightPointX(),
                        mCalibrationLineDrawView.getFarRightPointY(),
                        mPicDisplayStartX, mPicDisplayTopY);

                preData.setFarY(point.y);
                preData.setFarRightX(point.x);
                break;
        }
        mCalibrationLineDrawView.drawLine();
        refreshDebugView(preData);
    }

    private void drawPointAndLineByZoom(float zoom, float picDisplayStartX, float picDisplayTopY) {
        CalibrationData preData = mSession.getPreCalibrationData();
        final int displayWidth = mLayoutCalibration.getWidth();
        final int displayHeight = mLayoutCalibration.getHeight();

        Point displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                preData.getFarLeftX(), preData.getFarY(), picDisplayStartX, picDisplayTopY);
        setPointXY(mPointViewArray[P_FAR_LEFT], displayPoint.x, displayPoint.y);

        displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                preData.getFarRightX(), preData.getFarY(), picDisplayStartX, picDisplayTopY);
        setPointXY(mPointViewArray[P_FAR_RIGHT], displayPoint.x, displayPoint.y);

        displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                preData.getNearLeftX(), preData.getNearY(), picDisplayStartX, picDisplayTopY);
        setPointXY(mPointViewArray[P_NEAR_LEFT], displayPoint.x, displayPoint.y);

        displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                preData.getNearRightX(), preData.getNearY(), picDisplayStartX, picDisplayTopY);
        setPointXY(mPointViewArray[P_NEAR_RIGHT], displayPoint.x, displayPoint.y);

        displayPoint = DisplayUtil.getTransPicToDisplayPos(mLayoutCalibration.getWidth(),
                mLayoutCalibration.getHeight(), zoom, preData.getCenterX(), preData.getVanishingY(),
                picDisplayStartX, picDisplayTopY);
        setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);

        mCalibrationLineDrawView.drawLine();
    }

    private void setPointX(View view, float x) {
        float halfWidth = view.getWidth() / 2f;
        view.setX(x - halfWidth);
    }

    private void setPointY(View view, float y) {
        float halfHeight = view.getHeight() / 2f;
        view.setY(y - halfHeight);
    }

    private void setPointXY(View view, float x, float y) {
        float halfWidth = view.getWidth() / 2f;
        float halfHeight = view.getHeight() / 2f;
        view.setX(x - halfWidth);
        view.setY(y - halfHeight);
    }

    private float getPointX(View view) {
        float halfWidth = view.getWidth() / 2f;
        return view.getX() + halfWidth;
    }

    private float getPointY(View view) {
        float halfHeight = view.getHeight() / 2f;
        return view.getY() + halfHeight;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<CalibrationDistanceFragment> mFragment;
        private MyHandler(CalibrationDistanceFragment fragment) {
            mFragment = new WeakReference<CalibrationDistanceFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final CalibrationDistanceFragment fragment = mFragment.get();
            switch (msg.what) {

                default :
                    break;
            }
        }
    }

    @Override
    public void onZoomStarted(float var1, float var2, float var3) {
    }

    @Override
    public void onZooming(float var1, float var2, float var3) {
    }

    @Override
    public void onZoomEnded(float var1, float var2, float var3) {
    }

    @Override
    public void onNotifyDisplayInfo(float var1, float var2, float var3) {
        mPicDisplayStartX = var2;
        mPicDisplayTopY = var3;
        drawPointAndLineByZoom(var1, var2, var3);
    }
    /* workaround code : immersive mode not supported device (soft-key model) */
    private int mPrevCalibrationLayoutWidth = 0;
    private ViewTreeObserver.OnGlobalLayoutListener mEndImageGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            if (mLayoutCalibration != null && mPrevCalibrationLayoutWidth != mLayoutCalibration.getWidth()) {
                final float zoom = mLayoutCalibration.getZoom();
                final int displayWidth = mLayoutCalibration.getWidth();
                final int displayHeight = mLayoutCalibration.getHeight();

                if (zoom > Constants.Zoom.LEVEL1) {
                    mLayoutCalibration.zoomTo(zoom, displayWidth / 2f, displayHeight / 2f);
                }

                CalibrationData preData =  mSession.getPreCalibrationData();

                Point displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                        zoom, preData.getFarLeftX(), preData.getFarY(),mPicDisplayStartX, mPicDisplayTopY);
                setPointXY(mPointViewArray[P_FAR_LEFT], displayPoint.x, displayPoint.y);

                displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                        zoom, preData.getFarRightX(), preData.getFarY(), mPicDisplayStartX, mPicDisplayTopY);
                setPointXY(mPointViewArray[P_FAR_RIGHT], displayPoint.x, displayPoint.y);

                displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                        zoom, preData.getNearLeftX(), preData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                setPointXY(mPointViewArray[P_NEAR_LEFT], displayPoint.x, displayPoint.y);

                displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                        zoom, preData.getNearRightX(), preData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                setPointXY(mPointViewArray[P_NEAR_RIGHT], displayPoint.x, displayPoint.y);

                displayPoint = DisplayUtil.getTransPicToDisplayPos(mLayoutCalibration.getWidth(),
                        mLayoutCalibration.getHeight(), mLayoutCalibration.getZoom(),
                        preData.getCenterX(), preData.getVanishingY(), mPicDisplayStartX, mPicDisplayTopY);

                setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);

                mCalibrationLineDrawView.drawLine();
                mPrevCalibrationLayoutWidth = displayWidth;
            }
        }
    };

    private void refreshDebugView(CalibrationData data) {
        if (mDebugLayout.getVisibility() == View.VISIBLE) {
            mDebugTextView.setText(getString(R.string.debug_calibration_distance,
                    data.getFarY(), data.getFarLeftX(), data.getFarRightX(),
                    data.getNearY(), data.getNearLeftX(), data.getNearRightX()));
        }
    }
    private boolean checkValidateVanishingPoint() {
        boolean valid = false;
        float coefficient = 0.035f;
        double validDistance = (double) (Math.round(coefficient * DisplayUtil.CALIBRATION_PIC_HEIGHT));
        int width, height;
        double distance;

        Point predictionVanishingPoint = DisplayUtil.getTransDisplayToPicPos(mLayoutCalibration.getWidth(),
                mLayoutCalibration.getHeight(), mLayoutCalibration.getZoom(),
                getPointX(mPointViewArray[P_VANISHING]), getPointY(mPointViewArray[P_VANISHING]),
                mPicDisplayStartX, mPicDisplayTopY);

        CalibrationData preData = mSession.getPreCalibrationData();
        width = Math.abs(preData.getCenterX() - predictionVanishingPoint.x);
        height = Math.abs(preData.getVanishingY() - predictionVanishingPoint.y);

        distance = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));

        if (distance <= validDistance) {
            valid = true;
        }
        return valid;
    }
}

