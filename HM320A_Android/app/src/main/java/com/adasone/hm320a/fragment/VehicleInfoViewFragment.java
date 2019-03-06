package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
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
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VehicleData;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleInfoViewFragment extends Fragment {
    private static final String TAG = VehicleInfoViewFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = VehicleInfoViewFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private TextView mManufacturerValueTextView;
    private TextView mModelValueTextView;
    private TextView mFuelValueTextView;
    private TextView mReleaseDateValueTextView;

    public VehicleInfoViewFragment() {
        // Required empty public constructor
    }

    public static VehicleInfoViewFragment newInstance() {
        return new VehicleInfoViewFragment();
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
        View root= inflater.inflate(R.layout.fragment_vehicle_info_view, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        TextView titleVehicleTextView = (TextView) root.findViewById(R.id.tv_title_vehicle);

        TextView manufacturerCaptionTextView = (TextView) root.findViewById(R.id.tv_manufacturer_caption);
        TextView modelCaptionTextView = (TextView) root.findViewById(R.id.tv_model_caption);
        TextView fuelCaptionTextView = (TextView) root.findViewById(R.id.tv_fuel_caption);
        TextView releaseDateCaptionTextView = (TextView) root.findViewById(R.id.tv_release_date_caption);

        mManufacturerValueTextView = (TextView) root.findViewById(R.id.tv_manufacturer_value);
        mModelValueTextView = (TextView) root.findViewById(R.id.tv_model_value);
        mFuelValueTextView = (TextView) root.findViewById(R.id.tv_fuel_value);
        mReleaseDateValueTextView = (TextView) root.findViewById(R.id.tv_release_date_value);

        ImageView editVehicleImageView = (ImageView) root.findViewById(R.id.iv_edit_vehicle);
        editVehicleImageView.setOnClickListener(mOnClickListener);

        Button sendButton = (Button) root.findViewById(R.id.btn_send);
        sendButton.setOnClickListener(mOnClickListener);

        ImageView emailImageView = (ImageView) root.findViewById(R.id.iv_email);
        emailImageView.setOnClickListener(mOnClickListener);
        TextView sendEmailTextView = (TextView) root.findViewById(R.id.tv_vehicle_send_to_email);
        sendEmailTextView.setOnClickListener(mOnClickListener);
        TextView sendEmailDescTextView = (TextView) root.findViewById(R.id.tv_vehicle_send_to_email_desc);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic800(titleVehicleTextView);
        AppApplication.getAppApplication().setFontHYGothic700(manufacturerCaptionTextView,
                modelCaptionTextView, fuelCaptionTextView, releaseDateCaptionTextView,
                mManufacturerValueTextView, mModelValueTextView, mFuelValueTextView,
                mReleaseDateValueTextView, sendEmailTextView);
        AppApplication.getAppApplication().setFontHYGothic400(sendEmailDescTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(sendButton);

        mSession.getPreVehicleData().copyFrom(mSession.getDevVehicleData());
        setVehicleInfo(mSession.getPreVehicleData());
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
                case Constants.NotifyMsg.CHANGE_VEHICLE_INFO :
                    setVehicleInfo(mSession.getPreVehicleData());
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

    private void setVehicleInfo(VehicleData vehicleData) {
        if (vehicleData != null && !"".equals(vehicleData.getUniqueID())) {
            mManufacturerValueTextView.setText(vehicleData.getManufacturer());
            mModelValueTextView.setText(vehicleData.getModel());
            mFuelValueTextView.setText(vehicleData.getFuelType());
            mReleaseDateValueTextView.setText(vehicleData.getReleaseDate());
        }
    }

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
                case R.id.iv_edit_vehicle :
                    mListener.onMenuSelected(Constants.Menu.VEHICLE_INFO_EDIT, 0);
                    break;
                case R.id.btn_send :
                    if (!mListener.isUSBConnected()) {
                        Toast.makeText(getContext(), R.string.check_usb_connection, Toast.LENGTH_SHORT).show();
                    } else if (!vehicleInfoValidationCheck()) {
                        Toast.makeText(getContext(), R.string.enter_vehicle_info , Toast.LENGTH_SHORT).show();
                    } else {
                        mListener.reqWriteVehicleInfo();
                        getActivity().onBackPressed();
                    }
                    break;
                case R.id.iv_email :
                    // fall-through
                case R.id.tv_vehicle_send_to_email :
                    vehicleInfoSendToEmail();
                    break;
                default:
                    break;
            }
        }
    };


    private static class MyHandler extends Handler {
        private final WeakReference<VehicleInfoViewFragment> mFragment;
        private MyHandler(VehicleInfoViewFragment fragment) {
            mFragment = new WeakReference<VehicleInfoViewFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final VehicleInfoViewFragment fragment = mFragment.get();
            switch (msg.what) {

                default :
                    break;
            }
        }
    }

    private boolean vehicleInfoValidationCheck() {
        boolean ret = true;
        if ("".equals(mSession.getPreVehicleData().getUniqueID())) {
            ret = false;
        }
        return ret;
    }

    private void vehicleInfoSendToEmail () {
        Uri uri = Uri.parse("mailto:vehicle@adasone.com");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "[" + getString(R.string.device_name) + "] " + getString(R.string.vehicle_info_send_to_email));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.vehicle_info_email_extra_text));
        startActivity(intent);
    }
}
