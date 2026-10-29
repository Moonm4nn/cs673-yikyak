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
        pageSize: Int,
        latitude: Double,
        longitude: Double,
        sortByHot: Boolean
    ): List<SocialPost>

    suspend fun getPost(
        postId: String
    ): Result<SocialPost>
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
    override suspend fun getPosts(
        pageNum: Int,
        pageSize: Int,
        latitude: Double,
        longitude: Double,
        sortByHot: Boolean
    ): List<SocialPost> {
        // Calculate the index range of posts to fetch
        val fromPostIndex = pageNum * pageSize
        val toPostIndex = fromPostIndex + pageSize - 1

        // Fetch posts within the specified radius
        val jsonArrayResult = supabase.postgrest.rpc(
            function = "get_posts_within_radius",
            parameters = buildJsonObject {
                put("u_lat", latitude)
                put("u_lng", longitude)
                put("rad_km", 5.0)
                put("page_offset", fromPostIndex)
                put("page_limit", toPostIndex)
                put("sort_by_hot", sortByHot)
            }
        )


        // Convert the JSON array to a list of posts and return it
        return jsonArrayResult.decodeList<SocialPost>()
    }

    override suspend fun getPost(postId: String): Result<SocialPost> = runCatching {
        supabase.postgrest.from("posts").select {
            filter { eq("id", postId) }
        }.decodeSingle<SocialPost>()
    }
}
