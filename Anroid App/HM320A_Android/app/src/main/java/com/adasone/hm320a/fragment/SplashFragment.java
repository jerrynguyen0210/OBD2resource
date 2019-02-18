package com.adasone.hm320a.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.BuildConfig;
import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.FileUtil;


import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {
    private static final String TAG = SplashFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = SplashFragment.class.getSimpleName();

    private static final int MIN_SPLASH_SHOW_TIME_MILLIS = 1500;
    private static final int MAX_SPLASH_SHOW_TIME_MILLIS = 3000;

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;

    public SplashFragment() {
        // Required empty public constructor
    }

    public static SplashFragment newInstance() {
        return new SplashFragment();
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_splash, container, false);

        TextView versionTextView = (TextView) root.findViewById(R.id.tv_version);
        versionTextView.setText(BuildConfig.VERSION_NAME);
        AppApplication.getAppApplication().setFontHYGothic800(versionTextView);

        mHandler.sendMessage(mHandler.obtainMessage(MSG_PERMISSION_CHECK_START));
        mHandler.postDelayed(mSplashFinishRunnable, MIN_SPLASH_SHOW_TIME_MILLIS);
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

    private Runnable mSplashFinishRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SPLASH_TIME_FINISH));
        }
    };


    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 500;
    private static final int PERMISSIONS_REQUEST_INTERNET = 501;
    private static final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE = 502;

    private void checkWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getContext(), R.string.permission_needs_description, Toast.LENGTH_SHORT).show();
                }

                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                checkInternetPermission();
            }
        } else {
            checkInternetPermission();
        }
    }

    private void checkInternetPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                    Toast.makeText(getContext(), R.string.permission_needs_description, Toast.LENGTH_SHORT).show();
                }

                requestPermissions(
                        new String[]{Manifest.permission.INTERNET},
                        PERMISSIONS_REQUEST_INTERNET);

            } else {
                checkAccessNetworkStatePermission();
            }
        } else {
            checkAccessNetworkStatePermission();
        }
    }

    private void checkAccessNetworkStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_NETWORK_STATE)) {
                    Toast.makeText(getContext(), R.string.permission_needs_description, Toast.LENGTH_SHORT).show();
                }

                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                        PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE);

            } else {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_DIRECTORY_CHECK_START));
            }
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_DIRECTORY_CHECK_START));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean bDenied = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkInternetPermission();
                } else {
                    bDenied = true;
                }
                break;
            case PERMISSIONS_REQUEST_INTERNET:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAccessNetworkStatePermission();
                } else {
                    bDenied = true;
                }
                break;
            case PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_DIRECTORY_CHECK_START));
                } else {
                    bDenied = true;
                }
                break;
        }

        if (bDenied) {
            if (mListener != null) {
                Toast.makeText(getContext(), R.string.permission_denied_description, Toast.LENGTH_LONG).show();
                mListener.onMenuSelected(Constants.Menu.APP_FINISH, 0);
            }
        }
    }

    /*
     *  job 1 : SHOW SPLASH UI
     *  job 2 : PERMISSION CHECK & Load OpenCV
     */
    private AtomicInteger mJobFinishCounter = new AtomicInteger();

    private final static int MSG_PERMISSION_CHECK_START = 3000;
    private final static int MSG_DIRECTORY_CHECK_START = 3001;
    private final static int MSG_LOAD_OPEN_CV = 3002;
    private final static int MSG_LOAD_OPEN_CV_FAIL = 3003;
    private final static int MSG_APP_START_CONDITIONS_CHECK_FINISH = 3004;
    private final static int MSG_SPLASH_TIME_FINISH = 3005;
    private final static int MSG_CHECK_DEVICE_CONNECTION = 3006;
    private final static int MSG_WAITING_RECEIVE_DEVICE_INFO = 3007;
    private final static int MSG_SPLASH_UI_END = 3008;


    private static class MyHandler extends Handler {
        private final WeakReference<SplashFragment> mFragment;
        private MyHandler(SplashFragment fragment) {
            mFragment = new WeakReference<SplashFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final SplashFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case MSG_PERMISSION_CHECK_START :
                    Log.d(TAG, "PERMISSION_CHECK_START");
                    fragment.checkWriteExternalStoragePermission();
                    break;
                case MSG_DIRECTORY_CHECK_START :
                    Log.d(TAG, "MSG_DIRECTORY_CHECK_START");
                    FileUtil.checkDefaultDirectory();
                    this.sendMessage(this.obtainMessage(MSG_APP_START_CONDITIONS_CHECK_FINISH));
                    break;
                case MSG_LOAD_OPEN_CV :
/*
                    if (!OpenCVLoader.initDebug()) {
                        Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, fragment.getActivity(), fragment.mLoaderCallback);
                    } else {
                        Log.d(TAG, "OpenCV library found inside package. Using it!");
                        fragment.mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                    }
*/
                    break;
                case MSG_LOAD_OPEN_CV_FAIL :
                    fragment.getActivity().finish();
                    break;
                case MSG_APP_START_CONDITIONS_CHECK_FINISH :
                    Log.d(TAG, "MSG_APP_START_CONDITIONS_CHECK_FINISH");
                    if (fragment.mJobFinishCounter.incrementAndGet() == 2) {
                        this.sendMessage(this.obtainMessage(MSG_CHECK_DEVICE_CONNECTION));
                    }
                    break;
                case MSG_SPLASH_TIME_FINISH :
                    Log.d(TAG, "SPLASH_TIME_FINISH");
                    if (fragment.mJobFinishCounter.incrementAndGet() == 2) {
                        this.sendMessage(this.obtainMessage(MSG_CHECK_DEVICE_CONNECTION));
                    }
                    break;
                case MSG_CHECK_DEVICE_CONNECTION :
                    Log.d(TAG, "MSG_CHECK_DEVICE_CONNECTION");
                    if (!fragment.mListener.isUSBPermissionRequestPending()) {
                        if (fragment.mListener.isUSBConnected()) {
                            fragment.mListener.reqDeviceInfo();
                            this.sendMessageDelayed(this.obtainMessage(MSG_WAITING_RECEIVE_DEVICE_INFO), 200);
                        } else if (fragment.mListener.isUSBConnectionFailPending()) {
                            this.sendMessage(this.obtainMessage(MSG_CHECK_DEVICE_CONNECTION));
                        } else {
                            this.sendMessage(this.obtainMessage(MSG_SPLASH_UI_END));
                        }
                    } else {
                        this.sendMessageDelayed(this.obtainMessage(MSG_CHECK_DEVICE_CONNECTION), 200);
                    }
                    break;
                case MSG_WAITING_RECEIVE_DEVICE_INFO :
                    Log.d(TAG, "MSG_WAITING_RECEIVE_DEVICE_INFO");
                    if ((fragment.mListener.isUSBConnected() && fragment.mListener.isUSBDefaultInfoRequestPending())
                        || fragment.mListener.isUSBConnectionFailPending()) {
                        this.sendMessageDelayed(this.obtainMessage(MSG_WAITING_RECEIVE_DEVICE_INFO), 200);
                    } else {
                        this.sendMessage(this.obtainMessage(MSG_SPLASH_UI_END));
                    }
                    break;
                case MSG_SPLASH_UI_END :
                    Log.d(TAG, "MSG_SPLASH_UI_END");
                    fragment.mListener.onSplashDisplayFinish();
                    break;
                default :
                    break;
            }
        }
    }


}
