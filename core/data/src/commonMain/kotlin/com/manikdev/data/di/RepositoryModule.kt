package com.manikdev.data.di

import com.manikdev.common.AppIdentity
import com.manikdev.common.Config.SERVICE_SCOPE
import com.manikdev.data.io.fileDir
import com.manikdev.data.repository.AccountRepositoryImpl
import com.manikdev.data.repository.AlbumRepositoryImpl
import com.manikdev.data.repository.AnalyticsRepositoryImpl
import com.manikdev.data.repository.ArtistRepositoryImpl
import com.manikdev.data.repository.AutoEqRepositoryImpl
import com.manikdev.data.lyrics.LyricsRomanizerRepositoryImpl
import com.manikdev.data.repository.CommonRepositoryImpl
import com.manikdev.data.repository.DiscordRepositoryImpl
import com.manikdev.data.repository.HomeRepositoryImpl
import com.manikdev.data.repository.ImportRepositoryImpl
import com.manikdev.data.repository.LocalPlaylistRepositoryImpl
import com.manikdev.data.repository.LyricsCanvasRepositoryImpl
import com.manikdev.data.repository.PlaylistRepositoryImpl
import com.manikdev.data.repository.PodcastRepositoryImpl
import com.manikdev.data.repository.SearchRepositoryImpl
import com.manikdev.data.repository.SongRepositoryImpl
import com.manikdev.data.repository.StreamRepositoryImpl
import com.manikdev.data.repository.TasteRepositoryImpl
import com.manikdev.data.repository.UpdateRepositoryImpl
import com.manikdev.domain.repository.AccountRepository
import com.manikdev.domain.repository.AlbumRepository
import com.manikdev.domain.repository.AnalyticsRepository
import com.manikdev.domain.repository.ArtistRepository
import com.manikdev.domain.repository.AutoEqRepository
import com.manikdev.domain.repository.LyricsRomanizerRepository
import com.manikdev.domain.repository.CommonRepository
import com.manikdev.domain.repository.DiscordRepository
import com.manikdev.domain.repository.HomeRepository
import com.manikdev.domain.repository.ImportRepository
import com.manikdev.domain.repository.LocalPlaylistRepository
import com.manikdev.domain.repository.LyricsCanvasRepository
import com.manikdev.domain.repository.PlaylistRepository
import com.manikdev.domain.repository.PodcastRepository
import com.manikdev.domain.repository.SearchRepository
import com.manikdev.domain.repository.SongRepository
import com.manikdev.domain.repository.StreamRepository
import com.manikdev.domain.repository.TasteRepository
import com.manikdev.domain.repository.UpdateRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule =
    module {
        single<AccountRepository>(createdAtStart = true) {
            AccountRepositoryImpl(get(), get())
        }

        single<AlbumRepository>(createdAtStart = true) {
            AlbumRepositoryImpl(get(), get())
        }

        single<ArtistRepository>(createdAtStart = true) {
            ArtistRepositoryImpl(get(), get(), get())
        }

        single<CommonRepository>(createdAtStart = true) {
            CommonRepositoryImpl(get(named(SERVICE_SCOPE)), get(), get(), get(), get(), get()).apply {
                this.init("${fileDir()}/ytdlp-cookie.txt", get(), get<AppIdentity>().isDevBuild)
            }
        }

        // At start: a Full build drops the token kizzy's login left behind as soon as it launches.
        single<DiscordRepository>(createdAtStart = true) {
            DiscordRepositoryImpl(get())
        }

        // Lazy for the same reason its client is: the picker is the only thing that wants it.
        single<AutoEqRepository> {
            AutoEqRepositoryImpl(get(), get())
        }

        // Lazy: constructing it costs a few File.length() calls (plus, while the pack is missing,
        // one background read of the stored selection), but the kuromoji dictionary behind it is
        // loaded on first Japanese line, or right after a download installs it, and never at
        // startup — so this must NOT be createdAtStart, or every launch pays for a feature most
        // listeners leave off. The path is where Android keeps the downloaded ipadic pack (the
        // APK no longer bundles it); Desktop and iOS ignore it.
        single<LyricsRomanizerRepository> {
            LyricsRomanizerRepositoryImpl("${fileDir()}/kuromoji-ipadic", get())
        }

        single<HomeRepository>(createdAtStart = true) {
            HomeRepositoryImpl(get(), get())
        }

        single<ImportRepository>(createdAtStart = true) {
            ImportRepositoryImpl(get())
        }

        single<LocalPlaylistRepository>(createdAtStart = true) {
            LocalPlaylistRepositoryImpl(get(), get())
        }

        single<LyricsCanvasRepository>(createdAtStart = true) {
            LyricsCanvasRepositoryImpl(get(), get(), get(), get(), get())
        }

        single<PlaylistRepository>(createdAtStart = true) {
            PlaylistRepositoryImpl(get(), get(), get())
        }

        single<PodcastRepository>(createdAtStart = true) {
            PodcastRepositoryImpl(get(), get())
        }

        single<SearchRepository>(createdAtStart = true) {
            SearchRepositoryImpl(get(), get())
        }

        single<SongRepository>(createdAtStart = true) {
            SongRepositoryImpl(get(), get(), get())
        }

        single<StreamRepository>(createdAtStart = true) {
            StreamRepositoryImpl(get(), get())
        }

        single<UpdateRepository>(createdAtStart = true) {
            UpdateRepositoryImpl(get())
        }

        single<AnalyticsRepository>(createdAtStart = true) {
            AnalyticsRepositoryImpl(get())
        }

        // Lazy: only the taste card in Library asks for it.
        single<TasteRepository> {
            TasteRepositoryImpl(get(), get())
        }
    }