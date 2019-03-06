package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VehicleData;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.CommUtil;
import com.adasone.hm320a.util.DateTimeUtil;
import com.adasone.hm320a.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.StringTokenizer;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenuFragment extends Fragment {
    private static final String TAG = MainMenuFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = MainMenuFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private boolean mDateAndTimeReceiverRegistered = false;

    private LinearLayout mRootLayout;
    private LinearLayout mFourMenusLayout;
    private LinearLayout mFiveMenusLayout;

    private TextView mDateTextView;
    private TextView mTimeTextView;
    private TextView mMeridiemTextView;

    private TextView mFourMenusDesc1TextView;
    private TextView mFourMenusDesc2TextView;
    private TextView mFourMenusDesc4TextView;
    private TextView mFourMenusDesc5TextView;

    private TextView mFiveMenusDesc1TextView;
    private TextView mFiveMenusDesc2TextView;
    private TextView mFiveMenusDesc4TextView;
    private TextView mFiveMenusDesc5TextView;

    public MainMenuFragment() {
        // Required empty public constructor
    }

    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
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
        View root= inflater.inflate(R.layout.fragment_mainmenu, container, false);

        mRootLayout = (LinearLayout) root.findViewById(R.id.layout_root);

        mDateTextView = (TextView) root.findViewById(R.id.tv_date);
        mTimeTextView = (TextView) root.findViewById(R.id.tv_time);
        mMeridiemTextView = (TextView) root.findViewById(R.id.tv_meridiem);

        mFourMenusLayout = (LinearLayout) root.findViewById(R.id.layout_4menus);
        LinearLayout fourMenusMenu1Layout = (LinearLayout) root.findViewById(R.id.layout_4menus_menu1);
        LinearLayout fourMenusMenu2Layout = (LinearLayout) root.findViewById(R.id.layout_4menus_menu2);
        LinearLayout fourMenusMenu4Layout = (LinearLayout) root.findViewById(R.id.layout_4menus_menu4);
//        LinearLayout fourMenusMenu5Layout = (LinearLayout) root.findViewById(R.id.layout_4menus_menu5);

        fourMenusMenu1Layout.setOnClickListener(mMenuClickListener);
        fourMenusMenu2Layout.setOnClickListener(mMenuClickListener);
        fourMenusMenu4Layout.setOnClickListener(mMenuClickListener);
//        fourMenusMenu5Layout.setOnClickListener(mMenuClickListener);

        TextView fourMenusMenu1TitleTextView = (TextView) root.findViewById(R.id.tv_4menus_menu1_title);
        TextView fourMenusMenu2TitleTextView = (TextView) root.findViewById(R.id.tv_4menus_menu2_title);
        TextView fourMenusMenu4TitleTextView = (TextView) root.findViewById(R.id.tv_4menus_menu4_title);
//        TextView fourMenusMenu5TitleTextView = (TextView) root.findViewById(R.id.tv_4menus_menu5_title);

        mFourMenusDesc1TextView = (TextView) root.findViewById(R.id.tv_4menus_menu1_description);
        mFourMenusDesc2TextView = (TextView) root.findViewById(R.id.tv_4menus_menu2_description);
        mFourMenusDesc4TextView = (TextView) root.findViewById(R.id.tv_4menus_menu4_description);
//        mFourMenusDesc5TextView = (TextView) root.findViewById(R.id.tv_4menus_menu5_description);


        mFiveMenusLayout = (LinearLayout) root.findViewById(R.id.layout_5menus);
        LinearLayout fiveMenusMenu1Layout = (LinearLayout) root.findViewById(R.id.layout_5menus_menu1);
        LinearLayout fiveMenusMenu2Layout = (LinearLayout) root.findViewById(R.id.layout_5menus_menu2);
        LinearLayout fiveMenusMenu3Layout = (LinearLayout) root.findViewById(R.id.layout_5menus_menu3);
        LinearLayout fiveMenusMenu4Layout = (LinearLayout) root.findViewById(R.id.layout_5menus_menu4);
        LinearLayout fiveMenusMenu5Layout = (LinearLayout) root.findViewById(R.id.layout_5menus_menu5);

        fiveMenusMenu1Layout.setOnClickListener(mMenuClickListener);
        fiveMenusMenu2Layout.setOnClickListener(mMenuClickListener);
        fiveMenusMenu3Layout.setOnClickListener(mMenuClickListener);
        fiveMenusMenu4Layout.setOnClickListener(mMenuClickListener);
        fiveMenusMenu5Layout.setOnClickListener(mMenuClickListener);

        TextView fiveMenusMenu1TitleTextView = (TextView) root.findViewById(R.id.tv_5menus_menu1_title);
        TextView fiveMenusMenu2TitleTextView = (TextView) root.findViewById(R.id.tv_5menus_menu2_title);
        TextView fiveMenusMenu3TitleTextView = (TextView) root.findViewById(R.id.tv_5menus_menu3_title);
        TextView fiveMenusMenu4TitleTextView = (TextView) root.findViewById(R.id.tv_5menus_menu4_title);
        TextView fiveMenusMenu5TitleTextView = (TextView) root.findViewById(R.id.tv_5menus_menu5_title);

        mFiveMenusDesc1TextView = (TextView) root.findViewById(R.id.tv_5menus_menu1_description);
        mFiveMenusDesc2TextView = (TextView) root.findViewById(R.id.tv_5menus_menu2_description);
        mFiveMenusDesc4TextView = (TextView) root.findViewById(R.id.tv_5menus_menu4_description);
        mFiveMenusDesc5TextView = (TextView) root.findViewById(R.id.tv_5menus_menu5_description);

        // set font
        AppApplication.getAppApplication().setFontHYNGothicM(mDateTextView, mTimeTextView, mMeridiemTextView);
        AppApplication.getAppApplication().setFontHYGothic400(mFourMenusDesc1TextView,
                mFourMenusDesc2TextView, mFourMenusDesc4TextView, /*mFourMenusDesc5TextView,*/
                mFiveMenusDesc1TextView, mFiveMenusDesc2TextView, mFiveMenusDesc4TextView,
                mFiveMenusDesc5TextView);
        AppApplication.getAppApplication().setFontHYGothic800(fourMenusMenu1TitleTextView,
                fourMenusMenu2TitleTextView, fourMenusMenu4TitleTextView, /*fourMenusMenu5TitleTextView,*/
                fiveMenusMenu1TitleTextView, fiveMenusMenu2TitleTextView, fiveMenusMenu3TitleTextView,
                fiveMenusMenu4TitleTextView, fiveMenusMenu5TitleTextView);

        refreshVehicleDescription();
        refreshCalibrationDescription();
        refreshDownloadVideoDescription();
        refreshVersionDescription();

        selectMenuStyle();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerDateAndTimeReceiver();
        updateDateAndTime();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterDateAndTimeReceiver();
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
            if (CommUtil.Usb.CMD_CODE_VERSION_INFO.equals(cmd)) {
                refreshVersionDescription();
                selectMenuStyle();
            } else if (CommUtil.Usb.CMD_CODE_WRITE_VEHICLE_INFO.equals(cmd)
                    || CommUtil.Usb.CMD_CODE_READ_VEHICLE_INFO.equals(cmd)) {
                refreshVehicleDescription();
            } else if (CommUtil.Usb.CMD_CODE_WRITE_CALIBRATION_INFO.equals(cmd)
                    || CommUtil.Usb.CMD_CODE_READ_CALIBRATION_INFO.equals(cmd)) {
                refreshCalibrationDescription();
            }
        }

        @Override
        public void onNotifyMessage(int msg, Bundle bundle) {
            switch (msg) {
                case Constants.NotifyMsg.VIDEO_COUNT_REFRESH :
                    refreshDownloadVideoDescription();
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
    public View.OnClickListener mMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mListener == null) {
                Log.e(TAG, "mListener is null !!!");
                return;
            } else if (mListener.isUSBDefaultInfoRequestPending()) {
                Log.d(TAG, "Ignore menu click : default info request pending..");
                return;
            }
            switch (view.getId()) {
                case R.id.layout_4menus_menu1:
                    // fall-through
                case R.id.layout_5menus_menu1:
                    mListener.onMenuSelected(Constants.Menu.VEHICLE, 0);
                    break;
                case R.id.layout_4menus_menu2:
                    // fall-through
                case R.id.layout_5menus_menu2:
                    //mListener.onMenuSelected(Constants.Menu.CALIBRATION_CAM_LOCATION, 0);
                    mListener.onMenuSelected(Constants.Menu.VEHICLE_OBD_CHECK, 0);
                    break;
                case R.id.layout_5menus_menu3:
                    mListener.onMenuSelected(Constants.Menu.DTG, 0);
                    break;
                case R.id.layout_4menus_menu4:
//                    mListener.onMenuSelected(Constants.Menu.DTG, 0);
                    Toast.makeText(getActivity(),"Engineermode triggerd", Toast.LENGTH_SHORT).show();
                    Intent intent =new Intent(MainMenuFragment.this.getActivity(), com.adasone.hm320a.canbusanalyzer.ActivityEngineer.class); // = new Intent(MainMenuFragment.this,obd2scantool.canbusanalyzer.ActivityEngineer.class);
                    startActivity(intent);
                    break;
                    // fall-through
                case R.id.layout_5menus_menu4:
                    mListener.onMenuSelected(Constants.Menu.VIDEO, 0);
                    break;
//                case R.id.layout_4menus_menu5:
                    // fall-through
                case R.id.layout_5menus_menu5:
                    mListener.onMenuSelected(Constants.Menu.FIRMWARE, 0);
                    break;
                default:
                    break;
            }
        }
    };


    private static class MyHandler extends Handler {
        private final WeakReference<MainMenuFragment> mFragment;
        private MyHandler(MainMenuFragment fragment) {
            mFragment = new WeakReference<MainMenuFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final MainMenuFragment fragment = mFragment.get();
            switch (msg.what) {

                default :
                    break;
            }
        }
    }

    private void registerDateAndTimeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_DATE_CHANGED);

        getContext().registerReceiver(mDateAndTimeReceiver, filter);
        mDateAndTimeReceiverRegistered = true;
    }

    private void unregisterDateAndTimeReceiver() {
        try {
            if (mDateAndTimeReceiverRegistered) {
                getContext().unregisterReceiver(mDateAndTimeReceiver);
                mDateAndTimeReceiverRegistered = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mDateAndTimeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_TIME_TICK.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)) {
                updateDateAndTime();
            }
        }
    };

    private void updateDateAndTime() {
        String strDateTime = "";

        try {
            strDateTime = DateTimeUtil.getCurrDateAndTime(getContext());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        StringTokenizer tokens = new StringTokenizer(strDateTime, ",");

        if (tokens.hasMoreElements()) {
            mDateTextView.setText(tokens.nextToken());
        }
        if (tokens.hasMoreElements()) {
            mTimeTextView.setText(tokens.nextToken());
        }
        if (tokens.hasMoreElements()) {
            mMeridiemTextView.setText(tokens.nextToken());
        }
    }

    private void refreshVehicleDescription() {
        String desc;
        VehicleData vehicleData = mSession.getDevVehicleData();
        if (!"".equals(vehicleData.getUniqueID())) {
            desc = vehicleData.getManufacturer() + ", " + vehicleData.getModel();
        } else {
            desc = getString(R.string.symbol_dash);
        }
        mFourMenusDesc1TextView.setText(desc);
        mFiveMenusDesc1TextView.setText(desc);
    }

    private void refreshCalibrationDescription() {
        String desc;
        CalibrationData calibrationData = mSession.getDevCalibrationData();
        if (!"".equals(calibrationData.getDate())) {
            desc = getString(R.string.latest_date_of_change,
                    DateTimeUtil.getDateToDefaultUIFormat(getContext(), calibrationData.getDate()));
        } else {
            desc = getString(R.string.symbol_dash);
        }

        mFourMenusDesc2TextView.setText(desc);
        mFiveMenusDesc2TextView.setText(desc);
    }

    private void refreshDownloadVideoDescription() {
        String desc;
        if (!"".equals(mSession.getDeviceToken())) {
            int localVideoCount = FileUtil.getAllVideoLocalFilesCount(mSession.getDeviceToken());
            desc = getResources().getQuantityString(R.plurals.downloaded_file_count, localVideoCount, localVideoCount);
        } else {
            desc = getString(R.string.symbol_dash);
        }

        mFourMenusDesc4TextView.setText(desc);
        mFiveMenusDesc4TextView.setText(desc);
    }

    private void refreshVersionDescription() {
        String desc;

        if (!"".equals(mSession.getDeviceToken())) {
            if ("".equals(mSession.getDeviceVersionName())) {
                desc = getString(R.string.firmware_version_with_value, getString(R.string.unknown));
            } else {
                desc = getString(R.string.firmware_version_with_value, mSession.getDeviceVersionName());
            }
        } else {
            desc = getString(R.string.symbol_dash);
        }
        /*mFourMenusDesc5TextView.setText(desc);*/
        mFiveMenusDesc5TextView.setText(desc);
    }

    private void selectMenuStyle() {
        if (mSession.getDeviceDtgSupport() == Constants.DtgSupport.HW_SW_SUPPORTED) {
            mRootLayout.setBackgroundResource(R.drawable.main_menu5_bg);
            mFourMenusLayout.setVisibility(View.GONE);
            mFiveMenusLayout.setVisibility(View.VISIBLE);
        } else {
            mRootLayout.setBackgroundResource(R.drawable.main_menu4_bg);
            mFiveMenusLayout.setVisibility(View.GONE);
            mFourMenusLayout.setVisibility(View.VISIBLE);
        }
    }
}
