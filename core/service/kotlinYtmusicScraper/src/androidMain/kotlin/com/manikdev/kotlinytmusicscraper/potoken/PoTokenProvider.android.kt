package com.manikdev.kotlinytmusicscraper.potoken

actual fun defaultPoTokenProvider(): PoTokenProvider? =
    runCatching { AndroidPoTokenProvider(YouTube()) }
        .onFailure { android.util.Log.w("PoToken", "no minter installed: ${it.message}") }
        .getOrNull()
