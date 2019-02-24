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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
public class OBDCheckFragment extends Fragment {
    private static final String TAG = OBDCheckFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = OBDCheckFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;
    ListView mListView;

    String[] mobileArray = {"Turn Signal Left",
                            "Turn Signal Right",
                            "Brake",
                            "RPM",
                            "Speed",
                            };

    public OBDCheckFragment() {
        // Required empty public constructor
    }

    public static OBDCheckFragment newInstance() {
        return new OBDCheckFragment();
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

//    @Override
////    public void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        Activity activity = getActivity();
////        if (activity != null) {
////            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////        } else {
////            Log.e(TAG, "getActivity() is null !!");
////        }
////        mSession = mListener.getSession();
////    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_vehicle_info_view2, container, false);

        //ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        mListView = (ListView) root.findViewById(R.id.OBD_CheckList);

        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(),
                R.layout.obdcheck_list_items, mobileArray);
        if (getContext() != null) {
            mListView.setAdapter(adapter);
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
            switch (msg) {
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



    private static class MyHandler extends Handler {
        private final WeakReference<OBDCheckFragment> mFragment;
        private MyHandler(OBDCheckFragment fragment) {
            mFragment = new WeakReference<OBDCheckFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final OBDCheckFragment fragment = mFragment.get();
            switch (msg.what) {

                default :
                    break;
            }
        }
    }
}
