package edu.cs673.yikyak_clone.ui.screens.viewpost

import edu.cs673.yikyak_clone.data.model.Comment
import org.junit.Assert.assertEquals
import org.junit.Test

class CommentThreadingTest {

    private fun testComment(id: String, parentId: String? = null) = Comment(
        id = id,
        content = "Test comment",
        score = 0,
        createdAt = "2026-06-01T12:00:00Z",
        alias = "GreenOtter",
        parentId = parentId
    )

    @Test
    fun `top-level comments keep their order at depth 0`() {
        // Given three comments with no parent
        val comments = listOf(testComment("c1"), testComment("c2"), testComment("c3"))

        // When
        val result = threadComments(comments)

        // Then order is unchanged and all are top level
        assertEquals(listOf("c1", "c2", "c3"), result.map { it.comment.id })
        assertEquals(listOf(0, 0, 0), result.map { it.depth })
    }

    @Test
    fun `reply appears directly after its parent at depth 1`() {
        // Given a reply to c1 that comes last in the list
        val comments = listOf(
            testComment("c1"),
            testComment("c2"),
            testComment("r1", parentId = "c1")
        )

        // When
        val result = threadComments(comments)

        // Then the reply sits right under c1, before c2
        assertEquals(listOf("c1", "r1", "c2"), result.map { it.comment.id })
        assertEquals(listOf(0, 1, 0), result.map { it.depth })
    }

    @Test
    fun `nested reply chain gives increasing depths`() {
        // Given a chain a -> b -> c
        val comments = listOf(
            testComment("a"),
            testComment("b", parentId = "a"),
            testComment("c", parentId = "b")
        )

        // When
        val result = threadComments(comments)

        // Then each reply is one level deeper than its parent
        assertEquals(listOf("a", "b", "c"), result.map { it.comment.id })
        assertEquals(listOf(0, 1, 2), result.map { it.depth })
    }
}
