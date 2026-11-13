package com.manikdev.domain.data.model.mood.genre

import com.manikdev.domain.data.model.searchResult.songs.Thumbnail
import com.manikdev.domain.data.type.HomeContentType

data class Content(
    val playlistBrowseId: String,
    val thumbnail: List<Thumbnail>?,
    val title: Title,
) : HomeContentType