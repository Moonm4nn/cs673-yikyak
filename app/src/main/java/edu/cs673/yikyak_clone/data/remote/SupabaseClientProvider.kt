package edu.cs673.yikyak_clone.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://elpbevyjrifdobkpbdex.supabase.co",
        supabaseKey = "sb_publishable_t3M58TkchkbB5L_4DFQF0A_8LRRtFbg"
    ) {
        install(Postgrest.Companion)
    }
}