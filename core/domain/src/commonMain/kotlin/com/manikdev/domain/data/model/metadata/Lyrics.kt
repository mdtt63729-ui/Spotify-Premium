package com.manikdev.domain.data.model.metadata

import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    val error: Boolean = false,
    val lines: List<Line>?,
    val syncType: String?,
    val spotifyLyrics: SpotifyLyrics? = null,
)

@Serializable
data class SpotifyLyrics(
    val id: String,
    val vote: Int,
)