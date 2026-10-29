package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

/*
Repository layer for voting
*/

interface VoteRepository {
    // targetType: "post" or "comment"
    // value: 1 (upvote), -1 (downvote), or 0 (remove vote)
    suspend fun castVote(
        targetType: String,
        targetId: String,
        value: Int
    ): Result<Unit>
}

class SupabaseVoteRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : VoteRepository {
    private val TAG = "SupabaseVoteRepository"

    override suspend fun castVote(
        targetType: String,
        targetId: String,
        value: Int
    ): Result<Unit> = try {

        Log.d(TAG, "castVote: type=$targetType, id=$targetId, value=$value")

        supabase.postgrest.rpc(
            function = "cast_vote",
            parameters = buildJsonObject {
                put("p_target_type", targetType)
                put("p_target_id", targetId)
                put("p_value", value)
            }
        )
        Result.success(Unit)
    } catch (e: CancellationException) {
        Log.e(TAG, "castVote cancelled: type=$targetType, id=$targetId, value=$value")
        throw e // let coroutine cancellation propagate
    } catch (e: Exception) {
        Log.e(TAG, "castVote failed: type=$targetType, id=$targetId, value=$value", e)
        Result.failure(e)
    }
}