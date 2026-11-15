package com.manikdev.spotify.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manikdev.spotify.expect.ui.FrameStats
import com.manikdev.spotify.expect.ui.PerformanceSettings

/**
 * Small readout of the live frame numbers, on top of everything else.
 *
 * Colour tells the story without reading: green is holding, amber is dropping frames, red is
 * missing badly. It is deliberately tiny and in the corner so it does not change what it measures.
 */
@Composable
fun PerfOverlay(modifier: Modifier = Modifier) {
    if (!FrameStats.enabled) return
    val colour =
        when {
            FrameStats.slowPercent >= 30 -> Color(0xFFFF5252)
            FrameStats.slowPercent >= 10 -> Color(0xFFFFB300)
            else -> Color(0xFF4CAF50)
        }
    Column(
        modifier =
            modifier
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(
            text = "${FrameStats.fps} fps · slow ${FrameStats.slowPercent}%",
            color = colour,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = "worst ${FrameStats.worstFrameMs}ms · glass ${if (PerformanceSettings.highPerformanceMode) "off" else "on"}",
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}
