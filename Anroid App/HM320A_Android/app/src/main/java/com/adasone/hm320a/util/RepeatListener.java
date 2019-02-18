package com.adasone.hm320a.util;

import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

/**
 * http://stackoverflow.com/questions/4284224/android-hold-button-to-repeat-action
 */


/**
 * A class, that can be used as a TouchListener on any view (e.g. a Button).
 * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
 * click is fired immediately, next after initialInterval, and subsequent after
 * normalInterval.
 *
 * <p>Interval is scheduled after the onClick completes, so it has to run fast.
 * If it runs slow, it does not generate skipped onClicks.
 */
public class RepeatListener implements OnTouchListener {

    private Handler handler = new Handler();
    private static final int intervalGap = 5;

    private int initialInterval;
    private int startInterval;
    private int minimumInterval;
    private int intervalCount;
    private int interval;
    private int count;
    private final OnClickListener clickListener;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (count < intervalCount) {
                count++;
            } else {
                count = 0;
                interval -= intervalGap;
                if (interval < minimumInterval) {
                    interval = minimumInterval;
                }
            }
            handler.postDelayed(this, interval);
            clickListener.onClick(downView);
        }
    };

    private View downView;

    /**
     * @param initialInterval The interval after first click event
     * @param startInterval The interval after second and subsequent click
     *       events
     * @param minimumInterval The minimum interval
     * @param intervalCount The interval count
     * @param clickListener The OnClickListener, that will be called
     *       periodically
     */
    public RepeatListener(int initialInterval, int startInterval, int minimumInterval,
                          int intervalCount, OnClickListener clickListener) {
        if (clickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || startInterval < 0 || minimumInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.startInterval = startInterval;
        this.minimumInterval = minimumInterval;
        this.intervalCount = intervalCount;
        this.clickListener = clickListener;
    }

    private Rect rect = new Rect();

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopRepeat();
                handler.postDelayed(handlerRunnable, initialInterval);
                downView = view;
                rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                downView.setPressed(true);
                clickListener.onClick(view);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!rect.contains(view.getLeft() + (int) motionEvent.getX(),
                        view.getTop() + (int) motionEvent.getY())) {
                    // User moved outside bounds
                    stopRepeat();
                    if ( downView != null) {
                        downView.setPressed(false);
                        downView = null;
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopRepeat();
                if ( downView != null) {
                    downView.setPressed(false);
                    downView = null;
                }
                return true;
        }
        return false;
    }

    private void stopRepeat() {
        count = 0;
        interval = startInterval;
        handler.removeCallbacks(handlerRunnable);
    }

}
