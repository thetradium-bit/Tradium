package com.example.domain.trading

import com.example.domain.model.CandleData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

object TechnicalIndicators {

    /**
     * Relative Strength Index (RSI) using Wilder's smoothing technique.
     * Standard period: 14.
     */
    fun calculateRSI(candles: List<CandleData>, period: Int = 14): Double {
        if (candles.size <= period) return 50.0

        val closes = candles.map { it.close.toDouble() }
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until closes.size) {
            val change = closes[i] - closes[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        val rsi = 100.0 - (100.0 / (1.0 + rs))
        return (rsi * 100.0).toLong() / 100.0
    }

    /**
     * Moving Average Convergence Divergence (MACD)
     * Fast EMA: 12, Slow EMA: 26, Signal EMA: 9
     */
    data class MacdResult(
        val macdLine: Double,
        val signalLine: Double,
        val histogram: Double,
        val isBullishCross: Boolean
    )

    fun calculateMACD(
        candles: List<CandleData>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MacdResult {
        if (candles.size < slowPeriod + signalPeriod) {
            return MacdResult(0.0, 0.0, 0.0, false)
        }

        val closes = candles.map { it.close.toDouble() }
        val emaFast = calculateEMASeries(closes, fastPeriod)
        val emaSlow = calculateEMASeries(closes, slowPeriod)

        val macdSeries = mutableListOf<Double>()
        for (i in emaSlow.indices) {
            val fastIndex = i + (slowPeriod - fastPeriod)
            if (fastIndex in emaFast.indices) {
                macdSeries.add(emaFast[fastIndex] - emaSlow[i])
            }
        }

        val signalSeries = calculateEMASeries(macdSeries, signalPeriod)
        val latestMacd = macdSeries.lastOrNull() ?: 0.0
        val latestSignal = signalSeries.lastOrNull() ?: 0.0
        val histogram = latestMacd - latestSignal

        return MacdResult(
            macdLine = (latestMacd * 100.0).toLong() / 100.0,
            signalLine = (latestSignal * 100.0).toLong() / 100.0,
            histogram = (histogram * 100.0).toLong() / 100.0,
            isBullishCross = histogram > 0 && (macdSeries.getOrNull(macdSeries.size - 2) ?: 0.0) <= (signalSeries.getOrNull(signalSeries.size - 2) ?: 0.0)
        )
    }

    /**
     * Exponential Moving Average (EMA)
     */
    fun calculateEMA(values: List<Double>, period: Int): Double {
        if (values.size < period) return values.lastOrNull() ?: 0.0
        val k = 2.0 / (period + 1.0)
        var ema = values.take(period).average()
        for (i in period until values.size) {
            ema = (values[i] * k) + (ema * (1.0 - k))
        }
        return (ema * 100.0).toLong() / 100.0
    }

    private fun calculateEMASeries(values: List<Double>, period: Int): List<Double> {
        if (values.size < period) return emptyList()
        val result = mutableListOf<Double>()
        val k = 2.0 / (period + 1.0)
        var ema = values.take(period).average()
        result.add(ema)

        for (i in period until values.size) {
            ema = (values[i] * k) + (ema * (1.0 - k))
            result.add(ema)
        }
        return result
    }

    /**
     * Average True Range (ATR)
     */
    fun calculateATR(candles: List<CandleData>, period: Int = 14): Double {
        if (candles.size <= 1) return 0.0

        val trList = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val high = candles[i].high.toDouble()
            val low = candles[i].low.toDouble()
            val prevClose = candles[i - 1].close.toDouble()

            val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
            trList.add(tr)
        }

        if (trList.size < period) return trList.average()
        var atr = trList.take(period).average()
        for (i in period until trList.size) {
            atr = (atr * (period - 1) + trList[i]) / period
        }
        return (atr * 100.0).toLong() / 100.0
    }

    /**
     * Bollinger Bands
     */
    data class BollingerBandsResult(
        val upper: Double,
        val middle: Double,
        val lower: Double,
        val percentB: Double
    )

    fun calculateBollingerBands(candles: List<CandleData>, period: Int = 20, multiplier: Double = 2.0): BollingerBandsResult {
        if (candles.size < period) {
            val last = candles.lastOrNull()?.close?.toDouble() ?: 0.0
            return BollingerBandsResult(last, last, last, 0.5)
        }

        val subset = candles.takeLast(period).map { it.close.toDouble() }
        val sma = subset.average()
        val variance = subset.map { (it - sma) * (it - sma) }.average()
        val stdDev = sqrt(variance)

        val upper = sma + (multiplier * stdDev)
        val lower = sma - (multiplier * stdDev)
        val current = subset.last()
        val percentB = if (upper != lower) (current - lower) / (upper - lower) else 0.5

        return BollingerBandsResult(
            upper = (upper * 100.0).toLong() / 100.0,
            middle = (sma * 100.0).toLong() / 100.0,
            lower = (lower * 100.0).toLong() / 100.0,
            percentB = (percentB * 100.0).toLong() / 100.0
        )
    }
}
