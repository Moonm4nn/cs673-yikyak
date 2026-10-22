package edu.cs673.yikyak_clone.data.model

data class User(
    val id: String,
    val username: String,
    val karma: Int = 0,
    val isBanned: Boolean = false
)