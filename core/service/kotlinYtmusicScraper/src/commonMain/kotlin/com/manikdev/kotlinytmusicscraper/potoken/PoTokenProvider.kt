package com.manikdev.kotlinytmusicscraper.potoken

/**
 * Where a BotGuard poToken comes from.
 *
 * WEB_REMIX will not serve some tracks without one, and no amount of player-client rotation
 * helps in that case — the request is refused on its merits, not on which client asked. Echo and
 * BitChord both mint one in a hidden WebView; this is the seam through which such a minter is
 * handed to the scraper.
 *
 * The scraper is multiplatform, so the implementation is platform-specific and is installed once
 * at start-up. Until one is installed this stays null and every caller behaves exactly as before.
 */
interface PoTokenProvider {
    /**
     * A poToken for [videoId], or null when none can be minted right now.
     *
     * Implementations should cache: a token is expensive to produce and is reused for a while.
     */
    suspend fun poToken(videoId: String): String?
}

object PoTokenProviders {
    @Volatile
    var current: PoTokenProvider? = null
        private set

    fun install(provider: PoTokenProvider) {
        current = provider
    }

    fun clear() {
        current = null
    }
}
