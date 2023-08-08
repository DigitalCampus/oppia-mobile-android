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
package org.digitalcampus.oppia.utils.mediaplayer

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.MediaControllerBinding
import org.digitalcampus.oppia.listener.MediaPlayerControl
import java.util.Formatter
import java.util.Locale

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 *
 *
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 *
 *
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 *
 *
 * MediaController will hide and
 * show the buttons according to these rules:
 *
 *  *  The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 *  *  The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 *  *  The "rewind" and "fastforward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 *
 */
class VideoControllerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        private val useFastForward: Boolean = true
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val TAG = VideoControllerView::class.simpleName
        const val FASTFORWARD_SEEK_MS = 15000
        const val REWIND_SEEK_MS = 5000
        const val S_DEFAULT_TIMEOUT = 3000
        const val FADE_OUT = 1
        const val SHOW_PROGRESS = 2
    }

    var player: MediaPlayerControl? = null
    var isShowing = false
        private set
    var dragging = false
    var formatBuilder: StringBuilder? = null
    var formatter: Formatter? = null
    private var anchor: ViewGroup? = null
    private val fromXml : Boolean
    private var listenersSet = false
    private var nextListener: OnClickListener? = null
    private var prevListener: OnClickListener? = null
    private val handler: Handler = MessageHandler(this)
    private var binding: MediaControllerBinding

    init {
        binding = MediaControllerBinding.inflate(LayoutInflater.from(context))
        fromXml = attrs != null
        initControllerView()
    }

    fun setMediaPlayer(player: MediaPlayerControl?) {
        this.player = player
        updatePausePlay()
        updateFullScreen()
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    fun setAnchorView(view: ViewGroup?) {
        anchor = view
        val frameParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        removeAllViews()
        val v = makeControllerView()
        addView(v, frameParams)
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     *
     * @return The controller view.
     */
    private fun makeControllerView(): View {
        initControllerView()
        return binding.root
    }

    private fun initControllerView() {
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        initializeFastForwardButton()
        initializePauseButton()
        initializeRewindButton()
        initializeFullscreenButton()

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        if (!fromXml && !listenersSet) {
            binding.next.visibility = GONE
        }
        if (!fromXml && !listenersSet) {
            binding.prev.visibility = GONE
        }
        binding.mediacontrollerProgress.setOnSeekBarChangeListener(mSeekListener)
        binding.mediacontrollerProgress.max = 1000
        installPrevNextListeners()
    }

    private fun initializeFastForwardButton() {
        binding.ffwd.setOnClickListener { v: View? ->
            if (this.player == null) {
                return@setOnClickListener
            }
            player?.let {
                it.seekTo(it.currentPosition + FASTFORWARD_SEEK_MS)
            }
            setProgress()
            show(S_DEFAULT_TIMEOUT)
        }
        if (!fromXml) {
            binding.ffwd.visibility = if (useFastForward) VISIBLE else GONE
        }
    }

    private fun initializePauseButton() {
        binding.pause.requestFocus()
        binding.pause.setOnClickListener {
            doPauseResume()
            show(S_DEFAULT_TIMEOUT)
        }
    }

    private fun initializeFullscreenButton() {
        binding.fullscreen.requestFocus()
        binding.fullscreen.setOnClickListener {
            doToggleFullscreen()
            show(S_DEFAULT_TIMEOUT)
        }
    }

    private fun initializeRewindButton() {
        binding.rew.setOnClickListener {
            if (this.player == null) {
                return@setOnClickListener
            }
            player?.let {
                it.seekTo(it.currentPosition - REWIND_SEEK_MS)
            }
            setProgress()
            show(S_DEFAULT_TIMEOUT)
        }
        if (!fromXml) {
            binding.rew.visibility = if (useFastForward) VISIBLE else GONE
        }
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private fun disableUnsupportedButtons() {
        if (this.player == null) {
            return
        }
        try {
            if (player?.canPause() == false) {
                binding.pause.isEnabled = false
            }
            if (player?.canSeekBackward() == false) {
                binding.rew.isEnabled = false
            }
            if (player?.canSeekForward() == false) {
                binding.ffwd.isEnabled = false
            }
        } catch (ex: IncompatibleClassChangeError) {
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
     * the controller until hide() is called.
     */
    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    @JvmOverloads
    fun show(timeout: Int = S_DEFAULT_TIMEOUT) {
        if (!isShowing && anchor != null) {
            setProgress()
            binding.pause.requestFocus()
            disableUnsupportedButtons()
            val tlp = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            )
            anchor!!.addView(this, tlp)
            isShowing = true
        }
        updatePausePlay()
        updateFullScreen()

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        handler.sendEmptyMessage(SHOW_PROGRESS)
        val msg = handler.obtainMessage(FADE_OUT)
        if (timeout != 0) {
            handler.removeMessages(FADE_OUT)
            handler.sendMessageDelayed(msg, timeout.toLong())
        }
    }

    /**
     * Remove the controller from the screen.
     */
    fun hide() {
        if (anchor == null) {
            return
        }
        try {
            anchor!!.removeView(this)
            handler.removeMessages(SHOW_PROGRESS)
        } catch (ex: IllegalArgumentException) {
            Log.w("MediaController", "already removed")
        }
        isShowing = false
    }

    private fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder!!.setLength(0)
        return if (hours > 0) {
            formatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            formatter!!.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    fun setProgress(): Int {
        if (this.player == null || dragging) {
            return 0
        }
        val position = player?.currentPosition ?: 0
        val duration = player?.duration ?: 0
        if (duration > 0) {
            // use long to avoid overflow
            val pos = 1000L * position / duration
            binding.mediacontrollerProgress.progress = pos.toInt()
        }
        player?.let {
            binding.mediacontrollerProgress.secondaryProgress = it.bufferPercentage * 10
        }
        binding.time.text = stringForTime(duration)
        binding.timeCurrent.text = stringForTime(position)
        return position
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        show(S_DEFAULT_TIMEOUT)
        return true
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        show(S_DEFAULT_TIMEOUT)
        return false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        player?.let {
            val keyCode = event.keyCode
            val uniqueDown = (event.repeatCount == 0
                    && event.action == KeyEvent.ACTION_DOWN)
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (uniqueDown) {
                    doPauseResume()
                    show(S_DEFAULT_TIMEOUT)
                    binding.pause.requestFocus()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (uniqueDown && !it.isPlaying) {
                    it.start()
                    updatePausePlay()
                    show(S_DEFAULT_TIMEOUT)
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (uniqueDown && !it.isPlaying) {
                    it.pause()
                    updatePausePlay()
                    show(S_DEFAULT_TIMEOUT)
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                // don't show the controls for volume adjustment
                return super.dispatchKeyEvent(event)
            } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
                if (uniqueDown) {
                    hide()
                }
                return true
            }
            show(S_DEFAULT_TIMEOUT)
            return super.dispatchKeyEvent(event)
        }
        return true
    }

    fun updatePausePlay() {
        player?.let {
            if (it.isPlaying) {
                binding.pause.setImageResource(R.drawable.ic_media_pause)
            } else {
                binding.pause.setImageResource(R.drawable.ic_media_play)
            }
        }
    }

    private fun updateFullScreen() {
        player?.let {
            if (it.isFullScreen) {
                binding.fullscreen.setImageResource(R.drawable.ic_media_fullscreen_shrink)
            } else {
                binding.fullscreen.setImageResource(R.drawable.ic_media_fullscreen_stretch)
            }
        }
    }

    private fun doPauseResume() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.start()
            }
            updatePausePlay()
        }
    }

    private fun doToggleFullscreen() {
        player?.toggleFullScreen()
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
    private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStartTrackingTouch(bar: SeekBar) {
            show(3600000)
            dragging = true

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            handler.removeMessages(SHOW_PROGRESS)
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
            player?.let {
                if (!fromuser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return
                }
                val duration = it.duration.toLong()
                val newposition = duration * progress / 1000L
                it.seekTo(newposition.toInt())
                binding.timeCurrent.text = stringForTime(newposition.toInt())
            }
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            dragging = false
            setProgress()
            updatePausePlay()
            show(S_DEFAULT_TIMEOUT)

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            handler.sendEmptyMessage(SHOW_PROGRESS)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.pause.isEnabled = enabled
        binding.ffwd.isEnabled = enabled
        binding.rew.isEnabled = enabled
        binding.next.isEnabled = enabled && nextListener != null
        binding.prev.isEnabled = enabled && prevListener != null
        binding.mediacontrollerProgress.isEnabled = enabled
        disableUnsupportedButtons()
        super.setEnabled(enabled)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = VideoControllerView::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = VideoControllerView::class.java.name
    }

    private fun installPrevNextListeners() {
        binding.next.setOnClickListener(nextListener)
        binding.next.isEnabled = nextListener != null
        binding.prev.setOnClickListener(prevListener)
        binding.prev.isEnabled = prevListener != null
    }

    fun setPrevNextListeners(next: OnClickListener?, prev: OnClickListener?) {
        nextListener = next
        prevListener = prev
        listenersSet = true
        installPrevNextListeners()
        if (!fromXml) {
            binding.next.visibility = VISIBLE
        }
        if (!fromXml) {
            binding.prev.visibility = VISIBLE
        }
    }
}