package com.example.domain.model

data class CandleData(
    val timestamp: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
)

data class TechnicalIndicatorResult(
    val name: String,
    val value: String,
    val interpretation: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val score: Float            // 0.0 to 100.0
)

data class AIAnalysisResult(
    val pair: String,
    val timeframe: String,
    val strategyName: String,
    val bias: String,            // "STRONG BULLISH", "BULLISH", "NEUTRAL", "BEARISH", "STRONG BEARISH"
    val confidence: Int,         // 0 to 100%
    val keySupport: Double,
    val keyResistance: Double,
    val pivotPoint: Double,
    val summary: String,
    val patternDetected: String,
    val recommendedEntry: Double,
    val recommendedTp1: Double,
    val recommendedTp2: Double,
    val recommendedSl: Double,
    val indicators: List<TechnicalIndicatorResult>,
    val riskReward: String,
    val generatedAt: Long = System.currentTimeMillis()
)
