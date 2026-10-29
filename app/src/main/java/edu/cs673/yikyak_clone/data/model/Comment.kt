package edu.cs673.yikyak_clone.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val content: String,
    val score: Int,
    @SerialName("created_at") val createdAt: String,
    val alias: String,
    @SerialName("parent_id") val parentId: String?,
)
