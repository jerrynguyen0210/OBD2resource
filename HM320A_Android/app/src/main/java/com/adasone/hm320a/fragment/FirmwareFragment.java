package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.CustomDialog;
import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.server.AsyncTaskListener;
import com.adasone.hm320a.server.DownloadTask;
import com.adasone.hm320a.server.RetrieveGetTask;
import com.adasone.hm320a.server.ServerInfo;
import com.adasone.hm320a.util.BlurUtil;
import com.adasone.hm320a.util.CommUtil;
import com.adasone.hm320a.util.FileUtil;
import com.adasone.hm320a.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirmwareFragment extends Fragment {
    private static final String TAG = FirmwareFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = FirmwareFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_VERSION_CHECK = 1;
    private static final int STATUS_SERVER_DOWNLOAD = 2;
    private static final int STATUS_TRANSFER_FILE = 3;
    private int mCurrStatus = STATUS_NORMAL;

    private FrameLayout mRootLayout;
    private LinearLayout mNormalLayout;
    private LinearLayout mRetrieveLayout;
    private LinearLayout mDownloadLayout;

    private TextView versionValueTextView;
    private Button mActionButton;

    TextView mDownloadCaptionTextView;
    TextView mDownloadDescTextView;
    private ProgressBar mProgressBar;

    private RetrieveGetTask mRetrieveTask  = null;
    private DownloadTask mDownloadTask  = null;

    private int mCurrDeviceVersionCode;

    public FirmwareFragment() {
        // Required empty public constructor
    }

    public static FirmwareFragment newInstance() {
        return new FirmwareFragment();
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
        View root = inflater.inflate(R.layout.fragment_firmware, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mMenuOnClickListener);
        backImageView.setOnClickListener(mMenuOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        mRootLayout = (FrameLayout) root.findViewById(R.id.layout_root);
        mNormalLayout = (LinearLayout)root.findViewById(R.id.layout_normal);
        mRetrieveLayout = (LinearLayout)root.findViewById(R.id.layout_loading);
        mDownloadLayout = (LinearLayout)root.findViewById(R.id.layout_download);

        TextView versionTitleTextView = (TextView) root.findViewById(R.id.tv_version_title);
        versionValueTextView = (TextView) root.findViewById(R.id.tv_version_value);

        mActionButton = (Button) root.findViewById(R.id.btn_action);
        mActionButton.setOnClickListener(mMenuOnClickListener);

        // Download UI
        mDownloadCaptionTextView = (TextView) root.findViewById(R.id.tv_download_caption);
        mDownloadDescTextView = (TextView) root.findViewById(R.id.tv_download_desc);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressbar_download);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView, mDownloadCaptionTextView);
        AppApplication.getAppApplication().setFontHYGothic800();
        AppApplication.getAppApplication().setFontHYGothic700(versionTitleTextView);
        AppApplication.getAppApplication().setFontHYGothic600(mDownloadDescTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(versionValueTextView, mActionButton);

        mCurrDeviceVersionCode = mSession.getDeviceVersionCode();
        if (!"".equals(mSession.getDeviceVersionName())) {
            versionValueTextView.setText(mSession.getDeviceVersionName());
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
        if (mRetrieveTask != null) {
            mRetrieveTask.cancel(true);
        }
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
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
            boolean ret = true;
            switch (mCurrStatus) {
                case STATUS_NORMAL :
                    break;
                case STATUS_VERSION_CHECK :
                    //fall-through
                case STATUS_SERVER_DOWNLOAD :
                    //fall-through
                case STATUS_TRANSFER_FILE :
                    ret = false;
                    break;
            }
            return ret;
        }

        @Override
        public void onUSBConnectionChanged(boolean connect) {
        }

        @Override
        public void onRequestCompleted(String cmd) {
            if (CommUtil.Usb.CMD_CODE_SEND_FW_FILE.equals(cmd)) {
                // Sending resp OK
                //mActionButton.setText(R.string.latest_version);
                //mActionButton.setEnabled(false);
                //mActionButton.setOnClickListener(null);
            }
        }

        @Override
        public void onNotifyMessage(int msg, Bundle bundle) {
        }

        @Override
        public void onRequestTimeout(String cmd) {
        }

        @Override
        public void onSendFileProgressUpdate(boolean complete, long total, long progress) {
            if (complete) {
                if (total != 0) {
                    // Complete sending
                }
                mCurrStatus = STATUS_NORMAL;
                mDownloadLayout.setVisibility(View.GONE);
            } else {
                if (total > 0) {
                    mProgressBar.setProgress((int)(progress * 100 / total));
                }
            }
        }

        @Override
        public void onReceiveFileProgressUpdate(boolean complete, long total, long progress) {
        }
    };

    public View.OnClickListener mMenuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_menu :
                    mListener.onRequestMainMenu(false);
                    break;
                case R.id.iv_back :
                    getActivity().onBackPressed();
                    break;
                case R.id.btn_action:
                    if (NetworkUtil.getConnectivityStatus(getContext()) == NetworkUtil.TYPE_NOT_CONNECTED) {
                        Toast.makeText(getContext(), R.string.check_network_connection, Toast.LENGTH_SHORT).show();
                    } else if ("".equals(mSession.getDeviceVersionName())) {
                        Toast.makeText(getContext(), R.string.unknown_version_need_connection, Toast.LENGTH_SHORT).show();
                    } else {
                        if (mCurrStatus == STATUS_NORMAL &&
                                (mRetrieveTask == null && mDownloadTask == null)) {
                            mHandler.sendMessage(Message.obtain(mHandler, MSG_RETRIEVE_FW_VERSION));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private static final int MSG_RETRIEVE_FW_VERSION = 3000;
    private static final int MSG_CURRENT_IS_LATEST_VERSION = 3001;
    private static final int MSG_DOWNLOAD_FW_YES_NO = 3002;
    private static final int MSG_DOWNLOAD_POPUP_YES = 3003;
    private static final int MSG_DOWNLOAD_POPUP_NO = 3004;
    private static final int MSG_SEND_FW_FILE = 3005;
    private static final int MSG_ALREADY_DOWNLOADED_SEND_FW_YES_NO = 3006;
    private static final int MSG_SEND_POPUP_YES = 3007;
    private static final int MSG_SEND_POPUP_NO = 3008;
    private static final int MSG_ALREADY_DOWNLOADED_CHECK_USB = 3009;
    private static final int MSG_NOTIFICATION_OK = 3010;

    private static final String MSG_KEY_FW_FILE_PATH = "filepath";

    private String mFirmwareDownloadURL = "";
    private int mFirmwareServerVersionCode = 0;
    private long mFirmwareFileSize = 0;
    private String mFirmwareFileName = "";

    private static class MyHandler extends Handler {
        private final WeakReference<FirmwareFragment> mFragment;
        private MyHandler(FirmwareFragment fragment) {
            mFragment = new WeakReference<FirmwareFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final FirmwareFragment fragment = mFragment.get();
            if (fragment == null) {
                Log.e(TAG, "fragment is null");
                return;
            }
            switch (msg.what) {
                case MSG_RETRIEVE_FW_VERSION :
                    fragment.retrieveFirmWareVersion();
                    break;
                case MSG_CURRENT_IS_LATEST_VERSION :
                    Toast.makeText(fragment.getContext(), R.string.latest_version_desc,
                            Toast.LENGTH_SHORT).show();
                    fragment.mActionButton.setText(R.string.latest_version);
                    fragment.mActionButton.setEnabled(false);
                    fragment.mActionButton.setOnClickListener(null);
                    break;
                case MSG_DOWNLOAD_FW_YES_NO :
                    if ((fragment.mFirmwareFileSize + FileUtil.EXTRA_STORAGE_MARGIN) >
                            FileUtil.getExternalStorageFreeSpace()) {
                        new CustomDialog(fragment.getContext(), this)
                                .showNotificationDialog(R.string.notification,
                                        R.string.insufficient_storage, MSG_NOTIFICATION_OK);
                    } else {
                        double sizeMB = Math.floor((fragment.mFirmwareFileSize / (double) (1024 * 1024)) * 10d) / 10d;
                        new CustomDialog(fragment.getContext(), this)
                                .showQuestionYNDialog(
                                        fragment.getString(R.string.new_version),
                                        fragment.getString(R.string.found_new_version, String.valueOf(sizeMB)),
                                        MSG_DOWNLOAD_POPUP_YES, MSG_DOWNLOAD_POPUP_NO);
                    }
                    break;
                case MSG_DOWNLOAD_POPUP_YES :
                    if (NetworkUtil.getConnectivityStatus(fragment.getContext()) == NetworkUtil.TYPE_NOT_CONNECTED) {
                        Toast.makeText(fragment.getContext(), R.string.check_network_connection, Toast.LENGTH_SHORT).show();
                    } else {
                        if (fragment.mCurrStatus == STATUS_NORMAL &&
                                (fragment.mRetrieveTask == null && fragment.mDownloadTask == null)) {
                            fragment.downloadFirmWareFile(fragment.mFirmwareDownloadURL, fragment.mFirmwareFileSize, fragment.mFirmwareFileName);
                        }
                    }
                    break;
                case MSG_SEND_FW_FILE :
                    if (fragment.mListener.isUSBConnected()) {
                        Bundle data = msg.getData();
                        if (data != null) {
                            String mFirmwareFilePath = data.getString(MSG_KEY_FW_FILE_PATH);
                            if (mFirmwareFilePath != null) {
                                File file = new File(mFirmwareFilePath);
                                if (file.exists() && file.length() > 0) {
                                    fragment.mCurrStatus = STATUS_TRANSFER_FILE;
                                    fragment.mProgressBar.setProgress(0);
                                    fragment.mRootLayout.setDrawingCacheEnabled(true);
                                    Bitmap bitmap = BlurUtil.fastBlur(fragment.getContext(), fragment.mRootLayout.getDrawingCache() , 10);
                                    fragment.mRootLayout.setDrawingCacheEnabled(false);
                                    fragment.mDownloadLayout.setBackground(new BitmapDrawable(fragment.getResources(), bitmap));
                                    fragment.mDownloadCaptionTextView.setText(R.string.download_firmware_send_title);
                                    fragment.mDownloadDescTextView.setText(R.string.download_firmware_send_desc);
                                    fragment.mDownloadLayout.setVisibility(View.VISIBLE);

                                    String[] pathArray = mFirmwareFilePath.split(Constants.DIR_STR);
                                    fragment.mListener.reqSendFirmwareFile(mFirmwareFilePath, pathArray[pathArray.length -1], String.valueOf(file.length()));
                                }
                            }
                        }
                    } else {
                        Toast.makeText(fragment.getContext(),R.string.check_usb_connection, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_ALREADY_DOWNLOADED_SEND_FW_YES_NO :
                    new CustomDialog(fragment.getContext(), this)
                            .showQuestionYNDialog(R.string.new_version,
                                    R.string.exist_new_firmware_file_transfer_now_question,
                                    MSG_SEND_POPUP_YES, MSG_SEND_POPUP_NO);

                    break;
                case MSG_SEND_POPUP_YES :
                    if (fragment.mListener.isUSBConnected()) {
                        Message sendMsg = Message.obtain(this, MSG_SEND_FW_FILE);
                        Bundle data = new Bundle();
                        data.putString(MSG_KEY_FW_FILE_PATH, FileUtil.getFirmwareFileStorePath(fragment.mFirmwareFileName));
                        sendMsg.setData(data);
                        this.sendMessage(sendMsg);
                    } else {
                        Toast.makeText(fragment.getContext(),R.string.check_usb_connection, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_ALREADY_DOWNLOADED_CHECK_USB :
                    Toast.makeText(fragment.getContext(),
                            R.string.exist_new_firmware_file_need_usb_connection, Toast.LENGTH_SHORT).show();
                    break;
                default :
                    break;
            }
        }
    }

    private void retrieveFirmWareVersion() {
        mCurrStatus = STATUS_VERSION_CHECK;
        mNormalLayout.setVisibility(View.GONE);
        mRetrieveLayout.setVisibility(View.VISIBLE);
        mRetrieveTask = new RetrieveGetTask(mRetrieveVersionTaskListener);
        mRetrieveTask.execute(ServerInfo.FW_VER_RETRIEVE_URL);
    }

    private AsyncTaskListener mRetrieveVersionTaskListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            mCurrStatus = STATUS_NORMAL;
            mRetrieveLayout.setVisibility(View.GONE);
            mNormalLayout.setVisibility(View.VISIBLE);
            parseRetrieveVersionResult(jsonObject);
            mRetrieveTask = null;
        }

        @Override
        public void onFailure(Throwable thrown) {
            mCurrStatus = STATUS_NORMAL;
            mRetrieveLayout.setVisibility(View.GONE);
            mNormalLayout.setVisibility(View.VISIBLE);
            if (thrown instanceof IOException) {
                Toast.makeText(getContext(), getString(R.string.check_network_connection),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.error_reason, thrown.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "[onFailure] message : " + thrown.getMessage() + ", cause" + thrown.getCause());
            mRetrieveTask = null;
        }

        @Override
        public void onCancel() {
            mCurrStatus = STATUS_NORMAL;
            mRetrieveTask = null;
        }

        @Override
        public void onProgressUpdate(int progress) {

        }
    };

    private void parseRetrieveVersionResult(JSONObject jsonObject) {
        int errMsgResID = -1;
        try {
            if (jsonObject.has("result")) {
                if ("success".equals(jsonObject.getString("result"))) {
                    if (jsonObject.has("firmware-version")
                            && jsonObject.has("firmware-url")
                            && jsonObject.has("firmware-size")
                            && jsonObject.has("filename")) {
                        mFirmwareServerVersionCode = Integer.valueOf(jsonObject.getString("firmware-version"));

                        Message msg;
                        if (mCurrDeviceVersionCode < mFirmwareServerVersionCode) {
                            mFirmwareDownloadURL = jsonObject.getString("firmware-url");
                            mFirmwareFileSize = Long.parseLong(jsonObject.getString("firmware-size"));
                            mFirmwareFileName = jsonObject.getString("filename");

                            if (FileUtil.isExistDownloadedFirmwareFile(mFirmwareFileName, mFirmwareFileSize)) {
                                if (mListener.isUSBConnected()) {
                                    msg = Message.obtain(mHandler, MSG_ALREADY_DOWNLOADED_SEND_FW_YES_NO);
                                } else {
                                    msg = Message.obtain(mHandler, MSG_ALREADY_DOWNLOADED_CHECK_USB);
                                }
                            } else {
                                msg = Message.obtain(mHandler, MSG_DOWNLOAD_FW_YES_NO);
                            }
                        } else {
                            msg = Message.obtain(mHandler, MSG_CURRENT_IS_LATEST_VERSION);
                        }
                        mHandler.sendMessage(msg);
                    } else {
                        errMsgResID = R.string.server_error_key_not_exist;
                        Log.e(TAG, "key not exist");
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
                Toast.makeText(getContext(),
                        getString(R.string.error_reason, getString(errMsgResID)), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void downloadFirmWareFile(String strURL, long size, String filename) {
        mCurrStatus = STATUS_SERVER_DOWNLOAD;
        mProgressBar.setProgress(0);
        mRootLayout.setDrawingCacheEnabled(true);
        mRootLayout.getDrawingCache();
        Bitmap bitmap = BlurUtil.fastBlur(getContext(),mRootLayout.getDrawingCache() , 10);
        mRootLayout.setDrawingCacheEnabled(false);
        mDownloadLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
        mDownloadCaptionTextView.setText(R.string.download_firmware_download_title);
        mDownloadDescTextView.setText(R.string.download_firmware_download_desc);
        mDownloadLayout.setVisibility(View.VISIBLE);

        String filepath = FileUtil.getFirmwareFileStorePath(filename);
        mDownloadTask = new DownloadTask(mDownloadTaskListener, size, filepath);
        mDownloadTask.execute(strURL);
    }

    private AsyncTaskListener mDownloadTaskListener = new AsyncTaskListener() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            parseDownloadResult(jsonObject);
            mDownloadTask = null;
            mDownloadLayout.setVisibility(View.GONE);
            mCurrStatus = STATUS_NORMAL;
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
            Log.e(TAG, "[onFailure] message : " + thrown.getMessage() + ", cause : " + thrown.getCause());
            mDownloadTask = null;
            mDownloadLayout.setVisibility(View.GONE);
            mCurrStatus = STATUS_NORMAL;
        }

        @Override
        public void onCancel() {
            mDownloadTask = null;
            if (mDownloadLayout != null) {
                mDownloadLayout.setVisibility(View.GONE);
            }
            mCurrStatus = STATUS_NORMAL;
        }

        @Override
        public void onProgressUpdate(int progress) {
            mProgressBar.setProgress(progress);
        }
    };

    private void parseDownloadResult(JSONObject jsonObject) {
        try {
            if (jsonObject.has("result")) {
                if ("success".equals(jsonObject.getString("result"))) {
                    if (jsonObject.has("filepath")) {
                        if (mListener.isUSBConnected()) {
                            Toast.makeText(getContext(), R.string.download_complete_description, Toast.LENGTH_SHORT).show();
                            Message msg = Message.obtain(mHandler, MSG_SEND_FW_FILE);
                            Bundle data = new Bundle();
                            data.putString(MSG_KEY_FW_FILE_PATH, jsonObject.getString("filepath"));
                            msg.setData(data);
                            mHandler.sendMessage(msg);
                        } else {
                            Toast.makeText(getContext(),
                                    R.string.download_complete_need_usb_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e(TAG, "result is not success");
                }
            } else {
                Log.e(TAG, "result key not exist");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
