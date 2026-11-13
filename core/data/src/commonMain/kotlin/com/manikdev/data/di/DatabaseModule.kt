package com.manikdev.data.di

import DatabaseDao
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.manikdev.data.dataStore.DataStoreManagerImpl
import com.manikdev.data.dataStore.createDataStoreInstance
import com.manikdev.data.db.Converters
import com.manikdev.data.db.MusicDatabase
import com.manikdev.data.db.SyncDao
import com.manikdev.data.librarysync.SyncChangeRecorder
import com.manikdev.data.librarysync.SyncPrefs
import com.manikdev.data.librarysync.createLibrarySyncDataStore
import com.manikdev.data.db.datasource.AnalyticsDatasource
import com.manikdev.data.db.datasource.LocalDataSource
import com.manikdev.data.db.getDatabaseBuilder
import com.manikdev.domain.manager.DataStoreManager
import com.manikdev.kotlinytmusicscraper.YouTube
import com.manikdev.spotify.Spotify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import org.spotify.aiservice.AiClient
import org.spotify.lyrics.SpotifyLyricsClient
import kotlin.time.ExperimentalTime
import org.spotify.autoeq.AutoEq

@OptIn(ExperimentalTime::class)
val databaseModule =
    module {
        single(createdAtStart = true) {
            Converters()
        }
        // Database
        single(createdAtStart = true) {
            getDatabaseBuilder(
                get<Converters>()
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
        // DatabaseDao
        single(createdAtStart = true) {
            get<MusicDatabase>().getDatabaseDao()
        }
        // Library sync (#2663): its own small preferences file, its DAO, and the recorder every
        // write to a synced item goes through.
        single(createdAtStart = true) {
            get<MusicDatabase>().getSyncDao()
        }
        single(createdAtStart = true) {
            SyncPrefs(createLibrarySyncDataStore())
        }
        single(createdAtStart = true) {
            SyncChangeRecorder(get<SyncDao>(), get<SyncPrefs>())
        }
        // LocalDataSource
        single(createdAtStart = true) {
            LocalDataSource(get<DatabaseDao>(), get<MusicDatabase>(), get<SyncChangeRecorder>())
        }
        // AnalyticsDatasource
        single(createdAtStart = true) {
            AnalyticsDatasource(get<DatabaseDao>(), get<SyncChangeRecorder>())
        }
        // Datastore
        single(createdAtStart = true) {
            createDataStoreInstance()
        }
        // DatastoreManager
        single<DataStoreManager>(createdAtStart = true) {
            DataStoreManagerImpl(get<DataStore<Preferences>>())
        }

        // Move YouTube from Singleton to Koin DI
        single(createdAtStart = true) {
            YouTube()
        }

        single(createdAtStart = true) {
            Spotify()
        }

        single(createdAtStart = true) {
            AiClient()
        }

        single(createdAtStart = true) {
            SpotifyLyricsClient()
        }

        // Not created at start, unlike the rest: nothing needs it until someone opens the AutoEq
        // picker, and it holds an HTTP client the vast majority of sessions never use.
        single {
            AutoEq()
        }
    }