package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.datetime.Instant

@Serializable
data class SocialPost(
    // Filled with default values so that empty posts can be ready from the server
    val id: String,
    val user_id: String,
    val content: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("my_vote") val userVote: Int = 0,
    val score: Int = 0,
    val comment_count: Int = 0,
    val is_removed: Boolean = false,
    val created_at: Instant
)