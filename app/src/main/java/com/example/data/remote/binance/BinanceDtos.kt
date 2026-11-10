package com.example.data.remote.binance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BinanceTicker24Hr(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "lastPrice") val lastPrice: String,
    @Json(name = "priceChange") val priceChange: String,
    @Json(name = "priceChangePercent") val priceChangePercent: String,
    @Json(name = "highPrice") val highPrice: String,
    @Json(name = "lowPrice") val lowPrice: String,
    @Json(name = "volume") val volume: String,
    @Json(name = "quoteVolume") val quoteVolume: String,
    @Json(name = "openPrice") val openPrice: String? = null
)

@JsonClass(generateAdapter = true)
data class BinanceFuturesFundingRate(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "fundingRate") val fundingRate: String,
    @Json(name = "fundingTime") val fundingTime: Long,
    @Json(name = "markPrice") val markPrice: String? = null
)

@JsonClass(generateAdapter = true)
data class BinanceFuturesOpenInterest(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "openInterest") val openInterest: String,
    @Json(name = "time") val time: Long
)
