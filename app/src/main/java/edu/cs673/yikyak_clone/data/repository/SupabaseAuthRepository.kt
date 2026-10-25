package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth

class SupabaseAuthRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun signInAnonymously() {
        supabase.auth.signInAnonymously()
    }

    fun currentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun hasSession(): Boolean {
        return currentUserId() != null
    }
}