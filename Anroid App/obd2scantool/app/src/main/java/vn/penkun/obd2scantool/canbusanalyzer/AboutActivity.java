package vn.penkun.obd2scantool.canbusanalyzer;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {
    private TextView mTvInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0280R.layout.activity_about);
        this.mTvInfo = (TextView) findViewById(C0280R.id.tvInfo);
        this.mTvInfo.setText("App version: 0.16" + " Release");
    }
}
