package edu.cs673.yikyak_clone.ui.screens.viewpost

import edu.cs673.yikyak_clone.data.model.Comment

data class ThreadedComment(
    val comment: Comment,
    val depth: Int,
)

fun threadComments(comments: List<Comment>): List<ThreadedComment> {
    val threadedComments = mutableListOf<ThreadedComment>()
    val topLevelComments = comments.filter { it.parentId == null }
    for (comment in topLevelComments) {
        addWithReplies(comment, depth = 0, allComments = comments, result = threadedComments)
    }

    return threadedComments
}

private fun addWithReplies(
    comment: Comment,
    depth: Int,
    allComments: List<Comment>,
    result: MutableList<ThreadedComment>,
) {
    result.add(ThreadedComment(comment, depth))
    val directReplies = allComments.filter { it.parentId == comment.id }
    for (reply in directReplies) {
        addWithReplies(reply, depth + 1, allComments, result)
    }
}

