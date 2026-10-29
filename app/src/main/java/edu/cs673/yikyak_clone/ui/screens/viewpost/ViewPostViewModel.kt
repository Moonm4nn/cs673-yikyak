package edu.cs673.yikyak_clone.ui.screens.viewpost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs673.yikyak_clone.data.model.Comment
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.data.repository.CommentRepository
import edu.cs673.yikyak_clone.data.repository.PostRepository
import kotlinx.coroutines.launch

data class ViewPostUiState(
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val replyingTo: Comment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val post: SocialPost? = null,
)

class ViewPostViewModel(
    private val postId: String,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) : ViewModel() {
    var uiState by mutableStateOf(ViewPostUiState())
        private set

    init {
        loadPost()
        loadComments()
    }

    private fun loadComments() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            commentRepository.getComments(postId)
                .onSuccess { comments ->
                    uiState = uiState.copy(comments = comments, isLoading = false)
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Couldn't load comments")
                }
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPost(postId)
                .onSuccess { post ->
                    uiState = uiState.copy(post = post)
                }
                .onFailure {
                    uiState = uiState.copy(errorMessage = "Couldn't load post")
                }
        }
    }

    fun onCommentTextChange(text: String) {
        uiState = uiState.copy(commentText = text)
    }

    fun onReplyClick(comment: Comment) {
        uiState = uiState.copy(replyingTo = comment)
    }

    fun onCancelReply() {
        uiState = uiState.copy(replyingTo = null)
    }

    fun onSendClick() {
        val content = uiState.commentText.trim()

        if (content.isEmpty()) return

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            commentRepository.createComment(postId, content, uiState.replyingTo?.id)
                .onSuccess {
                    uiState = uiState.copy(commentText = "", replyingTo = null)
                    loadPost()
                    loadComments()
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Couldn't post comment")
                }
        }
    }

}