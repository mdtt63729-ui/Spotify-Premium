package com.manikdev.spotify.expect.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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

    /** Ring buffer of the last ~60 samples, newest last. Each line is tagged with its screen. */
    val history = mutableStateListOf<String>()

    /** The screen currently on top, set by the navigation host. */
    var screen by mutableStateOf("")

    /** Per-screen totals, so a screen can be judged without reading every line. */
    val perScreen = mutableStateMapOf<String, String>()

    private val screenFrames = mutableMapOf<String, Int>()
    private val screenSlow = mutableMapOf<String, Int>()
    private val screenWorst = mutableMapOf<String, Int>()

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
        val where = screen.ifBlank { "unknown" }
        history.add("$where | ${fps}fps slow=${slowPercent}% worst=${worstFrameMs}ms")
        screenFrames[where] = (screenFrames[where] ?: 0) + 1
        screenSlow[where] = (screenSlow[where] ?: 0) + slowPercent
        screenWorst[where] = maxOf(screenWorst[where] ?: 0, worstFrameMs)
        val samples = screenFrames.getValue(where)
        perScreen[where] =
            "avg slow ${screenSlow.getValue(where) / samples}% | worst ${screenWorst.getValue(where)}ms | samples $samples"
        while (history.size > 60) history.removeAt(0)
    }
}
