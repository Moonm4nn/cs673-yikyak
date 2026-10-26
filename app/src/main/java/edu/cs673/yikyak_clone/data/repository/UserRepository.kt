package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.User
interface UserRepository {
    suspend fun updateUsername(
        userId: String,
        username: String
    ): Result<Unit>

    suspend fun getUser(
        userId: String
    ): User?
}

class InMemoryUserRepository(
    initialUsers: List<User> = emptyList()
) : UserRepository {

    private val users = initialUsers
        .associateBy { it.id }
        .toMutableMap()

    override suspend fun updateUsername(
        userId: String,
        username: String
    ): Result<Unit> = runCatching {
        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }

        require(username.isNotBlank()) {
            "Username cannot be empty"
        }

        val existingUser = users[userId]
            ?: error("User not found")

        users[userId] = existingUser.copy(
            username = username.trim()
        )
    }

    override suspend fun getUser(userId: String): User? {
        return users[userId]
    }
}