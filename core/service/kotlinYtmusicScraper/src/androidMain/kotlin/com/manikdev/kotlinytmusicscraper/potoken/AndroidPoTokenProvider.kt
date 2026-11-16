package com.manikdev.kotlinytmusicscraper.potoken

/**
 * The Android side of the poToken seam.
 *
 * Step 1: installed, wired, and honest - it mints nothing yet, so [poToken] returns null and
 * every player request goes out exactly as it does without a provider. That is deliberate: an
 * absent token is treated as unauthenticated, where a malformed one gets the request refused.
 *
 * Step 2 fills in the body of this class:
 *  - ask the WAA endpoint for a challenge (`Ytmusic.createPoTokenChallenge()`),
 *  - run that challenge through the BotGuard VM in a hidden WebView and collect its response,
 *  - exchange the response for an integrity token (`Ytmusic.generatePoToken`),
 *  - cache the token and hand it back for the songs that need it.
 *
 * Nothing outside this file has to change when that lands - the seam is already in place and
 * `YouTube.player` already sends whatever comes back.
 */
class AndroidPoTokenProvider : PoTokenProvider {
    override suspend fun poToken(videoId: String): String? = null
}
