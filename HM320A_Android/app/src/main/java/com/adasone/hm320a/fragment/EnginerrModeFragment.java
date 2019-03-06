package com.adasone.hm320a.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adasone.hm320a.R;
import com.adasone.hm320a.data.Session;
import com.adasone.hm320a.interfaces.OnFragmentInteractionListener;

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
        View root= inflater.inflate(R.layout.activity_main_engmode, container, false);

//        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
//        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
//        menuImageView.setOnClickListener(mOnClickListener);
//        backImageView.setOnClickListener(mOnClickListener);
//        mBtnEnginemode=(LinearLayout) root.findViewById(R.id.btnEnginemode);
//        mBtnEnginemode.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                Toast.makeText(this, "Engineer mode triggered", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent();
////                intent.setClass("com.adasone.hm320a.fragment.MainMenuFragment","obd2scantool.canbusanalyzer.ActivityEngineer");
//                intent.setComponent(new ComponentName("obd2scantool.canbusanalyzer","obd2scantool.canbusanalyzer.ActivityEngineer"));
//                startActivity(intent);
//            }
//        });
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
