package com.adasone.hm320a;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.adasone.hm320a.application.AppApplication;

public class CustomDialog {
	private final static String TAG = CustomDialog.class.getSimpleName();

	private Context mContext;
	private Handler mHandler;

	public CustomDialog(Context context, Handler handler) {
		super();
		this.mContext = context;
		this.mHandler = handler;
	}


	public Dialog showQuestionYNDialog(String title, String contents, String btnYesString, String btnNoString, final int yesMessage, final int noMessage ) {
		final Dialog dialog = new Dialog(mContext);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_dialog_question);

		TextView titleTextView = (TextView) dialog.findViewById(R.id.tv_title);
        TextView contentsTextView = (TextView) dialog.findViewById(R.id.tv_contents);
        Button yesButton = (Button) dialog.findViewById(R.id.btn_yes);
        Button noButton = (Button) dialog.findViewById(R.id.btn_no);

        yesButton.setText(btnYesString);
        noButton.setText(btnNoString);

        AppApplication.getAppApplication().setFontHYGothic800(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic600(contentsTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(yesButton, noButton);

        titleTextView.setText(title);
        contentsTextView.setText(contents);

        dialog.show();
        yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                mHandler.sendMessage(mHandler.obtainMessage(yesMessage));
                dialog.dismiss();
			}
		});

        noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                mHandler.sendMessage(mHandler.obtainMessage(noMessage));
                dialog.dismiss();
			}
		});
        return dialog;
	}

    public Dialog showQuestionYNDialog(String title, String contents, final int yesMessage, final int noMessage ) {
        return showQuestionYNDialog(title, contents, mContext.getString(R.string.yes_caps), mContext.getString(R.string.no_caps), yesMessage, noMessage);
    }

	public Dialog showQuestionYNDialog(int titleResID, int contentsResID, final int yesMessage, final int noMessage) {
		return showQuestionYNDialog(mContext.getString(titleResID), mContext.getString(contentsResID), yesMessage, noMessage);
	}

    public Dialog showQuestionYNDialog(int titleResID, int contentsResID, int yesBtnResID, int noBtnResID, final int yesMessage, final int noMessage) {
        return showQuestionYNDialog(mContext.getString(titleResID), mContext.getString(contentsResID), mContext.getString(yesBtnResID), mContext.getString(noBtnResID), yesMessage, noMessage);
    }

    public Dialog showNotificationDialog(String title, String contents, final int okMessage) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_notification);
        dialog.setCancelable(false);

        TextView titleTextView = (TextView) dialog.findViewById(R.id.tv_title);
        TextView contentsTextView = (TextView) dialog.findViewById(R.id.tv_contents);
        Button okButton = (Button) dialog.findViewById(R.id.btn_ok);

        AppApplication.getAppApplication().setFontHYGothic800(titleTextView);
        AppApplication.getAppApplication().setFontHYGothic600(contentsTextView);
        AppApplication.getAppApplication().setFontHYNSupungB(okButton);

        titleTextView.setText(title);
        contentsTextView.setText(contents);

        dialog.show();
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendMessage(mHandler.obtainMessage(okMessage));
                dialog.dismiss();
            }
        });
        return dialog;
    }
    public Dialog showNotificationDialog(int titleResID, int contentsResID, final int okMessage) {
        return showNotificationDialog(mContext.getString(titleResID), mContext.getString(contentsResID), okMessage);
    }

    public Dialog createProgressNoUpdateStyleDialog(String description) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_progress_no_update_style);
        dialog.setCancelable(false);

        TextView descTextView = (TextView) dialog.findViewById(R.id.tv_description);
        descTextView.setText(description);
        AppApplication.getAppApplication().setFontHYGothic600(descTextView);

        return dialog;
    }
}

