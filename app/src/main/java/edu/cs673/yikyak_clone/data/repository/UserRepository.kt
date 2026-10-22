package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.User
interface UserRepository {
    suspend fun createUser(
        userId: String,
        username: String
    ): Result<Unit>

    suspend fun getUser(
        userId: String
    ): User?
}

class InMemoryUserRepository : UserRepository {

    private val users = mutableMapOf<String, User>()

    override suspend fun createUser(
        userId: String,
        username: String
    ): Result<Unit> = runCatching {
        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }

        require(username.isNotBlank()) {
            "Username cannot be empty"
        }

        check(userId !in users) {
            "A user with this ID already exists"
        }

        users[userId] = User(
            id = userId,
            username = username.trim()
        )
    }

    override suspend fun getUser(userId: String): User? {
        return users[userId]
    }
}