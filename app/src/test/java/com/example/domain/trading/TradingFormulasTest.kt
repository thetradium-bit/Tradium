package com.example.domain.trading

import com.example.domain.model.CandleData
import com.example.domain.model.TradeSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingFormulasTest {

    @Test
    fun testSpotPnlCalculation() {
        // Bought 1 BTC at $90,000, sold at $95,000 with 0.1% fee
        // Gross = $5,000, Buy fee = $90, Sell fee = $95, Net = $4,815
        val netPnl = TradingFormulas.calculateSpotPnl(
            entryPrice = 90000.0,
            exitPrice = 95000.0,
            amount = 1.0,
            feeRate = 0.001
        )
        assertEquals(4815.0, netPnl, 0.01)
    }

    @Test
    fun testFuturesLongPnlCalculation() {
        // Long BTC with $1,000 margin at 10x leverage ($10,000 notional, ~0.1 BTC at $100k)
        // Entry $100,000, Exit $105,000 (+5%), Gross = $500
        val pnl = TradingFormulas.calculateFuturesPnl(
            side = TradeSide.LONG,
            entryPrice = 100000.0,
            currentPrice = 105000.0,
            leverage = 10,
            marginUsdt = 1000.0,
            takerFeeRate = 0.0005
        )
        // Gross $500 - $5 entry fee - $5.25 exit fee = ~$489.75
        assertTrue(pnl > 485.0 && pnl < 495.0)
    }

    @Test
    fun testFuturesShortPnlCalculation() {
        // Short BTC with $1,000 margin at 10x leverage
        // Entry $100,000, Exit $95,000 (price dropped 5%), Profit ~ $500 minus fees
        val pnl = TradingFormulas.calculateFuturesPnl(
            side = TradeSide.SHORT,
            entryPrice = 100000.0,
            currentPrice = 95000.0,
            leverage = 10,
            marginUsdt = 1000.0,
            takerFeeRate = 0.0005
        )
        assertTrue(pnl > 485.0 && pnl < 495.0)
    }

    @Test
    fun testLiquidationPriceIsolatedLong() {
        // Long entry at $100,000 with 10x leverage, MMR 0.5%
        // Liq price should be approximately $90,500
        val liq = TradingFormulas.calculateLiquidationPrice(
            side = TradeSide.LONG,
            entryPrice = 100000.0,
            leverage = 10,
            maintenanceMarginRate = 0.005
        )
        assertEquals(90500.0, liq, 50.0)
    }

    @Test
    fun testRsiCalculations() {
        // 20 candles with upward trend should yield RSI > 60
        val upwardCandles = (1..25).map { i ->
            CandleData(
                timestamp = i.toLong(),
                open = (100 + i * 2).toFloat(),
                high = (105 + i * 2).toFloat(),
                low = (98 + i * 2).toFloat(),
                close = (104 + i * 2).toFloat(),
                volume = 100f
            )
        }
        val rsi = TechnicalIndicators.calculateRSI(upwardCandles, 14)
        assertTrue("RSI should reflect bullish momentum (got $rsi)", rsi > 60.0)
    }
}
