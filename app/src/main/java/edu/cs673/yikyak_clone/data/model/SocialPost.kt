package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialPost(
    val id: String,
    val username: String,
    val avatarUrl: String,
    val postImageUrl: String,
    val caption: String,
    val score: Int,
    val userVote: Int
)