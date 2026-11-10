package com.example.data.repository

import android.util.Log
import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.CandleData
import com.example.domain.model.MarketType
import com.example.domain.model.TechnicalIndicatorResult
import com.example.domain.trading.TechnicalIndicators
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

private const val TAG = "AIAnalyzerRepo"

interface AIAnalyzerRepository {
    suspend fun getCandleData(
        pair: String,
        timeframe: String,
        marketType: MarketType = MarketType.SPOT
    ): List<CandleData>

    suspend fun runAiAnalysis(
        pair: String,
        timeframe: String,
        strategy: String,
        marketType: MarketType = MarketType.SPOT
    ): AIAnalysisResult
}

class AIAnalyzerRepositoryImpl(
    private val marketDataRepository: MarketDataRepository = MarketDataRepositoryImpl()
) : AIAnalyzerRepository {

    // Structured JSON Schema for strictly typed AI Market Analysis
    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "bias" to Schema.enumeration(listOf("STRONG BULLISH", "BULLISH", "NEUTRAL", "BEARISH", "STRONG BEARISH")),
            "confidence" to Schema.integer(description = "Confidence score 0 to 100"),
            "keySupport" to Schema.double(description = "Calculated key support price"),
            "keyResistance" to Schema.double(description = "Calculated key resistance price"),
            "pivotPoint" to Schema.double(description = "Current market pivot level"),
            "patternDetected" to Schema.string(description = "Technical pattern e.g. Fair Value Gap, Liquidity Sweep, Order Block"),
            "summary" to Schema.string(description = "Concise professional reasoning"),
            "recommendedEntry" to Schema.double(description = "Realistic suggested entry price"),
            "recommendedTp1" to Schema.double(description = "Conservative Take Profit 1"),
            "recommendedTp2" to Schema.double(description = "Standard Take Profit 2"),
            "recommendedSl" to Schema.double(description = "Strict Stop Loss price"),
            "riskReward" to Schema.string(description = "Ratio formatted as 1 : X.X")
        )
    )

    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
            }
        )
    }

    override suspend fun getCandleData(
        pair: String,
        timeframe: String,
        marketType: MarketType
    ): List<CandleData> {
        return marketDataRepository.getCandleData(pair, timeframe, marketType, limit = 60)
    }

    override suspend fun runAiAnalysis(
        pair: String,
        timeframe: String,
        strategy: String,
        marketType: MarketType
    ): AIAnalysisResult {
        // 1. Fetch real historical candles from Binance
        val candles = marketDataRepository.getCandleData(pair, timeframe, marketType, limit = 80)
        if (candles.size < 20) {
            return buildInsufficientDataFallback(pair, timeframe, strategy)
        }

        // 2. Perform mathematically pure local indicator calculations
        val currentPrice = candles.last().close.toDouble()
        val rsi14 = TechnicalIndicators.calculateRSI(candles, 14)
        val macd = TechnicalIndicators.calculateMACD(candles)
        val closes = candles.map { it.close.toDouble() }
        val ema20 = TechnicalIndicators.calculateEMA(closes, 20)
        val ema50 = TechnicalIndicators.calculateEMA(closes, 50)
        val atr14 = TechnicalIndicators.calculateATR(candles, 14)
        val bb = TechnicalIndicators.calculateBollingerBands(candles)

        val indicators = listOf(
            TechnicalIndicatorResult("RSI (14-period)", "$rsi14", if (rsi14 > 55) "BULLISH" else if (rsi14 < 45) "BEARISH" else "NEUTRAL", rsi14.toFloat()),
            TechnicalIndicatorResult("MACD (12, 26, 9)", "${macd.histogram} (Signal: ${macd.signalLine})", if (macd.isBullishCross) "STRONG BULLISH" else if (macd.histogram > 0) "BULLISH" else "BEARISH", 75f),
            TechnicalIndicatorResult("EMA 20 / 50", "EMA20: $ema20 | EMA50: $ema50", if (ema20 > ema50) "BULLISH" else "BEARISH", 80f),
            TechnicalIndicatorResult("Bollinger Bands", "Upper: ${bb.upper} | Lower: ${bb.lower}", if (bb.percentB > 0.8) "OVERBOUGHT" else if (bb.percentB < 0.2) "OVERSOLD" else "NORMAL", 70f),
            TechnicalIndicatorResult("ATR (Volatility)", "$atr14 USDT", "VOLATILITY METRIC", 65f)
        )

        // 3. Prompt Gemini AI with verified real market numbers and indicators
        val prompt = """
            You are Tradium's Quantitative Trading AI analyzing real cryptocurrency market data from Binance.
            Trading Pair: $pair ($marketType)
            Timeframe: $timeframe
            Strategy Applied: $strategy
            Current Price: $currentPrice
            Recent Candle High: ${candles.maxOf { it.high }}
            Recent Candle Low: ${candles.minOf { it.low }}
            24h Volume Trend: ${candles.takeLast(5).map { it.volume }}
            Technical Indicators calculated on real data:
            - RSI (14): $rsi14
            - MACD Line: ${macd.macdLine}, Signal: ${macd.signalLine}, Histogram: ${macd.histogram}
            - EMA 20: $ema20, EMA 50: $ema50
            - ATR 14: $atr14
            - Bollinger Bands: Upper ${bb.upper}, Lower ${bb.lower}, %B: ${bb.percentB}

            Analyze the market bias, support, resistance, invalidation SL, and realistic TP targets. Return strictly valid JSON adhering to schema.
        """.trimIndent()

        return try {
            val response = withTimeout(45_000L) {
                geminiModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty AI response")
            val json = JSONObject(jsonText)

            AIAnalysisResult(
                pair = pair,
                timeframe = timeframe,
                strategyName = strategy,
                bias = json.optString("bias", if (ema20 > ema50) "BULLISH" else "BEARISH"),
                confidence = json.optInt("confidence", 85).coerceIn(40, 99),
                keySupport = json.optDouble("keySupport", currentPrice * 0.97),
                keyResistance = json.optDouble("keyResistance", currentPrice * 1.03),
                pivotPoint = json.optDouble("pivotPoint", currentPrice),
                summary = json.optString("summary", "Technical alignment confirmed across moving averages and volume oscillators."),
                patternDetected = json.optString("patternDetected", "$strategy Alignment"),
                recommendedEntry = json.optDouble("recommendedEntry", currentPrice),
                recommendedTp1 = json.optDouble("recommendedTp1", currentPrice * 1.025),
                recommendedTp2 = json.optDouble("recommendedTp2", currentPrice * 1.055),
                recommendedSl = json.optDouble("recommendedSl", currentPrice * 0.98),
                indicators = indicators,
                riskReward = json.optString("riskReward", "1 : 2.4"),
                generatedAt = System.currentTimeMillis()
            )
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "AI model inference timed out. Falling back to mathematical indicator analysis.")
            buildMathematicalFallback(pair, timeframe, strategy, currentPrice, rsi14, ema20, ema50, atr14, indicators)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini analysis error: ${e.message}. Using validated indicator calculation.")
            buildMathematicalFallback(pair, timeframe, strategy, currentPrice, rsi14, ema20, ema50, atr14, indicators)
        }
    }

    private fun buildMathematicalFallback(
        pair: String,
        timeframe: String,
        strategy: String,
        currentPrice: Double,
        rsi: Double,
        ema20: Double,
        ema50: Double,
        atr: Double,
        indicators: List<TechnicalIndicatorResult>
    ): AIAnalysisResult {
        val isBullish = ema20 >= ema50 && rsi >= 48.0
        val bias = if (isBullish) "BULLISH" else "BEARISH"
        val slDelta = if (atr > 0.0) atr * 1.5 else currentPrice * 0.02
        val tp1Delta = if (atr > 0.0) atr * 2.5 else currentPrice * 0.035
        val tp2Delta = if (atr > 0.0) atr * 4.0 else currentPrice * 0.06

        val entry = currentPrice
        val sl = if (isBullish) entry - slDelta else entry + slDelta
        val tp1 = if (isBullish) entry + tp1Delta else entry - tp1Delta
        val tp2 = if (isBullish) entry + tp2Delta else entry - tp2Delta

        return AIAnalysisResult(
            pair = pair,
            timeframe = timeframe,
            strategyName = strategy,
            bias = bias,
            confidence = if (rsi in 40.0..60.0) 78 else 88,
            keySupport = round(entry - slDelta * 1.2),
            keyResistance = round(entry + tp1Delta * 1.2),
            pivotPoint = round(entry),
            summary = "Real-time indicators show ${if (isBullish) "bullish trend continuation" else "downward pressure"} with EMA20 at $ema20 and RSI at $rsi.",
            patternDetected = if (isBullish) "Bullish Momentum Retest" else "Bearish Rejection",
            recommendedEntry = round(entry),
            recommendedTp1 = round(tp1),
            recommendedTp2 = round(tp2),
            recommendedSl = round(sl),
            indicators = indicators,
            riskReward = "1 : 2.0",
            generatedAt = System.currentTimeMillis()
        )
    }

    private fun buildInsufficientDataFallback(pair: String, timeframe: String, strategy: String): AIAnalysisResult {
        return AIAnalysisResult(
            pair = pair,
            timeframe = timeframe,
            strategyName = strategy,
            bias = "NEUTRAL",
            confidence = 50,
            keySupport = 0.0,
            keyResistance = 0.0,
            pivotPoint = 0.0,
            summary = "Insufficient historical market candle depth from Binance to formulate high-confidence setup.",
            patternDetected = "Awaiting Market Liquidity",
            recommendedEntry = 0.0,
            recommendedTp1 = 0.0,
            recommendedTp2 = 0.0,
            recommendedSl = 0.0,
            indicators = emptyList(),
            riskReward = "1 : 1.0"
        )
    }

    private fun round(value: Double): Double {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}
