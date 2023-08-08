package org.digitalcampus.oppia.utils.mediaplayer

import android.os.Handler
import android.os.Message
import org.digitalcampus.oppia.utils.mediaplayer.VideoControllerView.Companion.FADE_OUT
import org.digitalcampus.oppia.utils.mediaplayer.VideoControllerView.Companion.SHOW_PROGRESS
import java.lang.ref.WeakReference

class MessageHandler(view: VideoControllerView) : Handler() {
    private val mView: WeakReference<VideoControllerView> = WeakReference(view)

    override fun handleMessage(_msg: Message) {
        var msg = _msg
        val view = mView.get()
        view?.player?.let {
            val pos: Int
            when (msg.what) {
                FADE_OUT -> view.hide()
                SHOW_PROGRESS -> {
                    pos = view.setProgress()
                    if (!view.dragging && view.isShowing && it.isPlaying) {
                        msg = obtainMessage(SHOW_PROGRESS)
                        sendMessageDelayed(msg, 1000L - (pos % 1000L).toLong())
                    }
                }
                else -> {
                    // do nothing
                }
            }
        }

    }
}