package com.manikdev.data.mediaservice

import com.manikdev.domain.manager.DataStoreManager
import com.manikdev.domain.mediaservice.handler.MediaPlayerHandler
import com.manikdev.domain.repository.AnalyticsRepository
import com.manikdev.domain.repository.DiscordRepository
import com.manikdev.domain.repository.LocalPlaylistRepository
import com.manikdev.domain.repository.SongRepository
import com.manikdev.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

expect fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    discordRepository: DiscordRepository,
    coroutineScope: CoroutineScope,
): MediaPlayerHandler