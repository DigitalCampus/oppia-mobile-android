package org.digitalcampus.oppia.listener

interface MediaPlayerControl {
    fun start()
    fun pause()
    val duration: Int
    val currentPosition: Int
    fun seekTo(pos: Int)
    val isPlaying: Boolean
    val bufferPercentage: Int
    fun canPause(): Boolean
    fun canSeekBackward(): Boolean
    fun canSeekForward(): Boolean
    val isFullScreen: Boolean
    fun toggleFullScreen()
}