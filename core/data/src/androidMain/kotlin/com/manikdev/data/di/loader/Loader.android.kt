package com.manikdev.data.di.loader

import com.manikdev.media3.di.loadMediaService
import com.manikdev.data.librarysync.LibrarySyncPhoneRepositoryImpl
import com.manikdev.data.librarysync.SyncApplier
import com.manikdev.data.librarysync.SyncEngine
import com.manikdev.data.librarysync.SyncedSettings
import com.manikdev.data.loginsync.LoginSyncSenderRepositoryImpl
import com.manikdev.data.loginsync.LoginSyncStore
import com.manikdev.domain.repository.LibrarySyncPhoneRepository
import com.manikdev.domain.repository.LibrarySyncRepository
import com.manikdev.domain.repository.LoginSyncSenderRepository
import org.koin.core.context.loadKoinModules
import org.spotify.loginsync.LoginSyncClient
import org.spotify.loginsync.PairedSyncClient
import org.koin.dsl.module

actual fun loadMediaService() {
    loadMediaService()
}

actual fun loadLoginSyncModule() {
    loadKoinModules(
        module {
            single { LoginSyncStore(get(), get(), get()) }
            single<LoginSyncSenderRepository> { LoginSyncSenderRepositoryImpl(get(), LoginSyncClient()) }
            // Library sync (#2663): this phone drives every round with the computer it is paired with.
            single { SyncedSettings(get(), get()) }
            single { SyncEngine(get(), get(), SyncApplier(get(), get()), get()) }
            single<LibrarySyncPhoneRepository> {
                LibrarySyncPhoneRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), PairedSyncClient())
            }
            single<LibrarySyncRepository> { get<LibrarySyncPhoneRepository>() }
        },
    )
}
