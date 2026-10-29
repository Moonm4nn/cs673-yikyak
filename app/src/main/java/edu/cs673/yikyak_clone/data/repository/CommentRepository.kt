package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.Comment
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface CommentRepository {
    suspend fun getComments(postId: String): Result<List<Comment>>
    suspend fun createComment(postId: String, content: String, parentId: String?): Result<Unit>
}

class SupabaseCommentRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : CommentRepository {
    override suspend fun getComments(postId: String): Result<List<Comment>> = runCatching {
        supabase.postgrest
            .rpc(
                function = "get_comments",
                parameters = buildJsonObject { put("p_post_id", postId) },
            )
            .decodeList<Comment>()
    }

    override suspend fun createComment(
        postId: String,
        content: String,
        parentId: String?
    ): Result<Unit> = runCatching {
        supabase.postgrest.rpc(
            function = "create_comment",
            parameters = buildJsonObject {
                put("p_post_id", postId)
                put("p_content", content)
                put("p_parent_id", parentId)
            },
        )
        Unit
    }
}
