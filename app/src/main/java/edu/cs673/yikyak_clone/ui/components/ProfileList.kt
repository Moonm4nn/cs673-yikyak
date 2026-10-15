package edu.cs673.yikyak_clone.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

val supabase = createSupabaseClient(
    supabaseUrl = "https://elpbevyjrifdobkpbdex.supabase.co",
    supabaseKey = "sb_publishable_t3M58TkchkbB5L_4DFQF0A_8LRRtFbg"
) {
    install(Postgrest)
}

@Serializable
data class Profiles(
    val id: String,
    val karma: Int,
    val is_banned: Boolean,
    val created_at: String
)

@Composable
fun ProfileList() {
    var profiles by remember {mutableStateOf<List<Profiles>>(listOf())}
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                profiles = supabase.from("profiles")
                    .select().decodeList<Profiles>()
            } catch (e: Exception) {
                error = e.message
            }
        }
    }
    if (error != null) {
        Text(
            "Error loading profiles: $error",
            modifier = Modifier.padding(8.dp),
        )
        return
    }
    LazyColumn {
        items(
            profiles,
            key = { profile -> profile.id },
        ) { profile ->
            Text(
                profile.id,
                modifier = Modifier.padding(8.dp),
                color=Color.Black)

        }
    }
}