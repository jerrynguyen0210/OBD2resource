package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.AutoCalibrationResultData;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.BlurUtil;
import com.adasone.hm320a.util.DateTimeUtil;
import com.adasone.hm320a.util.DisplayUtil;
import com.adasone.hm320a.util.FileUtil;
import com.adasone.hm320a.view.CalibrationLineDrawView;
import com.adasone.hm320a.view.ZoomView;
import com.adasone.hm320a.vpart.CalibrationAutoDetector;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoCalibrationTotalFragment extends Fragment implements ZoomView.ZoomViewListener{
    private static final String TAG = AutoCalibrationTotalFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = AutoCalibrationTotalFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private FrameLayout mRootLayout;

    private LinearLayout mDebugLayout;
    private TextView mDebugTextView;

    private LinearLayout mProgressLayout;

    private ImageView mCalibrationBackgroundView;

    private ZoomView mLayoutCalibration;
    private CalibrationLineDrawView mCalibrationLineDrawView;

    private boolean mMeasureDone = false;
    private boolean mFindChessCorner = false;
    private AsyncTask<Void, Void, Void> mMeasureAsyncTask = null;

    private static final int P_FAR_LEFT = 0;
    private static final int P_FAR_RIGHT = 1;
    private static final int P_NEAR_LEFT = 2;
    private static final int P_NEAR_RIGHT = 3;
    private static final int P_MAX = 4;

    private View mBonnetLine;
    private ImageView mVanishingPointView;
    private ImageView[] mPointViewArray = new ImageView[P_MAX];

    private float mPicDisplayStartX = 0f;
    private float mPicDisplayTopY = 0f;

    private CalibrationAutoDetector mCalibrationAutoDetector = null;

    public AutoCalibrationTotalFragment() {
        // Required empty public constructor
    }

    public static AutoCalibrationTotalFragment newInstance() {
        return new AutoCalibrationTotalFragment();
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
        View root= inflater.inflate(R.layout.fragment_auto_calibration_total, container, false);

        mRootLayout = (FrameLayout) root.findViewById(R.id.layout_root);

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

        Button titleButton = (Button) root.findViewById(R.id.btn_title);

        ImageButton okButton = (ImageButton) root.findViewById(R.id.btn_ok);
        ImageButton cancelButton = (ImageButton) root.findViewById(R.id.btn_cancel);
        okButton.setOnClickListener(mOnClickListener);
        cancelButton.setOnClickListener(mOnClickListener);

        Button zoomInButton = (Button) root.findViewById(R.id.btn_zoom_in);
        Button zoomOutButton = (Button) root.findViewById(R.id.btn_zoom_out);
        Button measureButton = (Button) root.findViewById(R.id.btn_measure);
        zoomInButton.setOnClickListener(mOnClickListener);
        zoomOutButton.setOnClickListener(mOnClickListener);
        measureButton.setOnClickListener(mOnClickListener);

        mBonnetLine = (View) root.findViewById(R.id.view_dot_horizontal_bonnet_line);
        mVanishingPointView = (ImageView) root.findViewById(R.id.iv_vanishing_point);

        mDebugLayout = (LinearLayout) root.findViewById(R.id.layout_debug);
        mDebugTextView = (TextView) root.findViewById(R.id.tv_debug);
        if (Boolean.TRUE/*AppApplication.isDebug()*/) {
            mDebugLayout.setVisibility(View.VISIBLE);
        }
        mProgressLayout = (LinearLayout) root.findViewById(R.id.layout_progress);
        mProgressLayout.setVisibility(View.GONE);

        mPointViewArray[P_FAR_LEFT] = (ImageView) root.findViewById(R.id.iv_point_far_left);
        mPointViewArray[P_FAR_RIGHT] = (ImageView) root.findViewById(R.id.iv_point_far_right);
        mPointViewArray[P_NEAR_LEFT] = (ImageView) root.findViewById(R.id.iv_point_near_left);
        mPointViewArray[P_NEAR_RIGHT] = (ImageView) root.findViewById(R.id.iv_point_near_right);

        mPointViewArray[P_NEAR_RIGHT].post(new Runnable() {
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
                CalibrationData preData = mSession.getPreCalibrationData();
                Point displayPoint;

                if (preData.getBonnetPoint().y != DisplayUtil.CALIBRATION_NO_BONNET) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                            preData.getBonnetPoint().x, preData.getBonnetPoint().y,
                            mPicDisplayStartX, mPicDisplayTopY);
                    setPointY(mBonnetLine, displayPoint.y);
                } else {
                    mBonnetLine.setVisibility(View.GONE);
                }

                preData.setCenterX(0);
                preData.setVanishingY(0);
                preData.setNearY(0);
                preData.setNearLeftX(0);
                preData.setNearRightX(0);
                preData.setFarY(0);
                preData.setFarLeftX(0);
                preData.setFarRightX(0);

                mCalibrationLineDrawView.showVanishingPoint(false);
                mCalibrationLineDrawView.setViewArray(mPointViewArray);
                refreshDebugView(preData);
                mLayoutCalibration.getViewTreeObserver().addOnGlobalLayoutListener(mEndImageGlobalLayoutListener);
            }
        });

        AppApplication.getAppApplication().setFontHYGothic900(titleButton);
        AppApplication.getAppApplication().setFontHYGothic600(zoomInButton, zoomOutButton, measureButton, mDebugTextView);

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
        if (mMeasureAsyncTask != null
                && mMeasureAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mMeasureAsyncTask.cancel(true);
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
                    if (mListener.isUSBConnected()) {
                        if (mMeasureDone) {
                            CalibrationData presetData = mSession.getPreCalibrationData();
                            if (mFindChessCorner && presetData.getVanishingY() > 0) {
                                setCalibrationDateToNowDate();
                                mListener.reqWriteCalibrationInfo();
                                mListener.onRequestMainMenu(false);
                            } else {
                                Toast.makeText(getContext(), R.string.measured_value_incorrect, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.measurement_not_complete, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.check_usb_connection, Toast.LENGTH_SHORT).show();
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
                    }  else if (mLayoutCalibration.getZoom() <= Constants.Zoom.LEVEL4) {
                        mLayoutCalibration.zoomTo(Constants.Zoom.LEVEL3,
                                mLayoutCalibration.getWidth() / 2f, mLayoutCalibration.getHeight() / 2f);
                    }
                    break;
                case R.id.btn_measure:
                    if (FileUtil.isExistCalibrationFirstAutoPicture(mSession.getDeviceToken())
                            && FileUtil.isExistCalibrationSecondAutoPicture(mSession.getDeviceToken())) {

                        if (mMeasureAsyncTask != null
                                && mMeasureAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                            mMeasureAsyncTask.cancel(true);
                        }
                        if (mCalibrationAutoDetector == null) {
                            mCalibrationAutoDetector = new CalibrationAutoDetector();
                        }

                        mMeasureAsyncTask = new AutoCalibrationTask(mCalibrationAutoDetector,
                                mSession.getPreCalibrationData(),
                                FileUtil.getCalibrationFirstAutoFileStorePath(mSession.getDeviceToken()),
                                FileUtil.getCalibrationSecondAutoFileStorePath(mSession.getDeviceToken()));
                        mMeasureAsyncTask.execute();
                    } else {
                        Toast.makeText(getContext(), R.string.calibration_auto_img_not_exit, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void drawPointAndLineByZoom(float zoom, float picDisplayStartX, float picDisplayTopY) {
        CalibrationData preData = mSession.getPreCalibrationData();
        final int displayWidth = mLayoutCalibration.getWidth();
        final int displayHeight = mLayoutCalibration.getHeight();
        Point displayPoint;
        if (mPointViewArray[P_FAR_LEFT].getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    preData.getFarLeftX(), preData.getFarY(), picDisplayStartX, picDisplayTopY);
            setPointXY(mPointViewArray[P_FAR_LEFT], displayPoint.x, displayPoint.y);
        }

        if (mPointViewArray[P_FAR_RIGHT].getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    preData.getFarRightX(), preData.getFarY(), picDisplayStartX, picDisplayTopY);
            setPointXY(mPointViewArray[P_FAR_RIGHT], displayPoint.x, displayPoint.y);
        }

        if (mPointViewArray[P_NEAR_LEFT].getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    preData.getNearLeftX(), preData.getNearY(), picDisplayStartX, picDisplayTopY);
            setPointXY(mPointViewArray[P_NEAR_LEFT], displayPoint.x, displayPoint.y);
        }

        if (mPointViewArray[P_NEAR_RIGHT].getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    preData.getNearRightX(), preData.getNearY(), picDisplayStartX, picDisplayTopY);
            setPointXY(mPointViewArray[P_NEAR_RIGHT], displayPoint.x, displayPoint.y);
        }

        if (mCalibrationLineDrawView.getVisibility() == View.VISIBLE) {
            mCalibrationLineDrawView.drawLine();
        }

        if (mBonnetLine.getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    0, preData.getBonnetPoint().y, picDisplayStartX, picDisplayTopY);
            setPointY(mBonnetLine, displayPoint.y);
        }

        if (mVanishingPointView.getVisibility() == View.VISIBLE) {
            displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight, zoom,
                    preData.getCenterX(), preData.getVanishingY(), picDisplayStartX, picDisplayTopY);
            setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);
        }
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

    private void setCalibrationDateToNowDate() {
        CalibrationData presetData = mSession.getPreCalibrationData();
        presetData.setDate(DateTimeUtil.getCurrDateToCalibrationFormat(getContext()));
    }

    private static class MyHandler extends Handler {
        private final WeakReference<AutoCalibrationTotalFragment> mFragment;
        private MyHandler(AutoCalibrationTotalFragment fragment) {
            mFragment = new WeakReference<AutoCalibrationTotalFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final AutoCalibrationTotalFragment fragment = mFragment.get();
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

                CalibrationData preData = mSession.getPreCalibrationData();
                Point displayPoint;

                if (preData.getBonnetPoint().y != DisplayUtil.CALIBRATION_NO_BONNET) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getBonnetPoint().x, preData.getBonnetPoint().y, mPicDisplayStartX, mPicDisplayTopY);
                    setPointY(mBonnetLine, displayPoint.y);
                }

                if (mVanishingPointView.getVisibility() == View.VISIBLE) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getCenterX(), preData.getVanishingY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mVanishingPointView, displayPoint.x, displayPoint.y);
                }

                if (mPointViewArray[P_FAR_LEFT].getVisibility() == View.VISIBLE) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getFarLeftX(), preData.getFarY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_FAR_LEFT], displayPoint.x, displayPoint.y);
                }

                if (mPointViewArray[P_FAR_RIGHT].getVisibility() == View.VISIBLE) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getFarRightX(), preData.getFarY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_FAR_RIGHT], displayPoint.x, displayPoint.y);
                }

                if (mPointViewArray[P_NEAR_LEFT].getVisibility() == View.VISIBLE) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getNearLeftX(), preData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_NEAR_LEFT], displayPoint.x, displayPoint.y);
                }

                if (mPointViewArray[P_NEAR_RIGHT].getVisibility() == View.VISIBLE) {
                    displayPoint = DisplayUtil.getTransPicToDisplayPos(displayWidth, displayHeight,
                            zoom, preData.getNearRightX(), preData.getNearY(), mPicDisplayStartX, mPicDisplayTopY);
                    setPointXY(mPointViewArray[P_NEAR_RIGHT], displayPoint.x, displayPoint.y);
                }

                if (mCalibrationLineDrawView.getVisibility() == View.VISIBLE) {
                    mCalibrationLineDrawView.drawLine();
                }
                mPrevCalibrationLayoutWidth = displayWidth;
            }
        }
    };

    private void refreshDebugView(CalibrationData data) {
        if (mDebugLayout.getVisibility() == View.VISIBLE) {
            if (data.getBonnetPoint().y != DisplayUtil.CALIBRATION_NO_BONNET) {
                mDebugTextView.setText(getString(R.string.debug_calibration_total,
                        data.getBonnetPoint().y, data.getCenterX(), data.getVanishingY(),
                        data.getFarY(), data.getFarLeftX(), data.getFarRightX(),
                        data.getNearY(), data.getNearLeftX(), data.getNearRightX()));
            } else {
                mDebugTextView.setText(getString(R.string.debug_calibration_no_bonnet_line_total,
                        data.getCenterX(), data.getVanishingY(),
                        data.getFarY(), data.getFarLeftX(), data.getFarRightX(),
                        data.getNearY(), data.getNearLeftX(), data.getNearRightX()));
            }
        }
    }

    /**
     * AsyncTask for Auto calibration
     * @Void		Parameter of execute( ), doInBackground( ) method.
     * @Void	    Parameter of onProgressUpdate( ) method.
     * @Void	    Return value of doInBackground( ) method, and Parameter of onPostExecute( ) method.
     */
    private class AutoCalibrationTask extends AsyncTask<Void, Void, Void> {
        private CalibrationAutoDetector mDetector;
        private CalibrationData mData;
        private String mFilePath1;
        private String mFilePath2;

        public AutoCalibrationTask(CalibrationAutoDetector detector, CalibrationData data, String filePath1, String filePath2) {
            this.mDetector = detector;
            this.mData = data;
            this.mFilePath1 = filePath1;
            this.mFilePath2 = filePath2;
        }
        /* Works before doInBackground( ) */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mRootLayout.setDrawingCacheEnabled(true);
            Bitmap bitmap = BlurUtil.fastBlur(getContext(), mRootLayout.getDrawingCache() , 10);
            mRootLayout.setDrawingCacheEnabled(false);
            mProgressLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
            mProgressLayout.setVisibility(View.VISIBLE);
        }

        /* Works should be done in here. */
        @Override
        protected Void doInBackground(Void... params) {
            mDetector.startAutoCalibrationDetect(mFilePath1, mFilePath2,
                    mData.getCameraHeight(), mData.getCameraToBumper(), mData.getVehicleWidth(),  mData.getChessHeight());

            AutoCalibrationResultData resultData = this.mDetector.getCalibrationDetectionResult();
            Log.d(TAG, resultData.toString());

            mData.setCenterX(resultData.getCenterX());
            mData.setVanishingY(resultData.getVanishingY());
            mData.setNearY(resultData.getNearY());
            mData.setNearLeftX(resultData.getNearLeftX());
            mData.setNearRightX(resultData.getNearRightX());
            mData.setFarY(resultData.getFarY());
            mData.setFarLeftX(resultData.getFarLeftX());
            mData.setFarRightX(resultData.getFarRightX());
            mFindChessCorner = (resultData.getFindChess() == 1);
            return null;
        }

        /* Works after doInBackground( ) */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mMeasureDone = true;
            mVanishingPointView.setVisibility(View.VISIBLE);
            mPointViewArray[P_FAR_LEFT].setVisibility(View.VISIBLE);
            mPointViewArray[P_FAR_RIGHT].setVisibility(View.VISIBLE);
            mPointViewArray[P_NEAR_LEFT].setVisibility(View.VISIBLE);
            mPointViewArray[P_NEAR_RIGHT].setVisibility(View.VISIBLE);
            mCalibrationLineDrawView.setVisibility(View.VISIBLE);
            drawPointAndLineByZoom(mLayoutCalibration.getZoom(), mPicDisplayStartX, mPicDisplayTopY);
            refreshDebugView(mData);
            mProgressLayout.setVisibility(View.GONE);
            if (!mFindChessCorner) {
                Toast.makeText(getContext(), R.string.failed_recognize_chessboard, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            mProgressLayout.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressLayout.setVisibility(View.GONE);
        }
    }
}
