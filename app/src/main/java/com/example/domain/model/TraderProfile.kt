package com.example.domain.model

data class TraderProfile(
    val username: String = "AlphaTrader_Pro",
    val accountTier: String = "VIP Elite Diamond",
    val virtualBalance: Double = 10000.0,
    val initialBalance: Double = 10000.0,
    val totalTrades: Int = 18,
    val winRate: Double = 83.3,
    val bestTradePnl: Double = 420.50,
    val bestTradeRoi: Double = 84.1,
    val totalPnl: Double = 1680.40
)

data class LeaderboardUser(
    val rank: Int,
    val id: String,
    val username: String,
    val badge: String,
    val winRate: Double,
    val totalRoi: Double,
    val totalSignals: Int,
    val winningStreak: Int,
    val profitSharingRatio: String,
    val isFollowed: Boolean = false
)
