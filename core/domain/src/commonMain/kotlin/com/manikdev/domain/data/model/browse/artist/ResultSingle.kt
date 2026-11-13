package com.manikdev.domain.data.model.browse.artist

import com.manikdev.domain.data.model.searchResult.songs.Thumbnail
import com.manikdev.domain.data.type.HomeContentType

data class ResultSingle(
    val browseId: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType