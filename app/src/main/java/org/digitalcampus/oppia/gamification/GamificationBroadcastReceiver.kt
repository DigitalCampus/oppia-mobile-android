package org.digitalcampus.oppia.gamification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.digitalcampus.oppia.listener.GamificationEventListener

class GamificationBroadcastReceiver : BroadcastReceiver() {
    private var listener: GamificationEventListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra(GamificationService.SERVICE_MESSAGE)
        val points = intent.getIntExtra(GamificationService.SERVICE_POINTS, 0)
        listener?.onGamificationEvent(message, points)
        abortBroadcast()
    }

    fun setGamificationEventListener(listener: GamificationEventListener) {
        this.listener = listener
    }
}