package com.adasone.hm320a.canbusanalyzer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.R;
import com.adasone.hm320a.canbusanalyzer.CAdapter.CChannel;
import com.adasone.hm320a.canbusanalyzer.CAdapter.CChannel.CCanSettings;
import com.adasone.hm320a.canbusanalyzer.CAdapter.IOnStateChanged;
import com.adasone.hm320a.canbusanalyzer.CCanBusInterface.CCanFrame;
import com.adasone.hm320a.canbusanalyzer.CDatHolder.CDatTable.CDatItem;
import com.adasone.hm320a.canbusanalyzer.CDatHolder.CDatTrace;
import com.adasone.hm320a.canbusanalyzer.CLogger.IOnLogStr;
import com.adasone.hm320a.canbusanalyzer.OpenFileDialog.OpenDialogListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


public class ActivityEngineer extends BaseActivity {
    public static final String ADDMOB_APP_ID = "ca-app-pub-1328633298301741~8794403219";
    private static final String KEY_PREF_TRACE_SAVE_DIR = "MainActivity.TraceSaveDir";
    private static final int REQUEST_CODE_APP_SETTINGS = 100;
    private static final int REQUEST_CODE_CAN_SETTINGS = 101;
    public static final int REQUEST_READWRITE_STORAGE = 102;
    private static final int TABLE_UPDATE_PERIOD = 200;
    private static final String TAB_TAG_LOG = "tgLog";
    private static final String TAB_TAG_TABLE = "tgTable";
    private static final String TAB_TAG_TRACE = "tgTrace";
    private static final String TAB_TAG_HISTORY = "tgHistory";
    private static int mActiveChannelIndex = -1;
    private static String sLastTabTag = null;
    private CChannel mActiveChannel = null;
    //    private AdView mAdView;
    private CAdapter mAdapter;
    BroadcastReceiver mAdapterEventReceiver = new C02672();
    private Button mBtCheckAdapter;
    private ImageButton mBtSaveTrace;
    private ImageButton mBtSettings;
    private ImageButton mBtStart;
    private ImageButton mBtStop;
    private CFrameTableListAdapter mCurrentFrameTableListAdapter;
    private CFrameTraceListAdapter mCurrentFrameTraceListAdapter;
    private CDatHolder mDatHolder;
    private ArrayList<CFrameTableListAdapter> mFrameTableListAdapter;
    private ArrayList<CFrameTraceListAdapter> mFrameTraceListAdapter;
    private ImageView mIvAdapterState;
    private ListView mLvFrameTable;
    private ListView mLvFrameTrace;
    private RadioGroup mRgChannel;
    Handler mTableUpdateHandler = new Handler();
    Runnable mTableUpdateRunnable = new C02661();
    private TabHost mTabs;
    private CCanFrame mTestCanFrame;
    private TextView mTvCanMode;
    private TextView mTvCanRate;
    private TextView mTvLog;
    private TextView mTvTraceStatus;
    BroadcastReceiver mUsbReceiver = new C02694();

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$1 */
    class C02661 implements Runnable {
        C02661() {
        }

        public void run() {
            if (ActivityEngineer.this.mActiveChannel.isRunning()) {
                ActivityEngineer.this.updateTable(null, false);
            }
            ActivityEngineer.this.mTableUpdateHandler.postDelayed(ActivityEngineer.this.mTableUpdateRunnable, 200);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$2 */
    class C02672 extends BroadcastReceiver {
        C02672() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CAdapter.ACTION_LOG_STR) && intent.hasExtra(CAdapter.EXTRA_LOG_STR)) {
                ActivityEngineer.this.logStr(intent.getStringExtra(CAdapter.EXTRA_LOG_STR));
            }
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$3 */
    class C02683 implements OnClickListener {
        C02683() {
        }

        public void onClick(DialogInterface dialog, int which) {
//            MainActivity.this.startActivity(new Intent(MainActivity.this, DonationActivity.class));
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$4 */
    class C02694 extends BroadcastReceiver {
        C02694() {
        }

        public void onReceive(Context context, Intent intent) {
            ActivityEngineer.this.mAdapter.processReceiveIntent(intent);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$8 */
    class C02708 implements OnTabChangeListener {
//        C02708() {
//        }

        public void onTabChanged(String tabId) {
            ActivityEngineer.this.updateTable(tabId, true);
            ActivityEngineer.sLastTabTag = tabId;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$9 */
    class C02719 implements View.OnClickListener {
        C02719() {
        }

        public void onClick(View v) {
            ActivityEngineer.this.checkAdapter();
        }
    }

    public class CFrameTableListAdapter extends ArrayAdapter<CDatItem> {
        public CFrameTableListAdapter(ArrayList<CDatItem> list) {
            super(ActivityEngineer.this, 0, list);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String s;
            if (convertView == null) {
                convertView = ActivityEngineer.this.getLayoutInflater().inflate(R.layout.frame_table_item, null);
            }
            CDatItem data = (CDatItem) getItem(position);
            TextView tvFrameId = (TextView) convertView.findViewById(R.id.tvFrameId);
            if (ActivityEngineer.this.mActiveChannel.getCanSettings().isExtended()) {
                s = String.format("%1$08X", new Object[]{Integer.valueOf(data.getFrameId())});
            } else {
                s = String.format("%1$03X", new Object[]{Integer.valueOf(data.getFrameId())});
            }
            tvFrameId.setText(s);
            ((TextView) convertView.findViewById(R.id.tvData)).setText(data.getDataStr());
            ((TextView) convertView.findViewById(R.id.tvCount)).setText(Integer.toString(data.getCount()));
            ((TextView) convertView.findViewById(R.id.tvInterval)).setText(Integer.toString(data.getInterval() / 10));
            return convertView;
        }
    }

    public class CFrameTraceListAdapter extends ArrayAdapter<CDatTrace.CDatItem> {
        public CFrameTraceListAdapter(ArrayList<CDatTrace.CDatItem> list) {
            super(ActivityEngineer.this, 0, list);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String s;
            if (convertView == null) {
                convertView = ActivityEngineer.this.getLayoutInflater().inflate(R.layout.frame_trace_item, null);
            }
            CDatTrace.CDatItem data = (CDatTrace.CDatItem) getItem(position);
            ((TextView) convertView.findViewById(R.id.tvNumber)).setText(Integer.toString(position + 1));
            TextView tvFrameId = (TextView) convertView.findViewById(R.id.tvFrameId);
            if (ActivityEngineer.this.mActiveChannel.getCanSettings().isExtended()) {
                s = String.format("%1$08X", new Object[]{Integer.valueOf(data.getFrameId())});
            } else {
                s = String.format("%1$03X", new Object[]{Integer.valueOf(data.getFrameId())});
            }
            tvFrameId.setText(s);
            ((TextView) convertView.findViewById(R.id.tvData)).setText(data.getDataStr());
            ((TextView) convertView.findViewById(R.id.tvTime)).setText(String.format("%1$.3f", new Object[]{Double.valueOf(data.getTime())}));
            ((TextView) convertView.findViewById(R.id.tvLength)).setText(Integer.toString(data.getDataLen()));
            return convertView;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$5 */
    class C04435 implements OpenDialogListener {
        C04435() {
        }

        public void OnSelectedFile(String fileName) {
            Log.d(getClass().getName(), "selected file " + fileName);
            ActivityEngineer.this.saveTraceToFile(fileName);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$6 */
    class C04446 implements IOnLogStr {
        C04446() {
        }

        public void onLogStr(String s) {
            ActivityEngineer.this.logStr(s);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$7 */
    class C04457 implements IOnStateChanged {
        C04457() {
        }

        public void onStateChanged(boolean stateOn) {
            ActivityEngineer.this.onAdapterStateChanged(stateOn);
        }
    }

    private void updateTable(String tabTag, boolean isRenew) {
        if (tabTag == null) {
            tabTag = mTabs.getCurrentTabTag();
        }
        if (tabTag.equals(TAB_TAG_TABLE)) {
            mCurrentFrameTableListAdapter.notifyDataSetChanged();
        } else if (tabTag.equals(TAB_TAG_TRACE)) {
            CDatTrace trace = mDatHolder.getTrace(mActiveChannelIndex);
            trace.update();
            mCurrentFrameTraceListAdapter.notifyDataSetChanged();
            if (this.mActiveChannel.isRunning()) {
                if (isRenew) {
                    mLvFrameTrace.setSelection(this.mCurrentFrameTraceListAdapter.getCount() - 1);
                } else {
                    mLvFrameTrace.smoothScrollToPosition(this.mCurrentFrameTraceListAdapter.getCount() - 1);
                }
            }
            mTvTraceStatus.setText(Integer.toString(trace.getCount()));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void clearData() {
        mDatHolder.clearData();
        mCurrentFrameTableListAdapter.notifyDataSetChanged();
        mCurrentFrameTraceListAdapter.notifyDataSetChanged();
    }

    private boolean isKeepScreenOn() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(CAppSettings.KEY_PREF_KEEP_SCREEN_ON, false);
    }

    public String getModelName() {
        String s;
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            s = model;
        } else {
            s = manufacturer + " " + model;
        }
        return "Device name: " + s;
    }

    public String getAndroidVersion() {
        return "Android version: " + VERSION.SDK_INT + " (" + VERSION.RELEASE + ")";
    }

    public String getApplicationVersion() {
        return "Application version: " + BuildConfig.VERSION_NAME;
    }

    private void sendLogViaEmail() {
        String[] emailList = new String[]{"yaroslavtrymbach@gmail.com"};
        String mesTxt = (((getModelName() + "\n") + getAndroidVersion() + "\n") + getApplicationVersion() + "\n") + "\n<log start>\n" + mTvLog.getText() + "\n<log end>";
        Intent eMailIntent = new Intent("android.intent.action.SENDTO");
        eMailIntent.setData(Uri.parse("mailto:"));
        eMailIntent.putExtra("android.intent.extra.EMAIL", emailList);
        eMailIntent.putExtra("android.intent.extra.SUBJECT", "CanBusAnalyzer log");
        eMailIntent.putExtra("android.intent.extra.TEXT", mesTxt);
        try {
            startActivity(eMailIntent);
        } catch (Exception e) {
            String s = e.getMessage();
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            logStr(s);
        }
    }

//    public void makeDonation() {
//        Builder builder = new Builder(this);
//        builder.setTitle(R.string.cap_menu_donation);
//        builder.setMessage(R.string.info_donation);
//        builder.setPositiveButton(R.string.cap_button_continue, new C02683());
//        builder.setNegativeButton(R.string.cap_button_cancel, null);
//        builder.show();
//    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_clear_data:
                clearData();
                break;
            case R.id.menu_action_settings:
                SettingsActivity.clearResultIntent();
                startActivityForResult(new Intent(this, SettingsActivity.class), 100);
                break;
            case R.id.menu_action_send_email:
                sendLogViaEmail();
                break;
//            case R.id.menu_action_donation:
//                //makeDonation();
//                break;
            case R.id.menu_action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == -1 && data != null && data.getBooleanExtra(SettingsActivity.EXTRA_SCREEN_ORIENTATION_CHANGERD, false)) {
                setScreenOrientation();
            }
            if (this.mAdapter.isRunning()) {
                mLvFrameTable.setKeepScreenOn(isKeepScreenOn());
            }
        } else if (requestCode == REQUEST_CODE_CAN_SETTINGS && resultCode == -1) {
            mActiveChannel.getCanSettings().save();
            updateControlsState();
        }
    }

    private void setActiveChannel(int channelIndex) {
        if (mActiveChannelIndex != channelIndex) {
            mActiveChannelIndex = channelIndex;
            mActiveChannel = mAdapter.getChannel(channelIndex);
            mCurrentFrameTableListAdapter = (CFrameTableListAdapter) mFrameTableListAdapter.get(channelIndex);
            mLvFrameTable.setAdapter(this.mCurrentFrameTableListAdapter);
            mCurrentFrameTraceListAdapter = (CFrameTraceListAdapter) mFrameTraceListAdapter.get(channelIndex);
            mLvFrameTrace.setAdapter(this.mCurrentFrameTraceListAdapter);
            if (mActiveChannelIndex == 0) {
                mRgChannel.check(R.id.rbChannel1);
            } else if (mActiveChannelIndex == 1) {
                mRgChannel.check(R.id.rbChannel2);
            }
            updateControlsState();
        }
    }

    private void updateControlsState() {
        String s;
        if (this.mAdapter.isStateOn()) {
            boolean z;
            mIvAdapterState.setImageResource(R.drawable.adapteron);
            mBtCheckAdapter.setVisibility(View.VISIBLE);
            boolean isRun = mActiveChannel.isRunning();
            ImageButton imageButton = mBtStart;
            if (isRun) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
            imageButton = mBtSettings;
            if (isRun) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
            mBtStop.setEnabled(isRun);
        } else {
            mIvAdapterState.setImageResource(R.drawable.adapteroff);
            mBtCheckAdapter.setVisibility(View.VISIBLE);
            mBtStart.setEnabled(false);
            mBtStop.setEnabled(false);
            mBtSettings.setEnabled(true);
        }
        if (this.mActiveChannel.isRunning()) {
            mBtSaveTrace.setEnabled(false);
        } else {
            mBtSaveTrace.setEnabled(true);
        }
        CCanSettings canSettings = mActiveChannel.getCanSettings();
        if (canSettings.isExtended()) {
            s = getResources().getString(R.string.mode_extended);
        } else {
            s = getResources().getString(R.string.mode_standard);
        }
        mTvCanMode.setText(s);
        mTvCanRate.setText(canSettings.getRate().toStringEx());
    }

    private void start() {
        if (!this.mActiveChannel.isRunning()) {
            mActiveChannel.start();
            updateControlsState();
            if (isKeepScreenOn()) {
                mLvFrameTable.setKeepScreenOn(true);
            }
        }
    }

    private void stop() {
        if (this.mActiveChannel.isRunning()) {
            mActiveChannel.stop();
            updateControlsState();
            if (isKeepScreenOn() && !this.mAdapter.isRunning()) {
                mLvFrameTable.setKeepScreenOn(false);
            }
        }
    }

    private String formateSaveTraceFileName() {
        Calendar calendar = Calendar.getInstance();
        int dy = calendar.get(Calendar.YEAR);
        int dm = calendar.get(Calendar.MONTH) + 1;
        int dd = calendar.get(Calendar.DATE);
        int th = calendar.get(Calendar.HOUR);
        int tm = calendar.get(Calendar.MINUTE);
        int ts = calendar.get(Calendar.SECOND);
        return String.format("Trace-%1$02d%2$02d%3$02d-%4$02d%5$02d%6$02d.txt", new Object[]{Integer.valueOf(dy % 100), Integer.valueOf(dm), Integer.valueOf(dd), Integer.valueOf(th), Integer.valueOf(tm), Integer.valueOf(ts)});
    }

    public boolean saveTraceToFile(String filePath) {
        CDatTrace trace = mDatHolder.getTrace(mActiveChannelIndex);
        String errStr = "";
        boolean res = false;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            Resources r = getResources();
            writer.write(r.getString(R.string.cap_table_number) + "; " + r.getString(R.string.cap_table_time) + "; " + r.getString(R.string.cap_table_id) + "; " + r.getString(R.string.cap_table_length) + "; " + r.getString(R.string.cap_table_data) + ";");
            writer.newLine();
            for (int i = 0; i < trace.getCount(); i++) {
                CDatTrace.CDatItem item = trace.getItem(i);
                String s = (Integer.toString(i + 1) + "; ") + String.format("%1$.3f", new Object[]{Double.valueOf(item.getTime())}) + "; ";
                if (this.mActiveChannel.getCanSettings().isExtended()) {
                    s = s + String.format("%1$08x", new Object[]{Integer.valueOf(item.getFrameId())}) + "; ";
                } else {
                    s = s + String.format("%1$03x", new Object[]{Integer.valueOf(item.getFrameId())}) + "; ";
                }
                writer.write((s + Integer.toString(item.getDataLen()) + "; ") + item.getDataStr() + ";");
                writer.newLine();
            }
            writer.close();
            res = true;
        } catch (IOException ex) {
            errStr = ex.getMessage();
        }
        if (res) {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            if (filePath != null) {
                String dirPath = new File(filePath).getParent();
                Editor editor = PreferenceManager.getDefaultSharedPreferences(CGeneral.context).edit();
                editor.putString(KEY_PREF_TRACE_SAVE_DIR, dirPath);
                editor.commit();
            }
        } else {
            Toast.makeText(this, "Failed! " + errStr, Toast.LENGTH_SHORT).show();
        }
        return res;
    }

    private void saveTrace() {
        checkStorageWritePermission();
        String initPath = PreferenceManager.getDefaultSharedPreferences(CGeneral.context).getString(KEY_PREF_TRACE_SAVE_DIR, null);
        String startFileName = formateSaveTraceFileName();
        OpenFileDialog fileDialog = new OpenFileDialog(this);
        fileDialog.setStartPath(initPath);
        fileDialog.setStartFileName(startFileName);
        fileDialog.setIsEditModeOption().setIsKeepFileNameOption();
        fileDialog.setFolderIcon(getResources().getDrawable(R.drawable.folder));
        fileDialog.setFileIcon(getResources().getDrawable(R.drawable.file));
        fileDialog.setOpenDialogListener(new C04435());
        fileDialog.show();
    }

    private void changeSettings() {
        Intent intent = new Intent(this, com.adasone.hm320a.canbusanalyzer.CanSettingsActivity.class);
        intent.putExtra(CanSettingsActivity.EXTRA_CHANNEL, mActiveChannelIndex);
        startActivityForResult(intent, REQUEST_CODE_CAN_SETTINGS);
    }

    private void initAdapter() {
        mAdapter = CAdapter.getInstance();
        mAdapter.setOnLogStr(new C04446());
        mAdapter.setOnStateChanged(new C04457());
        mAdapter.init(this);
    }

    private void onAdapterStateChanged(boolean stateOn) {
        if (stateOn) {
            updateControlsState();
        } else {
            updateControlsState();
        }
    }

    private void clearLog() {
        mTvLog.setText("");
    }

    private void logStr(String S) {
        mTvLog.append(S + "\n");
    }

    private void checkStorageWritePermission() {
        if (VERSION.SDK_INT < 23) {
            return;
        }
        if (checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_READWRITE_STORAGE);
            return;
        }
        Log.d(CGeneral.APP_TAG, "Permissin is granted");
    }

    private void checkAdapter() {
        logStr("checkAdapter");
        mAdapter.check();
    }

    private void testFill() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_engineer_mode);
//        MobileAds.initialize(this, ADDMOB_APP_ID);
//        mAdView = (AdView) findViewById(R.id.adView);
//        mAdView.loadAd(new AdRequest.Builder().build());
        mTabs = (TabHost) findViewById(R.id.tabHost);
        mTabs.setup();
        TabHost.TabSpec spec = mTabs.newTabSpec(TAB_TAG_TABLE);
        spec.setContent(R.id.tabTable);
        spec.setIndicator("Table");
        mTabs.addTab(spec);
        spec = mTabs.newTabSpec(TAB_TAG_TRACE);
        spec.setContent(R.id.tabTrace);
        spec.setIndicator("Trace");
        mTabs.addTab(spec);
        spec = mTabs.newTabSpec(TAB_TAG_LOG);
        spec.setContent(R.id.tabLog);
        spec.setIndicator("Log");
        mTabs.addTab(spec);
        spec = mTabs.newTabSpec(TAB_TAG_HISTORY);
        spec.setContent(R.id.tabLog);
        spec.setIndicator("History");
        mTabs.addTab(spec);
        mTabs.setOnTabChangedListener(new C02708());
        mTvLog = (TextView) findViewById(R.id.tvLog);
        mIvAdapterState = (ImageView) findViewById(R.id.ivAdapterState);
        mBtCheckAdapter = (Button) findViewById(R.id.btCheckAdapter);
        mBtCheckAdapter.setOnClickListener(new C02719());
        mBtStart = (ImageButton) findViewById(R.id.btStart);
        mBtStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityEngineer.this.start();
            }
        });
        mBtStop = (ImageButton) findViewById(R.id.btStop);
        mBtStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityEngineer.this.stop();
            }
        });
        mBtSaveTrace = (ImageButton) findViewById(R.id.btSaveTrace);
        mBtSaveTrace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityEngineer.this.saveTrace();
            }
        });
        mBtSettings = (ImageButton) findViewById(R.id.btSettings);
        mBtSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityEngineer.this.changeSettings();
            }
        });
        mLvFrameTable = (ListView) findViewById(R.id.lvFrameTable);
        mLvFrameTrace = (ListView) findViewById(R.id.lvFrameTrace);
        mTvCanRate = (TextView) findViewById(R.id.tvCanRate);
        mTvCanMode = (TextView) findViewById(R.id.tvCanMode);
        mTvTraceStatus = (TextView) findViewById(R.id.tvTraceStatus);
        mDatHolder = CDatHolder.getInstance();
        mFrameTableListAdapter = new ArrayList();
        for (i = 0; i < 2; i++) {
            mFrameTableListAdapter.add(new CFrameTableListAdapter(this.mDatHolder.getTable(i).getDatList()));
        }
        mFrameTraceListAdapter = new ArrayList();
        for (i = 0; i < 2; i++) {
            mFrameTraceListAdapter.add(new CFrameTraceListAdapter(this.mDatHolder.getTrace(i).getDatList()));
        }
        mRgChannel = (RadioGroup) findViewById(R.id.rgChannel);
        mRgChannel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbChannel1:
                        ActivityEngineer.this.setActiveChannel(0);
                        return;
                    case R.id.rbChannel2:
                        ActivityEngineer.this.setActiveChannel(1);
                        return;
                    default:
                        return;
                }
            }
        });
        CGeneral.resources = getResources();
        CGeneral.context = this;
        testFill();
        initAdapter();
        if (mActiveChannelIndex == -1) {
            setActiveChannel(0);
        } else {
            int channel = mActiveChannelIndex;
            mActiveChannelIndex = -1;
            setActiveChannel(channel);
        }
        mTableUpdateHandler.postDelayed(this.mTableUpdateRunnable, 200);
    }

//    protected void onStart() {
//        super.onStart();
//    }

    protected void onResume() {
        super.onResume();
        mAdapter.check();
        if (sLastTabTag != null) {
            mTabs.setCurrentTabByTag(sLastTabTag);
        }
        registerReceiver(this.mUsbReceiver, new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED"));
        registerReceiver(this.mUsbReceiver, new IntentFilter(CAdapter.ACTION_USB_PERMISSION));
        IntentFilter f = new IntentFilter();
        f.addAction(CAdapter.ACTION_LOG_STR);
        registerReceiver(this.mAdapterEventReceiver, f);
        updateControlsState();
    }
//
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.mUsbReceiver);
        unregisterReceiver(this.mAdapterEventReceiver);
    }
}
