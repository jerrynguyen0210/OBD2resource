package com.adasone.hm320a;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.adasone.hm320a.application.AppApplication;
import com.adasone.hm320a.application.Constants;
import com.adasone.hm320a.util.DisplayUtil;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
	private final static String TAG = PlayerActivity.class.getSimpleName();

    private final MyHandler mHandler = new MyHandler(this);

    /*
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }
    */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
    protected String mUserAgent;
    private TrackSelection.Factory mAdaptiveTrackSelectionFactory;
    private DataSource.Factory mMediaDataSourceFactory;
    private SimpleExoPlayer mPlayer;
    private DefaultTrackSelector mTrackSelector;
    private SimpleExoPlayerView mSimpleExoPlayerView;
    private boolean mNeedRetrySource;
    private boolean mShouldAutoPlay;
    private TrackGroupArray mLastSeenTrackGroupArray;

    private int mResumeWindow;
    private long mResumePosition;

    ArrayList<Uri> mUriArray;
    private int mCurrentWindowIndex;

    // HM-320 Video file only
    private final static int CHANNEL_FRONT = 1;
    private final static int CHANNEL_REAR = 2;
    private int mVideoChannel = CHANNEL_FRONT;

    private TextView mVideoFileNameTextView;
    private ImageButton mChannelSelectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShouldAutoPlay = true;
        clearResumePosition();
        mUserAgent = Util.getUserAgent(this, getPackageName());
        mMediaDataSourceFactory = buildDataSourceFactory(true);
        /*
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        */
        setContentView(R.layout.activity_player);

        mVideoFileNameTextView = (TextView) findViewById(R.id.tv_video_title);

        mChannelSelectButton = (ImageButton) findViewById(R.id.btn_channel);
        mChannelSelectButton.setOnClickListener(mOnControlsClickListener);

        ImageButton backButton = (ImageButton) findViewById(R.id.btn_back);
        backButton.setOnClickListener(mOnControlsClickListener);

        TextView exoPositionTextView = (TextView) findViewById(R.id.exo_position);
        TextView exoDurationTextView = (TextView) findViewById(R.id.exo_duration);

        AppApplication.getAppApplication().setFontHYNGothicM(mVideoFileNameTextView,
                exoPositionTextView, exoDurationTextView);

        mSimpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        mSimpleExoPlayerView.setControllerVisibilityListener(mControlViewVisibilityListener);
        mSimpleExoPlayerView.requestFocus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisplayUtil.immersiveModeOff(this);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        // super.onWindowFocusChanged(hasFocus);

        if( hasFocus ) {
            DisplayUtil.immersiveModeOn(this);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<PlayerActivity> mActivity;
        private MyHandler(PlayerActivity activity) {
            mActivity = new WeakReference<PlayerActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final PlayerActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                default :
                    break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        mSimpleExoPlayerView.showController();
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || mSimpleExoPlayerView.dispatchMediaKeyEvent(event);
    }


    private View.OnClickListener mOnControlsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_back:
                    onBackPressed();
                    break;
                case R.id.btn_channel:
                    int renderIndex = -1;
                    MappingTrackSelector.MappedTrackInfo trackInfo =  mTrackSelector.getCurrentMappedTrackInfo();
                    if (trackInfo != null) {
                        for (int i = 0; i < trackInfo.length; i++) {
                            if (mPlayer.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                                renderIndex = i;
                                break;
                            }
                        }

                        if (renderIndex != -1 && !mTrackSelector.getRendererDisabled(renderIndex)) {
                            TrackGroupArray trackGroups = trackInfo.getTrackGroups(renderIndex);
                            if (trackGroups != null && trackGroups.length > 1) {
                                // TODO check channel
                                MappingTrackSelector.SelectionOverride override;
                                if (mVideoChannel == CHANNEL_FRONT) {
                                    override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, 1, 0);
                                } else {
                                    override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, 0, 0);
                                }
                                mTrackSelector.setSelectionOverride(renderIndex, trackGroups, override);
                            }
                        }
                    }
                break;
                default:
                    break;
            }
        }
    };

    private void initializePlayer() {
        boolean needNewPlayer = mPlayer == null;
        if (needNewPlayer) {
            mAdaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            mTrackSelector = new DefaultTrackSelector(mAdaptiveTrackSelectionFactory);
            mLastSeenTrackGroupArray = null;

            mPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), mTrackSelector);
            mPlayer.addListener(mExoEventListener);
            mSimpleExoPlayerView.setPlayer(mPlayer);
            mPlayer.setPlayWhenReady(mShouldAutoPlay);
        }
        if (needNewPlayer || mNeedRetrySource) {
            Intent intent = getIntent();
            mUriArray = intent.getParcelableArrayListExtra(Constants.Extra.VIDEO_LIST);
            mCurrentWindowIndex = intent.getIntExtra(Constants.Extra.SELECT_VIDEO, 0);

            if (mUriArray.size() > 0) {
                Uri[] uris = new Uri[mUriArray.size()];
                for (int i = 0; i < mUriArray.size(); i++) {
                    uris[i] = mUriArray.get(i);
                }

                if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                    // The player will be reinitialized if the permission is granted.
                    return;
                }
                MediaSource[] mediaSources = new MediaSource[uris.length];
                for (int i = 0; i < uris.length; i++) {
                    mediaSources[i] = buildMediaSource(uris[i]);
                }

                MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                        : new ConcatenatingMediaSource(mediaSources);
                boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
                if (haveResumePosition) {
                    mPlayer.seekTo(mResumeWindow, mResumePosition);
                }
                mPlayer.seekToDefaultPosition(mCurrentWindowIndex);
                mPlayer.prepare(mediaSource, !haveResumePosition, false);

                if (mUriArray != null) {
                    try {
                        Uri uri = mUriArray.get(mCurrentWindowIndex);
                        mVideoFileNameTextView.setText(uri.getLastPathSegment());
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "mUriArray IndexOutOfBoundsException");
                    }
                }
                mNeedRetrySource = false;
            } else {
                // TOTO : error handling
                finish();
            }
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mShouldAutoPlay = mPlayer.getPlayWhenReady();
            updateResumePosition();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void updateResumePosition() {
        mResumeWindow = mPlayer.getCurrentWindowIndex();
        mResumePosition = mPlayer.isCurrentWindowSeekable() ? Math.max(0, mPlayer.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        mResumeWindow = C.INDEX_UNSET;
        mResumePosition = C.TIME_UNSET;
    }


    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri, mMediaDataSourceFactory,
                new DefaultExtractorsFactory(), mHandler, null);
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, mUserAgent)
                , bandwidthMeter);
    }


    private ExoPlayer.EventListener mExoEventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            String errorString = null;

            if (trackGroups != mLastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        errorString = getString(R.string.player_error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        errorString = getString(R.string.player_error_unsupported_audio);
                    }

                    if (errorString != null) {
                        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
                    }
                }
                mLastSeenTrackGroupArray = trackGroups;
            }

            if (trackSelections != null) {
                TrackSelection ts = trackSelections.get(0);

                if (ts != null) {
                    Format format = ts.getSelectedFormat();
                    if (format != null) {
                        try {
                            Log.d(TAG, Format.toLogString(format));
                            mVideoChannel = Integer.valueOf(format.id);
                        } catch (NumberFormatException e) {
                            mVideoChannel = CHANNEL_FRONT;
                        } finally {
                            if (mVideoChannel == CHANNEL_FRONT) {
                                mChannelSelectButton.setBackgroundResource(R.drawable.selector_player_front_ch);
                            } else {
                                mChannelSelectButton.setBackgroundResource(R.drawable.selector_player_rear_ch);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED) {
                finish();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            String errorString = null;
            String strCause = null;

            if (error.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = error.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.player_error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = getString(R.string.player_error_no_secure_decoder,
                                    decoderInitializationException.mimeType);
                        } else {
                            errorString = getString(R.string.player_error_no_decoder,
                                    decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString = getString(R.string.player_error_instantiating_decoder,
                                decoderInitializationException.decoderName);
                    }
                }
            } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                // The error occurred loading data from a {@link MediaSource}.
                errorString = getString(R.string.player_error_loading_data_from_source);
                try {
                    strCause = error.getCause().getLocalizedMessage();
                } catch (NullPointerException ignore) {
                } finally {
                    if (strCause != null) {
                        errorString = errorString + "\n" + strCause;
                    }
                }
            } else if (error.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                //The error was an unexpected {@link RuntimeException}.
                errorString = getString(R.string.player_error_unexpected);
                try {
                    strCause = error.getCause().getLocalizedMessage();
                } catch (NullPointerException ignore) {
                } finally {
                    if (strCause != null) {
                        errorString = errorString + "\n" + strCause;
                    }
                }
            }

            if (errorString != null) {
                Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
            }

            mNeedRetrySource = true;

            if (isBehindLiveWindow(error)) {
                clearResumePosition();
                initializePlayer();
            } else {
                finish();
            }
        }

        @Override
        public void onPositionDiscontinuity() {
            if (mCurrentWindowIndex != mPlayer.getCurrentWindowIndex()) {
                mCurrentWindowIndex = mPlayer.getCurrentWindowIndex();
                if (mUriArray != null) {
                    try {
                        Uri uri = mUriArray.get(mCurrentWindowIndex);
                        mVideoFileNameTextView.setText(uri.getLastPathSegment());
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "mUriArray IndexOutOfBoundsException");
                    } finally {
                        mSimpleExoPlayerView.showController();
                    }
                }
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }
    };

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private PlaybackControlView.VisibilityListener mControlViewVisibilityListener
            = new PlaybackControlView.VisibilityListener() {
        @Override
        public void onVisibilityChange(int visibility) {
        }
    };
}


