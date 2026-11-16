package com.manikdev.kotlinytmusicscraper.potoken

import com.manikdev.kotlinytmusicscraper.YouTube

actual fun defaultPoTokenProvider(): PoTokenProvider? =
    runCatching { AndroidPoTokenProvider(YouTube()) }
        .onFailure { android.util.Log.w("PoToken", "no minter installed: ${it.message}") }
        .getOrNull()
