package com.manikdev.spotify.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.manikdev.kotlinytmusicscraper.models.PlaylistItem

/**
 * The tall banner carousel that sometimes takes the place of the first shelf.
 *
 * Cards are 9:16 — a portrait tile, not the 16:9 banner the shelves already use — with the title
 * over a scrim at the bottom, so the cover reads at a glance without a caption row underneath.
 * It is deliberately a plain LazyRow: snapping is what makes it feel like a carousel, and a
 * LazyRow snaps predictably on every device, where the Material3 hero carousel depends on its
 * own item sizing to behave the same way.
 */
@Composable
fun EchoBannerCarousel(
    items: List<PlaylistItem>,
    onItemClick: (PlaylistItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { playlist ->
            Column(
                modifier =
                    Modifier
                        .width(200.dp)
                        .clickable { onItemClick(playlist) },
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            // 9:16 - taller than it is wide.
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    AsyncImage(
                        model = playlist.thumbnail,
                        contentDescription = playlist.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.55f to Color.Transparent,
                                        1f to Color.Black.copy(alpha = 0.72f),
                                    ),
                                ),
                    )
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                playlist.songCountText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
