/*
*
*/

package com.adasone.hm320a.util;

import android.content.Context;
import android.util.Log;

import com.adasone.hm320a.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {
	private final static String TAG = DateTimeUtil.class.getSimpleName();

	/**
	 * @param context  The context to use.  Usually your {@link android.app.Application}
	 *                 or {@link android.app.Activity} object.
	 * @return  Locale.KOREA : yyyy.MM.dd,hh:mm,a
	 *           else : dd MMM yyyy,hh:mm,a
	 * @throws  IllegalArgumentException if the Format's pattern string is invalid.
	 */
	public static String getCurrDateAndTime(Context context) throws IllegalArgumentException {
		String dateTime;
		String outPattern;
		SimpleDateFormat outDateTimeFormat;

		Calendar calendar = Calendar.getInstance();
		outPattern = context.getString(R.string.main_date_time_display_format);

		if (Locale.KOREA.equals(Locale.getDefault())) {
		    outDateTimeFormat = new SimpleDateFormat(outPattern, Locale.KOREA);
    	} else {
	        outDateTimeFormat = new SimpleDateFormat(outPattern, Locale.US);
		}
		dateTime = outDateTimeFormat.format(calendar.getTimeInMillis());

		return dateTime;
	}

	/**
	 * @param context  The context to use.  Usually your {@link android.app.Application}
	 *                 or {@link android.app.Activity} object.
	 * @return  yyyy-MM-dd
	 *
	 */
	public static String getCurrDateToCalibrationFormat(Context context) {
		String date;
		String outPattern;
		SimpleDateFormat outDateFormat;

		Calendar calendar = Calendar.getInstance();

		try {
			outPattern = context.getString(R.string.calibration_date_format);

			outDateFormat = new SimpleDateFormat(outPattern, Locale.US);
			date = outDateFormat.format(calendar.getTimeInMillis());
		} catch (IllegalArgumentException e) {
			date = String.format(Locale.US, "%04d-%02d-%02d",
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
		}

		return date;
	}

	/**
	 * @param context  The context to use.  Usually your {@link android.app.Application}
	 *                 or {@link android.app.Activity} object.
	 * @param inDate  yyyy-MM-dd
	 * @return  Locale.KOREA : yyyy.MM.dd
	 *           else : dd MMM yyyy
	 */

	public static String getDateToDefaultUIFormat(Context context, String inDate) {
		Date date;
		String outDate;
		String inPattern;
		String outPattern;
		SimpleDateFormat inDateFormat;
		SimpleDateFormat outDateFormat;

		try {
			inPattern = context.getString(R.string.calibration_date_format);
			inDateFormat = new SimpleDateFormat(inPattern, Locale.US);

			outPattern = context.getString(R.string.default_date_format);
			if (Locale.KOREA.equals(Locale.getDefault())) {
				outDateFormat = new SimpleDateFormat(outPattern, Locale.KOREA);
			} else {
				outDateFormat = new SimpleDateFormat(outPattern, Locale.US);
			}

			date = inDateFormat.parse(inDate);
			outDate = outDateFormat.format(date);
		} catch (IllegalArgumentException | ParseException e) {
			Log.e(TAG, e.getMessage());
			Calendar calendar = Calendar.getInstance();
			outDate = String.format(Locale.US, "%04d.%02d.%02d",
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
		}
		return outDate;
	}
}
