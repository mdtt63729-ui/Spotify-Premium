package com.manikdev.spotify.expect.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Live frame numbers, readable on the screen itself.
 *
 * The measurement was there but only in logcat, which needs a cable or another app. This is the
 * same data in a small overlay, so the phone alone is enough: turn it on in Settings, do the
 * scrolling or play the video, and read the numbers off the screen.
 */
object FrameStats {
    var fps by mutableIntStateOf(0)
    var slowPercent by mutableIntStateOf(0)
    var worstFrameMs by mutableIntStateOf(0)
    var sampleMs by mutableIntStateOf(0)
    var enabled by mutableStateOf(false)

    fun update(
        fps: Int,
        slowPercent: Int,
        worstFrameMs: Int,
        sampleMs: Int,
    ) {
        this.fps = fps
        this.slowPercent = slowPercent
        this.worstFrameMs = worstFrameMs
        this.sampleMs = sampleMs
    }
}
