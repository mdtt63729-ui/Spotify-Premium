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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.manikdev.kotlinytmusicscraper.models.PlaylistItem

/** One titled row of playlists on Home. */
data class PlaylistShelf(
    val title: String,
    val items: List<PlaylistItem>,
)

/**
 * Playlist rows for Home — the thing that belongs here instead of single tracks.
 *
 * Each shelf is a horizontal row of playlist covers; tapping one opens the playlist, where its
 * songs are listed and play from there. Nothing here starts playback by itself, so no
 * SongItem → SongEntity conversion is involved.
 */
@Composable
fun EchoPlaylistShelves(
    shelves: List<PlaylistShelf>,
    onPlaylistClick: (PlaylistItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (shelves.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        shelves.forEach { shelf ->
            if (shelf.items.isEmpty()) return@forEach
            Text(
                text = shelf.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(shelf.items, key = { it.id }) { playlist ->
                    PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlaylistItem,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(148.dp)
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
                model = playlist.thumbnail,
                contentDescription = playlist.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        playlist.songCountText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
