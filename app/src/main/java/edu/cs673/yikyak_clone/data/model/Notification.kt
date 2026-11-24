package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    @SerialName("post_id") val postId: String,
    @SerialName("comment_id") val commentId: String,
    val type: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String,
    val content: String,
    val alias: String,
)
