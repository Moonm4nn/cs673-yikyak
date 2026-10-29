package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class SocialPost(
    val id: String,
    val user_id: String,
    //val avatarUrl: String,
    //val postImageUrl: String,
    val caption: String,
    val latitude: Double,
    val longitude: Double,
    val score: Int,
    val userVote: Int,
    val comment_count: Int,
    val is_removed: Boolean,
    val created_at: Instant
)