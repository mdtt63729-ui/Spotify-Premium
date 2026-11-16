package com.manikdev.kotlinytmusicscraper.potoken

import android.util.Log
import com.dokar.quickjs.QuickJs
import com.manikdev.kotlinytmusicscraper.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Mints BotGuard poTokens on Android.
 *
 * WEB_REMIX refuses some tracks outright without one, and no client rotation helps there. This
 * runs Google's own BotGuard VM — fetched fresh from the challenge response — inside QuickJS, the
 * same engine this module already uses for the n-transform, so no WebView and no Activity.
 *
 * Every failure returns null. An absent token is treated as unauthenticated; a malformed one gets
 * the request refused, so failing here must never be louder than not trying.
 */
class AndroidPoTokenProvider(
    private val youTube: YouTube,
) : PoTokenProvider {
    @Volatile
    private var cachedToken: String? = null

    override suspend fun poToken(videoId: String): String? {
        cachedToken?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching { mint() }
                .onFailure { Log.w(TAG, "mint failed: ${it.message}") }
                .getOrNull()
                ?.also { cachedToken = it }
        }
    }

    private suspend fun mint(): String? {
        val create = youTube.poTokenChallenge() ?: return null.also { Log.w(TAG, "no challenge") }
        val vmUrl = extract(create, "vm") ?: return null.also { Log.w(TAG, "no vm url") }
        val challenge = extract(create, "challenge") ?: return null.also { Log.w(TAG, "no challenge field") }
        Log.w(TAG, "challenge ${challenge.length} chars, vm $vmUrl")

        val vmScript =
            runCatching { URL(vmUrl).openStream().bufferedReader().use { it.readText() } }
                .getOrNull()
                ?: return null.also { Log.w(TAG, "vm script fetch failed") }

        val botguardResponse = runBotGuard(vmScript, challenge) ?: return null
        Log.w(TAG, "botguard response ${botguardResponse.length} chars")
        return youTube.poTokenFromChallenge(botguardResponse)
    }

    /**
     * Runs the VM script and hands it the challenge, the way Google's own page does: the script
     * defines a global interpreter, and calling it with the challenge returns the response string.
     */
    private fun runBotGuard(vmScript: String, challenge: String): String? =
        runCatching {
            // create() needs a dispatcher, and the engine is not Closeable - closed in finally.
            val js = QuickJs.create(Dispatchers.Default)
            try {
                js.evaluate<Any?>(vmScript)
                val call =
                    """
                    (function(){
                      try {
                        var bg = (typeof globalThis.botguard !== 'undefined') ? globalThis.botguard
                               : (typeof globalThis.vm !== 'undefined') ? globalThis.vm : null;
                        if (!bg) return 'no-interpreter';
                        var fn = bg.a || bg.invoke || bg.load || bg;
                        var out = (typeof fn === 'function') ? fn(${quote(challenge)}) : 'not-a-function';
                        return String(out);
                      } catch (e) { return 'error:' + e; }
                    })()
                    """.trimIndent()
                js.evaluate<String>(call)
            } finally {
                runCatching { js.close() }
            }
        }
            .onFailure { Log.w(TAG, "quickjs failed: ${it.message}") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() && !it.startsWith("error:") && it != "no-interpreter" && it != "not-a-function" }

    /** The challenge response is a nested JSON blob; this pulls one string field out of it. */
    private fun extract(json: String, field: String): String? =
        Regex("\"$field\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
            .find(json)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\/", "/")
            ?.replace("\\\\", "\\")

    private fun quote(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private companion object {
        const val TAG = "PoToken"
    }
}
