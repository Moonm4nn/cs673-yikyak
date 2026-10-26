package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

class SupabaseAuthRepository (
    private val supabase: SupabaseClient
){

    suspend fun signInAnonymously(): String {
        supabase.auth.signInAnonymously()

        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException(
                "Anonymous sign-in succeeded but no user ID was found"
            )
    }

    fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun hasSession(): Boolean {
        return currentUserId() != null
    }
}