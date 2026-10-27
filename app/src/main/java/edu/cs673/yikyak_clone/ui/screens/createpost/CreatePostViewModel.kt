package edu.cs673.yikyak_clone.ui.screens.createpost

import edu.cs673.yikyak_clone.data.repository.PostRepository
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch


//Holds UI state, input validation, and calls repository to create posts

private const val MAX_POST_LENGTH = 200
data class CreatePostUiState(
    val postContent: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val postCreated: Boolean = false
)

sealed interface CreatePostUiAction {
    data class UpdatePostContent(val input: String) : CreatePostUiAction
    data class UpdateLocation(val lat: Double, val lng: Double) : CreatePostUiAction
    data class CreatePost(val imageUri: Uri? = null) : CreatePostUiAction
}

class CreatePostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {
    var uiState by mutableStateOf(CreatePostUiState())
        private set

    fun onUiAction(action: CreatePostUiAction) {
        when(action) {

            is CreatePostUiAction.UpdateLocation -> {
                uiState = uiState.copy(
                    latitude = action.lat,
                    longitude = action.lng
                )
            }

            is CreatePostUiAction.UpdatePostContent -> {
                uiState = uiState.copy(
                    postContent = action.input,
                    errorMessage = null
                )
            }
            is CreatePostUiAction.CreatePost -> {
                val lat = uiState.latitude
                val lng = uiState.longitude

                if (lat == null || lng == null) {
                    uiState = uiState.copy(
                        errorMessage = "Location is required in order to post",
                        postCreated = false)
                    return
                }

                val content = uiState.postContent.trim()

                if (content.isEmpty()) {
                    uiState = uiState.copy(
                        errorMessage = "Post cannot be empty",
                        postCreated = false)
                    return
                }

                if (content.length > MAX_POST_LENGTH) {
                    uiState = uiState.copy(
                        errorMessage = "Post cannot exceed $MAX_POST_LENGTH characters",
                        postCreated = false)
                    return
                }

                viewModelScope.launch {
                    uiState = uiState.copy(
                        isLoading = true,
                        postCreated = false,
                    )

                    val result = postRepository.createPost(
                        postContent = uiState.postContent,
                        latitude = lat,
                        longitude = lng,
                        imageUri = action.imageUri
                    )

                    uiState = if (result.isSuccess) {
                        uiState.copy(
                            isLoading = false,
                            errorMessage = null,
                            postCreated = true)
                    } else {
                        uiState.copy(
                            isLoading = false,
                            postCreated = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to create post"
                        )
                    }
                }

            }
        }
    }
}

