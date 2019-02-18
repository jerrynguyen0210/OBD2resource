package com.adasone.hm320a.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class AppApplication extends Application {

    private static boolean DEBUG;
	public static AppApplication mApplication = null;

    private AppStatus mAppStatus;

    private Typeface mHYNGothicMedium;
    private Typeface mHYSupungBold;
    private Typeface mHYGothicA1_400;
    private Typeface mHYGothicA1_500;
    private Typeface mHYGothicA1_600;
    private Typeface mHYGothicA1_700;
    private Typeface mHYGothicA1_800;
    private Typeface mHYGothicA1_900;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mApplication = this;
		registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
		AppApplication.DEBUG = isDebuggable(this);

        mHYNGothicMedium = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HYN_GOTHIC_MEDIUM);
        mHYSupungBold = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_SUPUNG_BOLD);
        mHYGothicA1_400 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_400);
        mHYGothicA1_500 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_500);
        mHYGothicA1_600 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_600);
        mHYGothicA1_700 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_700);
        mHYGothicA1_800 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_800);
        mHYGothicA1_900 = Typeface.createFromAsset(this.getAssets(), Constants.Fonts.HY_GOTHIC_A1_900);

	}

	public void setFontHYNGothicM(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYNGothicMedium);
        }
    }

    public void setFontHYNSupungB(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYSupungBold);
        }
    }

    public void setFontHYGothic400 (TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_400);
        }
    }

    public void setFontHYGothic500 (TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_500);
        }
    }

    public void setFontHYGothic600(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_600);
        }
    }

    public void setFontHYGothic700(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_700);
        }
    }

    public void setFontHYGothic800(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_800);
        }
    }

    public void setFontHYGothic900(TextView...views) {
        for (TextView view:views) {
            view.setTypeface(mHYGothicA1_900);
        }
    }
	public static AppApplication getAppApplication() {
		return mApplication;
	}


    // ActivityLifecycleCallbacks
	public AppStatus getAppStatus() {
		return mAppStatus;
	}

    public AppApplication get(Context context) {
        return (AppApplication) context.getApplicationContext();
    }

	// check if app is foreground
	public boolean isForeground() {
		return mAppStatus != null && mAppStatus.ordinal() > AppStatus.BACKGROUND.ordinal();
	}

	public enum AppStatus {
		BACKGROUND,                // app is background
		RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
		FOREGROUND;                // app is foreground
	}

	public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
                // running activity is 1,
                // app must be returned from background just now (or first launch)
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
            } else if (running > 1) {
                // 2 or more running activities,
                // should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
                // no active activity
                // app goes to background
                mAppStatus = AppStatus.BACKGROUND;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

    // Debug mode
	private boolean isDebuggable(Context context) {
		boolean debuggable = false;

		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
			debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
		} catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
		}

        return debuggable;
	}

    public static boolean isDebug() {
        return AppApplication.DEBUG;
    }
}
