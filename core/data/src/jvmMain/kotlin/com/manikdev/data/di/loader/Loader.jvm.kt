package com.manikdev.data.di.loader

import com.spotify.media_jvm.di.loadDesktopPlayerModule
import com.manikdev.data.browserlogin.BrowserLoginRepositoryImpl
import com.manikdev.data.librarysync.LibrarySyncHostRepositoryImpl
import com.manikdev.data.librarysync.SyncApplier
import com.manikdev.data.librarysync.SyncEngine
import com.manikdev.data.librarysync.SyncedSettings
import com.manikdev.data.loginsync.LoginSyncHostRepositoryImpl
import com.manikdev.data.loginsync.LoginSyncStore
import com.manikdev.domain.repository.LibrarySyncHostRepository
import com.manikdev.domain.repository.LibrarySyncRepository
import com.manikdev.domain.repository.LoginSyncHostRepository
import com.manikdev.domain.repository.BrowserLoginRepository
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

actual fun loadMediaService() {
    loadDesktopPlayerModule()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncHostRepository> { LoginSyncHostRepositoryImpl(get()) }
            // Library sync (#2663): this computer keeps the log and serves its paired phones.
            single { SyncedSettings(get(), get()) }
            single { SyncEngine(get(), get(), SyncApplier(get(), get()), get()) }
            single<LibrarySyncHostRepository> { LibrarySyncHostRepositoryImpl(get(), get(), get(), get(), get(), get()) }
            single<LibrarySyncRepository> { get<LibrarySyncHostRepository>() }
            single<BrowserLoginRepository> { BrowserLoginRepositoryImpl() }
        },
    )
}
