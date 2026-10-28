package edu.cs673.yikyak_clone.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.first

class SupabaseAuthRepository (
    private val supabase: SupabaseClient
){

    suspend fun signInAnonymously(): String {
        val existingId = currentUserId()

        if (existingId != null) {
            android.util.Log.d(
                "YikYakAuth",
                "Reusing existing session: $existingId"
            )
            return existingId
        }

        android.util.Log.d(
            "YikYakAuth",
            "No current user; signing in anonymously"
        )

        supabase.auth.signInAnonymously()

        return currentUserId()
            ?: throw IllegalStateException(
                "Anonymous sign-in completed but no user ID was found"
            )
    }

    fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    suspend fun ensureAnonymousSession(): String {
        // Wait until Supabase has finished restoring the session.
        supabase.auth.sessionStatus.first {
            it !is SessionStatus.Initializing
        }

        // Reuse the existing session if one was restored.
        currentUserId()?.let { return it }

        // Only create an anonymous user if no session exists.
        supabase.auth.signInAnonymously()

        return currentUserId()
            ?: throw IllegalStateException(
                "Anonymous sign-in completed but no user ID was found"
            )
    }

    fun hasSession(): Boolean {
        return currentUserId() != null
    }
}