package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.Notification
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

interface NotificationRepository {
    suspend fun getMyNotifications(): Result<List<Notification>>
    suspend fun markNotificationsRead(): Result<Unit>
}

class SupabaseNotificationRepository(
    private val supabase: SupabaseClient = SupabaseClientProvider.client
) : NotificationRepository {

    override suspend fun getMyNotifications(): Result<List<Notification>> = runCatching {
        supabase.postgrest.rpc("get_my_notifications").decodeList<Notification>()
    }

    override suspend fun markNotificationsRead(): Result<Unit> = runCatching {
        supabase.postgrest.rpc("mark_notifications_read")
        Unit
    }
}
