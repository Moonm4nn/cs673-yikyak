package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.remote.SupabaseClientProviderTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class SupabaseUserRepositoryTest {

    private val testClient = SupabaseClientProviderTest.client

    private val repository = SupabaseUserRepository(testClient)
    private val authRepository = SupabaseAuthRepository(testClient)

    private suspend fun getAuthenticatedUserId(): String {
        if (authRepository.currentUserId() == null) {
            authRepository.signInAnonymously()
        }

        val userId = authRepository.currentUserId()

        assertNotNull("Authenticated user ID should exist", userId)
        assertTrue(
            "Authenticated user ID should not be blank",
            !userId.isNullOrBlank()
        )

        return requireNotNull(userId)
    }

    @Test
    fun updateUsername_authenticatedUser_updatesProfile() = runTest {
        val userId = getAuthenticatedUserId()

        val result = repository.updateUsername(
            userId = userId,
            username = "YakUser"
        )

        assertTrue(
            "Username update should succeed: ${result.exceptionOrNull()}",
            result.isSuccess
        )

        val user = repository.getUser(userId)

        assertNotNull("Profile should exist", user)
        assertEquals(userId, user?.id)
        assertEquals("YakUser", user?.username)
    }

    @Test
    fun updateUsername_blankId_returnsFailure() = runTest {
        val result = repository.updateUsername(
            userId = " ",
            username = "YakUser"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun updateUsername_blankUsername_returnsFailure() = runTest {
        val userId = getAuthenticatedUserId()

        val result = repository.updateUsername(
            userId = userId,
            username = "   "
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun updateUsername_usernameHasWhitespace_storesTrimmedUsername() = runTest {
        val userId = getAuthenticatedUserId()

        val result = repository.updateUsername(
            userId = userId,
            username = "  YakUser  "
        )

        assertTrue(
            "Username update should succeed: ${result.exceptionOrNull()}",
            result.isSuccess
        )

        assertEquals(
            "YakUser",
            repository.getUser(userId)?.username
        )
    }

    @Test
    fun updateUsername_existingUser_canBeUpdatedAgain() = runTest {
        val userId = getAuthenticatedUserId()

        val firstResult = repository.updateUsername(
            userId = userId,
            username = "FirstYak"
        )

        assertTrue(firstResult.isSuccess)

        val secondResult = repository.updateUsername(
            userId = userId,
            username = "SecondYak"
        )

        assertTrue(
            "Second update should succeed: ${secondResult.exceptionOrNull()}",
            secondResult.isSuccess
        )

        assertEquals(
            "SecondYak",
            repository.getUser(userId)?.username
        )
    }

    @Test
    fun getUser_unknownId_returnsNull() = runTest {
        val userId = getAuthenticatedUserId()
        val unknownId = UUID.randomUUID().toString()

        assertNotEquals(userId, unknownId)
        assertNull(repository.getUser(unknownId))
    }
}