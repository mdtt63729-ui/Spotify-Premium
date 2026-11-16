package com.manikdev.kotlinytmusicscraper.potoken

/** iOS target is not shipped; no minter, requests go out without a token. */
actual fun defaultPoTokenProvider(): PoTokenProvider? = null
