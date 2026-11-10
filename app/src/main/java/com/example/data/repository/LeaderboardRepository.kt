package com.example.data.repository

import com.example.domain.model.LeaderboardUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface LeaderboardRepository {
    fun getLeaderboard(): Flow<List<LeaderboardUser>>
    suspend fun toggleFollow(userId: String)
}

class LeaderboardRepositoryImpl : LeaderboardRepository {

    private val _users = MutableStateFlow(
        listOf(
            LeaderboardUser(
                rank = 1,
                id = "usr-01",
                username = "Nexus_AI_Whale",
                badge = "AI Quant Alpha",
                winRate = 91.8,
                totalRoi = 642.4,
                totalSignals = 184,
                winningStreak = 14,
                profitSharingRatio = "10%",
                isFollowed = true
            ),
            LeaderboardUser(
                rank = 2,
                id = "usr-02",
                username = "CryptoOracle_V",
                badge = "Whale Tier Master",
                winRate = 88.5,
                totalRoi = 498.2,
                totalSignals = 142,
                winningStreak = 9,
                profitSharingRatio = "12%",
                isFollowed = false
            ),
            LeaderboardUser(
                rank = 3,
                id = "usr-03",
                username = "HyperScalp_X",
                badge = "Futures Sniper",
                winRate = 85.2,
                totalRoi = 412.0,
                totalSignals = 310,
                winningStreak = 7,
                profitSharingRatio = "8%",
                isFollowed = true
            ),
            LeaderboardUser(
                rank = 4,
                id = "usr-04",
                username = "SatoshiMatrix",
                badge = "Swing Macro God",
                winRate = 82.9,
                totalRoi = 328.6,
                totalSignals = 89,
                winningStreak = 5,
                profitSharingRatio = "15%",
                isFollowed = false
            ),
            LeaderboardUser(
                rank = 5,
                id = "usr-05",
                username = "AlphaPredator_99",
                badge = "High Leverage Pro",
                winRate = 79.4,
                totalRoi = 286.3,
                totalSignals = 215,
                winningStreak = 6,
                profitSharingRatio = "10%",
                isFollowed = false
            )
        )
    )

    override fun getLeaderboard(): Flow<List<LeaderboardUser>> = _users.asStateFlow()

    override suspend fun toggleFollow(userId: String) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                user.copy(isFollowed = !user.isFollowed)
            } else user
        }
    }
}
