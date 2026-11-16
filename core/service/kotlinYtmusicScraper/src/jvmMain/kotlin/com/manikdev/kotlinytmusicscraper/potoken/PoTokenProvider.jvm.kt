package com.manikdev.kotlinytmusicscraper.potoken

/** Desktop has no WebView, so no minter: requests go out without a token, as before. */
actual fun defaultPoTokenProvider(): PoTokenProvider? = null
