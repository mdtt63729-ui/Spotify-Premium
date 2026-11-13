package com.manikdev.spotify.di

import com.manikdev.spotify.viewModel.AlbumViewModel
import com.manikdev.spotify.utils.VersionManager
import com.manikdev.spotify.viewModel.AnalyticsViewModel
import com.manikdev.spotify.viewModel.ListenTogetherSettingsViewModel
import com.manikdev.spotify.viewModel.ListenTogetherViewModel
import com.manikdev.spotify.viewModel.ArtistViewModel
import com.manikdev.spotify.viewModel.BrowseViewModel
import com.manikdev.spotify.viewModel.BrowserLoginViewModel
import com.manikdev.spotify.viewModel.CommentsViewModel
import com.manikdev.spotify.viewModel.SoundCloudPlayerViewModel
import com.manikdev.spotify.viewModel.HomeViewModel
import com.manikdev.spotify.viewModel.ImportViewModel
import com.manikdev.spotify.viewModel.LibraryDynamicPlaylistViewModel
import com.manikdev.spotify.viewModel.LibrarySidebarViewModel
import com.manikdev.spotify.viewModel.LibraryViewModel
import com.manikdev.spotify.viewModel.LocalPlaylistViewModel
import com.manikdev.spotify.viewModel.LibrarySyncHostViewModel
import com.manikdev.spotify.viewModel.LibrarySyncPhoneViewModel
import com.manikdev.spotify.viewModel.LogInViewModel
import com.manikdev.spotify.viewModel.LoginSyncHostViewModel
import com.manikdev.spotify.viewModel.LoginSyncSenderViewModel
import com.manikdev.spotify.viewModel.MoodViewModel
import com.manikdev.spotify.viewModel.MoreAlbumsViewModel
import com.manikdev.spotify.viewModel.NotificationViewModel
import com.manikdev.spotify.viewModel.NowPlayingBottomSheetViewModel
import com.manikdev.spotify.viewModel.PlaylistViewModel
import com.manikdev.spotify.viewModel.PodcastViewModel
import com.manikdev.spotify.viewModel.SearchViewModel
import com.manikdev.spotify.viewModel.AutoEqViewModel
import com.manikdev.spotify.viewModel.SettingsViewModel
import com.manikdev.spotify.viewModel.SharedViewModel
import com.manikdev.spotify.viewModel.SongSelectionViewModel
import com.manikdev.spotify.viewModel.TasteViewModel
import com.manikdev.spotify.viewModel.WrappedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        single {
            SharedViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        single {
            SearchViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            SongSelectionViewModel(
                get(),
                get(),
            )
        }
        // Only one of these resolves on a given platform: Desktop binds the host repository, Android the
        // sender. Koin resolves dependencies when a ViewModel is created, not when it is declared.
        viewModel { LoginSyncHostViewModel(get()) }
        viewModel { LoginSyncSenderViewModel(get(), get()) }
        viewModel { LibrarySyncHostViewModel(get()) }
        viewModel { LibrarySyncPhoneViewModel(get()) }
        viewModel { BrowserLoginViewModel(get()) }
        viewModel { CommentsViewModel(get(), get()) }
        viewModel { SoundCloudPlayerViewModel(get()) }
        viewModel {
            NowPlayingBottomSheetViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LibraryDynamicPlaylistViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ImportViewModel(
                get(),
            )
        }
        viewModel {
            AlbumViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            HomeViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            AutoEqViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            SettingsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ArtistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            PlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LogInViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            PodcastViewModel(
                get(),
            )
        }
        viewModel {
            MoreAlbumsViewModel(
                get(),
            )
        }
        viewModel {
            BrowseViewModel(
                get(),
            )
        }
        viewModel {
            LibrarySidebarViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            LocalPlaylistViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            NotificationViewModel(
                get(),
            )
        }
        viewModel {
            MoodViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            AnalyticsViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            WrappedViewModel(
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            TasteViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            ListenTogetherSettingsViewModel(get())
        }
        viewModel {
            ListenTogetherViewModel(
                repository = get(),
                dataStore = get(),
                bridge = get(),
            )
        }

    }
