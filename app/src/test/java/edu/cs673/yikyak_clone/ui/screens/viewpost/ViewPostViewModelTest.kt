package edu.cs673.yikyak_clone.ui.screens.viewpost

import edu.cs673.yikyak_clone.data.model.Comment
import edu.cs673.yikyak_clone.data.model.SocialPost
import edu.cs673.yikyak_clone.data.repository.FakeCommentRepository
import edu.cs673.yikyak_clone.data.repository.FakePostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewPostViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testPost(id: String = "post-1") = SocialPost(
        id = id,
        user_id = "user-1",
        content = "Test post",
        latitude = 0.0,
        longitude = 0.0,
        userVote = 0,
        score = 5,
        comment_count = 0,
        is_removed = false,
        created_at = Instant.parse("2026-06-01T12:00:00Z")
    )

    private fun testComment(id: String = "comment-1", parentId: String? = null) = Comment(
        id = id,
        content = "Test comment",
        score = 0,
        createdAt = "2026-06-01T12:00:00Z",
        alias = "GreenOtter",
        parentId = parentId
    )

    private fun createViewModel(
        postId: String = "post-1",
        commentRepository: FakeCommentRepository = FakeCommentRepository()
    ): ViewPostViewModel {
        val postRepository = FakePostRepository(posts = listOf(testPost(id = postId)))
        return ViewPostViewModel(postId, postRepository, commentRepository)
    }


    @Test
    fun `onSendClick - sends selected post id and trimmed comment text`() {
        val commentRepository = FakeCommentRepository()
        val viewModel = createViewModel(postId = "post-2", commentRepository = commentRepository)

        viewModel.onCommentTextChange("  Great post!  ")
        viewModel.onSendClick()

        assertEquals(1, commentRepository.createCallCount)
        assertEquals("post-2", commentRepository.lastPostId)
        assertEquals("Great post!", commentRepository.lastContent)
        assertNull(commentRepository.lastParentId)
        assertEquals("", viewModel.uiState.commentText)
    }

    @Test
    fun `reply sends parent comment id and clears reply state`() {
        val parent = testComment(id = "comment-7")
        val commentRepository = FakeCommentRepository(comments = listOf(parent))
        val viewModel = createViewModel(commentRepository = commentRepository)

        viewModel.onReplyClick(parent)
        viewModel.onCommentTextChange("Agreed")
        viewModel.onSendClick()

        assertEquals("comment-7", commentRepository.lastParentId)
        assertNull(viewModel.uiState.replyingTo)
    }

    @Test
    fun `onSendClick - failure keeps text and shows error`() {
        val commentRepository = FakeCommentRepository(
            createResult = Result.failure(Exception("Network error"))
        )
        val viewModel = createViewModel(commentRepository = commentRepository)

        viewModel.onCommentTextChange("Hello")
        viewModel.onSendClick()

        assertEquals("Hello", viewModel.uiState.commentText)
        assertEquals("Couldn't post comment", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `init - loads comments for the selected post`() {
        val comments = listOf(testComment(id = "c1"), testComment(id = "c2"))
        val commentRepository = FakeCommentRepository(comments = comments)

        val viewModel = createViewModel(postId = "post-2", commentRepository = commentRepository)

        assertEquals("post-2", commentRepository.lastGetCommentsPostId)
        assertEquals(2, viewModel.uiState.comments.size)
    }


}