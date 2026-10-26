package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class InMemoryUserRepositoryTest {

    private val userId = "user-123"

    private fun createRepository(): InMemoryUserRepository {
        return InMemoryUserRepository(
            initialUsers = listOf(
                User(
                    id = userId,
                    username = "OriginalYak",
                    creationTime = ""
                )
            )
        )
    }

    @Test
    fun updateUsername_validInput_updatesExistingUser() = runTest {
        val repository = createRepository()

        val result = repository.updateUsername(
            userId = userId,
            username = "YakUser"
        )

        assertTrue(result.isSuccess)

        val user = repository.getUser(userId)
        assertNotNull(user)
        assertEquals("YakUser", user?.username)
        assertEquals(userId, user?.id)
    }

    @Test
    fun updateUsername_blankId_returnsFailure() = runTest {
        val repository = createRepository()

        val result = repository.updateUsername(
            userId = " ",
            username = "YakUser"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun updateUsername_blankUsername_returnsFailure() = runTest {
        val repository = createRepository()

        val result = repository.updateUsername(
            userId = userId,
            username = "   "
        )

        assertTrue(result.isFailure)
        assertEquals(
            "OriginalYak",
            repository.getUser(userId)?.username
        )
    }

    @Test
    fun updateUsername_usernameHasWhitespace_storesTrimmedUsername() = runTest {
        val repository = createRepository()

        val result = repository.updateUsername(
            userId = userId,
            username = "  YakUser  "
        )

        assertTrue(result.isSuccess)
        assertEquals(
            "YakUser",
            repository.getUser(userId)?.username
        )
    }

    @Test
    fun updateUsername_unknownUser_returnsFailure() = runTest {
        val repository = createRepository()

        val result = repository.updateUsername(
            userId = "unknown-user",
            username = "YakUser"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun getUser_unknownId_returnsNull() = runTest {
        val repository = createRepository()

        assertNull(repository.getUser("does-not-exist"))
    }
}