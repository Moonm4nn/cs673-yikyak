package edu.cs673.yikyak_clone.data.repository

import android.net.Uri
import edu.cs673.yikyak_clone.data.model.SocialPost
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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

    suspend fun getPosts(
        pageNum: Int,
        pageSize: Int
    ): List<SocialPost>
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

    // Gets a single page worth of posts
    override suspend fun getPosts(pageNum: Int, pageSize: Int): List<SocialPost> {
        // Calculate the range of posts to fetch
        val fromPostIndex = pageNum * pageSize
        val toPostIndex = fromPostIndex + pageSize - 1

        // Fetch the posts from the database
        val jsonArrayResult = supabase.postgrest.from("posts").select {
                // Order by newest first
                order(column = "created_at", order = Order.DESCENDING)
                // Get the relevant range of posts for current page
                range(from = fromPostIndex.toLong(), to = toPostIndex.toLong())
            }

        // Convert the JSON array to a list of posts and return it
        return jsonArrayResult.decodeList<SocialPost>()
    }
}
