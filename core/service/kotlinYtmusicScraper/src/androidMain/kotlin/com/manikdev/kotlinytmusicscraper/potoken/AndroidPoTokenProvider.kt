package com.manikdev.kotlinytmusicscraper.potoken

import com.manikdev.kotlinytmusicscraper.YouTube

/**
 * The Android side of the poToken seam.
 *
 * Steps done: the class is installed, and it can reach the WAA challenge through [YouTube].
 * Steps remaining: run that challenge through the JS engine that is already in this module
 * (`FaradayJsDecoder`), exchange the result for an integrity token, cache it, return it.
 *
 * Every failure returns null on purpose. An absent token is treated as unauthenticated; a
 * malformed one gets the request refused, which would be worse than what we have today.
 */
class AndroidPoTokenProvider(
    private val youTube: YouTube,
) : PoTokenProvider {
    override suspend fun poToken(videoId: String): String? {
        val challenge =
            runCatching { youTube.poTokenChallenge() }
                .onFailure { android.util.Log.w("PoToken", "challenge failed: ${it.message}") }
                .getOrNull()
        // Step 3 goes here: evaluate [challenge] in the JS engine and exchange it. Until then the
        // challenge is fetched and logged, and no token is claimed.
        if (challenge != null) {
            android.util.Log.w("PoToken", "challenge fetched (${challenge.length} chars) for $videoId")
        }
        return null
    }
}
