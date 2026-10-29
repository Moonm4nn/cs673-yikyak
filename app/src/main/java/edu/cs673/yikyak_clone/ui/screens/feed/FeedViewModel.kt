package edu.cs673.yikyak_clone.ui.screens.feed
import androidx.lifecycle.ViewModel
import edu.cs673.yikyak_clone.data.model.SocialPost


class FeedViewModel(
) : ViewModel() {

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
}