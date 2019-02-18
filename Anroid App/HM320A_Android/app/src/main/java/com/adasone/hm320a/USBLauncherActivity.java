package com.adasone.hm320a;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class USBLauncherActivity extends AppCompatActivity {
    private static final String TAG = USBLauncherActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_launcher);

        if (getIntent() != null &&
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(getIntent().getAction())) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            intent.putExtra(UsbManager.EXTRA_ACCESSORY, getIntent().getParcelableExtra(UsbManager.EXTRA_ACCESSORY));
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
