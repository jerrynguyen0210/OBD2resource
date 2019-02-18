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
public class VideoListFragment extends Fragment {
    private static final String TAG = VideoListFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = VideoListFragment.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);
    private OnFragmentInteractionListener mListener;
    private Session mSession;

    private FrameLayout mRootLayout;
    private LinearLayout mLoadingLayout;
    private LinearLayout mListLayout;
    private LinearLayout mListEmptyLayout;
    private LinearLayout mDownloadLayout;

    private Button mNormalButton;
    private Button mEventButton;

    private CheckBox mSelectAllCheckBox;

    public static final int LIST_NORMAL = 0;
    public static final int LIST_EVENT = 1;
    private int mCurrVideoList = LIST_NORMAL;
    private boolean mListBuildCompleted = false;

    private ArrayList<VideoData> mNormalVideoArrayList;
    private ArrayList<VideoData> mEventVideoArrayList;

    private ListView mVideoListView;
    private VideoListAdapter mVideoListAdapter;
    private Button mDownloadButton;

    private TextView mDownloadFileNameTextView;
    private TextView mDownloadFileSizeTextView;
    private ProgressBar mDownloadProgressBar;

    private String mSelectedFilePath = "";
    private String mSelectedFileName = "";
    private int mSelectedPosition = 0;

    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_DOWNLOAD = 1;
    private static final int STATUS_SELECT = 2;
    private static final int STATUS_DELETE = 3;
    private int mCurrStatus = STATUS_NORMAL;

    private RetrieveVideoFromLocalAsyncTask mRetrieveVideoTask = null;
    private DeleteVideoFileAsyncTask mDeleteVideoFileTask = null;

    public VideoListFragment() {
        // Required empty public constructor
    }

    public static VideoListFragment newInstance() {
        return new VideoListFragment();
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
        View root= inflater.inflate(R.layout.fragment_video_list, container, false);

        ImageView menuImageView = (ImageView) root.findViewById(R.id.iv_menu);
        ImageView backImageView = (ImageView) root.findViewById(R.id.iv_back);
        menuImageView.setOnClickListener(mOnClickListener);
        backImageView.setOnClickListener(mOnClickListener);
        TextView titleTextView = (TextView) root.findViewById(R.id.tv_title);

        mRootLayout = (FrameLayout) root.findViewById(R.id.layout_root);
        mLoadingLayout = (LinearLayout) root.findViewById(R.id.layout_loading);
        mListLayout = (LinearLayout) root.findViewById(R.id.layout_list);
        mListEmptyLayout = (LinearLayout) root.findViewById(R.id.layout_empty_list);

        mNormalButton = (Button) root.findViewById(R.id.btn_normal_list);
        mEventButton = (Button) root.findViewById(R.id.btn_event_list);

        mNormalButton.setOnClickListener(mOnClickListener);
        mEventButton.setOnClickListener(mOnClickListener);

        mVideoListView = (ListView) root.findViewById(R.id.list_video);
        mVideoListAdapter = new VideoListAdapter(getContext(), mNormalVideoArrayList);
        mVideoListView.setAdapter(mVideoListAdapter);
        mVideoListView.setOnItemClickListener(mVideoListOnItemClickListener);
        mVideoListView.setOnItemLongClickListener(mVideoListOnItemLongClickListener);

        mDownloadButton = (Button) root.findViewById(R.id.btn_download);
        WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, false);
        mDownloadButton.setOnClickListener(mOnClickListener);

        mSelectAllCheckBox = (CheckBox) root.findViewById(R.id.checkbox_all);
        mSelectAllCheckBox.setOnClickListener(mSelectCheckBoxOnClickListener);
        mSelectAllCheckBox.setOnCheckedChangeListener(null);

        TextView dateColumnTextView = (TextView) root.findViewById(R.id.tv_date_column);
        TextView fileNameColumnTextView = (TextView) root.findViewById(R.id.tv_file_name_column);

        // Download UI
        mDownloadLayout = (LinearLayout) root.findViewById(R.id.layout_download);
        TextView downloadCaptionTextVIew = (TextView) root.findViewById(R.id.tv_download_caption);
        mDownloadFileNameTextView = (TextView)root.findViewById(R.id.tv_download_file_name);
        mDownloadFileSizeTextView = (TextView)root.findViewById(R.id.tv_download_file_size);
        Button downloadCancelButton = (Button) root.findViewById(R.id.btn_download_cancel);
        downloadCancelButton.setOnClickListener(mOnClickListener);
        mDownloadProgressBar = (ProgressBar) root.findViewById(R.id.progressbar_download);

        TextView noFilesTextVIew = (TextView)root.findViewById(R.id.tv_no_files);

        AppApplication.getAppApplication().setFontHYGothic900(titleTextView, mNormalButton,
                mEventButton, downloadCaptionTextVIew);
        AppApplication.getAppApplication().setFontHYGothic800(downloadCancelButton, noFilesTextVIew);
        AppApplication.getAppApplication().setFontHYGothic600(mDownloadFileNameTextView, mDownloadFileSizeTextView,
                dateColumnTextView, fileNameColumnTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(mDownloadButton);

        if (mListener.isUSBConnected()) {
            mLoadingLayout.setVisibility(View.VISIBLE);
            mListener.reqVideoFileList();
        } else {
            if ("".equals(mSession.getDeviceToken())) {
                mListEmptyLayout.setVisibility(View.VISIBLE);
            } else {
                // Retrieve local DB
                mLoadingLayout.setVisibility(View.VISIBLE);
                mRetrieveVideoTask = new RetrieveVideoFromLocalAsyncTask();
                mRetrieveVideoTask.execute();
            }
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
        if (mCurrStatus == STATUS_DOWNLOAD) {
            mListener.reqCancelDownloadVideoFile();
        }
        if (mRetrieveVideoTask != null
                && mRetrieveVideoTask.getStatus() != AsyncTask.Status.FINISHED) {
            mRetrieveVideoTask.cancel(true);
            mRetrieveVideoTask = null;
        }

        if (mDeleteVideoFileTask != null
                && mDeleteVideoFileTask.getStatus() != AsyncTask.Status.FINISHED) {
            mDeleteVideoFileTask.cancel(true);
            mDeleteVideoFileTask = null;
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
                case STATUS_DOWNLOAD :
                    // fall-through
                case STATUS_DELETE :
                    ret = false;
                    break;
                case STATUS_SELECT :
                    finishSelectStatus();
                    //mVideoListView.smoothScrollToPosition(mSelectedPosition);
                    refreshButton(mSelectedPosition);
                    ret = false;
                    break;
                case STATUS_NORMAL :
                    // fall-through
                default:
                    break;
            }
            return ret;
        }

        @Override
        public void onUSBConnectionChanged(boolean connect) {
            mCurrStatus = STATUS_NORMAL;
            if (getString(R.string.download_caps).equals(mDownloadButton.getText().toString())) {
                WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, false);
            }
            mDownloadLayout.setVisibility(View.GONE);
        }

        @Override
        public void onRequestCompleted(String cmd) {
            if (CommUtil.Usb.CMD_CODE_VIDEO_LIST.equals(cmd)) {
                mNormalVideoArrayList = mSession.getNormalVideoArrayList();
                mEventVideoArrayList = mSession.getEventVideoArrayList();

                addLocalVideoFileToList(mSession.getDeviceToken());

                if (mNormalVideoArrayList.size() > 0) {
                    removeDuplicationObject(mNormalVideoArrayList);
                    Collections.sort(mNormalVideoArrayList, mVideoNameOrderComparator);
                    checkFileExist(LIST_NORMAL, mNormalVideoArrayList);
                }
                if (mEventVideoArrayList.size() > 0) {
                    removeDuplicationObject(mEventVideoArrayList);
                    Collections.sort(mEventVideoArrayList, mVideoNameOrderComparator);
                    checkFileExist(LIST_EVENT, mEventVideoArrayList);
                }
                mListBuildCompleted = true;
                selectVideoList(mCurrVideoList);
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
        }

        @Override
        public void onReceiveFileProgressUpdate(boolean complete, long total, long progress) {
            if (complete) {
                if (total != 0) { // Complete download
                    mDownloadButton.setText(R.string.play_caps);
                    switch (mCurrVideoList) {
                        case LIST_NORMAL:
                            mNormalVideoArrayList.get(mSelectedPosition).setExist(true);
                            mVideoListAdapter.resetArrItem(mNormalVideoArrayList);
                            break;
                        case LIST_EVENT:
                            mEventVideoArrayList.get(mSelectedPosition).setExist(true);
                            mVideoListAdapter.resetArrItem(mEventVideoArrayList);
                            break;
                        default:
                            break;
                    }
                }
                mCurrStatus = STATUS_NORMAL;
                mDownloadLayout.setVisibility(View.GONE);
            } else {
                mDownloadFileSizeTextView.setText(getString(R.string.download_video_file_size, String.valueOf(progress/1024), String.valueOf(total/1024)));
                if (total > 0) {
                    mDownloadProgressBar.setProgress((int)(progress * 100 / total));
                }
            }
        }
    };

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
                case R.id.btn_normal_list:
                    if (mCurrVideoList != LIST_NORMAL && mCurrStatus != STATUS_DELETE) {
                        if (mCurrStatus == STATUS_SELECT) {
                            finishSelectStatus();
                        }
                        selectButton(view.getId());
                        selectVideoList(LIST_NORMAL);
                    }
                    break;
                case R.id.btn_event_list:
                    if (mCurrVideoList != LIST_EVENT && mCurrStatus != STATUS_DELETE) {
                        if (mCurrStatus == STATUS_SELECT) {
                            finishSelectStatus();
                        }
                        selectButton(view.getId());
                        selectVideoList(LIST_EVENT);
                    }
                    break;
                case R.id.btn_download:
                    if (mCurrStatus == STATUS_SELECT) {
                        int checkedCount = mVideoListAdapter.getFileCheckedCount();
                        new CustomDialog(getContext(), mHandler)
                                .showQuestionYNDialog(
                                        getResources().getQuantityString(R.plurals.delete_downloaded_file_title, checkedCount, checkedCount),
                                        getResources().getQuantityString(R.plurals.delete_downloaded_file_count, checkedCount, checkedCount),
                                        getString(R.string.delete_caps), getString(R.string.cancel_caps),
                                        MSG_DELETE_DIALOG_OK, MSG_DELETE_DIALOG_NO);

                    } else if (mCurrStatus != STATUS_DELETE) {
                        if (getString(R.string.play_caps).equals(mDownloadButton.getText().toString())) {
                            playVideoFile(mSelectedFileName);
                        } else { // DOWNLOAD
                            if (!"".equals(mSelectedFileName)) {
                                // TODO : MOVE
                                mCurrStatus = STATUS_DOWNLOAD;
                                mDownloadFileNameTextView.setText(getString(R.string.download_video_file_name, mSelectedFileName));
                                mDownloadFileSizeTextView.setText(R.string.download_video_file_size_unknown);
                                mDownloadProgressBar.setProgress(0);
                                mRootLayout.setDrawingCacheEnabled(true);
                                Bitmap bitmap = BlurUtil.fastBlur(getContext(), mRootLayout.getDrawingCache(), 10);
                                mRootLayout.setDrawingCacheEnabled(false);
                                mDownloadLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                                mDownloadLayout.setVisibility(View.VISIBLE);
                                mListener.reqDownloadVideoFile(mSelectedFilePath, mSelectedFileName);
                            } else {
                                Log.e(TAG, "mSelectedFileName is empty");
                            }
                        }
                    }
                    break;
                case R.id.btn_download_cancel :
                    mCurrStatus = STATUS_NORMAL;
                    mListener.reqCancelDownloadVideoFile();
                    mDownloadLayout.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    };

    private  AdapterView.OnItemClickListener mVideoListOnItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (mCurrStatus) {
                        case STATUS_NORMAL :
                            itemSelect(mCurrVideoList, position);
                            break;
                        case STATUS_SELECT :
                            mVideoListAdapter.checkToggle(position);
                            if (mVideoListAdapter.isCheckedAll()) {
                                mSelectAllCheckBox.setChecked(true);
                            } else {
                                mSelectAllCheckBox.setChecked(false);
                            }
                            refreshButton(position);
                            break;
                        case STATUS_DOWNLOAD :
                        case STATUS_DELETE :
                            // fall-through
                        default:
                            break;
                    }
                }
            };

    private  AdapterView.OnItemLongClickListener mVideoListOnItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (mCurrStatus) {
                        case STATUS_NORMAL :
                            if (mVideoListAdapter.getFileExistCount() > 0) {
                                mCurrStatus = STATUS_SELECT;
                                mSelectAllCheckBox.setVisibility(View.VISIBLE);
                                mVideoListAdapter.setMode(VideoListAdapter.MODE_SELECT);
                                mVideoListAdapter.checkToggle(position);
                                if (mVideoListAdapter.isCheckedAll()) {
                                    mSelectAllCheckBox.setChecked(true);
                                } else {
                                    mSelectAllCheckBox.setChecked(false);
                                }
                                refreshButton(position);
                            } else {
                                Toast.makeText(getContext(),
                                        getString(R.string.no_downloaded_file_can_be_deleted),
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case STATUS_SELECT :
                        case STATUS_DOWNLOAD :
                        case STATUS_DELETE :
                            // fall-through
                        default:
                            break;
                    }
                    return true;
                }
            };

    private  View.OnClickListener mSelectCheckBoxOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox)v;
                    if (checkBox.isChecked()) {
                        mVideoListAdapter.checkedAll();
                    } else {
                        mVideoListAdapter.uncheckedAll();
                    }
                    refreshButton(mSelectedPosition);
                }
            };

    private static final int MSG_DELETE_DIALOG_OK = 3001;
    private static final int MSG_DELETE_DIALOG_NO = 3002;
    private static class MyHandler extends Handler {
        private final WeakReference<VideoListFragment> mFragment;
        private MyHandler(VideoListFragment fragment) {
            mFragment = new WeakReference<VideoListFragment>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            final VideoListFragment fragment = mFragment.get();
            switch (msg.what) {
                case MSG_DELETE_DIALOG_OK :
                    fragment.startDeleteAsyncTask();
                    break;
                case MSG_DELETE_DIALOG_NO :
                    break;
                default :
                    break;
            }
        }
    }

    private final static Comparator<VideoData> mVideoNameOrderComparator = new Comparator<VideoData>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(VideoData object1,VideoData object2) {
            return collator.compare(object1.getFileName(), object2.getFileName());
        }
    };

    private void selectVideoList(int listKind) {
        boolean isData = false;

        if (mListBuildCompleted) {
            switch (listKind) {
                case LIST_NORMAL:
                    if (mNormalVideoArrayList.size() > 0) {
                        mVideoListAdapter.resetArrItem(mNormalVideoArrayList);
                        isData = true;
                    }
                    break;
                case LIST_EVENT:
                    if (mEventVideoArrayList.size() > 0) {
                        mVideoListAdapter.resetArrItem(mEventVideoArrayList);
                        isData = true;
                    }
                    break;
                default:
                    break;
            }
            if (isData) {
                itemSelect(listKind, 0);
                mLoadingLayout.setVisibility(View.GONE);
                mListEmptyLayout.setVisibility(View.GONE);
                mListLayout.setVisibility(View.VISIBLE);
            } else {
                mLoadingLayout.setVisibility(View.GONE);
                mListLayout.setVisibility(View.GONE);
                mListEmptyLayout.setVisibility(View.VISIBLE);
            }
        }
        mCurrVideoList = listKind;
    }

    private void selectButton(int view_id) {
        switch (view_id) {
            case R.id.btn_normal_list :
                mNormalButton.setBackgroundResource(R.drawable.rec2_type_bt);
                mNormalButton.setTextColor(Color.WHITE);
                mEventButton.setBackgroundResource(R.drawable.rec2_type_bt_dis);
                mEventButton.setTextColor(Color.rgb(107, 123, 147));
                break;
            case R.id.btn_event_list :
                mNormalButton.setBackgroundResource(R.drawable.rec2_type_bt_dis);
                mNormalButton.setTextColor(Color.rgb(107, 123, 147));
                mEventButton.setBackgroundResource(R.drawable.rec2_type_bt);
                mEventButton.setTextColor(Color.WHITE);
                break;
            default:
                break;
        }
    }

    private void setSelectedVideoFile(int list, int position) {
        switch (list) {
            case LIST_NORMAL:
                mSelectedFileName = mNormalVideoArrayList.get(position).getFileName();
                mSelectedFilePath = FileUtil.getNormalVideoFileStorePath(
                        mSession.getDeviceToken(), mSelectedFileName);
                break;
            case LIST_EVENT:
                mSelectedFileName = mEventVideoArrayList.get(position).getFileName();
                mSelectedFilePath = FileUtil.getEventVideoFileStorePath(
                        mSession.getDeviceToken(), mSelectedFileName);
                break;
            default:
                break;
        }
        mSelectedPosition = position;
    }

    private void playVideoFile(String filename) {
        ArrayList<Uri> mUriArrayList = new ArrayList<>();
        int selectVideoPos = -1;

        switch (mCurrVideoList) {
            case LIST_NORMAL :
                for (VideoData video : mNormalVideoArrayList) {
                    String filepath = FileUtil.getNormalVideoFileStorePath(
                            mSession.getDeviceToken(), video.getFileName());
                    File file = new File(filepath);
                    if(file.exists() && file.length() > 0) {
                        mUriArrayList.add(Uri.fromFile(file));
                        if (selectVideoPos == -1 && filename.equals(video.getFileName())) {
                            selectVideoPos = mUriArrayList.size() - 1;
                        }
                    }
                }
                break;
            case LIST_EVENT :
                for (VideoData video : mEventVideoArrayList) {
                    String filepath = FileUtil.getEventVideoFileStorePath(
                            mSession.getDeviceToken(), video.getFileName());
                    File file = new File(filepath);
                    if(file.exists() && file.length() > 0) {
                        mUriArrayList.add(Uri.fromFile(file));
                        if (selectVideoPos == -1 && filename.equals(video.getFileName())) {
                            selectVideoPos = mUriArrayList.size() - 1;
                        }
                    }
                }
                break;
        }

        Intent intent = new Intent(getContext(), PlayerActivity.class);
        intent.putParcelableArrayListExtra(Constants.Extra.VIDEO_LIST, mUriArrayList);
        intent.putExtra(Constants.Extra.SELECT_VIDEO, selectVideoPos);
        startActivity(intent);
    }

    private class RetrieveVideoFromLocalAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            buildVideoFileList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListBuildCompleted = true;
            selectVideoList(mCurrVideoList);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private void buildVideoFileList() {
        String deviceToken = mSession.getDeviceToken();
        mNormalVideoArrayList =
                DBManager.getInstance().getVideoList(VideoData.TYPE_NORMAL, deviceToken);
        mEventVideoArrayList =
                DBManager.getInstance().getVideoList(VideoData.TYPE_EVENT, deviceToken);
        addLocalVideoFileToList(deviceToken);

        if (mNormalVideoArrayList.size() > 0) {
            removeDuplicationObject(mNormalVideoArrayList);
            Collections.sort(mNormalVideoArrayList, mVideoNameOrderComparator);
            checkFileExist(LIST_NORMAL, mNormalVideoArrayList);
        }
        if (mEventVideoArrayList.size() > 0) {
            removeDuplicationObject(mEventVideoArrayList);
            Collections.sort(mEventVideoArrayList, mVideoNameOrderComparator);
            checkFileExist(LIST_EVENT, mEventVideoArrayList);
        }
    }

    private void addLocalVideoFileToList(String deviceToken) {
        File[] nFiles = FileUtil.getNormalVideoLocalFileList(deviceToken);
        for (File file : nFiles) {
            try {
                mNormalVideoArrayList.add(new VideoData(VideoData.TYPE_NORMAL, file.getName()));
            } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
            }
        }

        File[] eFiles = FileUtil.getEventVideoLocalFileList(deviceToken);
        for (File file : eFiles) {
            try {
                mEventVideoArrayList.add(new VideoData(VideoData.TYPE_EVENT, file.getName()));
            } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
            }
        }
    }

    private void removeDuplicationObject(ArrayList<VideoData> arrayList) {
        HashSet<VideoData> hs = new HashSet<>(arrayList);
        arrayList.clear();
        arrayList.addAll(hs);
    }

    private void checkFileExist(int listKind , ArrayList<VideoData> arrayList) {
        switch (listKind) {
            case LIST_NORMAL:
                for (VideoData video : arrayList) {
                    String filepath = FileUtil.getNormalVideoFileStorePath(
                            mSession.getDeviceToken(), video.getFileName());
                    File file = new File(filepath);
                    if(file.exists() && file.length() > 0) {
                        video.setExist(true);
                    }
                }
                break;
            case LIST_EVENT:
                for (VideoData video : arrayList) {
                    String filepath = FileUtil.getEventVideoFileStorePath(
                            mSession.getDeviceToken(), video.getFileName());
                    File file = new File(filepath);
                    if(file.exists() && file.length() > 0) {
                        video.setExist(true);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void itemSelect(int listKind, int position) {
        mVideoListAdapter.setSelectedPosition(position);
        mVideoListAdapter.notifyDataSetChanged();
        setSelectedVideoFile(listKind, position);
        refreshButton(position);
    }

    private void refreshButton(int position) {
        if (mCurrStatus == STATUS_NORMAL) {
            if (((VideoData) mVideoListAdapter.getItem(position)).getExist()) {
                mDownloadButton.setText(R.string.play_caps);
                WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, true);
            } else {
                mDownloadButton.setText(R.string.download_caps);
                if (mListener.isUSBConnected()) {
                    WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, true);
                } else {
                    WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, false);
                }
            }
        } else if (mCurrStatus == STATUS_SELECT) {
            int existCount = mVideoListAdapter.getFileExistCount();
            int checkedCount = mVideoListAdapter.getFileCheckedCount();

            mDownloadButton.setText(
                    getString(R.string.delete_caps_count, checkedCount, existCount));

            if (checkedCount > 0) {
                WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, true);
            } else {
                WidgetUtil.setBtnEnabled(getContext(), mDownloadButton, false);
            }
        }
    }

    private void finishSelectStatus() {
        mCurrStatus = STATUS_NORMAL;
        mSelectAllCheckBox.setVisibility(View.GONE);
        mSelectAllCheckBox.setChecked(false);
        mVideoListAdapter.setMode(VideoListAdapter.MODE_NORMAL);
        mVideoListAdapter.uncheckedAll();
    }

    private void startDeleteAsyncTask() {
        mDeleteVideoFileTask = new DeleteVideoFileAsyncTask();
        mDeleteVideoFileTask.execute();
    }

    private class DeleteVideoFileAsyncTask extends AsyncTask<Void, Void, Void> {
        Dialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomDialog(getContext(), mHandler)
                    .createProgressNoUpdateStyleDialog(getString(R.string.deleting));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String deviceToken = mSession.getDeviceToken();
            switch (mCurrVideoList) {
                case LIST_NORMAL :
                    // DELETE
                    for (VideoData data : mNormalVideoArrayList) {
                        if (data.getExist() && data.getChecked()) {
                            File file = new File(FileUtil.getNormalVideoFileStorePath(mSession.getDeviceToken(), data.getFileName()));
                            if (file.exists()) {
                                try {
                                    if (!file.delete()) {
                                        Log.e(TAG, "Delete fail : " + file.getAbsolutePath());
                                    }
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    // REBUILD FILE LIST
                    mNormalVideoArrayList =
                            DBManager.getInstance().getVideoList(VideoData.TYPE_NORMAL, deviceToken);
                    File[] nFiles = FileUtil.getNormalVideoLocalFileList(deviceToken);
                    for (File file : nFiles) {
                        try {
                            mNormalVideoArrayList.add(new VideoData(VideoData.TYPE_NORMAL, file.getName()));
                        } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
                        }
                    }
                    if (mNormalVideoArrayList.size() > 0) {
                        removeDuplicationObject(mNormalVideoArrayList);
                        Collections.sort(mNormalVideoArrayList, mVideoNameOrderComparator);
                        checkFileExist(LIST_NORMAL, mNormalVideoArrayList);
                    }
                    break;
                case LIST_EVENT :
                    // DELETE
                    for (VideoData data : mEventVideoArrayList) {
                        if (data.getExist() && data.getChecked()) {
                            File file = new File(FileUtil.getEventVideoFileStorePath(mSession.getDeviceToken(), data.getFileName()));
                            if (file.exists()) {
                                try {
                                    if (!file.delete()) {
                                        Log.e(TAG, "Delete fail : " + file.getAbsolutePath());
                                    }
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    // REBUILD FILE LIST
                    mEventVideoArrayList =
                            DBManager.getInstance().getVideoList(VideoData.TYPE_EVENT, deviceToken);
                    File[] eFiles = FileUtil.getEventVideoLocalFileList(deviceToken);
                    for (File file : eFiles) {
                        try {
                            mEventVideoArrayList.add(new VideoData(VideoData.TYPE_EVENT, file.getName()));
                        } catch (IllegalArgumentException | ParseException | IndexOutOfBoundsException ignore) {
                        }
                    }
                    if (mEventVideoArrayList.size() > 0) {
                        removeDuplicationObject(mEventVideoArrayList);
                        Collections.sort(mEventVideoArrayList, mVideoNameOrderComparator);
                        checkFileExist(LIST_EVENT, mEventVideoArrayList);
                    }
                    break;
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.notifyMessage(MainMenuFragment.FRAGMENT_TAG, Constants.NotifyMsg.VIDEO_COUNT_REFRESH, null);
            progressDialog.dismiss();
            finishSelectStatus();
            selectVideoList(mCurrVideoList);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
