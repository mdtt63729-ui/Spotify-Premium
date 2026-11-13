package com.manikdev.spotify.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.manikdev.domain.data.model.metadata.Lyrics
import com.manikdev.domain.data.model.streams.TimeLine
import com.manikdev.media_jvm_ui.ui.MediaPlayerViewWithSubtitleJvm
import com.manikdev.media_jvm_ui.ui.MediaPlayerViewWithUrl
import com.manikdev.media_jvm_ui.ui.rememberVideoAspectRatioJvm
import com.manikdev.spotify.ui.theme.typo

@Composable
actual fun MediaPlayerView(
    url: String,
    modifier: Modifier,
    cropToBounds: Boolean,
    onFirstFrame: () -> Unit,
) {
    MediaPlayerViewWithUrl(
        url = url,
        modifier = modifier,
        cropToBounds = cropToBounds,
        onFirstFrame = onFirstFrame,
    )
}

@Composable
actual fun rememberVideoAspectRatio(playerName: String): Float? = rememberVideoAspectRatioJvm()

@Composable
actual fun MediaPlayerViewWithSubtitle(
    modifier: Modifier,
    playerName: String,
    shouldPip: Boolean,
    shouldShowSubtitle: Boolean,
    shouldScaleDownSubtitle: Boolean,
    isInPipMode: Boolean,
    timelineState: TimeLine,
    lyricsData: Lyrics?,
    translatedLyricsData: Lyrics?,
    mainTextStyle: TextStyle,
    translatedTextStyle: TextStyle,
) {
    MediaPlayerViewWithSubtitleJvm(
        playerName = playerName,
        modifier = modifier,
        shouldShowSubtitle = shouldShowSubtitle,
        shouldScaleDownSubtitle = shouldScaleDownSubtitle,
        timelineState = timelineState,
        lyricsData = lyricsData,
        translatedLyricsData = translatedLyricsData,
        mainTextStyle = typo().bodyLarge,
        translatedTextStyle = typo().bodyMedium,
    )
}