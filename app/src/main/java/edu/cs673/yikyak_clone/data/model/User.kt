package edu.cs673.yikyak_clone.data.model

import kotlin.time.Instant
import kotlin.time.TimeMark

data class User(
    val id: String,
    val username: String,
    val karma: Int = 0,
    val isBanned: Boolean = false,
    val creationTime: String
)