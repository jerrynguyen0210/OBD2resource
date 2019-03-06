 package com.adasone.hm320a.util;

import android.os.Environment;

import com.adasone.hm320a.application.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtil {
	private final static String TAG = LogUtil.class.getSimpleName();

	private static String getDateString()	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.KOREA);
		return df.format(new Date());
	}

	public static Process startSaveLogcatToFile() {
		Process process = null;

		File modelDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.HM320A_DIR);
		// create log folder
		if (!modelDir.exists()) {
			if (!modelDir.mkdir()) {
				return null;
			}
		}

		File logDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.LOG_DIR);
		// create log folder
		if (!logDir.exists()) {
			if (!logDir.mkdir()) {
				return null;
			}
		}

		File logFile = new File(logDir, getDateString() + ".log" );

		try {
			process = Runtime.getRuntime().exec("logcat -v time -f " + logFile + " *:E");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return process;
	}
	public static void stopSaveLogcatToFile(Process process) {
		try {
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
