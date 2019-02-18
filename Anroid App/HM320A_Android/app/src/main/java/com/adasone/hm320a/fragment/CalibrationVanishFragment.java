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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.DisplayUtil;
import com.adasone.hm320a.util.RepeatListener;
import com.adasone.hm320a.view.ZoomView;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalibrationVanishFragment extends Fragment implements ZoomView.ZoomViewListener{
    private static final String TAG = CalibrationVanishFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = CalibrationVanishFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private LinearLayout mDebugLayout;
    private TextView mDebugTextView;

    private ImageView mCalibrationBackgroundView;

    private ZoomView mLayoutCalibration;

    private ImageView mVanishingPointView;
    private View mDotHorizontalLeftVanishingLine;
    private View mDotHorizontalRightVanishingLine;
    private View mDotVerticalTopCenterLine;
    private View mDotVerticalBottomCenterLine;

    private View mMiddleGuideLine;
    private int mMiddleGuideLinePicY = DisplayUtil.CALIBRATION_PIC_HEIGHT / 3;

    private static final float DISPLAY_START_X = 0f;
    private static final float DISPLAY_START_Y = 0f;

    private float mPicDisplayStartX = 0f;
    private float mPicDisplayTopY = 0f;

    public CalibrationVanishFragment() {
        // Required empty public constructor
    }

    public static CalibrationVanishFragment newInstance() {
        return new CalibrationVanishFragment();
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
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            Log.e(TAG, "getActivity() is null !!");
        }
        mSession = mListener.getSession();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_calibration_vanish, container, false);

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

        Button titleButton = (Button) root.findViewById(R.id.btn_title);

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
        if (Boolean.TRUE /*AppApplication.isDebug()*/) {
            mDebugLayout.setVisibility(View.VISIBLE);
        }

        mVanishingPointView = (ImageView) root.findViewById(R.id.iv_vanishing_point);
        mVanishingPointView.setOnTouchListener(mPointOnTouchListener);

        mDotHorizontalLeftVanishingLine = (View) root.findViewById(R.id.view_dot_horizontal_left_line);
        mDotHorizontalRightVanishingLine = (View) root.findViewById(R.id.view_dot_horizontal_right_line);
        mDotVerticalTopCenterLine = (View) root.findViewById(R.id.view_dot_vertical_top_line);
        mDotVerticalBottomCenterLine = (View) root.findViewById(R.id.view_dot_vertical_bottom_line);

        mMiddleGuideLine = (View) root.findViewById(R.id.view_middle_guide_line);

        mVanishingPointView.post(new Runnable() {
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

                if (!"".equals(userData.getDate())) {
                    int centerX = userData.getCenterX();
                    int vanishingY = userData.getVanishingY();

                    if (centerX != 0 && vanishingY != 0) {
                        Point displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                                mLayoutCalibration.getZoom(), centerX, vanishingY,
                                mPicDisplayStartX, mPicDisplayTopY);
                        setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);
                    }
                }

                drawDotLine();

                Point point = DisplayUtil.getTransDisplayToPicPos(displayWidth, displayHeight,
                        mLayoutCalibration.getZoom(),
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

        AppApplication.getAppApplication().setFontHYGothic900(titleButton);
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
                        mListener.onMenuSelected(Constants.Menu.CALIBRATION_DISTANCE, 0);
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

    private boolean mPointSelected = false;
    private int mIgnoreActionMoveCount = 0;
    private static final int IGNORE_ACTION_MOVE_MAX_COUNT = 3;
    public View.OnTouchListener mPointOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    mPointSelected = v.getId() == R.id.iv_vanishing_point;
                    break;
                case MotionEvent.ACTION_UP :
                    mPointSelected = false;
                    mIgnoreActionMoveCount = 0;
                    break;
                case MotionEvent.ACTION_MOVE :
                    if (mPointSelected) {
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
            if (mVanishingPointView != null) {
                float touchLawX = mVanishingPointView.getX() + (mVanishingPointView.getWidth() / 2f);
                float touchLawY = mVanishingPointView.getY() + (mVanishingPointView.getHeight() / 2f);
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
                drawPointAndLineByTouch(mVanishingPointView, touchLawX, touchLawY);
            }
        }
    });

    private void drawPointAndLineByTouch(View view, float touchLawX, float touchLawY) {
        float pointImageViewStartX = touchLawX - view.getWidth() / 2f;
        float pointImageViewStartY = touchLawY - view.getHeight() / 2f;
        float topLimit = DISPLAY_START_Y + 5.0f;
        float bottomLimit = mLayoutCalibration.getHeight() - 5.0f;
        float startLimit = DISPLAY_START_X + 5.0f;
        float endLimit = mLayoutCalibration.getWidth() - 5.0f;

        if (touchLawX < startLimit) {
            pointImageViewStartX = startLimit - view.getWidth() / 2f;
        } else if (touchLawX > endLimit) {
            pointImageViewStartX = endLimit - view.getWidth() / 2f;
        }

        if (touchLawY < topLimit) {
            pointImageViewStartY = topLimit - view.getHeight() / 2f;
        } else if (touchLawY > bottomLimit) {
            pointImageViewStartY = bottomLimit - view.getHeight() / 2f;
        }

        view.setX(pointImageViewStartX);
        view.setY(pointImageViewStartY);

        drawDotLine();

        Point point = DisplayUtil.getTransDisplayToPicPos(mLayoutCalibration.getWidth(),
                mLayoutCalibration.getHeight(), mLayoutCalibration.getZoom(),
                getPointX(mVanishingPointView), getPointY(mVanishingPointView),
                mPicDisplayStartX, mPicDisplayTopY);
        CalibrationData preData = mSession.getPreCalibrationData();
        preData.setCenterX(point.x);
        preData.setVanishingY(point.y);
        refreshDebugView(preData);
    }

    private void drawPointAndLineByZoom(float zoom, float picDisplayStartX, float picDisplayTopY) {
        CalibrationData preData = mSession.getPreCalibrationData();

        Point displayPoint = DisplayUtil.getTransPicToDisplayPos(mLayoutCalibration.getWidth(),
                mLayoutCalibration.getHeight(), zoom, preData.getCenterX(), preData.getVanishingY(),
                picDisplayStartX, picDisplayTopY);
        setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);

        drawDotLine();
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
        private final WeakReference<CalibrationVanishFragment> mFragment;
        private MyHandler(CalibrationVanishFragment fragment) {
            mFragment = new WeakReference<CalibrationVanishFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final CalibrationVanishFragment fragment = mFragment.get();
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
                    mLayoutCalibration.zoomTo(zoom,
                            mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                }

                CalibrationData preData =  mSession.getPreCalibrationData();

                Point displayPoint = DisplayUtil.getTransPicToDisplayPos(mLayoutCalibration.getWidth(),
                        mLayoutCalibration.getHeight(), mLayoutCalibration.getZoom(),
                        preData.getCenterX(), preData.getVanishingY(), mPicDisplayStartX, mPicDisplayTopY);

                setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);
                drawDotLine();
                mPrevCalibrationLayoutWidth = mLayoutCalibration.getWidth();
            }
        }
    };

    private void drawDotLine() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int GAP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.00f, displayMetrics);
        final int vanishingPointImageViewStartX = (int) mVanishingPointView.getX();
        final int vanishingPointImageViewStartY = (int) mVanishingPointView.getY();
        final int vanishingPointImageViewWidth = (int) mVanishingPointView.getWidth();
        final int vanishingPointImageViewHeight = (int) mVanishingPointView.getHeight();

        float dotHorizontalVanishingLineY = vanishingPointImageViewStartY +
                (vanishingPointImageViewHeight / 2) - (mDotHorizontalLeftVanishingLine.getHeight() / 2);
        float dotVerticalCenterLineX = vanishingPointImageViewStartX +
                (vanishingPointImageViewWidth / 2) - (mDotVerticalTopCenterLine.getWidth() / 2);

        mDotHorizontalLeftVanishingLine.setY(dotHorizontalVanishingLineY);
        mDotHorizontalLeftVanishingLine.setX(0.0f);
        mDotHorizontalLeftVanishingLine.getLayoutParams().width =
                (int)(vanishingPointImageViewStartX - mDotHorizontalLeftVanishingLine.getX() - GAP);
        mDotHorizontalLeftVanishingLine.requestLayout();

        mDotHorizontalRightVanishingLine.setY(dotHorizontalVanishingLineY);
        if (vanishingPointImageViewStartX + vanishingPointImageViewWidth + GAP > 0) {
            mDotHorizontalRightVanishingLine.setX(vanishingPointImageViewStartX + vanishingPointImageViewWidth + GAP);
            mDotHorizontalRightVanishingLine.getLayoutParams().width =
                    (int)(mLayoutCalibration.getWidth() - mDotHorizontalRightVanishingLine.getX() + GAP);
        } else {
            mDotHorizontalRightVanishingLine.setX(0.0f);
            mDotHorizontalRightVanishingLine.getLayoutParams().width = mLayoutCalibration.getWidth();
        }
        mDotHorizontalRightVanishingLine.requestLayout();

        mDotVerticalTopCenterLine.setX(dotVerticalCenterLineX);
        mDotVerticalTopCenterLine.setY(0.0f);
        mDotVerticalTopCenterLine.getLayoutParams().height =
                (int)(vanishingPointImageViewStartY - mDotVerticalTopCenterLine.getY() - GAP);
        mDotVerticalTopCenterLine.requestLayout();

        mDotVerticalBottomCenterLine.setX(dotVerticalCenterLineX);
        mDotVerticalBottomCenterLine.setY(vanishingPointImageViewStartY + vanishingPointImageViewHeight + GAP);
        mDotVerticalBottomCenterLine.getLayoutParams().height =
                (int)(mLayoutCalibration.getHeight() - vanishingPointImageViewStartY - vanishingPointImageViewHeight - GAP);

        if (vanishingPointImageViewStartY + vanishingPointImageViewHeight + GAP > 0) {
            mDotVerticalBottomCenterLine.setY(vanishingPointImageViewStartY + vanishingPointImageViewHeight + GAP);
            mDotVerticalBottomCenterLine.getLayoutParams().height =
                    (int)(mLayoutCalibration.getHeight() - vanishingPointImageViewStartY - vanishingPointImageViewHeight - GAP);
        } else {
            mDotVerticalBottomCenterLine.setY(0.0f);
            mDotVerticalBottomCenterLine.getLayoutParams().height = mLayoutCalibration.getHeight();
        }
        mDotVerticalBottomCenterLine.requestLayout();

        mMiddleGuideLine.setY(DisplayUtil.getTransPicToDisplayPos(mLayoutCalibration.getWidth(), mLayoutCalibration.getHeight(), mLayoutCalibration.getZoom(),
                0, mMiddleGuideLinePicY, mPicDisplayStartX, mPicDisplayTopY).y);
    }

    private void refreshDebugView(CalibrationData data) {
        if (mDebugLayout.getVisibility() == View.VISIBLE) {
            mDebugTextView.setText(getString(R.string.debug_calibration_vanishing,
                    data.getCenterX(), data.getVanishingY()));
        }
    }
}
