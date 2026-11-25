package edu.cs673.yikyak_clone.ui.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.model.Notification
import edu.cs673.yikyak_clone.data.repository.NotificationRepository
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    var uiState by mutableStateOf(NotificationsUiState())
        private set

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            notificationRepository.getMyNotifications()
                .onSuccess { notifications ->
                    uiState = uiState.copy(notifications = notifications, isLoading = false)
                    notificationRepository.markNotificationsRead()
                }
                .onFailure {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Couldn't load notifications",
                    )
                }
        }
    }
}
