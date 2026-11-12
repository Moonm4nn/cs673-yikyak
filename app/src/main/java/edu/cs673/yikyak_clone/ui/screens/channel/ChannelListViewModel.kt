package edu.cs673.yikyak_clone.ui.screens.channel   // use whatever package your file is already in

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.model.Channel
import edu.cs673.yikyak_clone.data.repository.ChannelRepository
import kotlinx.coroutines.launch

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChannelListViewModel(
    private val channelRepository: ChannelRepository
) : ViewModel() {
    var uiState by mutableStateOf(ChannelListUiState())
        private set

    init {
        loadChannels()
    }

    fun loadChannels() {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            channelRepository.getChannels()
                .onSuccess { channels ->
                    uiState = uiState.copy(channels = channels, isLoading = false)
                }
                .onFailure {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Couldn't load channels"
                    )
                }
        }
    }
}