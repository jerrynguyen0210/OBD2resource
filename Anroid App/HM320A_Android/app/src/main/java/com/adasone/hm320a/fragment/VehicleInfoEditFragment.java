package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.adapter.VehicleSelectListAdapter;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VehicleData;
import com.adasone.hm320a.database.DBManager;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.server.AsyncTaskListener;
import com.adasone.hm320a.server.RetrievePostTask;
import com.adasone.hm320a.server.ServerInfo;
import com.adasone.hm320a.util.NetworkUtil;
import com.adasone.hm320a.util.WidgetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class VehicleInfoEditFragment extends Fragment {
    private static final String TAG = VehicleInfoEditFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = VehicleInfoEditFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private LinearLayout mStepLayout;
    private TextView mStepTextView;
    private TextView mStepNameTextView;

    private static final int STEP_1 = 1;
    private static final int STEP_2 = 2;
    private static final int STEP_3 = 3;
    private static final int STEP_4 = 4;
    private static final int STEP_5 = 5;
    private static final int STEP_CAN_BAURRATE = 21;
    private static final int STEP_CAN_MODE = 22;

    private int mCurrentStep = STEP_1;

    private boolean mPressedNextBtn = false;

    private LinearLayout mLoadingLayout;
    private LinearLayout mListLayout;
    private ListView mVehicleListView;

    private Button mOkButton;

    HashMap<String, String> mManufacturerMap = new HashMap<>();
    private RetrievePostTask mServerRetrieveTask = null;
    private ModelListDBInsetTask mModelListDBInsetTask = null;

    private ArrayList<String> mVehicleTypeArrayList = new ArrayList<>();
    private ArrayList<String> mManufacturerArrayList = new ArrayList<>();
    private ArrayList<String> mModelArrayList = new ArrayList<>();
    private ArrayList<String> mFuelTypeArrayList = new ArrayList<>();
    private ArrayList<String> mReleaseDateArrayList = new ArrayList<>();
    private ArrayList<String> mCanBauratesList = new ArrayList<>();
    private ArrayList<String> mCanModesList = new ArrayList<>();

    private VehicleData mSelectedVehicleData = new VehicleData();
    private VehicleData mPreVehicleData;
    private int mSelectVehicleTypePosition = 0;
    private int mSelectManufacturePosition = 0;
    private int mSelectModelPosition = 0;
    private int mSelectFuelTypePosition = 0;
    private int mSelectReleaseDatePosition = 0;
    private int mEditPanelNumber = 0;
    private int mSelectCanBaurate = 0;
    private int mSelectCanMode = 0;

    private VehicleSelectListAdapter mVehicleSelectListAdapter = null;

    public VehicleInfoEditFragment() {
        // Required empty public constructor
    }

    public static VehicleInfoEditFragment newInstance(int inPanelNum) {
        VehicleInfoEditFragment fragment =  new VehicleInfoEditFragment();
        fragment.setEditPanelNumber(inPanelNum);
        return fragment;
    }

    private void setEditPanelNumber(int inNum) {
        mEditPanelNumber = inNum;
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
        mSession = mListener.getSession();
        Activity activity = getActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            Log.e(TAG, "getActivity() is null !!");
        }
        mPreVehicleData = mSession.getPreVehicleData();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_vehicle_info_edit, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        mStepLayout = (LinearLayout) root.findViewById(R.id.layout_step);
        mStepTextView = (TextView) root.findViewById(R.id.tv_step);
        mStepNameTextView = (TextView) root.findViewById(R.id.tv_step_name);

        mOkButton = (Button) root.findViewById(R.id.btn_ok);
        mOkButton.setOnClickListener(mOnClickListener);

        mLoadingLayout = (LinearLayout) root.findViewById(R.id.layout_loading);
        mListLayout = (LinearLayout) root.findViewById(R.id.layout_list);
        mVehicleListView = (ListView) root.findViewById(R.id.list_vehicle);

        mVehicleSelectListAdapter =
                new VehicleSelectListAdapter(getContext(), mManufacturerArrayList);

        mVehicleListView.setOnItemClickListener(mVehicleListOnItemClickListener);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic700(mStepNameTextView);
        AppApplication.getAppApplication().setFontHYGothic400(mStepTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(mOkButton);

        if (mEditPanelNumber == 0) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_VEHICLE_TYPE));
        } else {
            mCurrentStep = STEP_CAN_BAURRATE;
            mStepTextView.setText("SETTINGS");
            mStepNameTextView.setText("CAN BAURATE");
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_CAN_BAURATE_TYPE));
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
        if (mServerRetrieveTask != null) {
            mServerRetrieveTask.cancel(true);
            mServerRetrieveTask = null;
        }
        if (mModelListDBInsetTask != null) {
            mModelListDBInsetTask.cancel(true);
            mModelListDBInsetTask = null;
        }
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
            if (mPressedNextBtn) {
                mPressedNextBtn = false;
                return true;
            } else {
                return processPreviousButtonAction(mCurrentStep);
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
                case R.id.btn_ok :
                    processNextButtonAction(mCurrentStep);
                    break;
                default:
                    break;
            }
        }
    };

    private  AdapterView.OnItemClickListener mVehicleListOnItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mVehicleSelectListAdapter.setSelectedPosition(position);
                    switch (mCurrentStep) {
                        case STEP_1 :
                            mSelectVehicleTypePosition = position;
                            mVehicleSelectListAdapter.resetArrItem(mVehicleTypeArrayList);
                            break;
                        case STEP_2 :
                            mSelectManufacturePosition = position;
                            mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);
                            break;
                        case STEP_3 :
                            mSelectModelPosition = position;
                            mVehicleSelectListAdapter.resetArrItem(mModelArrayList);
                            break;
                        case STEP_4 :
                            mSelectFuelTypePosition = position;
                            mVehicleSelectListAdapter.resetArrItem(mFuelTypeArrayList);
                            break;
                        case STEP_5 :
                            mSelectReleaseDatePosition = position;
                            mVehicleSelectListAdapter.resetArrItem(mReleaseDateArrayList);
                            break;
                        case STEP_CAN_BAURRATE :
                            mSelectCanBaurate = position;
                            mVehicleSelectListAdapter.resetArrItem(mCanBauratesList);
                            break;
                        case STEP_CAN_MODE :
                            mSelectCanMode= position;
                            mVehicleSelectListAdapter.resetArrItem(mCanModesList);
                            break;
                        default:
                            break;
                    }
                }
            };

    private static final int MSG_RETRIEVE_VEHICLE_TYPE = 3000;
    private static final int MSG_RETRIEVE_MANUFACTURER = 3001;
    private static final int MSG_RETRIEVE_MODEL = 3002;
    private static final int MSG_RETRIEVE_FUEL_TYPE = 3003;
    private static final int MSG_RETRIEVE_RELEASE_DATE = 3004;
    private static final int MSG_RETRIEVE_END = 3005;
    private static final int MSG_RETRIEVE_CAN_BAURATE_TYPE = 4006;
    private static final int MSG_RETRIEVE_CAN_MODE_TYPE = 4007;

    private static class MyHandler extends Handler {
        private final WeakReference<VehicleInfoEditFragment> mFragment;
        private MyHandler(VehicleInfoEditFragment fragment) {
            mFragment = new WeakReference<VehicleInfoEditFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final VehicleInfoEditFragment fragment = mFragment.get();
            final Context context = fragment.getContext();
            switch (msg.what) {
                case MSG_RETRIEVE_VEHICLE_TYPE :
                    fragment.retrieveVehicleType();
                    break;
                case MSG_RETRIEVE_MANUFACTURER :
                    fragment.retrieveManufacturer();
                    break;
                case MSG_RETRIEVE_MODEL :
                    fragment.retrieveModel();
                    break;
                case MSG_RETRIEVE_FUEL_TYPE :
                    fragment.retrieveFuelType();
                    break;
                case MSG_RETRIEVE_RELEASE_DATE :
                    fragment.retrieveReleaseDate();
                    break;
                case MSG_RETRIEVE_END :
                    fragment.retrieveCompleted();
                    break;
                case MSG_RETRIEVE_CAN_BAURATE_TYPE :
                    fragment.retrieveVehicleCanBaurateType();
                    break;
                case MSG_RETRIEVE_CAN_MODE_TYPE :
                    fragment.retrieveVehicleCanModeType();
                    break;
                default :
                    break;
            }
        }
    }

    private void processNextButtonAction(int currentStep) {
        switch (currentStep) {
            case STEP_1 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt2);
                mStepTextView.setText(R.string.step2);
                mStepNameTextView.setText(R.string.manufacturer);

                int vehicleType = getVehicleTypeByListPosition(mSelectVehicleTypePosition);
                if (mSelectedVehicleData.getVehicleType() != vehicleType) {
                    mManufacturerMap.clear();
                    mManufacturerArrayList.clear();
                }
                mSelectedVehicleData.setVehicleType(vehicleType);
                mCurrentStep = STEP_2;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_MANUFACTURER));
                break;

            case STEP_2 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt3);
                mStepTextView.setText(R.string.step3);
                mStepNameTextView.setText(R.string.model_name);
                mSelectedVehicleData.setManufacturer(mManufacturerArrayList.get(mSelectManufacturePosition));

                mCurrentStep = STEP_3;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_MODEL));
                break;

            case STEP_3 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt4);
                mStepTextView.setText(R.string.step4);
                mStepNameTextView.setText(R.string.fuel_type);
                mSelectedVehicleData.setModel(mModelArrayList.get(mSelectModelPosition));

                mCurrentStep = STEP_4;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_FUEL_TYPE));
                break;

            case STEP_4 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt5);
                mStepTextView.setText(R.string.step5);
                mStepNameTextView.setText(R.string.release_date);
                mSelectedVehicleData.setFuelType(mFuelTypeArrayList.get(mSelectFuelTypePosition));

                mOkButton.setText(R.string.ok_caps);
                mCurrentStep = STEP_5;
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_RELEASE_DATE));
                break;

            case STEP_5 :
                mSelectedVehicleData.setReleaseDate(mReleaseDateArrayList.get(mSelectReleaseDatePosition));

                mSelectedVehicleData = DBManager.getInstance().getSelectedVehicle(
                        mSelectedVehicleData.getVehicleType(),
                        mSelectedVehicleData.getManufacturer(),
                        mSelectedVehicleData.getModel(),
                        mSelectedVehicleData.getFuelType(),
                        mSelectedVehicleData.getReleaseDate()
                );

                //Log.d (TAG, "mSelectedVehicleData.toString()" + mSelectedVehicleData.toString()) ;

                if (mListener != null) {
                    VehicleData vehicleData = mSession.getPreVehicleData();
                    vehicleData.copyFrom(mSelectedVehicleData);

                    mListener.notifyMessage(VehicleInfoViewFragment.FRAGMENT_TAG, Constants.NotifyMsg.CHANGE_VEHICLE_INFO, null);
                    mPressedNextBtn = true;
                    getActivity().onBackPressed();
                }
                break;
            case STEP_CAN_BAURRATE:
                if (mSelectCanBaurate < 0) {
                    return;
                }
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt2);
                mStepTextView.setText("SETTINGS");
                mStepNameTextView.setText("CAN MODE");

                mSelectedVehicleData.setCanBaurateType(mCanBauratesList.get(mSelectCanBaurate));

                mCurrentStep = STEP_CAN_MODE;
                mOkButton.setText(R.string.ok_caps);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_CAN_MODE_TYPE));
                break;
            case STEP_CAN_MODE:
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt2);

                mSelectedVehicleData.setCanModeType(mCanModesList.get(mSelectCanMode));

                if (mListener != null) {
                    VehicleData vehicleData = mSession.getPreVehicleData();
                    vehicleData.copyFrom(mSelectedVehicleData);

                    mListener.notifyMessage(VehicleInfoViewFragment.FRAGMENT_TAG, Constants.NotifyMsg.CHANGE_CAN_SETTINGS_INFO, null);
                    mPressedNextBtn = true;
                    getActivity().onBackPressed();
                }
                break;
            default:
                break;
        }
    }

    private boolean processPreviousButtonAction(int currentStep) {
        boolean ret = false;
        switch (currentStep) {
            case STEP_1 :
                ret = true;
                break;

            case STEP_2 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt1);
                mStepTextView.setText(R.string.step1);
                mStepNameTextView.setText(R.string.vehicle_type);

                mCurrentStep = STEP_1;
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                mVehicleSelectListAdapter.resetArrItem(mVehicleTypeArrayList);
                mVehicleSelectListAdapter.setSelectedPosition(mSelectVehicleTypePosition);
                mVehicleListView.smoothScrollToPosition(mSelectVehicleTypePosition);
                break;

            case STEP_3 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt2);
                mStepTextView.setText(R.string.step2);
                mStepNameTextView.setText(R.string.manufacturer);

                mCurrentStep = STEP_2;
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);
                mVehicleSelectListAdapter.setSelectedPosition(mSelectManufacturePosition);
                mVehicleListView.smoothScrollToPosition(mSelectManufacturePosition);
                break;

            case STEP_4 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt3);
                mStepTextView.setText(R.string.step3);
                mStepNameTextView.setText(R.string.model_name);

                mCurrentStep = STEP_3;
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                mVehicleSelectListAdapter.resetArrItem(mModelArrayList);
                mVehicleSelectListAdapter.setSelectedPosition(mSelectModelPosition);
                mVehicleListView.smoothScrollToPosition(mSelectModelPosition);
                break;

            case STEP_5 :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt4);
                mStepTextView.setText(R.string.step4);
                mStepNameTextView.setText(R.string.fuel_type);

                mCurrentStep = STEP_4;
                mOkButton.setText(R.string.next_caps);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                mVehicleSelectListAdapter.resetArrItem(mFuelTypeArrayList);
                mVehicleSelectListAdapter.setSelectedPosition(mSelectFuelTypePosition);
                mVehicleListView.smoothScrollToPosition(mSelectFuelTypePosition);
                break;
            case STEP_CAN_BAURRATE :
                ret = true;
                break;
            case STEP_CAN_MODE :
                mStepLayout.setBackgroundResource(R.drawable.vehicle_info2_tt1);
                mStepTextView.setText("SETTINGS");
                mStepNameTextView.setText("CAN BAURATE");

                mCurrentStep = STEP_CAN_BAURRATE;
                mOkButton.setText(R.string.next_caps);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                mVehicleSelectListAdapter.resetArrItem(mCanBauratesList);
                mVehicleSelectListAdapter.setSelectedPosition(mSelectCanBaurate);
                mVehicleListView.smoothScrollToPosition(mSelectCanBaurate);
                break;
            default:
                break;
        }
        return ret;
    }

    private void retrieveVehicleType() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mVehicleTypeArrayList.clear();

        mVehicleTypeArrayList.add(getString(R.string.vehicle_type_car));
        mVehicleTypeArrayList.add(getString(R.string.vehicle_type_bus));
        mVehicleTypeArrayList.add(getString(R.string.vehicle_type_truck));

        mOkButton.setEnabled(true);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
    }

    private void retrieveVehicleCanBaurateType() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mCanBauratesList.clear();

        mCanBauratesList.add("125kps");
        mCanBauratesList.add("250kps");
        mCanBauratesList.add("500kps");

        mOkButton.setEnabled(true);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
    }

    private void retrieveVehicleCanModeType() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mCanModesList.clear();

        mCanModesList.add("Normal");
        mCanModesList.add("Silent");

        mOkButton.setEnabled(true);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
    }


    private void retrieveManufacturer() {
        if (mManufacturerArrayList.size() == 0 || mManufacturerMap.size() == 0) {
            if (NetworkUtil.getConnectivityStatus(getContext()) == NetworkUtil.TYPE_NOT_CONNECTED) {
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);
                mListLayout.setVisibility(View.VISIBLE);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
                Toast.makeText(getContext(), R.string.check_network_connection, Toast.LENGTH_SHORT).show();
            } else {
                int vehicleType = mSelectedVehicleData.getVehicleType();
                mLoadingLayout.setVisibility(View.VISIBLE);
                mListLayout.setVisibility(View.GONE);
                mOkButton.setEnabled(false);
                mServerRetrieveTask = new RetrievePostTask(mManufacturerRetrieveTaskListener);
                mServerRetrieveTask.execute(ServerInfo.VEHICLE_MANUFACTURER_RETRIEVE_URL + "?"
                        + ServerInfo.PARAM_VEHICLE_TYPE + "=" +  vehicleType);
            }
        } else {
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void retrieveModel() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mModelArrayList.clear();
        mModelArrayList = DBManager.getInstance().getModelList(
                mSelectedVehicleData.getVehicleType(), mSelectedVehicleData.getManufacturer());

        if (mModelArrayList.size() == 0) {
            if (NetworkUtil.getConnectivityStatus(getContext()) == NetworkUtil.TYPE_NOT_CONNECTED) {
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mModelArrayList);
                mListLayout.setVisibility(View.VISIBLE);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
                Toast.makeText(getContext(), R.string.check_network_connection, Toast.LENGTH_SHORT).show();
            } else {
                int vehicleType = mSelectedVehicleData.getVehicleType();
                String manufacturerId = mManufacturerMap.get(mManufacturerArrayList.get(mSelectManufacturePosition));
                mLoadingLayout.setVisibility(View.VISIBLE);
                mListLayout.setVisibility(View.GONE);
                mOkButton.setEnabled(false);
                mServerRetrieveTask = new RetrievePostTask(mModelRetrieveTaskListener);
                mServerRetrieveTask.execute(ServerInfo.VEHICLE_MODEL_RETRIEVE_URL + "?"
                        + ServerInfo.PARAM_VEHICLE_TYPE + "=" +  vehicleType
                        + "&" + ServerInfo.PARAM_BRAND_ID + "=" + manufacturerId);
            }
        } else {
            Collections.sort(mModelArrayList, mStringOrderComparator);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
        }
    }

    private void retrieveFuelType() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mFuelTypeArrayList.clear();
        mFuelTypeArrayList = DBManager.getInstance().getFuelTypeList(
                mSelectedVehicleData.getVehicleType(),
                mSelectedVehicleData.getManufacturer(),
                mSelectedVehicleData.getModel());

        Collections.sort(mFuelTypeArrayList, mStringOrderComparator);
        mOkButton.setEnabled(true);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
    }

    private void retrieveReleaseDate() {
        mOkButton.setEnabled(false);
        mLoadingLayout.setVisibility(View.VISIBLE);
        mListLayout.setVisibility(View.GONE);

        mReleaseDateArrayList.clear();
        mReleaseDateArrayList = DBManager.getInstance().getReleaseDateList(
                mSelectedVehicleData.getVehicleType(),
                mSelectedVehicleData.getManufacturer(),
                mSelectedVehicleData.getModel(),
                mSelectedVehicleData.getFuelType());

        Collections.sort(mReleaseDateArrayList, mStringOrderComparator);
        mOkButton.setEnabled(true);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void retrieveCompleted() {

        switch (mCurrentStep) {
            case STEP_1 :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mVehicleTypeArrayList);

                if ("".equals(mPreVehicleData.getUniqueID())) {
                    mVehicleSelectListAdapter.setSelectedPosition(VehicleData.LIST_POS_TYPE_CAR);
                    mSelectVehicleTypePosition = VehicleData.LIST_POS_TYPE_CAR;
                } else {
                    int position = getListPositionByVehicleType(mPreVehicleData.getVehicleType());
                    mVehicleSelectListAdapter.setSelectedPosition(position);
                    mVehicleListView.smoothScrollToPosition(position);
                    mSelectVehicleTypePosition = position;
                }
                break;
            case STEP_2 :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);

                if (!"".equals(mPreVehicleData.getManufacturer())
                        && mPreVehicleData.getVehicleType() == mSelectedVehicleData.getVehicleType()) {
                    int pos = mManufacturerArrayList.indexOf(mPreVehicleData.getManufacturer());
                    if (pos != -1) {
                        mVehicleSelectListAdapter.setSelectedPosition(pos);
                        mVehicleListView.smoothScrollToPosition(pos);
                        mSelectManufacturePosition = pos;
                    } else {
                        mVehicleSelectListAdapter.setSelectedPosition(0);
                        mSelectManufacturePosition = 0;
                    }
                } else {
                    mVehicleSelectListAdapter.setSelectedPosition(0);
                    mSelectManufacturePosition = 0;
                }
                break;
            case STEP_3 :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mModelArrayList);

                if (!"".equals(mPreVehicleData.getModel())
                        && mPreVehicleData.getVehicleType() == mSelectedVehicleData.getVehicleType()
                        && mPreVehicleData.getManufacturer().equals(mSelectedVehicleData.getManufacturer()) ) {
                    int pos = mModelArrayList.indexOf(mPreVehicleData.getModel());
                    if (pos != -1) {
                        mVehicleSelectListAdapter.setSelectedPosition(pos);
                        mVehicleListView.smoothScrollToPosition(pos);
                        mSelectModelPosition = pos;
                    } else {
                        mVehicleSelectListAdapter.setSelectedPosition(0);
                        mSelectModelPosition = 0;
                    }
                } else {
                    mVehicleSelectListAdapter.setSelectedPosition(0);
                    mSelectModelPosition = 0;
                }
                break;
            case STEP_4 :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mFuelTypeArrayList);

                if (!"".equals(mPreVehicleData.getFuelType())
                        && mPreVehicleData.getVehicleType() == mSelectedVehicleData.getVehicleType()
                        && mPreVehicleData.getManufacturer().equals(mSelectedVehicleData.getManufacturer())
                        && mPreVehicleData.getModel().equals(mSelectedVehicleData.getModel())) {
                    int pos = mFuelTypeArrayList.indexOf(mPreVehicleData.getFuelType());
                    if (pos != -1) {
                        mVehicleSelectListAdapter.setSelectedPosition(pos);
                        mVehicleListView.smoothScrollToPosition(pos);
                        mSelectFuelTypePosition = pos;
                    } else {
                        mVehicleSelectListAdapter.setSelectedPosition(0);
                        mSelectFuelTypePosition = 0;
                    }
                } else {
                    mVehicleSelectListAdapter.setSelectedPosition(0);
                    mSelectFuelTypePosition = 0;
                }
                break;
            case STEP_5 :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mReleaseDateArrayList);

                if (!"".equals(mPreVehicleData.getReleaseDate())
                        && mPreVehicleData.getVehicleType() == mSelectedVehicleData.getVehicleType()
                        && mPreVehicleData.getManufacturer().equals(mSelectedVehicleData.getManufacturer())
                        && mPreVehicleData.getModel().equals(mSelectedVehicleData.getModel())
                        && mPreVehicleData.getFuelType().equals(mSelectedVehicleData.getFuelType())) {
                    int pos = mReleaseDateArrayList.indexOf(mPreVehicleData.getReleaseDate());
                    if (pos != -1) {
                        mVehicleSelectListAdapter.setSelectedPosition(pos);
                        mVehicleListView.smoothScrollToPosition(pos);
                        mSelectReleaseDatePosition = pos;
                    } else {
                        mVehicleSelectListAdapter.setSelectedPosition(0);
                        mSelectReleaseDatePosition = 0;
                    }
                } else {
                    mVehicleSelectListAdapter.setSelectedPosition(0);
                    mSelectReleaseDatePosition = 0;
                }
                break;
            case STEP_CAN_BAURRATE:
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mCanBauratesList);
                int pos = mCanBauratesList.indexOf(mPreVehicleData.getCanBaurateType());
                pos = pos < 0? 0:pos;
                mVehicleSelectListAdapter.setSelectedPosition(pos);
                mSelectCanBaurate = pos;
                break;
            case STEP_CAN_MODE :
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mCanModesList);
                int pos1 = mCanModesList.indexOf(mPreVehicleData.getCanModeType());
                pos1 = pos1 < 0? 0:pos1;
                mVehicleSelectListAdapter.setSelectedPosition(pos1);
                mSelectCanMode = pos1;
                break;
        }
        mLoadingLayout.setVisibility(View.GONE);
        mListLayout.setVisibility(View.VISIBLE);
    }
    private final static Comparator<String> mStringOrderComparator = new Comparator<String>() {
        @Override
        public int compare(String obj1, String obj2) {
            return obj1.compareToIgnoreCase(obj2);
        }
    };

    private AsyncTaskListener mManufacturerRetrieveTaskListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            mServerRetrieveTask = null;
            parseManufacturerListResult(jsonObject);
        }

        @Override
        public void onFailure(Throwable thrown) {
            if (thrown instanceof IOException) {
                Toast.makeText(getContext(), getString(R.string.check_network_connection),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.error_reason, thrown.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "[onFailure] message : " + thrown.getMessage() + ", cause" + thrown.getCause());

            mServerRetrieveTask = null;
            mVehicleListView.setAdapter(mVehicleSelectListAdapter);
            mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);
            mLoadingLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
        }

        @Override
        public void onCancel() {
            mServerRetrieveTask = null;
        }

        @Override
        public void onProgressUpdate(int progress) {
        }
    };

    private void parseManufacturerListResult(JSONObject jsonObject) {
        int errMsgResID = -1;
        int serverVehicleListVersion;
        int localVehicleListVersion;

        try {
            if (jsonObject.has("result")) {
                if ("success".equals(jsonObject.getString("result"))) {
                    if (jsonObject.has("ver")) {
                        int vehicleType = mSelectedVehicleData.getVehicleType();
                        serverVehicleListVersion = Integer.parseInt(jsonObject.getString("ver"));
                        localVehicleListVersion = DBManager.getInstance().getVehicleListVersion(vehicleType);
                        if (serverVehicleListVersion > localVehicleListVersion) {
                            DBManager.getInstance().deleteVehicleListTransaction(vehicleType);
                            DBManager.getInstance().setVehicleListVersion(vehicleType, localVehicleListVersion);
                        }
                        mManufacturerMap.clear();
                        mManufacturerArrayList.clear();
                        JSONArray itemsJSONArray = jsonObject.getJSONArray("items");
                        if (itemsJSONArray.length() > 0) {
                            for (int i=0; i < itemsJSONArray.length(); i++) {
                                JSONArray itemJSONArray = itemsJSONArray.getJSONArray(i);
                                mManufacturerMap.put(itemJSONArray.getString(1),itemJSONArray.getString(0));
                                mManufacturerArrayList.add(itemJSONArray.getString(1));
                                Log.d(TAG, "itemJSONArray.getString(1) : "  + itemJSONArray.getString(1));
                            }
                            Collections.sort(mManufacturerArrayList, mStringOrderComparator);
                            mLoadingLayout.setVisibility(View.GONE);
                            mListLayout.setVisibility(View.VISIBLE);
                            WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
                        } else {
                            errMsgResID = R.string.server_error_items_empty;
                            Log.e(TAG, "items empty");
                        }
                    } else {
                        errMsgResID = R.string.server_error_key_not_exist;
                        Log.e(TAG, "ver key not exist");
                    }
                } else {
                    errMsgResID = R.string.server_error_result_fail;
                    Log.e(TAG, "result is not success");
                }
            } else {
                errMsgResID = R.string.server_error_result_key_not_exist;
                Log.e(TAG, "result key not exist");
            }
        } catch (JSONException | NumberFormatException e) {
            errMsgResID = R.string.server_error_other_parsing_error;
            Log.e(TAG, "Exception : " + e.getMessage());
        } finally {
            if (errMsgResID != -1) {
                mVehicleListView.setAdapter(mVehicleSelectListAdapter);
                mVehicleSelectListAdapter.resetArrItem(mManufacturerArrayList);
                mLoadingLayout.setVisibility(View.GONE);
                mListLayout.setVisibility(View.VISIBLE);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
                Toast.makeText(getContext(),
                        getString(R.string.error_reason, getString(errMsgResID)), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private AsyncTaskListener mModelRetrieveTaskListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            mServerRetrieveTask = null;
            parseModelListResult(jsonObject);
        }

        @Override
        public void onFailure(Throwable thrown) {
            if (thrown instanceof IOException) {
                Toast.makeText(getContext(), getString(R.string.check_network_connection),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.error_reason, thrown.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "[onFailure] message : " + thrown.getMessage() + ", cause" + thrown.getCause());

            mServerRetrieveTask = null;
            mLoadingLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
        }

        @Override
        public void onCancel() {
            mServerRetrieveTask = null;
        }

        @Override
        public void onProgressUpdate(int progress) {
        }
    };

    private void parseModelListResult(JSONObject jsonObject) {
        int errMsgResID = -1;
        try {
            if (jsonObject.has("result")) {
                if ("success".equals(jsonObject.getString("result"))) {
                    if (jsonObject.has("ver")) {
                        JSONArray itemsJSONArray = jsonObject.getJSONArray("items");
                        if (itemsJSONArray.length() > 0) {
                            if (mModelListDBInsetTask != null) {
                                mModelListDBInsetTask.cancel(true);
                            }
                            mModelListDBInsetTask =
                                    new ModelListDBInsetTask(mSelectedVehicleData.getVehicleType(),
                                            mManufacturerArrayList.get(mSelectManufacturePosition),
                                            itemsJSONArray);
                            mModelListDBInsetTask.execute();
                        } else {
                            errMsgResID = R.string.server_error_items_empty;
                            Log.e(TAG, "items empty");
                        }
                    } else {
                        errMsgResID = R.string.server_error_key_not_exist;
                        Log.e(TAG, "ver key not exist");
                    }
                } else {
                    errMsgResID = R.string.server_error_result_fail;
                    Log.e(TAG, "result is not success");
                }
            } else {
                errMsgResID = R.string.server_error_result_key_not_exist;
                Log.e(TAG, "result key not exist");
            }
        } catch (JSONException | NumberFormatException e) {
            errMsgResID = R.string.server_error_other_parsing_error;
            Log.e(TAG, "Exception : " + e.getMessage());
        } finally {
            if (errMsgResID != -1) {
                mLoadingLayout.setVisibility(View.GONE);
                mListLayout.setVisibility(View.VISIBLE);
                WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
                Toast.makeText(getContext(),
                        getString(R.string.error_reason, getString(errMsgResID)), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * AsyncTask for db insert
     * @Void		Parameter of execute( ), doInBackground( ) method.
     * @Void	    Parameter of onProgressUpdate( ) method.
     * @Void	    Return value of doInBackground( ) method, and Parameter of onPostExecute( ) method.
     */

    private class ModelListDBInsetTask extends AsyncTask<Void, Void, Void> {
        private final JSONArray mJsonArray;
        private final int mVehicleType;
        private final String mManufacturer;

        public ModelListDBInsetTask(int vehicleType, String manufacturer, JSONArray jsonArray) {
            mVehicleType = vehicleType;
            mManufacturer = manufacturer;
            mJsonArray = jsonArray;
        }
        /* Works before doInBackground( ) */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /* Works should be done in here. */
        @Override
        protected Void doInBackground(Void... params) {
            DBManager.getInstance().insertVehicleListTransaction(mVehicleType, mManufacturer, mJsonArray);
            mModelArrayList.clear();
            mModelArrayList = DBManager.getInstance().getModelList(mVehicleType, mManufacturer);
            Collections.sort(mModelArrayList, mStringOrderComparator);
            return null;
        }

        /* Works after doInBackground( ) */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, true);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RETRIEVE_END));
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            mLoadingLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mLoadingLayout.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            WidgetUtil.setBtnEnabled(getContext(), mOkButton, false);
        }
    }

    private int getVehicleTypeByListPosition(int position) {
        int vehicleType;
        switch (position) {
            case VehicleData.LIST_POS_TYPE_TRUCK :
                vehicleType = VehicleData.TYPE_TRUCK;
                break;
            case VehicleData.LIST_POS_TYPE_BUS :
                vehicleType = VehicleData.TYPE_BUS;
                break;
            case VehicleData.LIST_POS_TYPE_CAR :
                // fall-through
            default:
                vehicleType = VehicleData.TYPE_CAR;
                break;
        }
        return vehicleType;
    }

    private int getListPositionByVehicleType(int type) {
        int position;
        switch (type) {
            case VehicleData.TYPE_TRUCK :
                position = VehicleData.LIST_POS_TYPE_TRUCK;
                break;
            case VehicleData.TYPE_BUS :
                position = VehicleData.LIST_POS_TYPE_BUS;
                break;
            case VehicleData.TYPE_CAR :
                // fall-through
            default:
                position = VehicleData.LIST_POS_TYPE_CAR;
                break;
        }
        return position;
    }
}
