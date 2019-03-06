package com.adasone.hm320a.canbusanalyzer;

import android.content.Intent;
import android.os.Bundle;

import com.adasone.hm320a.canbusanalyzer.SettingsFragment.IOnParameterChangeListener;

public class SettingsActivity extends BaseActivity implements IOnParameterChangeListener {
    public static final String EXTRA_SCREEN_ORIENTATION_CHANGERD = "screen_orientation_changed";
    private static Intent sResultIntent = null;

    public static void clearResultIntent() {
        if (sResultIntent != null) {
            sResultIntent.removeExtra(EXTRA_SCREEN_ORIENTATION_CHANGERD);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0280R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(16908290, new SettingsFragment()).commit();
        if (sResultIntent == null) {
            sResultIntent = new Intent();
        }
    }

    public void onParameterChange(int param) {
        switch (param) {
            case 1:
                sResultIntent.putExtra(EXTRA_SCREEN_ORIENTATION_CHANGERD, true);
                setResult(-1, sResultIntent);
                setScreenOrientation();
                return;
            default:
                return;
        }
    }

    protected void onResume() {
        super.onResume();
        setResult(-1, sResultIntent);
    }
}
