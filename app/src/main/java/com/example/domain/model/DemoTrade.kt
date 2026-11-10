package com.example.domain.model

enum class TradeSide {
    LONG,
    SHORT
}

enum class TradeStatus {
    OPEN,
    CLOSED_TP,
    CLOSED_SL,
    CLOSED_MANUAL
}

data class DemoTrade(
    val id: String,
    val signalId: String?,
    val pair: String,
    val side: TradeSide,
    val entryPrice: Double,
    val exitPrice: Double?,
    val leverage: Int,
    val marginUsdt: Double,
    val takeProfit: Double,
    val stopLoss: Double,
    val status: TradeStatus,
    val openedAt: Long,
    val closedAt: Long?,
    val realizedPnl: Double = 0.0
) {
    val positionSizeUsdt: Double
        get() = marginUsdt * leverage

    val liquidationPrice: Double
        get() {
            return if (side == TradeSide.LONG) {
                entryPrice * (1.0 - (0.9 / leverage))
            } else {
                entryPrice * (1.0 + (0.9 / leverage))
            }
        }

    fun calculateUnrealizedPnl(currentMarketPrice: Double): Double {
        val priceDiff = if (side == TradeSide.LONG) {
            currentMarketPrice - entryPrice
        } else {
            entryPrice - currentMarketPrice
        }
        val priceReturn = priceDiff / entryPrice
        return marginUsdt * leverage * priceReturn
    }

    fun calculateRoiPercentage(currentMarketPrice: Double): Double {
        val pnl = calculateUnrealizedPnl(currentMarketPrice)
        return (pnl / marginUsdt) * 100.0
    }
}
