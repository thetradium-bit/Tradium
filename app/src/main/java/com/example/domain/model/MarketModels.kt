package com.example.domain.model

enum class MarketType(val displayName: String) {
    SPOT("Spot"),
    FUTURES("USDT-M Futures")
}

data class MarketTicker(
    val symbol: String,
    val pairFormatted: String, // e.g. "BTC/USDT"
    val marketType: MarketType,
    val lastPrice: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val highPrice24h: Double,
    val lowPrice24h: Double,
    val volume24h: Double,
    val quoteVolume24h: Double,
    val fundingRate: Double? = null,
    val openInterest: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
