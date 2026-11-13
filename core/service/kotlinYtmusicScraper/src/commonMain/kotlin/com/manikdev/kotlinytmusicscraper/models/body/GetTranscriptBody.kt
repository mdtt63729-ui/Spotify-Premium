package com.manikdev.kotlinytmusicscraper.models.body

import com.manikdev.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)