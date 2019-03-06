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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoCalibrationChessFragment extends Fragment {
    private static final String TAG = AutoCalibrationChessFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = AutoCalibrationChessFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private ImageView mCalibrationPreImageView;

    private EditText mChessHeightEditText;

    public AutoCalibrationChessFragment() {
        // Required empty public constructor
    }

    public static AutoCalibrationChessFragment newInstance() {
        return new AutoCalibrationChessFragment();
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
        View root= inflater.inflate(R.layout.fragment_auto_calibration_chess, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        mCalibrationPreImageView = (ImageView) root.findViewById(R.id.iv_calibration_preview);

        TextView dateCaptionTextView = (TextView) root.findViewById(R.id.tv_calibration_date_caption);
        TextView dateValueTextView = (TextView) root.findViewById(R.id.tv_calibration_date_value);

        TextView chessHeightTextView = (TextView) root.findViewById(R.id.tv_chess_height);
        mChessHeightEditText = (EditText) root.findViewById(R.id.edit_chess_height);
        TextView unitTextView = (TextView) root.findViewById(R.id.tv_unit);

        Button nextButton = (Button) root.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(mOnClickListener);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic700(dateCaptionTextView, chessHeightTextView);
        AppApplication.getAppApplication().setFontHYNGothicM(dateValueTextView,
                mChessHeightEditText, unitTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(nextButton);

        CalibrationData preData = mSession.getPreCalibrationData();
        if (preData.getBackgroundBitmap() != null) {
            mCalibrationPreImageView.setImageDrawable(
                    new BitmapDrawable(getResources(), preData.getBackgroundBitmap()));
        }

        dateValueTextView.setText(DateTimeUtil.getDateToDefaultUIFormat(getContext(), preData.getDate()));

        if (preData.getChessHeight() != 0) {
            mChessHeightEditText.setText(String.valueOf(preData.getChessHeight()));
        }

        mChessHeightEditText.setSelectAllOnFocus(true);
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
                case R.id.btn_next :
                    if (isValidData()) {
                        try {
                            CalibrationData data = mSession.getPreCalibrationData();
                            data.setChessHeight(Integer.parseInt(mChessHeightEditText.getText().toString().trim()));
                            mListener.onMenuSelected(Constants.Menu.AUTO_CALIBRATION_BONNET, 0);
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

    private static class MyHandler extends Handler {
        private final WeakReference<AutoCalibrationChessFragment> mFragment;
        private MyHandler(AutoCalibrationChessFragment fragment) {
            mFragment = new WeakReference<AutoCalibrationChessFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final AutoCalibrationChessFragment fragment = mFragment.get();
            switch (msg.what) {
                default :
                    break;
            }
        }
    }

    private boolean isValidData() {
        boolean ret = true;
        int strResId = -1;

        if (mChessHeightEditText.getText().length() == 0
                || "0".equals(mChessHeightEditText.getText().toString())) {
            strResId = R.string.enter_chess_height;
            mChessHeightEditText.requestFocus();
        }

        if (strResId != -1) {
            ret = false;
            Toast.makeText(getContext(), strResId, Toast.LENGTH_SHORT).show();
        }

        return ret;
    }
}
