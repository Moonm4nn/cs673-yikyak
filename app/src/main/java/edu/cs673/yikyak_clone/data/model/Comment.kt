package edu.cs673.yikyak_clone.data.model

data class Comment(
    val id: String,
    val content: String,
    val score: Int,
    val createdAt: String,
    val alias: String,
    val parentId: String?,
)
