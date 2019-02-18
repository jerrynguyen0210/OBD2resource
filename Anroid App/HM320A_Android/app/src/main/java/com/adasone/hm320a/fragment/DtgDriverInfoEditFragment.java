package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.adapter.VehicleTypeListAdapter;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.DriverData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VehicleTypeData;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.VehicleTypeUtil;
import com.adasone.hm320a.view.MarqueeTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DtgDriverInfoEditFragment extends Fragment {
    private static final String TAG = DtgDriverInfoEditFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = DtgDriverInfoEditFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private MarqueeTextView mVehicleTypeSelectTextView;

    private EditText mVinEditText;
    private EditText mVehicleNumberEditText;
    private EditText mBusinessNumberEditText;
    private EditText mDriverCodeEditText;

    private PopupWindow mVehicleTypeListPopupWindow = null;

    private VehicleTypeListAdapter mVehicleTypeListAdapter;
    private ArrayList<VehicleTypeData> mVehicleTypeDataArray = new ArrayList<>();
    private int mSelectedVehicleTypeCode = -1;

    private DriverData mPreDriverData;

    public DtgDriverInfoEditFragment() {
        // Required empty public constructor
    }

    public static DtgDriverInfoEditFragment newInstance() {
        return new DtgDriverInfoEditFragment();
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
        mPreDriverData = mSession.getPreDriverData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_dtg_driver_info_edit, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        TextView modelNameCaptionTextView = (TextView) root.findViewById(R.id.tv_model_caption);
        TextView modelNameValueTextView = (TextView) root.findViewById(R.id.tv_model_value);

        TextView vehicleTypeCaptionTextView = (TextView) root.findViewById(R.id.tv_vehicle_type_caption);
        mVehicleTypeSelectTextView = (MarqueeTextView) root.findViewById(R.id.tv_vehicle_type_select);
        mVehicleTypeSelectTextView.setOnClickListener(mOnClickListener);

        TextView vinTextView = (TextView) root.findViewById(R.id.tv_vin_code);
        TextView vehicleNumberTextView = (TextView) root.findViewById(R.id.tv_vehicle_number);
        TextView businessNumberTextView = (TextView) root.findViewById(R.id.tv_business_number);
        TextView driverCodeTextView = (TextView) root.findViewById(R.id.tv_driver_code);

        mVinEditText = (EditText) root.findViewById(R.id.edit_vin_code);
        InputFilter[] vinFilters = new InputFilter[]{
                new InputFilter.LengthFilter(17),
                new InputFilter.AllCaps()
        };
        mVinEditText.setFilters(vinFilters);
        mVinEditText.setPrivateImeOptions("defaultInputmode=english;");

        mVehicleNumberEditText = (EditText) root.findViewById(R.id.edit_vehicle_number);
        mVehicleNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        mVehicleNumberEditText.setPrivateImeOptions("defaultInputmode=numeric;");

        mBusinessNumberEditText = (EditText) root.findViewById(R.id.edit_business_number);
        mBusinessNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mBusinessNumberEditText.setPrivateImeOptions("defaultInputmode=numeric;");

        mDriverCodeEditText = (EditText) root.findViewById(R.id.edit_driver_code);
        mDriverCodeEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        mDriverCodeEditText.setPrivateImeOptions("defaultInputmode=numeric;");

        Button okButton = (Button) root.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(mOnClickListener);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView, modelNameValueTextView);
        AppApplication.getAppApplication().setFontHYGothic700(modelNameCaptionTextView,
                vehicleTypeCaptionTextView, vinTextView, vehicleNumberTextView,
                businessNumberTextView, driverCodeTextView, mVinEditText,
                mVehicleNumberEditText, mBusinessNumberEditText, mDriverCodeEditText);
        AppApplication.getAppApplication().setFontHYGothic600(mVehicleTypeSelectTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(okButton);

        initVehicleTypeListPopupWindow();

        mSelectedVehicleTypeCode = mPreDriverData.getVehicleType();
        if (mSelectedVehicleTypeCode != -1) {
            mVehicleTypeSelectTextView.setText(VehicleTypeUtil.getVehicleTypeStringResId(mSelectedVehicleTypeCode));
        }
        if (!"".equals(mPreDriverData.getVIN())) {
            mVinEditText.setText(mPreDriverData.getVIN());
        }
        if (!"".equals(mPreDriverData.getVehicleRegNo())) {
            mVehicleNumberEditText.setText(mPreDriverData.getVehicleRegNo());
        }
        if (!"".equals(mPreDriverData.getBusinessRegNo())) {
            mBusinessNumberEditText.setText(mPreDriverData.getBusinessRegNo());
        }
        if (!"".equals(mPreDriverData.getDriverCode())) {
            mDriverCodeEditText.setText(mPreDriverData.getDriverCode());
        }
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
                    hideKeyboard();
                    mListener.onRequestMainMenu(false);
                    break;
                case R.id.iv_back :
                    hideKeyboard();
                    getActivity().onBackPressed();
                    break;
                case R.id.tv_vehicle_type_select:
                    showVehicleTypeListPopupWindow(mVehicleTypeSelectTextView);
                    break;
                case R.id.tv_selected_item :
                    mVehicleTypeListPopupWindow.dismiss();
                    break;
                case R.id.btn_ok :
                    if (!validateDriverInfo()) {
                        return;
                    }
                    mPreDriverData.initialize();
                    mPreDriverData.setVehicleType(mSelectedVehicleTypeCode);
                    mPreDriverData.setVIN(mVinEditText.getText().toString().trim());
                    mPreDriverData.setVehicleRegNo(mVehicleNumberEditText.getText().toString().trim());
                    mPreDriverData.setBusinessRegNo(mBusinessNumberEditText.getText().toString().trim());
                    mPreDriverData.setDriverCode(mDriverCodeEditText.getText().toString().trim());
                    mListener.notifyMessage(DtgInfoViewFragment.FRAGMENT_TAG, Constants.NotifyMsg.CHANGE_DTG_DRIVER_INFO, null);
                    getActivity().onBackPressed();
                    break;
                default:
                    break;
            }
        }
    };


    private static class MyHandler extends Handler {
        private final WeakReference<DtgDriverInfoEditFragment> mFragment;
        private MyHandler(DtgDriverInfoEditFragment fragment) {
            mFragment = new WeakReference<DtgDriverInfoEditFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final DtgDriverInfoEditFragment fragment = mFragment.get();
            switch (msg.what) {
                default :
                    break;
            }
        }
    }

    public void initVehicleTypeListPopupWindow() {
        mVehicleTypeListPopupWindow = new PopupWindow(getContext());
        mVehicleTypeListPopupWindow.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mVehicleTypeListPopupWindow.setBackgroundDrawable(getContext().getDrawable(R.drawable.vehicle_info4_type_on));
        } else {
            mVehicleTypeListPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.vehicle_info4_type_on));
        }

        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_window_vehicle_type, null);
        ListView vehicleTypeListView = (ListView) popupView.findViewById(R.id.list);
        TextView selectedItemTextView = (TextView) popupView.findViewById(R.id.tv_selected_item);
        selectedItemTextView.setOnClickListener(mOnClickListener);
        AppApplication.getAppApplication().setFontHYGothic600(selectedItemTextView);

        buildVehicleTypeDataArray();
        mVehicleTypeListAdapter = new VehicleTypeListAdapter(getContext(), mVehicleTypeDataArray);
        vehicleTypeListView.setAdapter(mVehicleTypeListAdapter);
        vehicleTypeListView.setClickable(true);
        vehicleTypeListView.setOnItemClickListener(mPopupListOnItemClickListener);

        // 184.33, 156.67dp : popup_window_vehicle_type layout_height
        //final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float)184.33, getResources().getDisplayMetrics());
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 156.67, getResources().getDisplayMetrics());
        mVehicleTypeListPopupWindow.setHeight(height);
        //mVehicleTypeListPopupWindow.setWidth(width);
        mVehicleTypeListPopupWindow.setContentView(popupView);
        mVehicleTypeListPopupWindow.setOnDismissListener(mPopupOnDismissListener);
    }

    public void showVehicleTypeListPopupWindow(View Anchor) {
        if (mVehicleTypeListPopupWindow == null) {
            initVehicleTypeListPopupWindow();
        }
        mVehicleTypeListPopupWindow.setWidth(mVehicleTypeSelectTextView.getWidth());
        if (Anchor != null) {
            mVehicleTypeListPopupWindow.showAsDropDown(Anchor, 0, -Anchor.getHeight(), Gravity.TOP);
        } else {
            mVehicleTypeListPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
    }
    private void buildVehicleTypeDataArray () {
        mVehicleTypeDataArray.clear();
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.CITY_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.RURAL_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.TOWN_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.INTERCITY_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.EXPRESS_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.CHARTERED_BUS));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.SPECIAL_PASSENGER_VEHICLE));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.REGULAR_TAXI));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.PRIVATE_TAXI));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.GENERAL_LORRY));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.INDIVIDUAL_LORRY));
        mVehicleTypeDataArray.add(new VehicleTypeData(Constants.VehicleType.NON_COMMERCIAL_VEHICLE));
    }

    private AdapterView.OnItemClickListener mPopupListOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedVehicleTypeCode = mVehicleTypeDataArray.get(position).getCode();
            mVehicleTypeSelectTextView.setText(VehicleTypeUtil.getVehicleTypeStringResId(mSelectedVehicleTypeCode));
            mVehicleTypeListAdapter.setSelectedPosition(position);
            mVehicleTypeListPopupWindow.dismiss();
        }
    };

    private PopupWindow.OnDismissListener mPopupOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
        }
    };


    private boolean validateDriverInfo() {
        boolean ret = true;
        int strResId = -1;

        if (mSelectedVehicleTypeCode == -1) {
            strResId = R.string.select_vehicle_type;
        } else if (mVinEditText.getText().length() == 0) {
            strResId = R.string.enter_vin_code;
            mVinEditText.requestFocus();
        } else if (DriverData.validateVIN(mVinEditText.getText().toString()) == Boolean.FALSE) {
            strResId = R.string.wrong_vin_code;
            mVinEditText.requestFocus();
        } else if (mVehicleNumberEditText.getText().length() == 0) {
            strResId = R.string.enter_vehicle_registration_number;
            mVehicleNumberEditText.requestFocus();
        } else if (DriverData.validateVehicleRegNumber(mVehicleNumberEditText.getText().toString()) == Boolean.FALSE) {
            strResId = R.string.wrong_vehicle_registration_number;
            mVehicleNumberEditText.requestFocus();
        } else if (mBusinessNumberEditText.getText().length() == 0) {
            strResId = R.string.enter_business_registration_number;
            mBusinessNumberEditText.requestFocus();
        } else if (DriverData.validateBusinessRegNumber(mBusinessNumberEditText.getText().toString()) == Boolean.FALSE) {
            strResId = R.string.wrong_business_registration_number;
            mBusinessNumberEditText.requestFocus();
        } else if (mDriverCodeEditText.getText().length() == 0) {
            strResId = R.string.enter_driver_code;
            mDriverCodeEditText.requestFocus();
        } else if (DriverData.validateDriverCode(mDriverCodeEditText.getText().toString()) == Boolean.FALSE) {
            strResId = R.string.wrong_driver_code;
            mDriverCodeEditText.requestFocus();
        }

        if (strResId != -1) {
            ret = false;
            Toast.makeText(getContext(), strResId, Toast.LENGTH_SHORT).show();
        }

        return ret;
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
