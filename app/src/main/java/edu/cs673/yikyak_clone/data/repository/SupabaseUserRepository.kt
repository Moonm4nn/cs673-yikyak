package edu.cs673.yikyak_clone.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import edu.cs673.yikyak_clone.data.model.User
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import edu.cs673.yikyak_clone.data.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.collections.contains
import kotlin.collections.set
import io.github.jan.supabase.postgrest.rpc

/** New User Insert
 *
 */
@Serializable
private data class NewProfile(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

)

/** Profile row builder
 *
 */
@Serializable
private data class ProfileRow(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String?,

    @SerialName("karma")
    val karma: Int,

    @SerialName("is_banned")
    val isBanned: Boolean,

    @SerialName("created_at")
    val creationTime: String,

    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean = false

)


class SupabaseUserRepository(
    private val supabase: SupabaseClient
) : UserRepository {

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

        supabase.from("profiles").update({
            set("username", username.trim())
        }) {
            filter {
                eq("id", userId)
            }
        }
    }

    override suspend fun getUser(userId: String): User? {
        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }

        val profiles = supabase.postgrest
            .rpc("get_my_profile")
            .decodeList<ProfileRow>()

        return profiles.firstOrNull()?.let { profile ->
            User(
                id = profile.id,
                username = profile.username,
                karma = profile.karma,
                isBanned = profile.isBanned,
                creationTime = profile.creationTime,
                onboardingCompleted = profile.onboardingCompleted
            )
        }
    }

    override suspend fun completeOnboarding(
        userId: String,
        username: String
    ): Result<Unit> = runCatching {
        require(userId.isNotBlank()) {
            "User ID cannot be empty"
        }
        require(username.isNotBlank()) {
            "Username cannot be empty"
        }

        supabase.from("profiles").update({
            set("username", username.trim())
            set("onboarding_completed", true)
        }) {
            filter {
                eq("id", userId)
            }
        }
    }
}