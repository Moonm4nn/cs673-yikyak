package edu.cs673.yikyak_clone.data.repository

class FakeVoteRepository(
    private var result: Result<Unit> = Result.success(Unit)
) : VoteRepository {
    var callCount: Int = 0

    override suspend fun castVote(
        targetType: String,
        targetId: String,
        value: Int,
    ): Result<Unit> {
        callCount += 1
        return result
    }
}