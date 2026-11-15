package com.manikdev.spotify.expect.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The one place the renderer asks whether it is allowed to spend the expensive pixels.
 *
 * Set from the activity when the "High performance mode" switch changes, so the effect code does
 * not have to reach into storage or a view model to find out. A plain observable object rather
 * than a CompositionLocal because the shader code is not in a composable's scope.
 */
object PerformanceSettings {
    var highPerformanceMode by mutableStateOf(false)
}
