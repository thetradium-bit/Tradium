package com.example.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Types of AI detected technical patterns on candlestick charts.
 */
enum class AIPatternType(val displayName: String, val isBullish: Boolean) {
    FAIR_VALUE_GAP("Fair Value Gap (FVG)", true),
    BULLISH_ORDER_BLOCK("Bullish Order Block", true),
    BEARISH_ORDER_BLOCK("Bearish Order Block", false),
    LIQUIDITY_SWEEP("Liquidity Sweep", true),
    BREAK_OF_STRUCTURE("Break of Structure (BOS)", true),
    CHANGE_OF_CHARACTER("Change of Character (CHoCH)", true),
    DOUBLE_BOTTOM("Double Bottom Reversal", true),
    DOUBLE_TOP("Double Top Reversal", false),
    BULLISH_FLAG("Bull Flag Breakout", true),
    BEARISH_PENNANT("Bearish Breakdown", false)
}

/**
 * Visual overlay zone or point marking an AI-detected pattern directly on candlesticks.
 */
data class AIPatternOverlay(
    val id: String,
    val type: AIPatternType,
    val startCandleIndex: Int,
    val endCandleIndex: Int,
    val topPrice: Float,
    val bottomPrice: Float,
    val label: String,
    val confidence: Int, // 0 - 100
    val targetPrice: Float? = null,
    val stopPrice: Float? = null,
    val description: String = ""
)

/**
 * Key horizontal price levels identified by AI (Support, Resistance, Entry, TP, SL).
 */
data class AIPriceLevel(
    val label: String,
    val price: Float,
    val levelType: AILevelType
)

enum class AILevelType {
    SUPPORT,
    RESISTANCE,
    ENTRY,
    TAKE_PROFIT,
    STOP_LOSS
}
