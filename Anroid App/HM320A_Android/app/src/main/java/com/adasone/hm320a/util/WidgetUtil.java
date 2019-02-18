package com.adasone.hm320a.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.adasone.hm320a.R;


public class WidgetUtil {
    public static final  String TAG = WidgetUtil.class.getSimpleName();


    public static void setBtnEnabled (Context context, Button button, boolean enabled) {
        int color;

        color = ContextCompat.getColor(context,
                enabled ? R.color.button_enable : R.color.button_disable );
        button.setTextColor(color);
        button.setEnabled(enabled);
    }

}
