package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.lifecycle.ViewModel
import edu.cs673.yikyak_clone.data.model.SocialPost
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import kotlinx.coroutines.launch

// Values for the UI state of the feed screen.
data class FeedUiState(
    val posts: List<SocialPost> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)

// Create interface for feed screen actions
sealed interface FeedUiAction {
    data class UpdateLocation(val lat: Double, val lng: Double) : FeedUiAction
    object LoadNextPage : FeedUiAction
    object RefreshFeed : FeedUiAction
}

class FeedViewModel(
    private val repository: SupabasePostRepository
) : ViewModel() {

    var uiState by mutableStateOf(FeedUiState())
        private set

    // Values for pagination
    private val pageSize = 15
    private var currPage = 0
    private var isFetchLocked = false

    // Create a Supabase client
    private val supabaseClient = SupabaseClientProvider.client

    // Post ids with a vote request currently in flight (ignore rapid repeat taps)
    private val votesInFlight = mutableSetOf<String>()


    //TODO: reconcile and implement methods below once rest of FeedViewModel is complete

    fun onVote(postId: String, tapped: Int) {

//        val post = posts.value.firstOrNull { it.id == postId } ?: return
//        if (!votesInFlight.add(postId)) return
//
//        // Tapping the same arrow again removes the vote
//        val newVote = if (post.userVote == tapped) 0 else tapped
//        val delta = newVote - post.userVote
//
//
//        updatePost(postId) { it.copy(userVote = newVote, score = it.score + delta) }
//
//        viewModelScope.launch {
//            try {
//                voteRepository.castVote("post", postId, newVote)
//                    .onFailure { e ->
//                        // Roll back to what it was before this tap
//                        updatePost(postId) { it.copy(userVote = post.userVote, score = post.score) }
//                        _errorMessage.value = "Couldn't register your vote. Please try again."
//                      }
//            } finally {
//                votesInFlight.remove(postId)
//            }
//        }
    }

    private fun updatePost(postId: String, transform: (SocialPost) -> SocialPost) {
//        posts.update { list -> list.map { if (it.id == postId) transform(it) else it } }
    }

    // Handle UI actions
    fun onUiAction(action: FeedUiAction) {
        when (action) {
            is FeedUiAction.UpdateLocation -> {
                //  Copy latitude and longitude if updated and not null
                if (uiState.latitude == null || uiState.longitude == null) {
                    uiState = uiState.copy(
                        latitude = action.lat,
                        longitude = action.lng,
                        errorMessage = null
                    )

                    currPage = 0
                    loadPosts(isRefresh = false, isNextPage = false)
                }
            }
            is FeedUiAction.RefreshFeed -> loadPosts(isRefresh = true, isNextPage = false)
            is FeedUiAction.LoadNextPage -> loadPosts(isRefresh = false, isNextPage = true)
        }
    }

    // Load posts from the repository
    private fun loadPosts(isRefresh: Boolean, isNextPage: Boolean) {
        // Ignore if no location available
        val latitude = uiState.latitude ?: return
        val longitude = uiState.longitude ?: return

        // Ignore if already loading
        if (isFetchLocked || (isNextPage && uiState.endReached)) return

        // Lock to prevent multiple loads at once
        isFetchLocked = true

        // Set loading state
        if (!isNextPage) {
            uiState = uiState.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                errorMessage = null
            )
        }

        // Reset pagination values
        val targetPage = if (isNextPage) currPage + 1 else 0

        viewModelScope.launch {

            // Attempt to load posts from the repository
            try {
                // Use the repository to load posts
                val newPosts = repository.getPosts(
                    pageNum = targetPage,
                    pageSize = pageSize,
                    latitude = latitude,
                    longitude = longitude
                )

                currPage = targetPage
                uiState = uiState.copy(
                    posts = if (isNextPage) uiState.posts + newPosts else newPosts,
                    endReached = newPosts.size < pageSize,
                    isLoading = false,
                    isRefreshing = false
                )

            // Handle errors
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = e.localizedMessage ?: "Failed to load posts",
                    isLoading = false,
                    isRefreshing = false
                )

            // Handle cancellation
            } finally {
                isFetchLocked = false
            }
        }
    }
}
