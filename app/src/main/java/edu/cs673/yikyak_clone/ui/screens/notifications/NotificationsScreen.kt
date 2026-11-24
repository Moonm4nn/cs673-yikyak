package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import edu.cs673.yikyak_clone.data.repository.SupabaseNotificationRepository

@Composable
fun NotificationsScreen() {
    val viewModel = remember {
        NotificationsViewModel(
            SupabaseNotificationRepository(),
        )
    }
    val uiState = viewModel.uiState
    Text(text = "Notifications: ${uiState.notifications.size}")
}
