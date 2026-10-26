package edu.cs673.yikyak_clone.data.remote

import edu.cs673.yikyak_clone.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.minimalSettings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

val httpClient = HttpClient(CIO)
object SupabaseClientProviderTest {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        httpEngine = CIO.create()
        install(Postgrest.Companion)
        install(Auth){
            minimalSettings()
        }
    }
}