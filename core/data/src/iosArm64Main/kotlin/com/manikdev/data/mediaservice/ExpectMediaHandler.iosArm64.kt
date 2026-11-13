package com.manikdev.data.mediaservice

import com.manikdev.domain.repository.DiscordRepository

actual fun createMediaServiceHandler(
    dataStoreManager: com.manikdev.domain.manager.DataStoreManager,
    songRepository: com.manikdev.domain.repository.SongRepository,
    streamRepository: com.manikdev.domain.repository.StreamRepository,
    localPlaylistRepository: com.manikdev.domain.repository.LocalPlaylistRepository,
    analyticsRepository: com.manikdev.domain.repository.AnalyticsRepository,
    discordRepository: DiscordRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.manikdev.domain.mediaservice.handler.MediaPlayerHandler {
    TODO("Not yet implemented")
}