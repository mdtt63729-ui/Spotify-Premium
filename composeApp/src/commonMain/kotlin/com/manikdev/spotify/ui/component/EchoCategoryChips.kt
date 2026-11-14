package com.manikdev.spotify.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manikdev.domain.data.model.mood.Mood

/**
 * The category chips Echo puts at the top of Home: every mood and genre the upstream browse
 * returns, as one horizontal row of filter chips.
 *
 * The data is the `FEmusic_moods_and_genres` response the app already loads for the Mood screen
 * (`Mood.sections` → `MoodItem.title` + `params`), so this adds no request of its own. Tapping a
 * chip opens that category's feed through the existing MoodDestination route.
 */
@Composable
fun EchoCategoryChips(
    mood: Mood?,
    onCategoryClick: (params: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = mood?.sections?.flatMap { it.items }.orEmpty()
    if (items.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.params + it.title }) { item ->
            val stripe =
                runCatching { Color(item.stripeColor.toInt()) }.getOrNull()
                    ?: MaterialTheme.colorScheme.primary
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.clickable { onCategoryClick(item.params) },
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = stripe,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
