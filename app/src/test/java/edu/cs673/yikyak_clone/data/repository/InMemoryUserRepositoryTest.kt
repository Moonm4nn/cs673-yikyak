package edu.cs673.yikyak_clone.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class InMemoryUserRepositoryTest {

    private val repository = InMemoryUserRepository()

    @Test
    fun createUser_validInput_storesUser() = runTest {
        val result = repository.createUser(
            userId = "user-123",
            username = "YakUser"
        )

        assertTrue(result.isSuccess)

        val user = repository.getUser("user-123")
        assertNotNull(user)
        assertEquals("YakUser", user?.username)
        assertEquals("user-123", user?.id)
    }

    @Test
    fun createUser_blankId_returnsFailure() = runTest {
        val result = repository.createUser(
            userId = " ",
            username = "YakUser"
        )

        assertTrue(result.isFailure)

        assertNull(repository.getUser(" "))
    }

    @Test
    fun createUser_blankUsername_returnsFailure() = runTest {
        val result = repository.createUser(
            userId = "user-123",
            username = "   "
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun createUser_usernameHasWhitespace_storesTrimmedUsername() = runTest {
        val result = repository.createUser(
            userId = "user-123",
            username = "  YakUser  "
        )

        assertTrue(result.isSuccess)
        assertEquals(
            "YakUser",
            repository.getUser("user-123")?.username
        )
    }

    @Test
    fun createUser_duplicateId_returnsFailure() = runTest {
        repository.createUser("user-123", "FirstYak")

        val secondResult = repository.createUser(
            userId = "user-123",
            username = "SecondYak"
        )

        assertTrue(secondResult.isFailure)
        assertEquals("FirstYak",
            repository.getUser("user-123")?.username)
    }

    @Test
    fun getUser_unknownId_returnsNull() = runTest {
        assertNull(repository.getUser("does-not-exist"))
    }
}