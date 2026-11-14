package com.manikdev.spotify.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

/**
 * State behind the settings search bar.
 *
 * Deliberately a plain object rather than a CompositionLocal or a ViewModel field: the settings
 * screen is a 3,600-line file of imperative `item {}` blocks, and the filter has to reach every
 * [SettingItem] and [SettingGroup] without restructuring any of it. A single observable string is
 * the smallest thing that does that, and it is reset whenever the screen is disposed.
 */
object SettingsSearchState {
    var query by mutableStateOf("")
}

/** True when [text] contains [query], case-insensitively. Empty query matches everything. */
fun settingsMatches(
    text: String,
    query: String,
): Boolean {
    if (query.isBlank()) return true
    return text.contains(query.trim(), ignoreCase = true)
}

/**
 * The pinned search field.
 *
 * It lives in a sticky header, so it is on screen at every scroll position — it only shrinks
 * (48dp → 40dp, with the icon and text scaled down with it) once the list has moved, which keeps
 * the transition quiet enough that it reads as part of the screen rather than a new widget.
 */
@Composable
fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val height by animateDpAsState(if (compact) 40.dp else 48.dp, label = "search-height")
    val iconSize by animateDpAsState(if (compact) 16.dp else 20.dp, label = "search-icon")
    val alpha by animateFloatAsState(if (compact) 0.92f else 1f, label = "search-alpha")

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = if (compact) 4.dp else 8.dp)
                .height(height),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp),
        ) {
            // Drawn rather than an icon vector: the app module has no material-icons artifact,
            // and a two-stroke magnifier is the whole glyph anyway.
            val glyphColor = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(modifier = Modifier.size(iconSize)) {
                val stroke = size.minDimension * 0.12f
                drawCircle(
                    color = glyphColor,
                    radius = size.minDimension * 0.33f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.42f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                )
                drawLine(
                    color = glyphColor,
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.66f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.92f),
                    strokeWidth = stroke,
                )
            }
            Box(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search settings",
                        style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle =
                        (if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium)
                            .copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {}),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .clickable { onQueryChange("") },
                )
            }
        }
    }
}
