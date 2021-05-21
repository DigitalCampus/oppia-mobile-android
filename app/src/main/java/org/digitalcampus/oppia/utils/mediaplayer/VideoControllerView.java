/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.digitalcampus.oppia.utils.mediaplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.MediaControllerBinding;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.NonNull;


/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * <p>
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
public class VideoControllerView extends FrameLayout {

    private static final String TAG = VideoControllerView.class.getSimpleName();

    private static final int FASTFORWARD_SEEK_MS = 15000;
    private static final int REWIND_SEEK_MS = 5000;
    private static final int S_DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private MediaPlayerControl mPlayer;
    private Context mContext;
    private ViewGroup mAnchor;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mUseFastForward;
    private boolean mFromXml;
    private boolean mListenersSet;
    private View.OnClickListener mNextListener;
    private View.OnClickListener mPrevListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private Handler mHandler = new MessageHandler(this);

    private MediaControllerBinding binding;

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context) {
        this(context, true);
        Log.i(TAG, TAG);
    }

//    @Override
//    public void onFinishInflate() {
//        if (mRoot != null) {
//            initControllerView(mRoot);
//        }
//        super.onFinishInflate();
//    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView() {
        binding = MediaControllerBinding.inflate(LayoutInflater.from(mContext));

        initControllerView();

        return binding.getRoot();
    }

    private void initControllerView() {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        initializeFastForwardButton();
        initializePauseButton();
        initializeRewindButton();
        initializeFullscreenButton();

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        if (binding.next != null && !mFromXml && !mListenersSet) {
            binding.next.setVisibility(View.GONE);
        }

        if (binding.prev != null && !mFromXml && !mListenersSet) {
            binding.prev.setVisibility(View.GONE);
        }

        if (binding.mediacontrollerProgress != null) {
            if (binding.mediacontrollerProgress instanceof SeekBar) {
                binding.mediacontrollerProgress.setOnSeekBarChangeListener(mSeekListener);
            }
            binding.mediacontrollerProgress.setMax(1000);
        }


        installPrevNextListeners();
    }

    private void initializeFastForwardButton() {
        if (binding.ffwd != null) {
            binding.ffwd.setOnClickListener(v -> {
                if (mPlayer == null) {
                    return;
                }
                mPlayer.seekTo(mPlayer.getCurrentPosition() + FASTFORWARD_SEEK_MS);
                setProgress();
                show(S_DEFAULT_TIMEOUT);
            });
            if (!mFromXml) {
                binding.ffwd.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void initializePauseButton() {
        if (binding.pause != null) {
            binding.pause.requestFocus();
            binding.pause.setOnClickListener(v -> {
                doPauseResume();
                show(S_DEFAULT_TIMEOUT);
            });
        }
    }

    private void initializeFullscreenButton() {
        if (binding.fullscreen != null) {
            binding.fullscreen.requestFocus();
            binding.fullscreen.setOnClickListener(v -> {
                doToggleFullscreen();
                show(S_DEFAULT_TIMEOUT);
            });
        }
    }

    private void initializeRewindButton() {
        if (binding.rew != null) {
            binding.rew.setOnClickListener(v1 -> {
                if (mPlayer == null) {
                    return;
                }
                mPlayer.seekTo(mPlayer.getCurrentPosition() - REWIND_SEEK_MS);
                setProgress();
                show(S_DEFAULT_TIMEOUT);
            });
            if (!mFromXml) {
                binding.rew.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(S_DEFAULT_TIMEOUT);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }

        try {
            if (binding.pause != null && !mPlayer.canPause()) {
                binding.pause.setEnabled(false);
            }
            if (binding.rew != null && !mPlayer.canSeekBackward()) {
                binding.rew.setEnabled(false);
            }
            if (binding.ffwd != null && !mPlayer.canSeekForward()) {
                binding.ffwd.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (binding.pause != null) {
                binding.pause.requestFocus();
            }
            disableUnsupportedButtons();

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );

            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }

        try {
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (binding.mediacontrollerProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                binding.mediacontrollerProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            binding.mediacontrollerProgress.setSecondaryProgress(percent * 10);
        }

        if (binding.time != null)
            binding.time.setText(stringForTime(duration));
        if (binding.timeCurrent != null)
            binding.timeCurrent.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(S_DEFAULT_TIMEOUT);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(S_DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(S_DEFAULT_TIMEOUT);
                if (binding.pause != null) {
                    binding.pause.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(S_DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(S_DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(S_DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    public void updatePausePlay() {
        if (binding == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            binding.pause.setImageResource(R.drawable.ic_media_pause);
        } else {
            binding.pause.setImageResource(R.drawable.ic_media_play);
        }
    }

    public void updateFullScreen() {
        if (binding == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isFullScreen()) {
            binding.fullscreen.setImageResource(R.drawable.ic_media_fullscreen_shrink);
        } else {
            binding.fullscreen.setImageResource(R.drawable.ic_media_fullscreen_stretch);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }

        mPlayer.toggleFullScreen();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (binding.timeCurrent != null)
                binding.timeCurrent.setText(stringForTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(S_DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (binding.pause != null) {
            binding.pause.setEnabled(enabled);
        }
        if (binding.ffwd != null) {
            binding.ffwd.setEnabled(enabled);
        }
        if (binding.rew != null) {
            binding.rew.setEnabled(enabled);
        }
        if (binding.next != null) {
            binding.next.setEnabled(enabled && mNextListener != null);
        }
        if (binding.prev != null) {
            binding.prev.setEnabled(enabled && mPrevListener != null);
        }
        if (binding.mediacontrollerProgress != null) {
            binding.mediacontrollerProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(VideoControllerView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(VideoControllerView.class.getName());
    }

    private void installPrevNextListeners() {
        if (binding.next != null) {
            binding.next.setOnClickListener(mNextListener);
            binding.next.setEnabled(mNextListener != null);
        }

        if (binding.prev != null) {
            binding.prev.setOnClickListener(mPrevListener);
            binding.prev.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (binding != null) {
            installPrevNextListeners();

            if (binding.next != null && !mFromXml) {
                binding.next.setVisibility(View.VISIBLE);
            }
            if (binding.prev != null && !mFromXml) {
                binding.prev.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        boolean isFullScreen();

        void toggleFullScreen();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000L - ((long) pos % 1000L));
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }
}