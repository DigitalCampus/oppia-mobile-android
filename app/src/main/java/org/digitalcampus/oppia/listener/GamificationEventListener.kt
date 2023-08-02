package org.digitalcampus.oppia.listener

interface GamificationEventListener {
    fun onGamificationEvent(message: String?, points: Int)
}