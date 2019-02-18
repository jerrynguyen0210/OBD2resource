package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.CustomDialog;
import com.adasone.hm320a.PlayerActivity;
import com.adasone.hm320a.R;
import com.adasone.hm320a.adapter.VideoListAdapter;
import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.data.VideoData;
import com.adasone.hm320a.database.DBManager;
import com.adasone.hm320a.interfaces.OnActivityInteractionListener;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;
import com.adasone.hm320a.util.BlurUtil;
import com.adasone.hm320a.util.CommUtil;
import com.adasone.hm320a.util.FileUtil;
import com.adasone.hm320a.util.WidgetUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnginerrModeFragment extends Fragment {
    private static final String TAG = EnginerrModeFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = EnginerrModeFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private Session mSession;


    private static final int STATUS_NORMAL = 0;

    private static final int STATUS_DELETE = 3;
    private int mCurrStatus = STATUS_NORMAL;

    public EnginerrModeFragment() {
        // Required empty public constructor
    }

    public static EnginerrModeFragment newInstance() {
        return new EnginerrModeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
        View root= inflater.inflate(R.layout.fragment_engineer_mode, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);

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

    public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_menu :
                    if (mCurrStatus != STATUS_DELETE) {
                        mListener.onRequestMainMenu(false);
                    }
                    break;
                case R.id.iv_back :
                    getActivity().onBackPressed();
                    break;
                default:
                    break;
            }
        }
    };

}
