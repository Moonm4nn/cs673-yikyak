package edu.cs673.yikyak_clone.ui.screens.feed

import androidx.lifecycle.ViewModel
import edu.cs673.yikyak_clone.data.model.SocialPost
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.repository.PostRepository
import edu.cs673.yikyak_clone.data.repository.VoteRepository
import edu.cs673.yikyak_clone.data.remote.SupabaseClientProvider
import android.util.Log
import kotlinx.coroutines.launch

// Values for the UI state of the feed screen.

private const val REMOVAL_SCORE_THRESHOLD = -3
data class FeedUiState(
    val posts: List<SocialPost> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sortByHot: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)

// Create interface for feed screen actions
sealed interface FeedUiAction {
    data class UpdateLocation(val lat: Double, val lng: Double) : FeedUiAction
    data class SetSortByHot(val isHot: Boolean) : FeedUiAction
    object LoadNextPage : FeedUiAction
    object RefreshFeed : FeedUiAction
}

class FeedViewModel(
    private val repository: PostRepository,
    private val voteRepository: VoteRepository
) : ViewModel() {

    private val TAG = "FeedViewModel"
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

    fun onVote(postId: String, tapped: Int) {
        val post = uiState.posts.firstOrNull { it.id == postId } ?: return
        if (!votesInFlight.add(postId)) return

        // Tapping the same arrow again removes the vote
        val newVote = if (post.userVote == tapped) 0 else tapped
        val delta = newVote - post.userVote

        Log.d(TAG, "onVote: $postId, $newVote")

        updatePost(postId) { it.copy(userVote = newVote, score = it.score + delta) }

        viewModelScope.launch {
            try {
                voteRepository.castVote("post", postId, newVote)
                    .onSuccess {
                        uiState = uiState.copy(
                            posts = uiState.posts.filterNot { it.id == postId && it.score <= REMOVAL_SCORE_THRESHOLD }
                        )
                    }
                    .onFailure { e ->
                        // Roll back to what it was before this tap
                        updatePost(postId) { it.copy(userVote = post.userVote, score = post.score) }
                        uiState = uiState.copy(
                            errorMessage = "Couldn't register your vote. Please try again."
                        )
                      }
            } finally {
                votesInFlight.remove(postId)
            }
        }
    }

    private fun updatePost(postId: String, transform: (SocialPost) -> SocialPost) {
        Log.d(TAG, "updatePost: $postId")
        uiState = uiState.copy(
            posts = uiState.posts.map { if (it.id == postId) transform(it) else it }
        )
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
            is FeedUiAction.SetSortByHot -> {
                //  Copy sort order if updated
                if (uiState.sortByHot != action.isHot) {
                    uiState = uiState.copy(
                        sortByHot = action.isHot,
                        posts = emptyList(),
                        endReached = false,
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
        val sortByHot = uiState.sortByHot

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
                    longitude = longitude,
                    sortByHot = sortByHot
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
