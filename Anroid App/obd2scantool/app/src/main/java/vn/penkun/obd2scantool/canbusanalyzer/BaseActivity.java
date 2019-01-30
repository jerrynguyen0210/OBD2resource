package vn.penkun.obd2scantool.canbusanalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class BaseActivity extends Activity {
    public void setScreenOrientation() {
        String value = PreferenceManager.getDefaultSharedPreferences(this).getString(CAppSettings.KEY_PREF_SCREEN_ORIENTATION, "");
        int setValue = 7;
        Object obj = -1;
        switch (value.hashCode()) {
            case -1852618822:
                if (value.equals(CAppSettings.VALUE_SCREEN_ORIENTATION_SENSOR)) {
                    obj = 2;
                    break;
                }
                break;
            case -77725029:
                if (value.equals(CAppSettings.VALUE_SCREEN_ORIENTATION_LANDSCAPE)) {
                    obj = 1;
                    break;
                }
                break;
            case 1511893915:
                if (value.equals(CAppSettings.VALUE_SCREEN_ORIENTATION_PORTRAIT)) {
                    obj = null;
                    break;
                }
                break;
        }
        switch ((Integer)obj) {
            case 0:  // Hieu convert from null to 0
                setValue = 1;
                break;
            case 1:
                setValue = 0;
                break;
            case 2:
                setValue = 4;
                break;
        }
        setRequestedOrientation(setValue);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOrientation();
    }
}
