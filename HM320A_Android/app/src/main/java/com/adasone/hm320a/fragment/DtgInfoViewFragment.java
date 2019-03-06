package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.DriverData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.VehicleTypeUtil;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class DtgInfoViewFragment extends Fragment {
    private static final String TAG = DtgInfoViewFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = DtgInfoViewFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private TextView mVehicleTypeValueTextView;
    private TextView mVinCodeValueTextView;
    private TextView mVehicleNumberValueTextView;
    private TextView mBusinessNumberValueTextView;
    private TextView mDriverCodeValueTextView;

    public DtgInfoViewFragment() {
        // Required empty public constructor
    }

    public static DtgInfoViewFragment newInstance() {
        return new DtgInfoViewFragment();
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
        View root = inflater.inflate(R.layout.fragment_dtg_info_view, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        TextView titleDriverInfoTextView = (TextView) root.findViewById(R.id.tv_title_driver_info);

        TextView vehicleTypeCaptionTextView = (TextView) root.findViewById(R.id.tv_vehicle_type_caption);
        TextView vinCodeCaptionTextView = (TextView) root.findViewById(R.id.tv_vin_code_caption);
        TextView vehicleNumberCaptionTextView = (TextView) root.findViewById(R.id.tv_vehicle_number_caption);
        TextView businessNumberCaptionTextView = (TextView) root.findViewById(R.id.tv_business_number_caption);
        TextView driverCodeCaptionTextView = (TextView) root.findViewById(R.id.tv_driver_code_caption);

        mVehicleTypeValueTextView = (TextView) root.findViewById(R.id.tv_vehicle_type_value);
        mVinCodeValueTextView = (TextView) root.findViewById(R.id.tv_vin_code_value);
        mVehicleNumberValueTextView = (TextView) root.findViewById(R.id.tv_vehicle_number_value);
        mBusinessNumberValueTextView = (TextView) root.findViewById(R.id.tv_business_number_value);
        mDriverCodeValueTextView = (TextView) root.findViewById(R.id.tv_driver_code_value);

        ImageView editDriverImageView = (ImageView) root.findViewById(R.id.iv_edit_driver);
        editDriverImageView.setOnClickListener(mOnClickListener);

        Button mSendButton = (Button) root.findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(mOnClickListener);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic800(titleDriverInfoTextView);
        AppApplication.getAppApplication().setFontHYGothic700(vehicleTypeCaptionTextView,
                vinCodeCaptionTextView, vehicleNumberCaptionTextView, businessNumberCaptionTextView,
                driverCodeCaptionTextView, mVehicleTypeValueTextView, mVinCodeValueTextView,
                mVehicleNumberValueTextView, mBusinessNumberValueTextView, mDriverCodeValueTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(mSendButton);

        mSession.getPreDriverData().copyFrom(mSession.getDevDriverData());

        setDriverInfo(mSession.getPreDriverData());
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
                case Constants.NotifyMsg.CHANGE_DTG_DRIVER_INFO :
                    setDriverInfo(mSession.getPreDriverData());
                    break;
                default :
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
                    mListener.onRequestMainMenu(false);
                    break;
                case R.id.iv_back :
                    getActivity().onBackPressed();
                    break;
                case R.id.iv_edit_driver :
                    mListener.onMenuSelected(Constants.Menu.DTG_DRIVER_INFO_EDIT, 0);
                    break;
                case R.id.btn_send :
                    if (!mListener.isUSBConnected()) {
                        Toast.makeText(getContext(), R.string.check_usb_connection, Toast.LENGTH_SHORT).show();
                    } else if (!driverInfoValidationCheck()) {
                        Toast.makeText(getContext(), R.string.enter_driver_info , Toast.LENGTH_SHORT).show();
                    } else {
                        mListener.reqWriteDTGDriverInfo();
                        getActivity().onBackPressed();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private static class MyHandler extends Handler {
        private final WeakReference<DtgInfoViewFragment> mFragment;
        private MyHandler(DtgInfoViewFragment fragment) {
            mFragment = new WeakReference<DtgInfoViewFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final DtgInfoViewFragment fragment = mFragment.get();
            if (fragment == null) {
                Log.e(TAG, "fragment is null");
                return;
            }
            switch (msg.what) {

                default :
                    break;
            }
        }
    }
    private void setDriverInfo(DriverData driverData) {
        if (driverData != null) {
            try {
                mVehicleTypeValueTextView.setText(VehicleTypeUtil.getVehicleTypeStringResId(driverData.getVehicleType()));
                mVinCodeValueTextView.setText(driverData.getVIN());
                mVehicleNumberValueTextView.setText(driverData.getVehicleRegNo());
                mBusinessNumberValueTextView.setText(driverData.getBusinessRegNo());
                mDriverCodeValueTextView.setText(driverData.getDriverCode());
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean driverInfoValidationCheck() {
        boolean ret = true;

        if (getString(R.string.symbol_dash).equals(mVehicleTypeValueTextView.getText().toString())
                || !DriverData.validateVIN(mVinCodeValueTextView.getText().toString())
                || !DriverData.validateVehicleRegNumber(mVehicleNumberValueTextView.getText().toString())
                || !DriverData.validateBusinessRegNumber(mBusinessNumberValueTextView.getText().toString())
                || !DriverData.validateDriverCode(mDriverCodeValueTextView.getText().toString())
                ) {
            ret = false;
        }
        return ret;
    }
}
