package vn.penkun.obd2scantool.canbusanalyzer;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
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
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import vn.penkun.obd2scantool.canbusanalyzer.CAdapter.CChannel;
import vn.penkun.obd2scantool.canbusanalyzer.CAdapter.CChannel.CCanSettings;
import vn.penkun.obd2scantool.canbusanalyzer.CAdapter.IOnStateChanged;
import vn.penkun.obd2scantool.canbusanalyzer.CCanBusInterface.CCanFrame;
import vn.penkun.obd2scantool.canbusanalyzer.CDatHolder.CDatTable.CDatItem;
import vn.penkun.obd2scantool.canbusanalyzer.CDatHolder.CDatTrace;
import vn.penkun.obd2scantool.canbusanalyzer.CLogger.IOnLogStr;
import vn.penkun.obd2scantool.canbusanalyzer.OpenFileDialog.OpenDialogListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends BaseActivity {
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
            if (MainActivity.this.mActiveChannel.isRunning()) {
                MainActivity.this.updateTable(null, false);
            }
            MainActivity.this.mTableUpdateHandler.postDelayed(MainActivity.this.mTableUpdateRunnable, 200);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$2 */
    class C02672 extends BroadcastReceiver {
        C02672() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CAdapter.ACTION_LOG_STR) && intent.hasExtra(CAdapter.EXTRA_LOG_STR)) {
                MainActivity.this.logStr(intent.getStringExtra(CAdapter.EXTRA_LOG_STR));
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
            MainActivity.this.mAdapter.processReceiveIntent(intent);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$8 */
    class C02708 implements OnTabChangeListener {
        C02708() {
        }

        public void onTabChanged(String tabId) {
            MainActivity.this.updateTable(tabId, true);
            MainActivity.sLastTabTag = tabId;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$9 */
    class C02719 implements View.OnClickListener {
        C02719() {
        }

        public void onClick(View v) {
            MainActivity.this.checkAdapter();
        }
    }

    public class CFrameTableListAdapter extends ArrayAdapter<CDatItem> {
        public CFrameTableListAdapter(ArrayList<CDatItem> list) {
            super(MainActivity.this, 0, list);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String s;
            if (convertView == null) {
                convertView = MainActivity.this.getLayoutInflater().inflate(C0280R.layout.frame_table_item, null);
            }
            CDatItem data = (CDatItem) getItem(position);
            TextView tvFrameId = (TextView) convertView.findViewById(C0280R.id.tvFrameId);
            if (MainActivity.this.mActiveChannel.getCanSettings().isExtended()) {
                s = String.format("%1$08X", new Object[]{Integer.valueOf(data.getFrameId())});
            } else {
                s = String.format("%1$03X", new Object[]{Integer.valueOf(data.getFrameId())});
            }
            tvFrameId.setText(s);
            ((TextView) convertView.findViewById(C0280R.id.tvData)).setText(data.getDataStr());
            ((TextView) convertView.findViewById(C0280R.id.tvCount)).setText(Integer.toString(data.getCount()));
            ((TextView) convertView.findViewById(C0280R.id.tvInterval)).setText(Integer.toString(data.getInterval() / 10));
            return convertView;
        }
    }

    public class CFrameTraceListAdapter extends ArrayAdapter<CDatTrace.CDatItem> {
        public CFrameTraceListAdapter(ArrayList<CDatTrace.CDatItem> list) {
            super(MainActivity.this, 0, list);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String s;
            if (convertView == null) {
                convertView = MainActivity.this.getLayoutInflater().inflate(C0280R.layout.frame_trace_item, null);
            }
            CDatTrace.CDatItem data = (CDatTrace.CDatItem) getItem(position);
            ((TextView) convertView.findViewById(C0280R.id.tvNumber)).setText(Integer.toString(position + 1));
            TextView tvFrameId = (TextView) convertView.findViewById(C0280R.id.tvFrameId);
            if (MainActivity.this.mActiveChannel.getCanSettings().isExtended()) {
                s = String.format("%1$08X", new Object[]{Integer.valueOf(data.getFrameId())});
            } else {
                s = String.format("%1$03X", new Object[]{Integer.valueOf(data.getFrameId())});
            }
            tvFrameId.setText(s);
            ((TextView) convertView.findViewById(C0280R.id.tvData)).setText(data.getDataStr());
            ((TextView) convertView.findViewById(C0280R.id.tvTime)).setText(String.format("%1$.3f", new Object[]{Double.valueOf(data.getTime())}));
            ((TextView) convertView.findViewById(C0280R.id.tvLength)).setText(Integer.toString(data.getDataLen()));
            return convertView;
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$5 */
    class C04435 implements OpenDialogListener {
        C04435() {
        }

        public void OnSelectedFile(String fileName) {
            Log.d(getClass().getName(), "selected file " + fileName);
            MainActivity.this.saveTraceToFile(fileName);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$6 */
    class C04446 implements IOnLogStr {
        C04446() {
        }

        public void onLogStr(String s) {
            MainActivity.this.logStr(s);
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.MainActivity$7 */
    class C04457 implements IOnStateChanged {
        C04457() {
        }

        public void onStateChanged(boolean stateOn) {
            MainActivity.this.onAdapterStateChanged(stateOn);
        }
    }

    private void updateTable(String tabTag, boolean isRenew) {
        if (tabTag == null) {
            tabTag = this.mTabs.getCurrentTabTag();
        }
        if (tabTag.equals(TAB_TAG_TABLE)) {
            this.mCurrentFrameTableListAdapter.notifyDataSetChanged();
        } else if (tabTag.equals(TAB_TAG_TRACE)) {
            CDatTrace trace = this.mDatHolder.getTrace(mActiveChannelIndex);
            trace.update();
            this.mCurrentFrameTraceListAdapter.notifyDataSetChanged();
            if (this.mActiveChannel.isRunning()) {
                if (isRenew) {
                    this.mLvFrameTrace.setSelection(this.mCurrentFrameTraceListAdapter.getCount() - 1);
                } else {
                    this.mLvFrameTrace.smoothScrollToPosition(this.mCurrentFrameTraceListAdapter.getCount() - 1);
                }
            }
            this.mTvTraceStatus.setText(Integer.toString(trace.getCount()));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0280R.menu.menu_main, menu);
        return true;
    }

    private void clearData() {
        this.mDatHolder.clearData();
        this.mCurrentFrameTableListAdapter.notifyDataSetChanged();
        this.mCurrentFrameTraceListAdapter.notifyDataSetChanged();
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
        String mesTxt = (((getModelName() + "\n") + getAndroidVersion() + "\n") + getApplicationVersion() + "\n") + "\n<log start>\n" + this.mTvLog.getText() + "\n<log end>";
        Intent eMailIntent = new Intent("android.intent.action.SENDTO");
        eMailIntent.setData(Uri.parse("mailto:"));
        eMailIntent.putExtra("android.intent.extra.EMAIL", emailList);
        eMailIntent.putExtra("android.intent.extra.SUBJECT", "CanBusAnalyzer log");
        eMailIntent.putExtra("android.intent.extra.TEXT", mesTxt);
        try {
            startActivity(eMailIntent);
        } catch (Exception e) {
            String s = e.getMessage();
            Toast.makeText(this, s, 1).show();
            logStr(s);
        }
    }

    public void makeDonation() {
        Builder builder = new Builder(this);
        builder.setTitle(C0280R.string.cap_menu_donation);
        builder.setMessage(C0280R.string.info_donation);
        builder.setPositiveButton(C0280R.string.cap_button_continue, new C02683());
        builder.setNegativeButton(C0280R.string.cap_button_cancel, null);
        builder.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case C0280R.id.menu_action_clear_data:
                clearData();
                break;
            case C0280R.id.menu_action_settings:
                SettingsActivity.clearResultIntent();
                startActivityForResult(new Intent(this, SettingsActivity.class), 100);
                break;
            case C0280R.id.menu_action_send_email:
                sendLogViaEmail();
                break;
            case C0280R.id.menu_action_donation:
                makeDonation();
                break;
            case C0280R.id.menu_action_about:
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
                this.mLvFrameTable.setKeepScreenOn(isKeepScreenOn());
            }
        } else if (requestCode == REQUEST_CODE_CAN_SETTINGS && resultCode == -1) {
            this.mActiveChannel.getCanSettings().save();
            updateControlsState();
        }
    }

    private void setActiveChannel(int channelIndex) {
        if (mActiveChannelIndex != channelIndex) {
            mActiveChannelIndex = channelIndex;
            this.mActiveChannel = this.mAdapter.getChannel(channelIndex);
            this.mCurrentFrameTableListAdapter = (CFrameTableListAdapter) this.mFrameTableListAdapter.get(channelIndex);
            this.mLvFrameTable.setAdapter(this.mCurrentFrameTableListAdapter);
            this.mCurrentFrameTraceListAdapter = (CFrameTraceListAdapter) this.mFrameTraceListAdapter.get(channelIndex);
            this.mLvFrameTrace.setAdapter(this.mCurrentFrameTraceListAdapter);
            if (mActiveChannelIndex == 0) {
                this.mRgChannel.check(C0280R.id.rbChannel1);
            } else if (mActiveChannelIndex == 1) {
                this.mRgChannel.check(C0280R.id.rbChannel2);
            }
            updateControlsState();
        }
    }

    private void updateControlsState() {
        String s;
        if (this.mAdapter.isStateOn()) {
            boolean z;
            this.mIvAdapterState.setImageResource(C0280R.drawable.adapteron);
            this.mBtCheckAdapter.setVisibility(4);
            boolean isRun = this.mActiveChannel.isRunning();
            ImageButton imageButton = this.mBtStart;
            if (isRun) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
            imageButton = this.mBtSettings;
            if (isRun) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
            this.mBtStop.setEnabled(isRun);
        } else {
            this.mIvAdapterState.setImageResource(C0280R.drawable.adapteroff);
            this.mBtCheckAdapter.setVisibility(0);
            this.mBtStart.setEnabled(false);
            this.mBtStop.setEnabled(false);
            this.mBtSettings.setEnabled(true);
        }
        if (this.mActiveChannel.isRunning()) {
            this.mBtSaveTrace.setEnabled(false);
        } else {
            this.mBtSaveTrace.setEnabled(true);
        }
        CCanSettings canSettings = this.mActiveChannel.getCanSettings();
        if (canSettings.isExtended()) {
            s = getResources().getString(C0280R.string.mode_extended);
        } else {
            s = getResources().getString(C0280R.string.mode_standard);
        }
        this.mTvCanMode.setText(s);
        this.mTvCanRate.setText(canSettings.getRate().toStringEx());
    }

    private void start() {
        if (!this.mActiveChannel.isRunning()) {
            this.mActiveChannel.start();
            updateControlsState();
            if (isKeepScreenOn()) {
                this.mLvFrameTable.setKeepScreenOn(true);
            }
        }
    }

    private void stop() {
        if (this.mActiveChannel.isRunning()) {
            this.mActiveChannel.stop();
            updateControlsState();
            if (isKeepScreenOn() && !this.mAdapter.isRunning()) {
                this.mLvFrameTable.setKeepScreenOn(false);
            }
        }
    }

    private String formateSaveTraceFileName() {
        Calendar calendar = Calendar.getInstance();
        int dy = calendar.get(1);
        int dm = calendar.get(2) + 1;
        int dd = calendar.get(5);
        int th = calendar.get(11);
        int tm = calendar.get(12);
        int ts = calendar.get(13);
        return String.format("Trace-%1$02d%2$02d%3$02d-%4$02d%5$02d%6$02d.txt", new Object[]{Integer.valueOf(dy % 100), Integer.valueOf(dm), Integer.valueOf(dd), Integer.valueOf(th), Integer.valueOf(tm), Integer.valueOf(ts)});
    }

    public boolean saveTraceToFile(String filePath) {
        CDatTrace trace = this.mDatHolder.getTrace(mActiveChannelIndex);
        String errStr = "";
        boolean res = false;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            Resources r = getResources();
            writer.write(r.getString(C0280R.string.cap_table_number) + "; " + r.getString(C0280R.string.cap_table_time) + "; " + r.getString(C0280R.string.cap_table_id) + "; " + r.getString(C0280R.string.cap_table_length) + "; " + r.getString(C0280R.string.cap_table_data) + ";");
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
            Toast.makeText(this, "Success", 0).show();
            if (filePath != null) {
                String dirPath = new File(filePath).getParent();
                Editor editor = PreferenceManager.getDefaultSharedPreferences(CGeneral.context).edit();
                editor.putString(KEY_PREF_TRACE_SAVE_DIR, dirPath);
                editor.commit();
            }
        } else {
            Toast.makeText(this, "Failed! " + errStr, 0).show();
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
        fileDialog.setFolderIcon(getResources().getDrawable(C0280R.drawable.folder));
        fileDialog.setFileIcon(getResources().getDrawable(C0280R.drawable.file));
        fileDialog.setOpenDialogListener(new C04435());
        fileDialog.show();
    }

    private void changeSettings() {
        Intent intent = new Intent(this, CanSettingsActivity.class);
        intent.putExtra(CanSettingsActivity.EXTRA_CHANNEL, mActiveChannelIndex);
        startActivityForResult(intent, REQUEST_CODE_CAN_SETTINGS);
    }

    private void initAdapter() {
        this.mAdapter = CAdapter.getInstance();
        this.mAdapter.setOnLogStr(new C04446());
        this.mAdapter.setOnStateChanged(new C04457());
        this.mAdapter.init(this);
    }

    private void onAdapterStateChanged(boolean stateOn) {
        if (stateOn) {
            updateControlsState();
        } else {
            updateControlsState();
        }
    }

    private void clearLog() {
        this.mTvLog.setText("");
    }

    private void logStr(String S) {
        this.mTvLog.append(S + "\n");
    }

    private void checkStorageWritePermission() {
        if (VERSION.SDK_INT < 23) {
            return;
        }
        if (checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", Process.myPid(), Process.myUid()) != 0) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, REQUEST_READWRITE_STORAGE);
            return;
        }
        Log.d(CGeneral.APP_TAG, "Permissin is granted");
    }

    private void checkAdapter() {
        logStr("checkAdapter");
        this.mAdapter.check();
    }

    private void testFill() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        setContentView(C0280R.layout.activity_main);
//        MobileAds.initialize(this, ADDMOB_APP_ID);
//        this.mAdView = (AdView) findViewById(C0280R.id.adView);
//        this.mAdView.loadAd(new AdRequest.Builder().build());
        this.mTabs = (TabHost) findViewById(C0280R.id.tabHost);
        this.mTabs.setup();
        TabSpec spec = this.mTabs.newTabSpec(TAB_TAG_TABLE);
        spec.setContent(C0280R.id.tabTable);
        spec.setIndicator("Table");
        this.mTabs.addTab(spec);
        spec = this.mTabs.newTabSpec(TAB_TAG_TRACE);
        spec.setContent(C0280R.id.tabTrace);
        spec.setIndicator("Trace");
        this.mTabs.addTab(spec);
        spec = this.mTabs.newTabSpec(TAB_TAG_LOG);
        spec.setContent(C0280R.id.tabLog);
        spec.setIndicator("Log");
        this.mTabs.addTab(spec);
        spec = this.mTabs.newTabSpec(TAB_TAG_HISTORY);
        spec.setContent(C0280R.id.tabLog);
        spec.setIndicator("History");
        this.mTabs.addTab(spec);
        this.mTabs.setOnTabChangedListener(new C02708());
        this.mTvLog = (TextView) findViewById(C0280R.id.tvLog);
        this.mIvAdapterState = (ImageView) findViewById(C0280R.id.ivAdapterState);
        this.mBtCheckAdapter = (Button) findViewById(C0280R.id.btCheckAdapter);
        this.mBtCheckAdapter.setOnClickListener(new C02719());
        this.mBtStart = (ImageButton) findViewById(C0280R.id.btStart);
        this.mBtStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.start();
            }
        });
        this.mBtStop = (ImageButton) findViewById(C0280R.id.btStop);
        this.mBtStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.stop();
            }
        });
        this.mBtSaveTrace = (ImageButton) findViewById(C0280R.id.btSaveTrace);
        this.mBtSaveTrace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.saveTrace();
            }
        });
        this.mBtSettings = (ImageButton) findViewById(C0280R.id.btSettings);
        this.mBtSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.changeSettings();
            }
        });
        this.mLvFrameTable = (ListView) findViewById(C0280R.id.lvFrameTable);
        this.mLvFrameTrace = (ListView) findViewById(C0280R.id.lvFrameTrace);
        this.mTvCanRate = (TextView) findViewById(C0280R.id.tvCanRate);
        this.mTvCanMode = (TextView) findViewById(C0280R.id.tvCanMode);
        this.mTvTraceStatus = (TextView) findViewById(C0280R.id.tvTraceStatus);
        this.mDatHolder = CDatHolder.getInstance();
        this.mFrameTableListAdapter = new ArrayList();
        for (i = 0; i < 2; i++) {
            this.mFrameTableListAdapter.add(new CFrameTableListAdapter(this.mDatHolder.getTable(i).getDatList()));
        }
        this.mFrameTraceListAdapter = new ArrayList();
        for (i = 0; i < 2; i++) {
            this.mFrameTraceListAdapter.add(new CFrameTraceListAdapter(this.mDatHolder.getTrace(i).getDatList()));
        }
        this.mRgChannel = (RadioGroup) findViewById(C0280R.id.rgChannel);
        this.mRgChannel.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case C0280R.id.rbChannel1:
                        MainActivity.this.setActiveChannel(0);
                        return;
                    case C0280R.id.rbChannel2:
                        MainActivity.this.setActiveChannel(1);
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
        this.mTableUpdateHandler.postDelayed(this.mTableUpdateRunnable, 200);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        this.mAdapter.check();
        if (sLastTabTag != null) {
            this.mTabs.setCurrentTabByTag(sLastTabTag);
        }
        registerReceiver(this.mUsbReceiver, new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED"));
        registerReceiver(this.mUsbReceiver, new IntentFilter(CAdapter.ACTION_USB_PERMISSION));
        IntentFilter f = new IntentFilter();
        f.addAction(CAdapter.ACTION_LOG_STR);
        registerReceiver(this.mAdapterEventReceiver, f);
        updateControlsState();
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.mUsbReceiver);
        unregisterReceiver(this.mAdapterEventReceiver);
    }
}
