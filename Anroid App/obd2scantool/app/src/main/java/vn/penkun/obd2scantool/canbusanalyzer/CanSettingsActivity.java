package vn.penkun.obd2scantool.canbusanalyzer;

import android.app.AlertDialog.Builder;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import vn.penkun.obd2scantool.canbusanalyzer.CAdapter.CChannel.CCanSettings;
import vn.penkun.obd2scantool.canbusanalyzer.CCanBusInterface.CCanRate;
import vn.penkun.obd2scantool.canbusanalyzer.CCanBusInterface.CCanRateCustom;
import vn.penkun.obd2scantool.hexkeyboardlibrary.CHexKeyboard;
import java.util.ArrayList;

public class CanSettingsActivity extends BaseActivity {
    public static final String EXTRA_CHANNEL = "channel";
    private Button mBtApply;
    private Button mBtCancel;
    private CCanBusInterface mCanBusInterface;
    private CCanSettings mCanSettings;
    private int mChannelIndex;
    private EditText mEdBTR0;
    private EditText mEdBTR1;
    CHexKeyboard mHexKeyboard;
    private Spinner mSpMode;
    private Spinner mSpRate;
    private TextView mTvHeader;

    /* renamed from: com.yatrim.canbusanalyzer.CanSettingsActivity$1 */
    class C02601 implements OnItemSelectedListener {
        C02601() {
        }

        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            CanSettingsActivity.this.updateBTR((CCanRate) parent.getSelectedItem());
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.CanSettingsActivity$2 */
    class C02612 implements OnClickListener {
        C02612() {
        }

        public void onClick(View v) {
            CanSettingsActivity.this.apply();
        }
    }

    /* renamed from: com.yatrim.canbusanalyzer.CanSettingsActivity$3 */
    class C02623 implements OnClickListener {
        C02623() {
        }

        public void onClick(View v) {
            CanSettingsActivity.this.setResult(0);
            CanSettingsActivity.this.finish();
        }
    }

    private void updateBTR(CCanRate rate) {
        boolean isEdited = rate.rate == 0;
        if (!isEdited) {
            this.mHexKeyboard.hideCustomKeyboard();
        }
        this.mEdBTR0.setEnabled(isEdited);
        this.mEdBTR0.setText(YarConverter.ByteToHex(rate.getBTR0()));
        this.mEdBTR1.setEnabled(isEdited);
        this.mEdBTR1.setText(YarConverter.ByteToHex(rate.getBTR1()));
    }

    private void apply() {
        boolean z = true;
        int pos = this.mSpMode.getSelectedItemPosition();
        CCanSettings cCanSettings = this.mCanSettings;
        if (pos != 1) {
            z = false;
        }
        cCanSettings.setIsExtended(z);
        CCanRate rate = (CCanRate) this.mSpRate.getSelectedItem();
        if (rate.rate == 0) {
            int BTR0 = CTools.hexStrToInt(this.mEdBTR0.getText().toString(), -1);
            int BTR1 = CTools.hexStrToInt(this.mEdBTR1.getText().toString(), -1);
            if (BTR0 == -1 || BTR1 == -1) {
                showErrorMsg(getResources().getString(C0280R.string.str_msg_need_btr_set));
                return;
            }
            ((CCanRateCustom) rate).setBTRs((byte) BTR0, (byte) BTR1);
        }
        this.mCanSettings.setRate(rate);
        setResult(-1);
        finish();
    }

    private void init() {
        Resources resources = getResources();
        String s = resources.getString(C0280R.string.cap_can_settings) + " ";
        switch (this.mChannelIndex) {
            case 0:
                s = s + resources.getString(C0280R.string.cap_channel1);
                break;
            case 1:
                s = s + resources.getString(C0280R.string.cap_channel2);
                break;
        }
        this.mTvHeader.setText(s);
        ArrayList<String> modeList = new ArrayList();
        modeList.add(getResources().getString(C0280R.string.mode_standard));
        modeList.add(getResources().getString(C0280R.string.mode_extended));
        ArrayAdapter<String> modeAdapter = new ArrayAdapter(this, 17367048, modeList);
        modeAdapter.setDropDownViewResource(17367049);
        this.mSpMode.setAdapter(modeAdapter);
        int pos = 0;
        if (this.mCanSettings.isExtended()) {
            pos = 1;
        }
        this.mSpMode.setSelection(pos);
        ArrayAdapter<CCanRate> rateAdapter = new ArrayAdapter(this, 17367048, this.mCanBusInterface.getRateList());
        rateAdapter.setDropDownViewResource(17367049);
        this.mSpRate.setAdapter(rateAdapter);
        this.mSpRate.setSelection(this.mCanBusInterface.getRatePosition(this.mCanSettings.getRate().rate));
        this.mSpRate.setOnItemSelectedListener(new C02601());
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0280R.layout.activity_can_settings);
        this.mSpMode = (Spinner) findViewById(C0280R.id.spMode);
        this.mSpRate = (Spinner) findViewById(C0280R.id.spRate);
        this.mTvHeader = (TextView) findViewById(C0280R.id.tvHeader);
        this.mEdBTR0 = (EditText) findViewById(C0280R.id.edBTR0);
        this.mEdBTR1 = (EditText) findViewById(C0280R.id.edBTR1);
        this.mHexKeyboard = new CHexKeyboard(this, C0280R.id.keyboardview);
        this.mHexKeyboard.registerEditText(C0280R.id.edBTR0);
        this.mHexKeyboard.registerEditText(C0280R.id.edBTR1);
        this.mBtApply = (Button) findViewById(C0280R.id.btApply);
        this.mBtCancel = (Button) findViewById(C0280R.id.btCancel);
        this.mBtApply.setOnClickListener(new C02612());
        this.mBtCancel.setOnClickListener(new C02623());
        this.mChannelIndex = getIntent().getIntExtra(EXTRA_CHANNEL, 0);
        this.mCanBusInterface = CCanBusInterface.getInstance();
        this.mCanSettings = CAdapter.getInstance().getChannel(this.mChannelIndex).getCanSettings();
        init();
    }

    public void onBackPressed() {
        if (this.mHexKeyboard.isCustomKeyboardVisible()) {
            this.mHexKeyboard.hideCustomKeyboard();
        } else {
            finish();
        }
    }

    private void showErrorMsg(String message) {
        showMsg(getResources().getString(C0280R.string.cap_error), message);
    }

    private void showMsg(String caption, String message) {
        Builder builder = new Builder(this);
        builder.setTitle(caption);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
