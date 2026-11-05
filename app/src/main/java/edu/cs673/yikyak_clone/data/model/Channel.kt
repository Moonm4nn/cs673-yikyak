package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: String,
    val name: String
)