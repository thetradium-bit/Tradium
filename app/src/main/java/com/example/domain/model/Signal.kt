package com.example.domain.model

enum class SignalType {
    LONG,
    SHORT,
    SPOT_BUY
}

enum class SignalStatus {
    ACTIVE,
    TP1_HIT,
    TP2_HIT,
    TP3_HIT,
    SL_HIT,
    CLOSED
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class Signal(
    val id: String,
    val pair: String,              // e.g. "BTC/USDT", "ETH/USDT", "SOL/USDT"
    val assetSymbol: String,       // e.g. "BTC"
    val type: SignalType,
    val status: SignalStatus,
    val entryPrice: Double,
    val currentPrice: Double,
    val targetPrice1: Double,
    val targetPrice2: Double,
    val targetPrice3: Double,
    val stopLoss: Double,
    val leverage: String,          // e.g. "10x", "5x", "Spot"
    val timeframe: String,         // e.g. "15m", "1h", "4h", "1D"
    val confidenceScore: Int,      // e.g. 94 (percentage)
    val riskLevel: RiskLevel,
    val aiReasoning: String,
    val technicalIndicators: List<String>,
    val timestamp: Long,
    val author: String = "Neural AI v4.2"
) {
    val profitPercentage: Double
        get() {
            return when (type) {
                SignalType.LONG, SignalType.SPOT_BUY -> {
                    ((currentPrice - entryPrice) / entryPrice) * 100.0
                }
                SignalType.SHORT -> {
                    ((entryPrice - currentPrice) / entryPrice) * 100.0
                }
            }
        }

    val riskRewardRatio: Double
        get() {
            val risk = kotlin.math.abs(entryPrice - stopLoss)
            val reward = kotlin.math.abs(targetPrice2 - entryPrice)
            return if (risk > 0) (reward / risk) else 1.0
        }
}
