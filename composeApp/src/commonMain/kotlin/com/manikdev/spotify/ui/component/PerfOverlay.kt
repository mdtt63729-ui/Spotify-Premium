package com.manikdev.spotify.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manikdev.spotify.expect.ui.FrameStats
import com.manikdev.spotify.expect.ui.PerformanceSettings
import kotlin.math.roundToInt

/**
 * Live frame numbers you can drag anywhere and tap for the full log.
 *
 * Dragging matters because the readout must not sit on top of the thing being measured - the
 * corner it covers is different on every screen. Tapping opens the last thirty samples (about
 * ninety seconds) as text with a copy button, so the numbers can be sent without a cable, a
 * screenshot or a second app.
 */
@Composable
fun PerfOverlay(modifier: Modifier = Modifier) {
    if (!FrameStats.enabled) return
    var showLog by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var boxWidth by remember { mutableStateOf(0) }
    var boxHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val clipboard = LocalClipboardManager.current

    val colour =
        when {
            FrameStats.slowPercent >= 30 -> Color(0xFFFF5252)
            FrameStats.slowPercent >= 10 -> Color(0xFFFFB300)
            else -> Color(0xFF4CAF50)
        }

    Column(
        modifier =
            modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .onSizeChanged {
                    boxWidth = it.width
                    boxHeight = it.height
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .clickable { showLog = true }
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

    if (showLog) {
        val report =
            buildString {
                appendLine("Spotify frame report")
                appendLine("glass=${!PerformanceSettings.highPerformanceMode} hpm=${PerformanceSettings.highPerformanceMode}")
                appendLine()
                appendLine("--- per screen (worst first) ---")
                FrameStats.perScreen
                    .entries
                    .sortedByDescending { it.value.substringAfter("worst ").substringBefore("ms").toIntOrNull() ?: 0 }
                    .forEach { (where, summary) -> appendLine("$where: $summary") }
                appendLine()
                appendLine("--- samples, newest last ---")
                FrameStats.history.forEach { appendLine(it) }
            }
        AlertDialog(
            onDismissRequest = { showLog = false },
            title = { Text("Frame log") },
            text = {
                Column(
                    modifier =
                        Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = report,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(report))
                    showLog = false
                }) { Text("Copy") }
            },
            dismissButton = {
                TextButton(onClick = { showLog = false }) { Text("Close") }
            },
        )
    }
}
