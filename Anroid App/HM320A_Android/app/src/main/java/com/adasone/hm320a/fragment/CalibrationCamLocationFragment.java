package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.DateTimeUtil;
import com.adasone.hm320a.util.DisplayUtil;
import com.adasone.hm320a.util.FileUtil;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class CalibrationCamLocationFragment extends Fragment {
    private static final String TAG = CalibrationCamLocationFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = CalibrationCamLocationFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private ImageView mCalibrationPreImageView;

    private EditText mCameraHeightEditText;
    private EditText mVehicleWidthEditText;
    private EditText mCameraToBumperEditText;

    private PopupWindow mHelpPopupWindow = null;
    private View mPopupView;

    public CalibrationCamLocationFragment() {
        // Required empty public constructor
    }

    public static CalibrationCamLocationFragment newInstance() {
        return new CalibrationCamLocationFragment();
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
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            Log.e(TAG, "getActivity() is null !!");
        }
        mSession = mListener.getSession();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_calibration_cam_location, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);
        //titleTextView.setOnLongClickListener(mOnOnLongClickListener);

        mCalibrationPreImageView = (ImageView) root.findViewById(R.id.iv_calibration_preview);

        TextView dateCaptionTextView = (TextView) root.findViewById(R.id.tv_calibration_date_caption);
        TextView dateValueTextView = (TextView) root.findViewById(R.id.tv_calibration_date_value);

        TextView cameraHeightTextView = (TextView) root.findViewById(R.id.tv_camera_height);
        ImageView cameraHeightHelpImageView = (ImageView) root.findViewById(R.id.iv_camera_height_help);
        cameraHeightHelpImageView.setOnClickListener(mOnClickListener);
        mCameraHeightEditText = (EditText) root.findViewById(R.id.edit_camera_height);
        TextView unit1TextView = (TextView) root.findViewById(R.id.tv_unit1);

        TextView vehicleWidthTextView = (TextView) root.findViewById(R.id.tv_vehicle_width);
        ImageView vehicleWidthHelpImageView = (ImageView) root.findViewById(R.id.iv_vehicle_width_help);
        vehicleWidthHelpImageView.setOnClickListener(mOnClickListener);
        mVehicleWidthEditText = (EditText) root.findViewById(R.id.edit_vehicle_width);
        TextView unit2TextView = (TextView) root.findViewById(R.id.tv_unit2);

        TextView cameraToBumperTextView = (TextView) root.findViewById(R.id.tv_camera_to_bumper);
        ImageView cameraToBumperHelpImageView = (ImageView) root.findViewById(R.id.iv_camera_to_bumper_help);
        cameraToBumperHelpImageView.setOnClickListener(mOnClickListener);
        mCameraToBumperEditText = (EditText) root.findViewById(R.id.edit_camera_to_bumper);
        TextView unit3TextView = (TextView) root.findViewById(R.id.tv_unit3);

        Button nextButton = (Button) root.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(mOnClickListener);

        mCameraHeightEditText.setNextFocusDownId(R.id.edit_vehicle_width);
        mVehicleWidthEditText.setNextFocusDownId(R.id.edit_camera_to_bumper);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic700(dateCaptionTextView, cameraHeightTextView,
                vehicleWidthTextView, cameraToBumperTextView);
        AppApplication.getAppApplication().setFontHYNGothicM(dateValueTextView,
                mCameraHeightEditText, unit1TextView, mVehicleWidthEditText, unit2TextView,
                mCameraToBumperEditText, unit3TextView);
        AppApplication.getAppApplication().setFontHYNSupungB(nextButton);

        CalibrationData userData = mSession.getDevCalibrationData();
        CalibrationData preData = mSession.getPreCalibrationData();

        if (FileUtil.isExistCalibrationNormalPicture(mSession.getDeviceToken())) {
            preData.setBackgroundBitmap(
                    DisplayUtil.getBitmapCalibrationFile(
                            mSession.getDeviceToken(),
                            FileUtil.getCalibrationNormalFileStorePath(mSession.getDeviceToken())));
        } else if (FileUtil.isExistCalibrationFirstAutoPicture(mSession.getDeviceToken())) {
            preData.setBackgroundBitmap(
                    DisplayUtil.getBitmapCalibrationFile(
                            mSession.getDeviceToken(),
                            FileUtil.getCalibrationFirstAutoFileStorePath(mSession.getDeviceToken())));
        }

        if (preData.getBackgroundBitmap() != null) {
            mCalibrationPreImageView.setImageDrawable(
                    new BitmapDrawable(getResources(), preData.getBackgroundBitmap()));
        }

        if ("".equals(userData.getDate())) {
            preData.setDate(DateTimeUtil.getCurrDateToCalibrationFormat(getContext()));
        } else {
            preData.setDate(userData.getDate());
            mCameraHeightEditText.setText(String.valueOf(userData.getCameraHeight()));
            mVehicleWidthEditText.setText(String.valueOf(userData.getVehicleWidth()));
            mCameraToBumperEditText.setText(String.valueOf(userData.getCameraToBumper()));
        }
        mCameraHeightEditText.setSelectAllOnFocus(true);
        mVehicleWidthEditText.setSelectAllOnFocus(true);
        mCameraToBumperEditText.setSelectAllOnFocus(true);
        dateValueTextView.setText(DateTimeUtil.getDateToDefaultUIFormat(getContext(), preData.getDate()));

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
            if (mHelpPopupWindow != null && mHelpPopupWindow.isShowing()) {
                mHelpPopupWindow.dismiss();
                return false;
            } else {
                return true;
            }
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
                        mCalibrationPreImageView.setImageDrawable(
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
                case R.id.iv_menu :
                    if (mListener != null) {
                        mListener.onRequestMainMenu(false);
                    }
                    break;
                case R.id.iv_back :
                    getActivity().onBackPressed();
                    break;
                case R.id.iv_camera_height_help:
                    // fall-through
                case R.id.iv_vehicle_width_help:
                    // fall-through
                case R.id.iv_camera_to_bumper_help:
                    showHelpPopupWindow(view);
                    break;
                case R.id.btn_next :
                    if (isValidData()) {
                        try {
                            CalibrationData data = mSession.getPreCalibrationData();
                            data.setCameraHeight(Integer.parseInt(mCameraHeightEditText.getText().toString().trim()));
                            data.setVehicleWidth(Integer.parseInt(mVehicleWidthEditText.getText().toString().trim()));
                            data.setCameraToBumper(Integer.parseInt(mCameraToBumperEditText.getText().toString().trim()));
                            mListener.onMenuSelected(Constants.Menu.CALIBRATION_BONNET, 0);
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), R.string.error_wrong_format, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public View.OnLongClickListener mOnOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            boolean ret = true;
            switch (v.getId()) {
                case R.id.tv_title :
                    if (FileUtil.isExistCalibrationFirstAutoPicture(mSession.getDeviceToken())
                            && FileUtil.isExistCalibrationSecondAutoPicture(mSession.getDeviceToken())) {
                        if (isValidData()) {
                            try {
                                CalibrationData data = mSession.getPreCalibrationData();
                                data.setCameraHeight(Integer.parseInt(mCameraHeightEditText.getText().toString().trim()));
                                data.setVehicleWidth(Integer.parseInt(mVehicleWidthEditText.getText().toString().trim()));
                                data.setCameraToBumper(Integer.parseInt(mCameraToBumperEditText.getText().toString().trim()));
                                mListener.onMenuSelected(Constants.Menu.AUTO_CALIBRATION_CHESS, 0);
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), R.string.error_wrong_format, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.calibration_auto_img_not_exit, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
        }
    };


    public void showHelpPopupWindow(View view) {
        if (mHelpPopupWindow == null) {
            mPopupView = getActivity().getLayoutInflater().inflate(R.layout.popup_window_calibration_help, null);
            mHelpPopupWindow = new PopupWindow(mPopupView,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
        }

        LinearLayout helpContentsLayout = (LinearLayout) mPopupView.findViewById(R.id.layout_help_contents);
        TextView helpTitleTextView = (TextView) mPopupView.findViewById(R.id.tv_calibration_help_popup_title);
        TextView helpDescTextView = (TextView) mPopupView.findViewById(R.id.tv_calibration_help_popup_desc);
        Button okButton = (Button) mPopupView.findViewById(R.id.btn_calibration_help_ok);

        AppApplication.getAppApplication().setFontHYGothic900(helpTitleTextView);
        AppApplication.getAppApplication().setFontHYGothic500(helpDescTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(okButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelpPopupWindow.dismiss();
            }
        });

        switch (view.getId()) {
            case R.id.iv_camera_height_help:
                helpContentsLayout.setBackgroundResource(R.drawable.cali_popup_bg1);
                helpTitleTextView.setText(R.string.camera_height_help_title);
                helpDescTextView.setText(R.string.camera_height_help_desc);
                break;
            case R.id.iv_vehicle_width_help:
                helpContentsLayout.setBackgroundResource(R.drawable.cali_popup_bg2);
                helpTitleTextView.setText(R.string.vehicle_width_help_title);
                helpDescTextView.setText(R.string.vehicle_width_help_desc);
                break;
            case R.id.iv_camera_to_bumper_help:
                helpContentsLayout.setBackgroundResource(R.drawable.cali_popup_bg3);
                helpTitleTextView.setText(R.string.camera_to_bumper_help_title);
                helpDescTextView.setText(R.string.camera_to_bumper_help_desc);
                break;
        }
        mHelpPopupWindow.showAtLocation(mPopupView, Gravity.CENTER, 0, 0);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<CalibrationCamLocationFragment> mFragment;
        private MyHandler(CalibrationCamLocationFragment fragment) {
            mFragment = new WeakReference<CalibrationCamLocationFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final CalibrationCamLocationFragment fragment = mFragment.get();
            switch (msg.what) {

                default :
                    break;
            }
        }
    }

    private boolean isValidData() {
        boolean ret = true;
        int strResId = -1;

        if (mCameraHeightEditText.getText().length() == 0
                || "0".equals(mCameraHeightEditText.getText().toString())) {
            strResId = R.string.enter_camera_height;
            mCameraHeightEditText.requestFocus();
        } else if (mVehicleWidthEditText.getText().length() == 0
                || "0".equals(mVehicleWidthEditText.getText().toString())) {
            strResId = R.string.enter_vehicle_width;
            mVehicleWidthEditText.requestFocus();
        } else if (mCameraToBumperEditText.getText().length() == 0) {
            strResId = R.string.enter_camera_to_bumper;
            mCameraToBumperEditText.requestFocus();
        }
        if (strResId != -1) {
            ret = false;
            Toast.makeText(getContext(), strResId, Toast.LENGTH_SHORT).show();
        }

        return ret;
    }
}
