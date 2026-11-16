package com.manikdev.kotlinytmusicscraper.models.body

import com.manikdev.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlayerBody(
    val context: Context,
    val videoId: String,
    val playlistId: String?,
    val cpn: String?,
    val contentCheckOk: Boolean = true,
    val racyCheckOk: Boolean = true,
    val playbackContext: PlaybackContext? = null,
    val serviceIntegrityDimensions: ServiceIntegrityDimensions? = null,
) {
    @Serializable
    data class ServiceIntegrityDimensions(
        /**
         * A freshly minted BotGuard token. There used to be a hardcoded string here that had
         * expired long ago, and it was sent with every player request - a stale token is worse
         * than none, because the request is refused on its merits rather than being treated as
         * unauthenticated. It is a required argument now, so that cannot happen again.
         */
        val poToken: String,
    )

    @Serializable
    data class PlaybackContext(
        val contentPlaybackContext: ContentPlaybackContext = ContentPlaybackContext(),
    ) {
        @Serializable
        data class ContentPlaybackContext(
            val html5Preference: String = "HTML5_PREF_WANTS",
            val signatureTimestamp: Int = 20073,
        )
    }
}