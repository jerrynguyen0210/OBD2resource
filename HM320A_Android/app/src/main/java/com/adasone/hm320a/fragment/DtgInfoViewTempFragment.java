package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.adasone.hm320a.R;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class DtgInfoViewTempFragment extends Fragment {
    private static final String TAG = DtgInfoViewTempFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = DtgInfoViewTempFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;


    public DtgInfoViewTempFragment() {
        // Required empty public constructor
    }

    public static DtgInfoViewTempFragment newInstance() {
        return new DtgInfoViewTempFragment();
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
        View root = inflater.inflate(R.layout.fragment_dtg_info_view_temp, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);
        TextView descTextView = (TextView) root.findViewById(R.id.tv_desc);

        Button mOkButton = (Button) root.findViewById(R.id.btn_ok);
        mOkButton.setOnClickListener(mOnClickListener);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic700(descTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(mOkButton);

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
                    mListener.onRequestMainMenu(false);
                    break;
                case R.id.iv_back :
                    getActivity().onBackPressed();
                    break;
                case R.id.btn_ok :
                    getActivity().onBackPressed();
                default:
                    break;
            }
        }
    };

    private static class MyHandler extends Handler {
        private final WeakReference<DtgInfoViewTempFragment> mFragment;
        private MyHandler(DtgInfoViewTempFragment fragment) {
            mFragment = new WeakReference<DtgInfoViewTempFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final DtgInfoViewTempFragment fragment = mFragment.get();
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
}
