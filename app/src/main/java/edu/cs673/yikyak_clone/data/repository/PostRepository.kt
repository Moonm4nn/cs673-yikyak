package edu.cs673.yikyak_clone.data.repository

import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/*
Repository layer for post operations (keeps Data separate from UI)
for creating / retrieving posts while keeping database details
out of the UI and ViewModel
*/

interface PostRepository {
    suspend fun createPost(
        postContent: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri? = null
    ): Result<Unit>
}

class SupabasePostRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : PostRepository {
    override suspend fun createPost(
        postContent: String,
        latitude: Double,
        longitude: Double,
        imageUri: Uri?
    ): Result<Unit> = runCatching {
        supabase.postgrest.rpc(
            function = "create_post",
            parameters = buildJsonObject {
                put("p_content", postContent)
                put("p_lat", latitude)
                put("p_lng", longitude)
            }
        )
    }
}