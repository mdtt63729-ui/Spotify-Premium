package com.manikdev.kotlinytmusicscraper.pages

import com.manikdev.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)