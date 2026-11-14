package com.manikdev.spotify.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.manikdev.kotlinytmusicscraper.models.SongItem

/**
 * Shelf order, copied from Echo Music's HomeSection weights: the hero carousel first, then
 * Daily Discover, Keep Listening, Forgotten Favorites, From the community, and Similar last.
 * Each entry is a title and how many cards it claims from the shared pool.
 */
private val SHELVES =
    listOf(
        "Your daily discover" to 15,
        "Keep listening" to 10,
        "Forgotten favorites" to 10,
        "From the community" to 10,
        "Similar to your picks" to 15,
    )

/** The hero carousel takes whatever the named shelves left over. */
private const val HERO_CARDS = 10

/**
 * "For you" — the cold-start carousel.
 *
 * Renders up to [EchoRecommendationEngine.TARGET_CARDS] cards straight from the engine, so a
 * fresh install has something to listen to before it has any history of its own. The row is
 * lazily composed and each card is a plain column of thumbnail + two lines of text: no blur,
 * no per-item shader, nothing that costs a frame while the user flings the list.
 */
@Composable
fun EchoForYouSection(
    cards: List<SongItem>,
    modifier: Modifier = Modifier,
    /** When false the hero carousel renders at the bottom of the block instead of the top. */
    heroFirst: Boolean = true,
    onSongClick: (SongItem) -> Unit = {},
) {
    if (cards.isEmpty()) return

    // One pool, several shelves. distinctBy runs once over the whole list, so a song can only
    // ever appear in the first shelf that claims it — nothing repeats anywhere on Home.
    val rows =
        remember(cards) {
            val pool = cards.distinctBy { it.id }
            var cursor = 0
            buildList {
                add("Made for you" to pool.take(HERO_CARDS))
                cursor = minOf(HERO_CARDS, pool.size)
                SHELVES.forEach { (title, count) ->
                    if (cursor >= pool.size) return@forEach
                    val end = minOf(cursor + count, pool.size)
                    add(title to pool.subList(cursor, end))
                    cursor = end
                }
            }.filter { it.second.isNotEmpty() }.let { rows ->
                // Echo moves its hero carousel between the top and the bottom of Home.
                if (heroFirst || rows.size < 2) rows else rows.drop(1) + rows.first()
            }
        }

    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (title, row) ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(row, key = { it.id }) { song ->
                    EchoSongCard(
                        song = song,
                        width = if (index == 0) 200.dp else 148.dp,
                        onClick = { onSongClick(song) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EchoSongCard(
    song: SongItem,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(width)
                .clickable(onClick = onClick),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = song.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = song.artists.joinToString(", ") { it.name },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
