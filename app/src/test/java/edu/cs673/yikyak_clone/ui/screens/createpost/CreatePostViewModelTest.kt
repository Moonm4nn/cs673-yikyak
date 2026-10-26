package edu.cs673.yikyak_clone.ui.screens.createpost

import edu.cs673.yikyak_clone.data.repository.FakePostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePostViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank content shows validation error and does not call repository`() = runTest {
        val repository = FakePostRepository()
        val viewModel = CreatePostViewModel(repository)

        viewModel.onUiAction(CreatePostUiAction.UpdatePostContent("   "))
        viewModel.onUiAction(CreatePostUiAction.CreatePost())

        assertEquals("Post cannot be empty", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `valid content calls repository and marks post created`() = runTest {
        val repository = FakePostRepository()
        val viewModel = CreatePostViewModel(repository)

        viewModel.onUiAction(CreatePostUiAction.UpdatePostContent("Testing testing"))
        viewModel.onUiAction(CreatePostUiAction.CreatePost())

        advanceUntilIdle()

        assertEquals("Testing testing", repository.lastPostContent)
        assertFalse(viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.postCreated)
        assertEquals(1, repository.createCallCount)
    }

    @Test
    fun `too long content shows validation error`() = runTest {
        val repository = FakePostRepository()
        val viewModel = CreatePostViewModel(repository)

        val tooLongText = "a".repeat(201)
        viewModel.onUiAction(CreatePostUiAction.UpdatePostContent(tooLongText))
        viewModel.onUiAction(CreatePostUiAction.CreatePost())

        assertEquals("Post cannot exceed 200 characters", viewModel.uiState.errorMessage)
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `repository failure shows error message`() = runTest {
        val repository = FakePostRepository(
            result = Result.failure(Exception("Network error"))
        )
        val viewModel = CreatePostViewModel(repository)

        viewModel.onUiAction(CreatePostUiAction.UpdatePostContent("Test post"))
        viewModel.onUiAction(CreatePostUiAction.CreatePost())

        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.errorMessage)
        assertFalse(viewModel.uiState.isLoading)
        assertFalse(viewModel.uiState.postCreated)
        assertEquals(1, repository.createCallCount)
    }
}