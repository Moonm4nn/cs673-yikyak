package edu.cs673.yikyak_clone.data.repository

import edu.cs673.yikyak_clone.data.model.Comment

class FakeCommentRepository(
    private val comments: List<Comment> = emptyList(),
    private val createResult: Result<Unit> = Result.success(Unit)
) : CommentRepository {
    var getCommentsCallCount = 0
    var lastGetCommentsPostId: String? = null
    var createCallCount = 0
    var lastPostId: String? = null
    var lastContent: String? = null
    var lastParentId: String? = null

    override suspend fun getComments(postId: String): Result<List<Comment>> {
        getCommentsCallCount++
        lastGetCommentsPostId = postId
        return Result.success(comments)
    }

    override suspend fun createComment(
        postId: String,
        content: String,
        parentId: String?
    ): Result<Unit> {
        createCallCount++
        lastPostId = postId
        lastContent = content
        lastParentId = parentId
        return createResult
    }
}