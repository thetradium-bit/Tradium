package com.example.domain.trading

import com.example.domain.model.TradeSide
import java.math.BigDecimal
import java.math.RoundingMode

object TradingFormulas {

    /**
     * Spot P&L:
     * Buy cost = entryPrice * amount
     * Sell revenue = exitPrice * amount
     * Net P&L = (exitPrice - entryPrice) * amount - fee
     */
    fun calculateSpotPnl(
        entryPrice: Double,
        exitPrice: Double,
        amount: Double,
        feeRate: Double = 0.001 // 0.1% standard Binance fee
    ): Double {
        val entry = BigDecimal.valueOf(entryPrice)
        val exit = BigDecimal.valueOf(exitPrice)
        val qty = BigDecimal.valueOf(amount)
        val feePercent = BigDecimal.valueOf(feeRate)

        val grossPnl = (exit.subtract(entry)).multiply(qty)
        val entryFee = entry.multiply(qty).multiply(feePercent)
        val exitFee = exit.multiply(qty).multiply(feePercent)
        val netPnl = grossPnl.subtract(entryFee).subtract(exitFee)

        return netPnl.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Futures Isolated Margin Unrealized & Realized P&L:
     * Position Size (Notional) = marginUsdt * leverage
     * Base Asset Quantity = (marginUsdt * leverage) / entryPrice
     *
     * LONG:
     * PnL = Quantity * (currentPrice - entryPrice)
     *
     * SHORT:
     * PnL = Quantity * (entryPrice - currentPrice)
     */
    fun calculateFuturesPnl(
        side: TradeSide,
        entryPrice: Double,
        currentPrice: Double,
        leverage: Int,
        marginUsdt: Double,
        takerFeeRate: Double = 0.0005 // 0.05% standard futures fee
    ): Double {
        if (entryPrice <= 0.0 || currentPrice <= 0.0 || leverage <= 0 || marginUsdt <= 0.0) return 0.0

        val entry = BigDecimal.valueOf(entryPrice)
        val current = BigDecimal.valueOf(currentPrice)
        val margin = BigDecimal.valueOf(marginUsdt)
        val lev = BigDecimal.valueOf(leverage.toLong())

        val notional = margin.multiply(lev)
        val quantity = notional.divide(entry, 8, RoundingMode.HALF_UP)

        val priceDiff = if (side == TradeSide.LONG) {
            current.subtract(entry)
        } else {
            entry.subtract(current)
        }

        val grossPnl = quantity.multiply(priceDiff)
        val entryFee = notional.multiply(BigDecimal.valueOf(takerFeeRate))
        val exitFee = quantity.multiply(current).multiply(BigDecimal.valueOf(takerFeeRate))
        val netPnl = grossPnl.subtract(entryFee).subtract(exitFee)

        return netPnl.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Estimated Liquidation Price for Isolated Margin:
     * Maintenance Margin Rate (MMR) ~ 0.5% for major crypto pairs (Binance Tier 1).
     *
     * LONG Liq Price = entryPrice * (1 - (1 / leverage) + MMR)
     * SHORT Liq Price = entryPrice * (1 + (1 / leverage) - MMR)
     */
    fun calculateLiquidationPrice(
        side: TradeSide,
        entryPrice: Double,
        leverage: Int,
        maintenanceMarginRate: Double = 0.005
    ): Double {
        if (entryPrice <= 0.0 || leverage <= 0) return 0.0
        val entry = BigDecimal.valueOf(entryPrice)
        val levFraction = BigDecimal.ONE.divide(BigDecimal.valueOf(leverage.toLong()), 6, RoundingMode.HALF_UP)
        val mmr = BigDecimal.valueOf(maintenanceMarginRate)

        val liqPrice = if (side == TradeSide.LONG) {
            val factor = BigDecimal.ONE.subtract(levFraction).add(mmr)
            entry.multiply(factor)
        } else {
            val factor = BigDecimal.ONE.add(levFraction).subtract(mmr)
            entry.multiply(factor)
        }

        return liqPrice.setScale(2, RoundingMode.HALF_UP).toDouble().coerceAtLeast(0.0)
    }
}
