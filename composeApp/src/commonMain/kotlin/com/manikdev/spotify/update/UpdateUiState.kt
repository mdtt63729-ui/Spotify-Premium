package com.manikdev.spotify.update

import kotlinx.coroutines.flow.StateFlow

/**
 * One available release, as read from the GitHub release feed.
 *
 * [notes] is the release body split into the lines the sheet shows: the bold headline first,
 * then the "• " bullets and the "– " detail lines underneath them.
 */
data class UpdateInfo(
    val versionName: String,
    val notes: List<String>,
    val apkUrl: String = "",
    val sizeBytes: Long = 0L,
)

/** What the update sheet is showing right now. */
sealed interface UpdateUiState {
    data object Idle : UpdateUiState

    /** A newer release exists and nothing has been downloaded yet. */
    data class Available(val info: UpdateInfo) : UpdateUiState

    /** An APK for [info] is on its way to local storage. */
    data class Downloading(
        val info: UpdateInfo,
        val progress: Float,
    ) : UpdateUiState

    /**
     * The APK is already on disk. This is what a user who downloaded but never installed sees on
     * the next launch — the sheet offers Install, not Download, until a newer release appears.
     */
    data class ReadyToInstall(val info: UpdateInfo) : UpdateUiState

    data class Failed(
        val info: UpdateInfo?,
        val message: String,
    ) : UpdateUiState
}

/**
 * The updater as the UI sees it. Implemented per platform; the sheet never touches Android types.
 */
interface UpdateHostController {
    val state: StateFlow<UpdateUiState>

    /** Look for a newer release (and pick up an APK that is already downloaded). */
    fun check()

    /** Start (or resume) the download for the current [UpdateInfo]. */
    fun download()

    /** Launch the system installer for the downloaded APK. */
    fun install()

    /** Hide the sheet until the next launch. */
    fun dismiss()
}
