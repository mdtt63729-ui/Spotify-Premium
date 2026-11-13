package com.manikdev.spotify.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manikdev.spotify.update.UpdateHostController
import com.manikdev.spotify.update.UpdateUiState
import kotlin.math.sin

/**
 * Hosts the in-app update sheet.
 *
 * The sheet is the whole update surface: it appears from the bottom, keeps the app identity and
 * both versions visible at all times, and pins its actions to the bottom so "Update" and
 * "Maybe later" never scroll away while the changelog is being read.
 */
@Composable
fun UpdateHost(
    controller: UpdateHostController,
    appName: String = "Spotify",
    currentVersion: String = "",
    icon: Painter? = null,
) {
    val state by controller.state.collectAsStateWithLifecycle()
    if (state is UpdateUiState.Idle) return

    UpdateSheet(
        state = state,
        appName = appName,
        currentVersion = currentVersion,
        icon = icon,
        onDownload = controller::download,
        onInstall = controller::install,
        onDismiss = controller::dismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateSheet(
    state: UpdateUiState,
    appName: String,
    currentVersion: String,
    icon: Painter?,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val info =
        when (state) {
            is UpdateUiState.Available -> state.info
            is UpdateUiState.Downloading -> state.info
            is UpdateUiState.ReadyToInstall -> state.info
            is UpdateUiState.Failed -> state.info
            UpdateUiState.Idle -> null
        } ?: return

    // Reading state: collapsed shows the first few lines, "Read more" opens the rest, and the
    // scrollable area is capped at half the screen so the actions below stay reachable.
    var expanded by remember(info.versionName) { mutableStateOf(false) }
    // 320dp is half of a typical phone viewport; the sheet never grows past this, so the
    // pinned actions stay reachable while the notes scroll.
    val halfScreen = 320.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The app's own launcher icon, clipped to a rounded square.
                if (icon != null) {
                    Image(
                        painter = icon,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp),
                    ) {}
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentVersion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "  →  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = info.versionName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                // Dynamic-colour chip that marks the version as the latest one.
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "Latest update",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Column(
                modifier =
                    Modifier
                        .heightIn(max = if (expanded) halfScreen else 140.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                info.notes.forEach { line ->
                    val isHeadline = !line.startsWith("•") && !line.startsWith("–") && !line.startsWith("-")
                    Text(
                        text = if (isHeadline) line else line.removePrefix("• ").let { "• $it" },
                        style =
                            if (isHeadline) {
                                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                        color =
                            if (isHeadline) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }

            if (!expanded && info.notes.size > 2) {
                TextButton(onClick = { expanded = true }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Text("Read more")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Pinned actions. The row crossfades to the progress readout while downloading and
            // never leaves the bottom of the sheet.
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 3 }) togetherWith (fadeOut() + slideOutVertically { -it / 3 })
                },
                label = "update-actions",
            ) { current ->
                when (current) {
                    is UpdateUiState.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            WavyProgress(
                                progress = current.progress,
                                modifier = Modifier.fillMaxWidth().height(24.dp),
                            )
                            Text(
                                text = "Downloading… ${(current.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    is UpdateUiState.ReadyToInstall -> {
                        Button(onClick = onInstall, shape = CircleShape, modifier = Modifier.fillMaxWidth()) {
                            Text("Install update")
                        }
                    }

                    is UpdateUiState.Failed -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = current.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onDownload, shape = CircleShape, modifier = Modifier.fillMaxWidth()) {
                                Text("Try again")
                            }
                        }
                    }

                    else -> {
                        Button(onClick = onDownload, shape = CircleShape, modifier = Modifier.fillMaxWidth()) {
                            Text("Update now")
                        }
                    }
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Maybe later")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Wavy determinate progress: a sine ribbon filled up to [progress].
 *
 * Drawn by hand rather than with the Material wavy indicator so it behaves identically on
 * Android and desktop — the multiplatform material3 artifact does not ship the wavy variants.
 */
@Composable
fun WavyProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val wave = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val amplitude = size.height / 3.2f
        val midY = size.height / 2f
        val wavelength = size.width / 6f
        fun pathFor(upTo: Float): Path {
            val path = Path()
            var x = 0f
            path.moveTo(0f, midY)
            while (x <= upTo) {
                path.lineTo(x, midY + amplitude * sin((x / wavelength) * 2f * Math.PI.toFloat()))
                x += 4f
            }
            return path
        }
        drawPath(pathFor(size.width), color = track, style = Stroke(width = size.height / 3f))
        drawPath(
            path = pathFor(size.width * progress.coerceIn(0f, 1f)),
            brush = Brush.horizontalGradient(listOf(wave, wave.copy(alpha = 0.6f))),
            style = Stroke(width = size.height / 3f),
        )
        drawCircle(
            color = wave,
            radius = size.height / 5f,
            center = Offset(size.width * progress.coerceIn(0f, 1f), midY),
        )
    }
}
