package com.manikdev.spotify

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Choreographer
import com.maxrave.logger.Logger

/**
 * Reports how many frames the app is actually getting, every three seconds.
 *
 * This exists because the lag has been chased four times by reading code and guessing, and that
 * has not worked. A frame that takes longer than ~24ms means the app missed its 40fps budget -
 * on a 120Hz panel it means the frame was late. The line also carries whether the glass effect
 * and high performance mode were on, so the numbers say which configuration is costing frames
 * instead of us assuming.
 *
 * Read it with: adb logcat -s FrameMonitor
 */
object FrameMonitor {
    private const val SLOW_FRAME_MS = 24L
    private const val WINDOW_MS = 3_000L
    private var started = false

    fun start() {
        if (started) return
        started = true
        Handler(Looper.getMainLooper()).post {
            var lastFrameMs = 0L
            var windowStart = SystemClock.uptimeMillis()
            var frames = 0
            var slow = 0
            var worst = 0L

            val callback =
                object : Choreographer.FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        val nowMs = frameTimeNanos / 1_000_000L
                        if (lastFrameMs != 0L) {
                            val delta = nowMs - lastFrameMs
                            frames++
                            if (delta > SLOW_FRAME_MS) {
                                slow++
                                if (delta > worst) worst = delta
                            }
                        }
                        lastFrameMs = nowMs

                        val elapsed = SystemClock.uptimeMillis() - windowStart
                        if (elapsed >= WINDOW_MS && frames > 0) {
                            val fps = frames * 1000L / elapsed
                            val pct = slow * 100L / frames
                            com.manikdev.spotify.expect.ui.FrameStats.update(
                                fps = fps.toInt(),
                                slowPercent = pct.toInt(),
                                worstFrameMs = worst.toInt(),
                                sampleMs = elapsed.toInt(),
                            )
                            Logger.w(
                                "FrameMonitor",
                                "fps=$fps slow=${pct}% worst=${worst}ms frames=$frames " +
                                    "glass=${!com.manikdev.spotify.expect.ui.PerformanceSettings.highPerformanceMode} " +
                                    "hpm=${com.manikdev.spotify.expect.ui.PerformanceSettings.highPerformanceMode}",
                            )
                            windowStart = SystemClock.uptimeMillis()
                            frames = 0
                            slow = 0
                            worst = 0
                        }
                        Choreographer.getInstance().postFrameCallback(this)
                    }
                }
            Choreographer.getInstance().postFrameCallback(callback)
        }
    }
}
