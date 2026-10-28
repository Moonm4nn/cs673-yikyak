package edu.cs673.yikyak_clone.data.repository

class FakeVoteRepository(
    private var result: Result<Unit> = Result.success(Unit)
) : VoteRepository {
    var createCallCount: Int = 0

    override suspend fun castVote(
        targetType: String,
        targetId: String,
        value: Int,
    ): Result<Unit> {
        createCallCount += 1
        return result
    }
}