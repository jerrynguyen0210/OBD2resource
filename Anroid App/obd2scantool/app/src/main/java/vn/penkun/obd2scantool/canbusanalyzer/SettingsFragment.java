package vn.penkun.obd2scantool.canbusanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private IOnParameterChangeListener mParamChangeListener = null;

    public interface IOnParameterChangeListener {
        public static final int PARAM_SCREEN_ORIENTATION = 1;

        void onParameterChange(int i);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (getActivity() instanceof IOnParameterChangeListener) {
            this.mParamChangeListener = (IOnParameterChangeListener) getActivity();
        } else {
            Toast.makeText(getActivity(), "Host activity not implement IOnPrameterChangeListener interfase", 0).show();
        }
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(C0280R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CAppSettings.KEY_PREF_SCREEN_ORIENTATION)) {
            ListPreference pref = (ListPreference) findPreference(key);
            pref.setSummary(pref.getEntry());
            onParamChanged(1);
        }
    }

    private void onParamChanged(int paramIndex) {
        if (this.mParamChangeListener != null) {
            this.mParamChangeListener.onParameterChange(paramIndex);
        }
    }
}
