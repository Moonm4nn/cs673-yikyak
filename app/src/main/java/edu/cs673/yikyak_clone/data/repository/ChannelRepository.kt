package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.Channel
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

interface ChannelRepository {
    suspend fun getChannels(): Result<List<Channel>>
}

class SupabaseChannelRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : ChannelRepository {

    override suspend fun getChannels(): Result<List<Channel>> = runCatching {
        supabase.postgrest.rpc("get_channels").decodeList<Channel>()
    }
}