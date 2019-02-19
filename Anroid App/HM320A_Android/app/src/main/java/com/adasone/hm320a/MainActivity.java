package com.adasone.hm320a;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.application.Constants.Menu;
import com.adasone.hm320a.data.CalibrationData;
import com.adasone.hm320a.data.DriverData;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VehicleData;
import com.adasone.hm320a.data.VideoData;
import com.adasone.hm320a.database.DBHelper;
import com.adasone.hm320a.database.DBManager;
import com.adasone.hm320a.fragment.AutoCalibrationBonnetFragment;
import com.adasone.hm320a.fragment.AutoCalibrationChessFragment;
import com.adasone.hm320a.fragment.AutoCalibrationTotalFragment;
import com.adasone.hm320a.fragment.CalibrationBonnetFragment;
import com.adasone.hm320a.fragment.CalibrationCamLocationFragment;
import com.adasone.hm320a.fragment.CalibrationDistanceFragment;
import com.adasone.hm320a.fragment.CalibrationTotalFragment;
import com.adasone.hm320a.fragment.CalibrationVanishFragment;
import com.adasone.hm320a.fragment.DtgDriverInfoEditFragment;
import com.adasone.hm320a.fragment.DtgInfoViewFragment;
import com.adasone.hm320a.fragment.EnginerrModeFragment;
import com.adasone.hm320a.fragment.FirmwareFragment;
import com.adasone.hm320a.fragment.MainMenuFragment;
import com.adasone.hm320a.fragment.SplashFragment;
import com.adasone.hm320a.fragment.VehicleInfoEditFragment;
import com.adasone.hm320a.fragment.VehicleInfoViewFragment;
import com.adasone.hm320a.fragment.VideoListFragment;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.interfaces.TimeoutObserver;
import com.adasone.hm320a.util.CommUtil;
import com.adasone.hm320a.util.DisplayUtil;
import com.adasone.hm320a.util.FileUtil;
import com.adasone.hm320a.util.LogUtil;
import com.adasone.hm320a.util.Watchdog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    // USB Message
    private static final boolean SHOW_DEBUG_TOAST = Boolean.FALSE;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private static ArrayDeque<OnActivityInteractionListener> mActivityInteractionListener = new ArrayDeque<OnActivityInteractionListener>(50);
    private final MyHandler mHandler = new MyHandler(this);
    private java.lang.Process mLogProcess = null;

    private boolean mUSBConnectionStatus = false;
    private boolean mUsbReceiverRegistered = false;
    private UsbManager mUsbManager;
    private UsbAccessory mAccessory = null;
    private ParcelFileDescriptor mFileDescriptor = null;

    private USBReceiveThread mUSBReceiveThread = null;
    private USBSendThread mUSBSendThread = null;
    private BlockingQueue<SendJob> mSendJobBlockingQueue = new LinkedBlockingQueue<SendJob>(10);

    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending = false;
    private boolean mDefaultInfoRequestPending = false;
    private boolean mConnectionFailPending = false;

    private int mAttachMode = Constants.AttachMode.USB_CONN;
    private Session mSession = new Session();

    private static final int NORMAL_STATE = 0;
    private static final int SPLASH_STATE = 1;
    private static final int FIRST_SETTING_STATE = 2;

    private int mCurrState = SPLASH_STATE;

    // Download video file path
    private String mDownloadFilePath;

    private ConcurrentHashMap<String, Watchdog> mWatchdogMap = new ConcurrentHashMap<>();

    private Dialog mCommunicationDialog = null;

    private class SendJob {
        boolean checkTimeout;
        String type;
        String cmd;
        private StringBuilder sb = new StringBuilder();

        SendJob (boolean checkTimeout, String type, String cmd, String ... args) {
            this.checkTimeout = checkTimeout;
            this.type = type;
            this.cmd = cmd;
            sb.append(type).append(CommUtil.Usb.CHAR_SPLIT).append(cmd);
            for (String arg:args) {
                sb.append(CommUtil.Usb.CHAR_SPLIT);
                sb.append(arg);
            }
        }
        SendJob (boolean checkTimeout, String type, String cmd) {
            this.checkTimeout = checkTimeout;
            this.type = type;
            this.cmd = cmd;
            sb.append(type).append(CommUtil.Usb.CHAR_SPLIT).append(cmd);
        }

        String getTransferData() {
            return sb.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AppApplication.isDebug()) {
            mLogProcess = LogUtil.startSaveLogcatToFile();
        }
        DBManager.initializeInstance(new DBHelper(getApplicationContext()));

        registerUsbReceiver();

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if (getIntent() != null &&
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(getIntent().getAction())) {
            mAccessory = (UsbAccessory) getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ACCESSORY_ATTACH_USB_CONN));
        } else {
            UsbAccessory[] accessories = mUsbManager.getAccessoryList();
            mAccessory = (accessories == null ? null : accessories[0]);

            if (mAccessory != null) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_ACCESSORY_ATTACH_LAUNCH_APP));
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        replaceFragment(SplashFragment.newInstance(), SplashFragment.FRAGMENT_TAG, false, Constants.Anim.NONE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null &&
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
            mAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_ACCESSORY_ATTACH_AFTER_APP_RUNNING));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterUsbReceiver();
        removeAllWatchdogFromMap();
        closeAccessory(false);
        mHandler.removeCallbacksAndMessages(null);
        if (mLogProcess != null) {
            LogUtil.stopSaveLogcatToFile(mLogProcess);
            mLogProcess = null;
        }
        if (mCommunicationDialog != null) {
            mCommunicationDialog.dismiss();
            mCommunicationDialog = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        // super.onWindowFocusChanged(hasFocus);

        if( hasFocus ) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            if ((uiOptions & View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    == View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            DisplayUtil.immersiveModeOn(this);
        }
    }

    private static final int MSG_ACCESSORY_ATTACH_LAUNCH_APP = 3001;
    private static final int MSG_ACCESSORY_ATTACH_AFTER_APP_RUNNING = 3002;
    private static final int MSG_ACCESSORY_ATTACH_USB_CONN = 3003;
    private static final int MSG_ACCESSORY_DETACHED = 3004;
    private static final int MSG_CONNECTION_FAIL_DIALOG_OK = 4001;
    private static final int MSG_CONNECTION_OPEN_DIALOG_OK = 4002;
    private static final int MSG_CONNECTION_CLOSE_DIALOG_OK = 4003;
    private static final int MSG_SEND_VEHICLE_INFO_DIALOG_OK = 4004;
    private static final int MSG_SEND_CALIBRATION_INFO_DIALOG_OK = 4005;
    private static final int MSG_RECEIVE_CALIBRATION_PIC_DIALOG_OK = 4006;
    private static final int MSG_SEND_DTG_INFO_DIALOG_OK = 4007;

    private static final int MSG_RESP_FROM_DEVICE = 5001;
    private static final int MSG_VIDEO_LIST_BUILD_COMPLETED = 5002;
    private static final int MSG_VIDEO_FILE_DOWNLOAD_PROGRESS = 5003;
    private static final int MSG_SENDING_FILE_PROGRESS = 5004;

    private static final int MSG_NOTIFICATION_OK = 6001;

    private static final String MSG_KEY_TOTAL = "total";
    private static final String MSG_KEY_PROGRESS = "progress";

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_ACCESSORY_ATTACH_LAUNCH_APP :
                    activity.mAttachMode = Constants.AttachMode.LAUNCH_APP;
                    activity.openOrRequestPermission();
                    break;
                case MSG_ACCESSORY_ATTACH_AFTER_APP_RUNNING :
                    activity.mAttachMode = Constants.AttachMode.AFTER_APP_RUNNING;
                    activity.openOrRequestPermission();
                    break;
                case MSG_ACCESSORY_ATTACH_USB_CONN :
                    activity.mAttachMode = Constants.AttachMode.USB_CONN;
                    activity.openOrRequestPermission();
                    break;
                case MSG_ACCESSORY_DETACHED :
                    if (activity.mCurrState == NORMAL_STATE) {
                        activity.closeAccessory(true);
                    } else {
                        activity.finish();
                    }
                    break;
                case MSG_CONNECTION_FAIL_DIALOG_OK :
                    activity.finish();
                    break;
                case MSG_CONNECTION_OPEN_DIALOG_OK :
                    break;
                case MSG_CONNECTION_CLOSE_DIALOG_OK :
                    break;
                case MSG_SEND_VEHICLE_INFO_DIALOG_OK :
                    break;
                case MSG_SEND_CALIBRATION_INFO_DIALOG_OK :
                    break;
                case MSG_RECEIVE_CALIBRATION_PIC_DIALOG_OK :
                    break;
                case MSG_SEND_DTG_INFO_DIALOG_OK :
                    break;
                case MSG_RESP_FROM_DEVICE :
                    activity.parseDeviceResponseData((String)msg.obj);
                    break;
                case MSG_VIDEO_LIST_BUILD_COMPLETED:
                    activity.notifyRequestCompleted(VideoListFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_VIDEO_LIST);
                    break;
                case MSG_VIDEO_FILE_DOWNLOAD_PROGRESS:
                    Bundle v_bundle = msg.getData();
                    long v_total = v_bundle.getLong(MSG_KEY_TOTAL);
                    long v_progress = v_bundle.getLong(MSG_KEY_PROGRESS);

                    activity.publishReceiveFileProgress(VideoListFragment.FRAGMENT_TAG, (v_progress >= v_total), v_total, v_progress);
                    if (v_progress >= v_total) {
                        activity.notifyMessage(MainMenuFragment.FRAGMENT_TAG, Constants.NotifyMsg.VIDEO_COUNT_REFRESH, null);
                    }
                    break;
                case MSG_SENDING_FILE_PROGRESS:
                    Bundle f_bundle = msg.getData();
                    long f_total = f_bundle.getLong(MSG_KEY_TOTAL);
                    long f_progress = f_bundle.getLong(MSG_KEY_PROGRESS);

                    activity.publishSendFileProgress(FirmwareFragment.FRAGMENT_TAG , (f_progress >= f_total), f_total, f_progress);
                    break;
                default :
                    break;
            }
        }
    }

    private void openOrRequestPermission() {
        if (mAccessory != null) {
            if (mUsbManager.hasPermission(mAccessory)) {
                openAccessory();
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mPermissionRequestPending = true;
                        mUsbManager.requestPermission(mAccessory, mPermissionIntent);
                    }
                }
            }
        }
    }

    private void openAccessory() {
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();

            if (mUSBReceiveThread != null) {
                mUSBReceiveThread.interrupt();
            }
            mUSBReceiveThread = new USBReceiveThread(mHandler, new FileInputStream(fd));
            if (mUSBSendThread != null) {
                mUSBSendThread.interrupt();
            }
            mUSBSendThread = new USBSendThread(mHandler, new FileOutputStream(fd));
            mUSBReceiveThread.start();
            mUSBSendThread.start();
            setUSBConnectionStatus(true);
            mSession.initialize();
            /*
            Toast.makeText(getApplicationContext(),
                    R.string.connected_to_device, Toast.LENGTH_SHORT).show();
            */
            if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                mCommunicationDialog.dismiss();
                mCommunicationDialog = null;
            }
            mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                    .showNotificationDialog(
                            R.string.device_connection_dialog_title,
                            R.string.device_connection_dialog_open_desc,
                            MSG_CONNECTION_OPEN_DIALOG_OK);

            if (mAttachMode == Constants.AttachMode.AFTER_APP_RUNNING) {
                if (mCurrState == NORMAL_STATE) {
                    onRequestMainMenu(false);
                    reqDeviceInfo();
                }
            }
        } else {
            mConnectionFailPending = true;
            setUSBConnectionStatus(false);
            if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                mCommunicationDialog.dismiss();
                mCommunicationDialog = null;
            }
            mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                    .showNotificationDialog(
                            R.string.device_connection_failure,
                            R.string.device_connection_failure_desc,
                            MSG_CONNECTION_FAIL_DIALOG_OK);
        }
    }

    private void closeAccessory(boolean detached) {
        removeAllWatchdogFromMap();

        if (mUSBReceiveThread != null) {
            mUSBReceiveThread.interrupt();
            if (!detached && isUSBConnected()) {
                reqSoftClose();
            }
            try {
                mUSBReceiveThread.join(2000);
                mUSBReceiveThread = null;
            } catch (InterruptedException ignore) {
            }
        }


        if (mUSBSendThread != null) {
            mUSBSendThread.interrupt();
            try {
                mUSBSendThread.join(2000);
                mUSBSendThread = null;
            } catch (InterruptedException ignore) {
            }
        }

        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException ignore) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
        if (isUSBConnected()) {
            if (detached) {
                /*
                Toast.makeText(getApplicationContext(),
                        R.string.disconnected_to_device, Toast.LENGTH_SHORT).show();
                */
                if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                    mCommunicationDialog.dismiss();
                    mCommunicationDialog = null;
                }
                mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                        .showNotificationDialog(
                                R.string.device_connection_dialog_title,
                                R.string.device_connection_dialog_close_desc,
                                MSG_CONNECTION_CLOSE_DIALOG_OK);
            }
            setUSBConnectionStatus(false);
        }
    }

    private void setUSBConnectionStatus(boolean connect) {
        mUSBConnectionStatus = connect;
        try {
            OnActivityInteractionListener activityInteractionListener = mActivityInteractionListener.getFirst();
            activityInteractionListener.onUSBConnectionChanged(mUSBConnectionStatus);
        } catch (NoSuchElementException ignored) {
        }
    }

    private void notifyRequestCompleted(String fragmentTag, String cmd) {
        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            if(fragmentTag.equals(listener.getTag())) {
                listener.onRequestCompleted(cmd);
                break;
            }
        }
    }

    private void notifyAllRequestCompleted(String cmd) {
        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            listener.onRequestCompleted(cmd);
        }
    }

    @Override
    public void notifyMessage(String fragmentTag, int msg, Bundle bundle) {
        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            if(fragmentTag.equals(listener.getTag())) {
                listener.onNotifyMessage(msg, bundle);
                break;
            }
        }
    }

    public void notifyCalibrationImageUpdate() {
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

        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            if(CalibrationCamLocationFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || CalibrationBonnetFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || CalibrationVanishFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || CalibrationDistanceFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || CalibrationTotalFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || AutoCalibrationChessFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || AutoCalibrationBonnetFragment.FRAGMENT_TAG.equals(listener.getTag())
                    || AutoCalibrationTotalFragment.FRAGMENT_TAG.equals(listener.getTag())) {
                listener.onNotifyMessage(Constants.NotifyMsg.CALIBRATION_IMAGE_REFRESH, null);
            }
        }
    }

    private void publishSendFileProgress(String fragmentTag, boolean complete, long total, long progress) {
        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            if(fragmentTag.equals(listener.getTag())) {
                listener.onSendFileProgressUpdate(complete, total, progress);
                break;
            }
        }
    }

    private void publishReceiveFileProgress(String fragmentTag, boolean complete, long total, long progress) {
        for(OnActivityInteractionListener listener : mActivityInteractionListener) {
            if(fragmentTag.equals(listener.getTag())) {
                listener.onReceiveFileProgressUpdate(complete, total, progress);
                break;
            }
        }
    }

    public void replaceFragment(Fragment fragment, String tag, boolean allowingStateLoss, int animation) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (animation) {
            case Constants.Anim.SLIDING:
                transaction.setCustomAnimations(R.anim.sliding_left, R.anim.sliding_right, R.anim.sliding_left, R.anim.sliding_right);
                break;
            case Constants.Anim.FADE:
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                break;
        }
        transaction.replace(R.id.container, fragment, tag);
        if (allowingStateLoss) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }

    public void addFragment(Fragment fragment, String tag, int animation) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (animation) {
            case Constants.Anim.SLIDING:
                transaction.setCustomAnimations(R.anim.sliding_left, R.anim.sliding_right, R.anim.sliding_left, R.anim.sliding_right);
                break;
            case Constants.Anim.FADE:
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                break;
        }
        transaction.add(R.id.container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        try {
            OnActivityInteractionListener activityInteractionListener = mActivityInteractionListener.getFirst();
            if (activityInteractionListener.onBackPressed()) {
                super.onBackPressed();
            }
        } catch (NoSuchElementException e) {
            super.onBackPressed();
        }
    }

    /* OnFragmentInteractionListener */
    @Override
    public boolean isUSBConnected() {
        return mUSBConnectionStatus;
    }

    @Override
    public boolean isUSBPermissionRequestPending() {
        return mPermissionRequestPending;
    }

    @Override
    public boolean isUSBDefaultInfoRequestPending() {
        return mDefaultInfoRequestPending;
    }
    @Override
    public boolean isUSBConnectionFailPending() {
        return mConnectionFailPending;
    }
    @Override
    public void onSplashDisplayFinish() {
        if (isUSBConnected()) {
            //if ("".equals(mSession.getDevVehicleData().getUniqueID())) {
            //    mCurrState = FIRST_SETTING_STATE;
            //    replaceFragment(VehicleInfoEditFragment.newInstance(Constants.ParamFrom.FIRST),
            //            VehicleInfoEditFragment.FRAGMENT_TAG, true, Constants.Anim.NONE);
            //} else {
                mCurrState = NORMAL_STATE;
                replaceFragment(MainMenuFragment.newInstance(),
                        MainMenuFragment.FRAGMENT_TAG, true, Constants.Anim.NONE);
            //}
        } else {
            mSession.setDeviceToken(DBManager.getInstance().getLatestDeviceToken());
            if (!"".equals(mSession.getDeviceToken())) {
                mSession.setDeviceVersionName(DBManager.getInstance().getFirmwareVersionName(mSession.getDeviceToken()));
                mSession.setDeviceVersionCode(DBManager.getInstance().getFirmwareVersionCode(mSession.getDeviceToken()));
                mSession.setDeviceDtgSupport(DBManager.getInstance().getDtgSupported(mSession.getDeviceToken()));
                mSession.getDevVehicleData().copyFrom(
                        DBManager.getInstance().getVehicleData(mSession.getDeviceToken()));
                mSession.getDevDriverData().copyFrom(
                        DBManager.getInstance().getDriverData(mSession.getDeviceToken()));
                mSession.getDevCalibrationData().copyFrom(
                        DBManager.getInstance().getCalibrationData(mSession.getDeviceToken()));
            }
            mCurrState = NORMAL_STATE;
            replaceFragment(MainMenuFragment.newInstance(),
                    MainMenuFragment.FRAGMENT_TAG, true, Constants.Anim.NONE);
        }
    }

    @Override
    public void onMenuSelected(int menu, int arg) {
        switch (menu) {
            case Menu.APP_FINISH:
                finish();
                break;
            case Menu.VEHICLE:
                addFragment(VehicleInfoViewFragment.newInstance(), VehicleInfoViewFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.VEHICLE_INFO_EDIT:
                addFragment(VehicleInfoEditFragment.newInstance(0), VehicleInfoEditFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.VEHICLE_CAN_SETTINGS_EDIT:
                addFragment(VehicleInfoEditFragment.newInstance(1), VehicleInfoEditFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.CALIBRATION_CAM_LOCATION:
                if (FileUtil.isExistCalibrationNormalPicture(mSession.getDeviceToken()) || FileUtil.isExistCalibrationFirstAutoPicture(mSession.getDeviceToken())) {
                    addFragment(CalibrationCamLocationFragment.newInstance(), CalibrationCamLocationFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.calibration_img_not_exit, Toast.LENGTH_SHORT).show();
                }
                break;
            case Menu.CALIBRATION_BONNET:
                addFragment(CalibrationBonnetFragment.newInstance(), CalibrationBonnetFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.CALIBRATION_VANISH:
                addFragment(CalibrationVanishFragment.newInstance(), CalibrationVanishFragment.FRAGMENT_TAG, Constants.Anim.SLIDING);
                break;
            case Menu.CALIBRATION_DISTANCE:
                addFragment(CalibrationDistanceFragment.newInstance(), CalibrationDistanceFragment.FRAGMENT_TAG, Constants.Anim.SLIDING);
                break;
            case Menu.CALIBRATION_TOTAL:
                addFragment(CalibrationTotalFragment.newInstance(), CalibrationTotalFragment.FRAGMENT_TAG, Constants.Anim.SLIDING);
                break;
            case Menu.AUTO_CALIBRATION_CHESS:
                addFragment(AutoCalibrationChessFragment.newInstance(), AutoCalibrationChessFragment.FRAGMENT_TAG, Constants.Anim.SLIDING);
                break;
            case Menu.AUTO_CALIBRATION_BONNET:
                addFragment(AutoCalibrationBonnetFragment.newInstance(), AutoCalibrationBonnetFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.AUTO_CALIBRATION_TOTAL:
                addFragment(AutoCalibrationTotalFragment.newInstance(), AutoCalibrationTotalFragment.FRAGMENT_TAG, Constants.Anim.SLIDING);
                break;
            case Menu.DTG:
                addFragment(DtgInfoViewFragment.newInstance(), DtgInfoViewFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.DTG_DRIVER_INFO_EDIT:
                addFragment(DtgDriverInfoEditFragment.newInstance(), DtgDriverInfoEditFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.VIDEO:
                addFragment(EnginerrModeFragment.newInstance(), EnginerrModeFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            case Menu.FIRMWARE:
                addFragment(FirmwareFragment.newInstance(), FirmwareFragment.FRAGMENT_TAG, Constants.Anim.NONE);
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestMainMenu(boolean first) {
        // TODO : Fix after-image effects
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (first) {
            mCurrState = NORMAL_STATE;
            replaceFragment(MainMenuFragment.newInstance(), MainMenuFragment.FRAGMENT_TAG, false, Constants.Anim.NONE);
        }
    }

    @Override
    public Session getSession() {
        return mSession;
    }

    private void registerUsbReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        getApplicationContext().registerReceiver(mUsbReceiver, filter);
        mUsbReceiverRegistered = true;
    }

    private void unregisterUsbReceiver() {
        try {
            if (mUsbReceiverRegistered) {
                getApplicationContext().unregisterReceiver(mUsbReceiver);
                mUsbReceiverRegistered = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)) {
                synchronized(this){
                    mAccessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)) {
                        mPermissionRequestPending = false;
                        openAccessory();
                    } else {
                        Log.i(TAG, "Permission denied");
                        finish();
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals((mAccessory))) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_ACCESSORY_DETACHED));
                }
            }
        }
    };

    @Override
    public void addActivityInteractionListener(OnActivityInteractionListener listener) {
        try {
            mActivityInteractionListener.push(listener);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeActivityInteractionListener(OnActivityInteractionListener listener) {
        try {
            mActivityInteractionListener.remove(listener);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void reqDeviceInfo() {
        mDefaultInfoRequestPending = true;
        reqReadVersionInfo();
    }

    @Override
    public boolean reqReadVersionInfo() {
        boolean ret = true;
        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_VERSION_INFO);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqReadVehicleInfo() {
        boolean ret = true;
        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_READ_VEHICLE_INFO);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqWriteVehicleInfo() {
        boolean ret = true;
        VehicleData data = mSession.getPreVehicleData();

        SendJob job = new SendJob(true,
                CommUtil.Usb.DATA_TYPE_REQ,
                CommUtil.Usb.CMD_CODE_WRITE_VEHICLE_INFO,
                String.valueOf(data.getVehicleType()),
                data.getUniqueID(),
                data.getManufacturer(),
                data.getModel(),
                data.getFuelType(),
                data.getReleaseDate(),
                data.getOBDManufacturerID(),
                data.getOBDModelID(),
                data.getOBDFuelTypeID());

        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqReadCalibrationInfo() {
        boolean ret = true;
        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_READ_CALIBRATION_INFO);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqWriteCalibrationInfo() {
        boolean ret = true;
        SendJob job = null;
        CalibrationData data = mSession.getPreCalibrationData();

        if (!"".equals(data.getDate())) {
            job = new SendJob(true,
                    CommUtil.Usb.DATA_TYPE_REQ,
                    CommUtil.Usb.CMD_CODE_WRITE_CALIBRATION_INFO,
                    data.getDate(),
                    String.valueOf(data.getCameraHeight()),
                    String.valueOf(data.getVehicleWidth()),
                    String.valueOf(data.getCameraToBumper()),
                    String.valueOf(data.getBonnetPoint().y),
                    String.valueOf(data.getVanishingY()),
                    String.valueOf(data.getCenterX()),
                    String.valueOf(data.getNearY()),
                    String.valueOf(data.getNearLeftX()),
                    String.valueOf(data.getNearRightX()),
                    String.valueOf(data.getFarY()),
                    String.valueOf(data.getFarLeftX()),
                    String.valueOf(data.getFarRightX()),
                    String.valueOf(data.getFarDistance()),
                    String.valueOf(data.getNearDistance())
            );
        } else {
            ret = false;
        }

        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }


    @Override
    public boolean reqReadDTGDriverInfo() {
        boolean ret = true;
        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_READ_DTG_DRIVER_INFO);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqWriteDTGDriverInfo() {
        boolean ret = true;
        DriverData data = mSession.getPreDriverData();

        SendJob job = new SendJob(true,
                CommUtil.Usb.DATA_TYPE_REQ,
                CommUtil.Usb.CMD_CODE_WRITE_DTG_DRIVER_INFO,
                String.valueOf(data.getVehicleType()),
                data.getVIN(),
                data.getVehicleRegNo(),
                data.getBusinessRegNo(),
                data.getFormatDriverCode());

        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqVideoFileList() {
        boolean ret = true;

        mSession.initializeVideoList();

        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_VIDEO_LIST);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqDownloadVideoFile(String filepath, String filename) {
        boolean ret = true;

        SendJob job = new SendJob(true, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_DOWNLOAD_VIDEO, filename);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
            mDownloadFilePath = filepath;
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean reqCancelDownloadVideoFile() {
        boolean ret = true;

        if (mUSBReceiveThread != null) {
            mUSBReceiveThread.cancelVideoFileDownload();
            SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_DOWNLOAD_CANCEL);
            if (mUSBSendThread != null) {
                mUSBSendThread.registerJob(job);
            } else {
                ret = false;
            }
        } else {
            ret = false;
        }
        return ret;
    }

    public boolean respCalibrationNormalFileReceived() {
        boolean ret = true;

        if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
            mCommunicationDialog.dismiss();
            mCommunicationDialog = null;
        }
        mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                .showNotificationDialog(
                        R.string.notice,
                        R.string.calibration_img_received,
                        MSG_RECEIVE_CALIBRATION_PIC_DIALOG_OK);
        /*
        Toast.makeText(getApplicationContext(), R.string.calibration_img_received, Toast.LENGTH_SHORT).show();
        */

        SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_RESP,
                CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_NORMAL_PIC, CommUtil.Usb.CHAR_RESP_SUCCESS);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        notifyCalibrationImageUpdate();
        return ret;
    }

    public boolean respCalibrationAutoFirstFileReceived() {
        boolean ret = true;

        SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_RESP,
                CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_AUTO_FIRST_PIC, CommUtil.Usb.CHAR_RESP_SUCCESS);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    public boolean respCalibrationAutoSecondFileReceived() {
        boolean ret = true;

        if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
            mCommunicationDialog.dismiss();
            mCommunicationDialog = null;
        }
        mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                .showNotificationDialog(
                        R.string.notice,
                        R.string.calibration_img_received,
                        MSG_RECEIVE_CALIBRATION_PIC_DIALOG_OK);
        /*
        Toast.makeText(getApplicationContext(), R.string.calibration_img_received, Toast.LENGTH_SHORT).show();
        */
        SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_RESP,
                CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_AUTO_SECOND_PIC, CommUtil.Usb.CHAR_RESP_SUCCESS);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        notifyCalibrationImageUpdate();
        return ret;
    }

    @Override
    public boolean reqSendFirmwareFile(String filepath,  String filename, String size) {
        boolean ret = true;
        if (mUSBSendThread != null) {
            mUSBSendThread.setSendingFilePath(filepath);
            SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_SEND_FW_FILE, size, filename);
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    public boolean reqSoftClose() {
        boolean ret = true;
        SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_REQ, CommUtil.Usb.CMD_CODE_SOFT_CLOSE);
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    public boolean respNotSupported(String cmd) {
        boolean ret = true;

        SendJob job = new SendJob(false, CommUtil.Usb.DATA_TYPE_RESP, cmd, CommUtil.Usb.CHAR_RESP_FAIL, CommUtil.Usb.REASON_NOT_SUPPORTED );
        if (mUSBSendThread != null) {
            mUSBSendThread.registerJob(job);
        } else {
            ret = false;
        }
        return ret;
    }

    private class USBReceiveThread extends Thread {
        private static final String THREAD_NAME = "USBReceiveThread";

        private static final int WAIT_MODE_NORMAL = 1;
        private static final int WAIT_MODE_CALIBRATION_NORMAL_FILE = 2;
        private static final int WAIT_MODE_CALIBRATION_AUTO_FIRST_FILE = 3;
        private static final int WAIT_MODE_CALIBRATION_AUTO_SECOND_FILE = 4;
        private static final int WAIT_MODE_VIDEO_LIST = 5;
        private static final int WAIT_MODE_VIDEO_FILE = 6;

        final int BUFFER_SIZE = 16384; // 16 * 1024
        byte[] buffer = new byte[BUFFER_SIZE];
        Handler mMainHandler;
        FileInputStream mFIS;
        int mWaitingMode = WAIT_MODE_NORMAL;
        String mFilePath;
        long mFileSize = 0;
        long mReadSize = 0;
        FileOutputStream mFos = null;
        Bundle mMsgBundle = new Bundle();

        USBReceiveThread(Handler handler, FileInputStream fis) {
            super(THREAD_NAME);
            mMainHandler = handler;
            mFIS = fis;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final int bytes = mFIS.read(buffer);

                    if (bytes > 0) {
                        switch (getWaitMode()) {
                            case WAIT_MODE_NORMAL:
                                if (!Thread.currentThread().isInterrupted()) {
                                    String dataType = new String(buffer, 0, 3, java.nio.charset.StandardCharsets.UTF_8);
                                    if (CommUtil.Usb.DATA_TYPE_REQ.equals(dataType) || CommUtil.Usb.DATA_TYPE_RESP.equals(dataType)) {

                                        mMainHandler.sendMessage(
                                                mMainHandler.obtainMessage(MSG_RESP_FROM_DEVICE,
                                                        new String(buffer, 0, bytes, java.nio.charset.StandardCharsets.UTF_8)));
                                        synchronized (this) {
                                            this.wait();
                                        }
                                    } else {
                                        // ignore
                                    }
                                }
                                break;
                            case WAIT_MODE_CALIBRATION_NORMAL_FILE:
                            case WAIT_MODE_CALIBRATION_AUTO_FIRST_FILE:
                                // fall-through
                            case WAIT_MODE_CALIBRATION_AUTO_SECOND_FILE:
                                try {
                                    if (mFos == null) {
                                        File file = new File(mFilePath);
                                        if (file.exists()) {
                                            boolean del = file.delete();
                                        }
                                        mFos = new FileOutputStream(mFilePath);
                                    }

                                    mFos.write(buffer, 0, bytes);
                                    mReadSize += bytes;

                                    if (mFileSize <= mReadSize) {
                                        mFos.flush();
                                        mFos.close();
                                        mFos = null;
                                        final int mode = mWaitingMode;
                                        mWaitingMode = WAIT_MODE_NORMAL;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mode == WAIT_MODE_CALIBRATION_NORMAL_FILE) {
                                                    FileUtil.deleteCalibrationAutoFileIfExist(mSession.getDeviceToken());
                                                    respCalibrationNormalFileReceived();
                                                } else if (mode == WAIT_MODE_CALIBRATION_AUTO_FIRST_FILE) {
                                                    FileUtil.deleteCalibrationNormalFileIfExist(mSession.getDeviceToken());
                                                    respCalibrationAutoFirstFileReceived();
                                                } else if (mode == WAIT_MODE_CALIBRATION_AUTO_SECOND_FILE) {
                                                    respCalibrationAutoSecondFileReceived();
                                                }
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    mFos = null;
                                    mWaitingMode = WAIT_MODE_NORMAL;
                                }
                                break;
                            case WAIT_MODE_VIDEO_LIST:
                                try {
                                    String data = new String(buffer, 0, bytes, java.nio.charset.StandardCharsets.UTF_8);
                                    parseVideoFileListNextResp(data.split(CommUtil.Usb.CHAR_SPLIT));
                                } catch (IndexOutOfBoundsException ignore) {
                                }
                                break;
                            case WAIT_MODE_VIDEO_FILE:
                                try {
                                    if (mFos == null) {
                                        File file = new File(mFilePath);
                                        if (file.exists()) {
                                            boolean del = file.delete();
                                        }
                                        mFos = new FileOutputStream(mFilePath);
                                    }

                                    mFos.write(buffer, 0, bytes);
                                    mReadSize += bytes;

                                    if (mFileSize <= mReadSize) {
                                        mFos.flush();
                                        mFos.close();
                                        mFos = null;
                                        mWaitingMode = WAIT_MODE_NORMAL;
                                    }

                                    mMsgBundle.putLong(MSG_KEY_TOTAL, mFileSize);
                                    mMsgBundle.putLong(MSG_KEY_PROGRESS, mReadSize);
                                    Message msg = mMainHandler.obtainMessage(MSG_VIDEO_FILE_DOWNLOAD_PROGRESS);
                                    msg.setData(mMsgBundle);
                                    mMainHandler.sendMessage(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    mFos = null;
                                    mWaitingMode = WAIT_MODE_NORMAL;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (getWaitMode() == WAIT_MODE_NORMAL) {
                        Thread.sleep(10);
                    }
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            this.cancelVideoFileDownload();
            if (mFIS != null) {
                try {
                    mFIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mFIS = null;
                }
            }
        }

        synchronized int getWaitMode() {
            return mWaitingMode;
        }

        synchronized void setWaitModeNormal() {
            mFos = null;
            mWaitingMode = WAIT_MODE_NORMAL;
        }

        synchronized void setWaitModeReceiveCalibrationNormalFile(long fileSize) {
            mWaitingMode = WAIT_MODE_CALIBRATION_NORMAL_FILE;
            mFilePath = FileUtil.getCalibrationNormalFileStorePath(mSession.getDeviceToken());
            mFileSize = fileSize;
            mReadSize = 0;
        }

        synchronized void setWaitModeReceiveCalibrationAutoFirstFile(long fileSize) {
            mWaitingMode = WAIT_MODE_CALIBRATION_AUTO_FIRST_FILE;
            mFilePath = FileUtil.getCalibrationFirstAutoFileStorePath(mSession.getDeviceToken());
            mFileSize = fileSize;
            mReadSize = 0;
        }

        synchronized void setWaitModeReceiveCalibrationAutoSecondFile(long fileSize) {
            mWaitingMode = WAIT_MODE_CALIBRATION_AUTO_SECOND_FILE;
            mFilePath = FileUtil.getCalibrationSecondAutoFileStorePath(mSession.getDeviceToken());
            mFileSize = fileSize;
            mReadSize = 0;
        }

        synchronized void setWaitModeReceiveVideoList() {
            mWaitingMode = WAIT_MODE_VIDEO_LIST;
        }

        synchronized void setWaitModeReceiveVideoFile(String filePath, long fileSize) {
            mWaitingMode = WAIT_MODE_VIDEO_FILE;
            mFilePath = filePath;
            mFileSize = fileSize;
            mReadSize = 0;
        }

        synchronized void cancelVideoFileDownload() {
            mWaitingMode = WAIT_MODE_NORMAL;
            if (mFos != null) {
                try {
                    mFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mFos = null;
                }
                File videoFile = new File(mFilePath);
                if (videoFile.exists()) {
                    boolean delete = videoFile.delete();
                }
                mFilePath = "";
                mFileSize = 0;
                mReadSize = 0;
            }
        }

        void wakeUp() {
            synchronized(this) {
                this.notify();
            }
        }
    }

    private class USBSendThread extends Thread {
        private static final String THREAD_NAME = "USBSendThread";
        Handler mMainHandler;
        FileOutputStream mFOS;
        String mFilepath = "";
        final int BUFFER_SIZE = 16384; // 16 * 1024
        byte[] buffer = new byte[BUFFER_SIZE];
        Bundle mMsgBundle = new Bundle();

        USBSendThread(Handler handler, FileOutputStream fos) {
            super(THREAD_NAME);
            mMainHandler = handler;
            mFOS = fos;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final SendJob job = mSendJobBlockingQueue.take();

                    if (job.checkTimeout) {
                        addWatchdogToMap(job.cmd);
                    }

                    if (CommUtil.Usb.CMD_CODE_DOWNLOAD_CANCEL.equals(job.cmd)) {
                        removeWatchdogFromMap(CommUtil.Usb.CMD_CODE_DOWNLOAD_VIDEO);
                    }

                    if (SHOW_DEBUG_TOAST) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), job.getTransferData(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    mFOS.write(job.getTransferData().getBytes());

                    if (CommUtil.Usb.CMD_CODE_SEND_FW_FILE.equals(job.cmd)) {
                        // TODO refactoring
                        File file = new File(getSendingFilePath());
                        if (file.exists() && file.length() > 0) {
                            FileInputStream fis = new FileInputStream(file);
                            long fileLength = file.length();
                            try{
                                int readSize = 0;
                                int totalSendSize = 0;
                                while((readSize = fis.read(buffer)) != -1) {
                                    // TODO : cancel
                                    mFOS.write(buffer, 0, readSize);
                                    totalSendSize += readSize;
                                    mMsgBundle.putLong(MSG_KEY_TOTAL, fileLength);
                                    mMsgBundle.putLong(MSG_KEY_PROGRESS, totalSendSize);
                                    Message msg = mMainHandler.obtainMessage(MSG_SENDING_FILE_PROGRESS);
                                    msg.setData(mMsgBundle);
                                    mMainHandler.sendMessage(msg);
                                }
                                /* workaround code : last packet is not sent issue  */
                                mFOS.write(CommUtil.Usb.END_MARK.getBytes());
                            } catch (IOException e) {
                                // TODO : Error Handling
                            } finally {
                                mFilepath = "";
                                try{
                                    fis.close();
                                    fis = null;
                                } catch(IOException ignore) {
                                }
                            }
                        }
                    } else {
                        //send zero-byte for bulk transfer completing
                        mFOS.write(buffer, 0, 0);
                    }

                    Thread.sleep(10);
                } catch (IOException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (mFOS != null) {
                try {
                    mFOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mFOS = null;
                }
            }
        }

        void registerJob(SendJob job) {
            try {
                mSendJobBlockingQueue.put(job);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized void setSendingFilePath(String filePath) {
            mFilepath = filePath;
        }

        synchronized String getSendingFilePath() {
            return mFilepath;
        }
    }

    private void parseDeviceResponseData(String data) {
        String[] dataArray = data.split(CommUtil.Usb.CHAR_SPLIT);

        if (SHOW_DEBUG_TOAST) {
            Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
        }

        try {
            String cmd = dataArray[CommUtil.Usb.POS_CMD];
            if (CommUtil.Usb.DATA_TYPE_RESP.equals(dataArray[CommUtil.Usb.POS_TYPE])) {
                removeWatchdogFromMap(cmd);

                if (CommUtil.Usb.CMD_CODE_READ_VEHICLE_INFO.equals(cmd)) {
                    parseReadVehicleInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_WRITE_VEHICLE_INFO.equals(cmd)) {
                    parseWriteVehicleInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_READ_CALIBRATION_INFO.equals(cmd)) {
                    parseReadCalibrationInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_WRITE_CALIBRATION_INFO.equals(cmd)) {
                    parseWriteCalibrationInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_READ_DTG_DRIVER_INFO.equals(cmd)) {
                    parseReadDTGDriverInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_WRITE_DTG_DRIVER_INFO.equals(cmd)) {
                    parseWriteDTGDriverInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_VIDEO_LIST.equals(cmd)) {
                    parseVideoFileListFirstResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_DOWNLOAD_VIDEO.equals(cmd)) {
                    parseVideoFileDownloadResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_DOWNLOAD_CANCEL.equals(cmd)) {
                    parseVideoFileDownloadCancelResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_VERSION_INFO.equals(cmd)) {
                    parseReadVersionInfoResp(dataArray);
                } else if (CommUtil.Usb.CMD_CODE_SEND_FW_FILE.equals(cmd)) {
                    parseReadSendingFirmwareFileResp(dataArray);
                } else {
                    Log.e(TAG, "Response : unknown cmd - " + cmd);
                }
            } else if (CommUtil.Usb.DATA_TYPE_REQ.equals(dataArray[CommUtil.Usb.POS_TYPE])) {
                if (CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_NORMAL_PIC.equals(cmd)) {
                    long fileSize = Long.parseLong(dataArray[2]);
                    if (fileSize > 0 && mUSBReceiveThread != null) {
                        mUSBReceiveThread.setWaitModeReceiveCalibrationNormalFile(fileSize);
                    }
                } else if (CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_AUTO_FIRST_PIC.equals(cmd)) {
                    long fileSize = Long.parseLong(dataArray[2]);
                    if (fileSize > 0 && mUSBReceiveThread != null) {
                        mUSBReceiveThread.setWaitModeReceiveCalibrationAutoFirstFile(fileSize);
                    }
                } else if (CommUtil.Usb.CMD_CODE_RECEIVE_CALIBRATION_AUTO_SECOND_PIC.equals(cmd)) {
                    long fileSize = Long.parseLong(dataArray[2]);
                    if (fileSize > 0 && mUSBReceiveThread != null) {
                        mUSBReceiveThread.setWaitModeReceiveCalibrationAutoSecondFile(fileSize);
                    }
                } else {
                    respNotSupported(cmd);
                }
            } else {
                Log.e(TAG, "Response : unknown type - " + dataArray[0]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
        if (mUSBReceiveThread != null) {
            mUSBReceiveThread.wakeUp();
        }
    }


    private void parseReadVersionInfoResp (String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                if (dataArray.length > CommUtil.Usb.POS_RESULT + 1) {
                    mSession.setDeviceToken(Constants.DEFAULT_DEV_CODE);
                    FileUtil.checkDeviceDirectory(mSession.getDeviceToken());
                    int versionCode = -1;
                    try {
                        versionCode = Integer.parseInt(dataArray[4]);
                    } catch (NumberFormatException ignore) {
                    }

                    if (versionCode != -1) {
                        mSession.setDeviceVersionCode(versionCode);
                        mSession.setDeviceVersionName(dataArray[3]);

                        try {
                            mSession.setDeviceDtgSupport(Integer.parseInt(dataArray[6]));

                            int deviceAPIVersion = Integer.parseInt(dataArray[5]);
                            int appAPIVersion = Integer.parseInt(CommUtil.Usb.API_VER);

                            if (deviceAPIVersion > appAPIVersion) {
                                Toast.makeText(getApplicationContext(), R.string.version_incompatible_update_app, Toast.LENGTH_LONG).show();
                            } else if (deviceAPIVersion < appAPIVersion) {
                                Toast.makeText(getApplicationContext(), R.string.version_incompatible_update_device, Toast.LENGTH_LONG).show();
                            }
                            Log.d(TAG, "Protocol API DEV version : " + deviceAPIVersion + "APP version : " + appAPIVersion);
                        } catch (NumberFormatException ignore) {
                        }

                        if (!DBManager.getInstance().isExistDevice(mSession.getDeviceToken())) {
                            DBManager.getInstance().registerDevice(mSession.getDeviceToken());
                        }

                        DBManager.getInstance().setFirmwareVersion(mSession.getDeviceToken()
                                ,mSession.getDeviceVersionName(),
                                String.valueOf(mSession.getDeviceVersionCode()),
                                String.valueOf(mSession.getDeviceDtgSupport()));

                        DBManager.getInstance().setLatestDeviceToken(mSession.getDeviceToken());
                    } else {
                        errMessage = getString(R.string.comm_error_unknown_version);
                    }
                }
                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_VERSION_INFO);
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                mDefaultInfoRequestPending = false;
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            } else {
                reqReadVehicleInfo();
            }
        }
    }

     private void parseReadVehicleInfoResp (String[] dataArray) {
         String errMessage = "";
         try {
             if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                 if (dataArray.length > CommUtil.Usb.POS_RESULT + 1) {
                     VehicleData data = mSession.getDevVehicleData();
                     data.setVehicleType(Integer.valueOf(dataArray[3]));
                     data.setUniqueID(dataArray[4]);
                     data.setManufacturer(dataArray[5]);
                     data.setModel(dataArray[6]);
                     data.setFuelType(dataArray[7]);
                     data.setReleaseDate(dataArray[8]);
                     data.setOBDManufacturerID(dataArray[9]);
                     data.setOBDModelID(dataArray[10]);
                     data.setOBDFuelTypeID(dataArray[11]);
                     DBManager.getInstance().setVehicleData(mSession.getDeviceToken(), data);
                 } else {
                     // No data
                     DBManager.getInstance().deleteVehicleData(mSession.getDeviceToken());
                 }
                 // Notify to main-menu
                 notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_READ_VEHICLE_INFO);
             } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                 errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
             } else {
                 errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
             }
         } catch (IndexOutOfBoundsException e) {
             errMessage = getString(R.string.comm_error_exception, e.getMessage());
         } finally {
             if (!"".equals(errMessage)) {
                 mDefaultInfoRequestPending = false;
                 Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
             } else {
                 reqReadCalibrationInfo();
             }
         }
     }

    private void parseWriteVehicleInfoResp (String[] dataArray) {
        String message = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                mSession.getDevVehicleData().copyFrom(mSession.getPreVehicleData());

                DBManager.getInstance().setVehicleData(mSession.getDeviceToken(), mSession.getDevVehicleData());

                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_WRITE_VEHICLE_INFO);
                message = getString(R.string.success_send_vehicle_info);

            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                message = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                message = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException e) {
            message = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(message)) {
                /*
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                */
                if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                    mCommunicationDialog.dismiss();
                    mCommunicationDialog = null;
                }
                mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                        .showNotificationDialog(
                                getString(R.string.notice),
                                message,
                                MSG_SEND_VEHICLE_INFO_DIALOG_OK);
            }
        }
    }

    private void parseReadCalibrationInfoResp (String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                if (dataArray.length > CommUtil.Usb.POS_RESULT + 1) {
                    CalibrationData data = mSession.getDevCalibrationData();
                    data.setDate(dataArray[3]);
                    data.setCameraHeight(Integer.parseInt(dataArray[4]));
                    data.setVehicleWidth(Integer.parseInt(dataArray[5]));
                    data.setCameraToBumper(Integer.parseInt(dataArray[6]));
                    data.setBonnetPoint(new Point(0, Integer.parseInt(dataArray[7])));
                    data.setVanishingY(Integer.parseInt(dataArray[8]));
                    data.setCenterX(Integer.parseInt(dataArray[9]));
                    data.setNearY(Integer.parseInt(dataArray[10]));
                    data.setNearLeftX(Integer.parseInt(dataArray[11]));
                    data.setNearRightX(Integer.parseInt(dataArray[12]));
                    data.setFarY(Integer.parseInt(dataArray[13]));
                    data.setFarLeftX(Integer.parseInt(dataArray[14]));
                    data.setFarRightX(Integer.parseInt(dataArray[15]));
                    data.setFarDistance(Integer.parseInt(dataArray[16]));
                    data.setNearDistance(Integer.parseInt(dataArray[17]));
                    DBManager.getInstance().setCalibrationData(mSession.getDeviceToken(), data);
                } else {
                    // No data
                    DBManager.getInstance().deleteCalibrationData(mSession.getDeviceToken());
                }
                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_READ_CALIBRATION_INFO);

            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                mDefaultInfoRequestPending = false;
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            } else {
                if (mSession.getDeviceDtgSupport() == Constants.DtgSupport.HW_SW_SUPPORTED) {
                    reqReadDTGDriverInfo();
                } else {
                    mDefaultInfoRequestPending = false;
                }
            }
        }
    }

    private void parseWriteCalibrationInfoResp (String[] dataArray) {
        String message = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                mSession.getDevCalibrationData().copyFrom(mSession.getPreCalibrationData());

                DBManager.getInstance().setCalibrationData(mSession.getDeviceToken(), mSession.getDevCalibrationData());
                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_WRITE_CALIBRATION_INFO);
                message = getString(R.string.success_send_calibration_info);
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                message = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                message = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException e) {
            message = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(message)) {
                /*
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                */
                if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                    mCommunicationDialog.dismiss();
                    mCommunicationDialog = null;
                }
                mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                        .showNotificationDialog(
                                getString(R.string.notice),
                                message,
                                MSG_SEND_CALIBRATION_INFO_DIALOG_OK);
            }
        }
    }

    private void parseReadDTGDriverInfoResp (String[] dataArray) {
        String errMessage = "";
        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                if (dataArray.length > CommUtil.Usb.POS_RESULT + 1) {
                    DriverData data = mSession.getDevDriverData();
                    data.setVehicleType(Integer.valueOf(dataArray[3]));
                    data.setVIN(dataArray[4]);
                    data.setVehicleRegNo(dataArray[5]);
                    data.setBusinessRegNo(dataArray[6]);
                    data.setFormatDriverCode(dataArray[7]);
                    DBManager.getInstance().setDriverData(mSession.getDeviceToken(), data);
                } else {
                    // No data
                    DBManager.getInstance().deleteDriverData(mSession.getDeviceToken());
                }
                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_READ_DTG_DRIVER_INFO);
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            mDefaultInfoRequestPending = false;
            if (!"".equals(errMessage)) {
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseWriteDTGDriverInfoResp (String[] dataArray) {
        String message = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                mSession.getDevDriverData().copyFrom(mSession.getPreDriverData());

                DBManager.getInstance().setDriverData(mSession.getDeviceToken(), mSession.getDevDriverData());

                // Notify to main-menu
                notifyRequestCompleted(MainMenuFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_WRITE_DTG_DRIVER_INFO);
                message = getString(R.string.success_send_dtg_info);

            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                message = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                message = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException e) {
            message = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(message)) {
                /*
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                */
                if (mCommunicationDialog != null && mCommunicationDialog.isShowing()) {
                    mCommunicationDialog.dismiss();
                    mCommunicationDialog = null;
                }
                mCommunicationDialog = new CustomDialog(MainActivity.this, mHandler)
                        .showNotificationDialog(
                                getString(R.string.notice),
                                message,
                                MSG_SEND_DTG_INFO_DIALOG_OK);
            }
        }
    }

    private void parseVideoFileListFirstResp (String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                mSession.getNormalVideoArrayList().clear();
                mSession.getEventVideoArrayList().clear();
                mUSBReceiveThread.setWaitModeReceiveVideoList();
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void  parseVideoFileListNextResp (String[] dataArray) throws IndexOutOfBoundsException {
        for (String filename : dataArray) {
            if (CommUtil.Usb.END_MARK.equals(filename)) {
                mUSBReceiveThread.setWaitModeNormal();
                DBManager.getInstance().deleteVideoList(mSession.getDeviceToken());
                if (mSession.getNormalVideoArrayList().size() > 0) {
                    DBManager.getInstance().insertVideoList(mSession.getDeviceToken(), mSession.getNormalVideoArrayList());
                }
                if (mSession.getEventVideoArrayList().size() > 0) {
                    DBManager.getInstance().insertVideoList(mSession.getDeviceToken(), mSession.getEventVideoArrayList());
                }
                mHandler.sendMessage(mHandler.obtainMessage(MSG_VIDEO_LIST_BUILD_COMPLETED));
            } else {
                try {
                    String fileType = String.valueOf(filename.charAt(0));
                    if (CommUtil.Usb.CHAR_NORMAL_VIDEO.equals(fileType)) {
                        mSession.getNormalVideoArrayList().add(new VideoData(VideoData.TYPE_NORMAL, filename));
                    } else if (CommUtil.Usb.CHAR_EVENT_VIDEO.equals(fileType)) {
                        mSession.getEventVideoArrayList().add(new VideoData(VideoData.TYPE_EVENT, filename));
                    } else {
                        Log.e(TAG, "parseVideoFileListNextResp : unknown file name - " + filename);
                    }
                } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
                }
            }
        }
    }

    private void parseVideoFileDownloadResp(String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                long fileSize = Long.parseLong(dataArray[3]);
                if ((fileSize + FileUtil.EXTRA_STORAGE_MARGIN) > FileUtil.getExternalStorageFreeSpace()) {
                    reqCancelDownloadVideoFile();
                    new CustomDialog(MainActivity.this, mHandler)
                            .showNotificationDialog(R.string.notification,
                                    R.string.insufficient_storage, MSG_NOTIFICATION_OK);
                } else {
                    mUSBReceiveThread.setWaitModeReceiveVideoFile(mDownloadFilePath, fileSize);
                }
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                if (CommUtil.Usb.REASON_NOT_EXIST_FILE.equals(dataArray[CommUtil.Usb.ERR_CODE])) {
                    // TODO : (FAIL_CODE = 03 Not exist file) delete from list??
                    errMessage = getString(R.string.comm_error_file_not_exist);
                } else if (CommUtil.Usb.REASON_FILE_SIZE_ZERO.equals(dataArray[CommUtil.Usb.ERR_CODE])) {
                    errMessage = getString(R.string.comm_error_file_size_zero);
                } else {
                    errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
                }
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseVideoFileDownloadCancelResp(String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                publishReceiveFileProgress(VideoListFragment.FRAGMENT_TAG, true, 0, 0);
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseReadSendingFirmwareFileResp (String[] dataArray) {
        String errMessage = "";

        try {
            if (CommUtil.Usb.CHAR_RESP_SUCCESS.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                // Notify
                notifyRequestCompleted(FirmwareFragment.FRAGMENT_TAG, CommUtil.Usb.CMD_CODE_SEND_FW_FILE);
            } else if (CommUtil.Usb.CHAR_RESP_FAIL.equals(dataArray[CommUtil.Usb.POS_RESULT])) {
                errMessage = getString(R.string.comm_error_result_fail, dataArray[CommUtil.Usb.ERR_CODE]);
            } else {
                errMessage = getString(R.string.comm_error_result_unknown, dataArray[CommUtil.Usb.POS_RESULT]);
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            errMessage = getString(R.string.comm_error_exception, e.getMessage());
        } finally {
            if (!"".equals(errMessage)) {
                Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addWatchdogToMap(String key) {
        Watchdog watchdog = new Watchdog(key, Constants.RESP_TIMEOUT);
        mWatchdogMap.put(key, watchdog);
        watchdog.addTimeoutObserver(mTimeoutObserver);
        watchdog.start();
    }

    private void removeWatchdogFromMap(String key) {
        Watchdog watchdog = mWatchdogMap.get(key);
        if (watchdog != null) {
            watchdog.stop();
            watchdog.removeTimeoutObserver(mTimeoutObserver);
            mWatchdogMap.remove(key);
        }
    }

    private void removeAllWatchdogFromMap() {
        for (Map.Entry<String, Watchdog> entry : mWatchdogMap.entrySet()) {
            entry.getValue().stop();
            mWatchdogMap.remove(entry.getKey());
        }
    }

    private TimeoutObserver mTimeoutObserver = new TimeoutObserver() {
        @Override
        public void timeoutOccurred(Watchdog w) {
            final String cmd = w.getId();
            w.stop();
            w.removeTimeoutObserver(this);
            if (mWatchdogMap.containsKey(cmd)) {
                mWatchdogMap.remove(cmd);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDefaultInfoRequestPending = false;
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.device_no_response) + " (" + cmd + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };
}

